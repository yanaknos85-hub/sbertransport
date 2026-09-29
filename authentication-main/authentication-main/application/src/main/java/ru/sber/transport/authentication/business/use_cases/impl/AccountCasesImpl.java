package ru.sber.transport.authentication.business.use_cases.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.sber.transport.authentication.business.dto.*;
import ru.sber.transport.authentication.business.exceptions.*;
import ru.sber.transport.authentication.business.providers.*;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.messaging.senders.EmailSender;
import ru.sber.transport.authentication.messaging.senders.SmsSender;
import ru.sber.transport.authentication.providers.grpc.notification.NotificationsGrpcClient;
import ru.sber.transport.authentication.providers.reset.dao.ResetCodeRepository;
import ru.sber.transport.authentication.web.cache.RefreshTokenCacheService;
import ru.sber.transport.authentication.web.exceptions.IncorrectScopeException;
import ru.sber.transport.authentication.web.exceptions.MaxAttemptsResetCodeException;
import ru.sber.transport.authentication.web.exceptions.RegistrationDataConflictException;
import ru.sber.transport.authentication.web.exceptions.SendingTargetException;
import ru.sber.transport.authentication.web.exceptions.WrongCodeException;
import ru.sber.transport.authentication.web.model.RegistrationRequestDto;
import ru.sber.transport.authentication.web.model.RegistrationResponseDto;
import ru.sber.transport.database.authentication.tables.records.ResetCodeRecord;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Реализация бизнес-случаев учетной записи.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AccountCasesImpl implements AccountCases {

    public static final String ADMIN_LOGIN = "admin";

    public static final String INITIAL_USER_ID = "00000000-0000-0000-0000-000000000000";

    public static final String ROLE_INITIAL_USER = "ROLE_INITIAL_USER";

    private static final String TWO_FACTOR_TEXT = "Ваш код для авторизации в АС СберТранспорт: ";

    private static final String OWNERSHIP_CONFIRMATION_TEXT = "Ваш код подтверждения аккаунта в АС СберТранспорт: ";

    private static final String TRANSPORT_PASSWORD_MESSAGE = """
            Ваш логин и транспортный пароль для авторизации в АС СберТранспорт:
            Логин: {login}
            Пароль: {password}""";

    private static final String TRANSPORT_PASSWORD_MESSAGE_V2 = """
            Ваш транспортный пароль для авторизации в АС СберТранспорт: {password}
            Пожалуйста, измените его
            """;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountProvider accountProvider;

    private final AccessTokenProvider accessTokenProvider;

    private final RefreshProvider refreshProvider;

    private final RoleProvider roleProvider;

    private final EmailSender emailSender;

    private final SmsSender smsSender;

    private final TextProvider textProvider;

    private final ResetCodeRepository resetCodeRepository;

    private final TwoFactorProvider twoFactorProvider;

    private final TransferPasswordProvider transferPasswordProvider;

    private final RefreshTokenCacheService refreshTokenCacheService;

    private final NotificationsGrpcClient notificationsGrpcClient;

    @Setter(AccessLevel.PACKAGE)
    @Value("${security.password.transfer.length:12}")
    private Integer transferPasswordLength;

    @Setter(AccessLevel.PACKAGE)
    @Value("${initial.password}")
    private String initPassword;

    @Setter(AccessLevel.PACKAGE)
    @Value("${users.max-number-of-attempts}")
    private Integer maxNumberOfAttempts;

    @Setter(AccessLevel.PACKAGE)
    @Value("${reset-code.max-number-of-attempts}")
    private Integer maxNumberOfAttemptsResetCode;

    @Override
    public Token login(String login, String password) throws AccountNotFoundException, WrongPasswordException, TooManyLoginTriesException, NoSuchAlgorithmException, InvalidKeySpecException {
        var accountOpt = accountProvider.get(login);

        var technical = false;
        AccountDto account;

        if (accountOpt.isEmpty()) {
            account = getTechnical(login, password);
            technical = true;
        } else {
            account = accountOpt.get();
            if (account.getNumberOfLoginAttempts() >= maxNumberOfAttempts) {
                throw new TooManyLoginTriesException();
            }
            if (!BCrypt.checkpw(password, account.getHash())) {
                var curLoginAttemptsCount = account.getNumberOfLoginAttempts() + 1;
                account.setNumberOfLoginAttempts(curLoginAttemptsCount);
                accountProvider.save(account);

                if (curLoginAttemptsCount >= maxNumberOfAttempts) {
                    throw new TooManyLoginTriesException();
                }

                throw new WrongPasswordException();
            }

            account.setNumberOfLoginAttempts(0);
            accountProvider.save(account);
        }

        return login(technical, account, false);
    }

    @Override
    public Token login(UUID userId, String code) throws CodeException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (!twoFactorProvider.checkCode(userId, code)) {
            throw new CodeException();
        }
        var accountOpt = accountProvider.get(userId).orElseThrow();
        var token = login(false, accountOpt, true);
        twoFactorProvider.removeCode(userId);
        return token;
    }

    @Override
    public void logout(UUID userId, String token) throws AccountNotFoundException {
        var account = accountProvider.get(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        accessTokenProvider.toBlackList(token);
        refreshProvider.release(account, token);
    }

    @Override
    public Token relogin(String refresh) throws RefreshTokenExpired, NoSuchAlgorithmException, InvalidKeySpecException {
        var cachedToken = getCachedToken(refresh);
        if (cachedToken != null) {
            log.debug("Returned cached token for session: {};", refresh);
            return cachedToken;
        }
        var account = refreshProvider.search(refresh).orElseThrow(() -> new RefreshTokenExpired(refresh));
        var oldAccessToken = refreshProvider.getAccessToken(refresh);
        accessTokenProvider.toBlackList(oldAccessToken);
        refreshProvider.release(refresh);
        var jwt = accessTokenProvider.generate(account, true);
        var newRefresh = refreshProvider.generate(jwt.value(), account);
        var token = new Token(jwt.value(), newRefresh.value(), account.isTransferPassword(), AuthType.BASIC, jwt.expiration(), newRefresh.expiration());
        caching(refresh, token);
        return token;
    }

    @Override
    public void resetPassword(UUID userId) throws AccountNotFoundException {
        var accountDto = accountProvider.get(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        var password = String.valueOf(transferPasswordProvider.generateTransferPassword(transferPasswordLength));
        var hash = BCrypt.hashpw(password, BCrypt.gensalt());

        accountDto.setNumberOfLoginAttempts(0);
        accountProvider.save(accountDto);
        accountProvider.setPassword(accountDto, hash, !Scope.CONTRACTOR.equals(accountDto.getScope()));

        var data = new HashedMap<String, Object>();
        data.put("login", accountDto.getLogin());
        data.put("password", password);

        var email = accountDto.getEmail();
        if (email != null) {
            var defaultForList = roleProvider.getDefaultForList(accountDto);
            if (defaultForList.contains(UserMessage.Scope.DISPATCHER.name())) {
                emailSender.send(List.of(email), null, textProvider.getResetPasswordEmail(UserMessage.Scope.DISPATCHER), data);
                return;
            } else if (defaultForList.contains(UserMessage.Scope.DRIVER.name())) {
                emailSender.send(List.of(email), null, textProvider.getResetPasswordEmail(UserMessage.Scope.DRIVER), data);
                if (accountDto.getPhone() != null) {
                    smsSender.send(accountDto.getPhone(), TRANSPORT_PASSWORD_MESSAGE, data);
                }
                return;
            }
            emailSender.send(List.of(email), null, textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE), data);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WARNING, USER WITHOUT EMAIL IS RESETTING PASSWORD!!! User %s has no email".formatted(accountDto.getLogin()));
            }
        }
    }

    @SuppressWarnings("java:S3958")
    @Override
    public void changePassword(String login, String password) throws AccountNotFoundException, PasswordCheckException,
            NonTransferException {
        var account = accountProvider.get(login).orElseThrow(() -> new AccountNotFoundException(login));
        var failedChecks = Arrays.stream(CheckType.values())
                .filter(checkType -> checkType.getCheck().test(login, password))
                .toList();
        if (!account.isTransferPassword()) {
            throw new NonTransferException();
        }
        if (!failedChecks.isEmpty()) {
            throw new PasswordCheckException(failedChecks);
        }
        var hash = BCrypt.hashpw(password, BCrypt.gensalt());
        accountProvider.setPassword(account, hash, false);
        var tokenData = refreshProvider.searchTokenData(account);
        for (var tokenItem : tokenData.entrySet()) {
            accessTokenProvider.toBlackList(tokenItem.getKey());
            refreshProvider.release(tokenItem.getValue());
        }
    }

    @Override
    public void setRoles(UUID userId, Set<String> roles) throws AccountNotFoundException {
        var account = accountProvider.get(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        roleProvider.setRoles(account, roles);
        var tokenData = refreshProvider.searchTokenData(account);
        for (var tokenItem : tokenData.entrySet()) {
            accessTokenProvider.toBlackList(tokenItem.getKey());
        }
    }

    @Override
    public Set<RoleDto> getRoles(UUID userId) throws AccountNotFoundException {
        var account = accountProvider.getAllActiveness(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        return roleProvider.getRoles(account);
    }

    @Override
    public void deactivate(Collection<UUID> userId) {
        var accounts = accountProvider.get(userId);
        accountProvider.deactivate(accounts);
        var tokenData = refreshProvider.searchTokenData(accounts);
        for (var tokenItem : tokenData.entrySet()) {
            accessTokenProvider.toBlackList(tokenItem.getKey());
            refreshProvider.release(tokenItem.getValue());
        }
    }

    @Override
    public void save(Collection<AccountDto> accountDtos) {
        var result = new LinkedList<AccountDto>();
        var toSend = new HashedMap<AccountDto, String>();
        for (var accountDto : accountDtos) {
            log.debug("Attempting to save account: {}", accountDto);
            accountProvider.get(accountDto.getId())
                    .ifPresentOrElse(account -> {
                        accountDto.setLogin(account.getLogin());
                        accountDto.setHash(account.getHash());
                        accountDto.setTransferPassword(account.isTransferPassword());
                    }, () -> {
                        var login = accountDto.getLogin();
                        var count = accountProvider.getLoginsCount(login);
                        var finalLogin = count == 0 ? login : login + count;
                        accountDto.setLogin(finalLogin);
                        var password = String.valueOf(transferPasswordProvider.generateTransferPassword(transferPasswordLength));
                        if (accountDto.getHash() == null || accountDto.getHash().isBlank() || !isIntegrationAccount(accountDto.getScope())) {
                            accountDto.setHash(BCrypt.hashpw(password, BCrypt.gensalt()));
                        }
                        accountDto.setTransferPassword(true);
                        toSend.put(accountDto, password);
                    });
            if (isIntegrationAccount(accountDto.getScope())) {
                accountDto.setTransferPassword(false);
            }
            result.add(accountDto);
        }
        accountProvider.save(result);
        toSend.forEach(this::sendMessage);
    }

    @Override
    public RegistrationResponseDto register(RegistrationRequestDto dto) {
        Scope scope;
        try {
            scope = Scope.valueOf(dto.scope());
        } catch (IllegalArgumentException e) {
            log.warn("Incorrect scope {}", dto.scope());
            throw new IncorrectScopeException();
        }
        if (scope != Scope.CONTRACTOR) {
            log.warn("Incorrect scope {}", dto.scope());
            throw new IncorrectScopeException();
        }

        var count = accountProvider.getLoginsCount(dto.login());
        if (count > 0) {
            throw new RegistrationDataConflictException();
        }

        var userId = UUID.randomUUID();
        var account = AccountDto.builder()
                .id(userId)
                .login(dto.login())
                .hash(hashPassword(dto.password()))
                .transferPassword(false)
                .scope(scope)
                .email(dto.email())
                .active(true)
                .build();
        accountProvider.save(account);
        sendMessage(account, dto.password());
        return new RegistrationResponseDto(userId);
    }

    @Override
    public void sendCodeForOwnershipProof(OwnershipProofDto ownershipProofDto) throws AccountNotFoundException {
        var account = accountProvider.get(ownershipProofDto.getLogin()).orElseThrow(() -> new AccountNotFoundException(ownershipProofDto.getLogin()));
        var code = 100000 + RANDOM.nextInt(900000);
        var hash = BCrypt.hashpw(String.valueOf(code), BCrypt.gensalt());
        var sendingTarget = getSendingTarget(account, ownershipProofDto.getChannel());
        if (sendingTarget == null) {
            throw new SendingTargetException(ownershipProofDto.getChannel().name());
        }
        var resetCode = resetCodeRepository.findById(account.getId());
        if (resetCode.isPresent()) {
            resetCode.get().setSendingTarget(sendingTarget);
            resetCode.get().setHash(hash);
            resetCode.get().setSendingChannel(ownershipProofDto.getChannel().name());
            resetCode.get().setNumberOfLoginAttempts(0);
            resetCodeRepository.save(resetCode.get());
        } else {
            resetCodeRepository.save(new ResetCodeRecord(account.getId(), hash, sendingTarget, ownershipProofDto.getChannel().name(), 0));
        }
        sendInstantByChannel(account.getId(), code, ownershipProofDto.getChannel());
    }

    @Transactional(noRollbackFor = {MaxAttemptsResetCodeException.class, WrongCodeException.class})
    @Override
    public void codeCheck(String code, OwnershipProofDto ownershipProofDto) throws AccountNotFoundException {
        var account = accountProvider.get(ownershipProofDto.getLogin()).orElseThrow(() -> new AccountNotFoundException(ownershipProofDto.getLogin()));
        var resetCode = resetCodeRepository.findById(account.getId())
                .orElseThrow(() -> new EntityNotFoundException(ResetCodeRecord.class, account.getId()));

        if (BCrypt.checkpw(code, resetCode.getHash())) {
            resetCodeRepository.delete(resetCode);
            resetPassword(resetCode.getAccountId(), SendingChannel.valueOf(resetCode.getSendingChannel()), resetCode.getSendingTarget());
            return;
        }

        int attempts = resetCode.getNumberOfLoginAttempts() + 1;
        resetCode.setNumberOfLoginAttempts(attempts);
        if (attempts >= maxNumberOfAttemptsResetCode) {
            resetCodeRepository.delete(resetCode);
            throw new MaxAttemptsResetCodeException();
        }

        resetCodeRepository.save(resetCode);
        throw new WrongCodeException(code);
    }

    private void resetPassword(UUID userId, SendingChannel sendingChannel, String sendingTarget) throws AccountNotFoundException {
        var accountDto = accountProvider.get(userId).orElseThrow(() -> new AccountNotFoundException(userId));
        var password = String.valueOf(transferPasswordProvider.generateTransferPassword(transferPasswordLength));
        var hash = BCrypt.hashpw(password, BCrypt.gensalt());

        accountDto.setNumberOfLoginAttempts(0);
        accountProvider.save(accountDto);
        accountProvider.setPassword(accountDto, hash, !Scope.CONTRACTOR.equals(accountDto.getScope()));

        var data = new HashedMap<String, Object>();
        data.put("password", password);

        switch (sendingChannel) {
            case EMAIL -> emailSender.send(List.of(sendingTarget), null, textProvider.getResetPasswordEmailV2(), data);
            case SMS -> smsSender.send(sendingTarget, TRANSPORT_PASSWORD_MESSAGE_V2, data);
        }
    }

    private void sendSecondFactorMessage(AccountDto account, TwoFactor factor) {
        emailSender.send(List.of(account.getEmail()), null, textProvider.getSecondFactorEmailText(),
                Map.of("token", factor.token(), "code", factor.code(), "expiration", factor.expiration(), "codeUrl", twoFactorProvider.codeUrl()));
        smsSender.send(account.getPhone(), TWO_FACTOR_TEXT + "{code}", Map.of("code", factor.code()));
    }

    private AccountDto getTechnical(String login, String password) throws AccountNotFoundException {
        if (StringUtils.hasText(initPassword) && accountProvider.empty() && ADMIN_LOGIN.equals(login) && password.equals(initPassword)) {
            return AccountDto.builder().id(UUID.fromString(INITIAL_USER_ID))
                    .login(login).roles(Map.of(ROLE_INITIAL_USER, true)).build();
        }
        throw new AccountNotFoundException(login);
    }

    private void sendMessage(AccountDto accountDto, String password) {
        var data = new HashedMap<String, Object>();
        data.put("login", accountDto.getLogin());
        data.put("password", password);

        if (accountDto.getEmail() != null) {
            var defaultForList = roleProvider.getDefaultForList(accountDto);
            if (defaultForList.contains(UserMessage.Scope.DISPATCHER.name())) {
                emailSender.send(List.of(accountDto.getEmail()), null, textProvider.getResetPasswordEmail(UserMessage.Scope.DISPATCHER), data);
                return;
            } else if (defaultForList.contains(UserMessage.Scope.DRIVER.name())) {
                emailSender.send(List.of(accountDto.getEmail()), null, textProvider.getResetPasswordEmail(UserMessage.Scope.DRIVER), data);
                if (accountDto.getPhone() != null) {
                    smsSender.send(accountDto.getPhone(), TRANSPORT_PASSWORD_MESSAGE, data);
                }
                return;
            } else if (accountDto.getScope() != null && Scope.AUTOSERVICE.name().equals(accountDto.getScope().name())) {
                emailSender.send(List.of(accountDto.getEmail()), null, textProvider.getResetPasswordEmail(UserMessage.Scope.AUTOSERVICE), data);
                return;
            } else if (accountDto.getScope() != null &&
                    (UserMessage.Scope.AUTOSERVICE_TA.name().equals(accountDto.getScope().name()) ||
                            UserMessage.Scope.CONTRACTOR.name().equals(accountDto.getScope().name()))) {
                // Больше не отправляем данные туз по почте, так-как реализовали автоматический проброс данных для интеграции
                return;
            }
            emailSender.send(List.of(accountDto.getEmail()), null, textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE), data);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("WARNING, USER WITHOUT EMAIL IS RESETTING PASSWORD!!! User %s has no email".formatted(accountDto.getLogin()));
            }
        }
    }

    @NotNull
    private Token login(boolean technical, AccountDto account, boolean enforceBasic) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RefreshTokenData refresh = null;
        AccessTokenData jwt;
        var authType = enforceBasic ? AuthType.BASIC : account.getAuthType();
        jwt = accessTokenProvider.generate(account, enforceBasic);
        if (!technical && (authType == null || AuthType.BASIC.equals(authType))) {
            refresh = refreshProvider.generate(jwt.value(), account);
        }
        if (AuthType.TWO_FA.equals(authType)) {
            sendSecondFactorMessage(account, twoFactorProvider.generate(account, jwt));
        }

        var refreshValue = Optional.ofNullable(refresh).map(RefreshTokenData::value).orElse(null);
        var refreshExpiration = Optional.ofNullable(refresh).map(RefreshTokenData::expiration).orElse(null);
        return new Token(jwt.value(), refreshValue, account.isTransferPassword(), authType, jwt.expiration(), refreshExpiration);
    }

    private void caching(String refresh, Token token) {
        var requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        var request = requestAttributes.getRequest();
        var key = refresh + "|" + getIp(request);
        refreshTokenCacheService.add(key, token);
    }

    private String getIp(HttpServletRequest request) {
        var realIpHeader = "x-real-ip";
        if (request.getHeader(realIpHeader) != null && !request.getHeader(realIpHeader).isEmpty()) {
            return request.getHeader(realIpHeader);
        } else return request.getRemoteAddr();
    }

    private Token getCachedToken(String refresh) {
        var requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        var request = requestAttributes.getRequest();
        var key = refresh + "|" + getIp(request);
        return refreshTokenCacheService.get(key);
    }

    private String getSendingTarget(AccountDto account, SendingChannel sendingChannel) {
        return switch (sendingChannel) {
            case EMAIL -> account.getEmail();
            case SMS -> account.getPhone();
        };
    }

    private void sendInstantByChannel(UUID userId, int code, SendingChannel sendingChannel) {
        var data = new HashedMap<String, Object>();
        data.put("code", code);
        var text = switch (sendingChannel) {
            case EMAIL -> textProvider.getEmailConfirmation();
            case SMS -> OWNERSHIP_CONFIRMATION_TEXT + "{code}";
        };

        notificationsGrpcClient.send(userId, text, data, sendingChannel);
    }

    private boolean isIntegrationAccount(Scope scope) {
        return Scope.CONTRACTOR.equals(scope) || Scope.AUTOSERVICE_TA.equals(scope);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
