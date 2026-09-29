package ru.sber.transport.authentication.web.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authentication.business.dto.CheckType;
import ru.sber.transport.authentication.business.dto.OwnershipProofDto;
import ru.sber.transport.authentication.business.exceptions.*;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.business.use_cases.AuditCases;
import ru.sber.transport.authentication.web.AuthenticationController;
import ru.sber.transport.authentication.web.exceptions.*;
import ru.sber.transport.authentication.web.mapper.RoleMapper;
import ru.sber.transport.authentication.web.mapper.TokenMapper;
import ru.sber.transport.authentication.web.model.CodeData;
import ru.sber.transport.authentication.web.model.Role;
import ru.sber.transport.authentication.web.model.Token;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementation of controller for working with authentication.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@NoAuthorize("/login")
class AuthenticationControllerImpl implements AuthenticationController {

    private final AccountCases accountCases;

    private final TokenMapper tokenMapper;

    private final RoleMapper roleMapper;

    private final AuditCases auditCases;

    private final ObjectMapper objectMapper;

    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class, AccountNotFoundException.class})
    @Override
    public Token loginGet(String userAgent, String authorization, String xClientType) {
        var credentialsParts = authorization.split(" ");
        if (credentialsParts.length != 2) {
            throw new WrongAuthenticationDataException(authorization);
        }
        var type = credentialsParts[0];
        var credentialsValue = credentialsParts[1];
        if ("Basic".equals(type)) {
            log.debug("Basic authentication started");
            return basicAuthentication(credentialsValue, userAgent, xClientType, tokenMapper::toDto);
        } else if ("Token".equals(type)) {
            log.debug("Refresh token authentication started");
            return refreshAuthentication(credentialsValue, userAgent, xClientType, tokenMapper::toDto);
        }
        throw new UnknownAuthenticationTypeException(type);
    }

    @Override
    public Token loginPost(String userAgent, String authorization, String xClientType) {
        return loginGet(userAgent, authorization, xClientType);
    }

    @Override
    public Token code(JwtAuthenticationToken authentication, CodeData code) throws NoSuchAlgorithmException, InvalidKeySpecException {
        var userId = UUID.fromString(authentication.getToken().getId());
        try {
            var result = accountCases.login(userId, code.code());
            return tokenMapper.toDto(result);
        } catch (CodeException e) {
            throw new LoginException(LoginException.Type.WRONG_TWO_FA);
        }
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("No token provided for logout");
            return;
        }
        var tokenParts = token.split("\\.");
        var payload = Base64.getDecoder().decode(tokenParts[1]);
        try {
            var payloadMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
            var userId = UUID.fromString(String.valueOf(payloadMap.get("jti")));
            accountCases.logout(userId, token.replace("Bearer ", ""));
            auditCases.logout(userId);
        } catch (AccountNotFoundException e) {
            log.warn("Logout incomplete: %s".formatted(e.getMessage()));
        } catch (IOException e) {
            log.warn("Wrong payload: %s".formatted(new String(payload, StandardCharsets.UTF_8)));
        }
    }

    @SuppressWarnings("java:S3958")
    @Override
    public void changeTransferPassword(String credentials) {
        var rawCreds = credentials.replace("Basic ", "");
        if (!rawCreds.contains(":")) {
            rawCreds = new String(Base64.getDecoder().decode(rawCreds));
        }
        var splitIndexOf = rawCreds.indexOf(':') ;
        var login = rawCreds.substring(0,splitIndexOf);
        var password= rawCreds.substring(splitIndexOf + 1);
        try {
            accountCases.changePassword(login, password);
        } catch (AccountNotFoundException e) {
            throw new LoginException(LoginException.Type.WRONG_CREDENTIALS);
        } catch (PasswordCheckException e) {
            throw new PasswordException(e.getFailedChecks().stream().map(CheckType::name).toList());
        } catch (NonTransferException e) {
            throw new PasswordException(List.of("STATE_NON_TRANSFER"));
        }
    }

    @Override
    public void resetPassword(UUID userId) {
        try {
            accountCases.resetPassword(userId);
        } catch (AccountNotFoundException e) {
            throw new LoginException(LoginException.Type.WRONG_CREDENTIALS);
        }
    }

    @Override
    public void sendCodeForOwnershipProof(OwnershipProofDto ownershipProofDto) throws AccountNotFoundException {
        accountCases.sendCodeForOwnershipProof(ownershipProofDto);
    }

    @Override
    public void confirmationOfOwnershipAndResetPassword(String code, OwnershipProofDto ownershipProofDto) throws AccountNotFoundException {
        accountCases.codeCheck(code, ownershipProofDto);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<Role> getUserRoles(@PathVariable("userId") UUID userId) {
        try {
            return accountCases.getRoles(userId).stream().map(roleMapper::toDto).toList();
        } catch (AccountNotFoundException e) {
            throw new LoginException(LoginException.Type.WRONG_CREDENTIALS);
        }
    }

    /**
     * Аутентификация по токену обновления.
     *
     * @param credentialsValue креды.
     * @return токен в случае успешной аутентификации.
     */
    private <T> T refreshAuthentication(String credentialsValue, String userAgent, String clientType, Function<ru.sber.transport.authentication.business.dto.Token, T> mapper) throws AccountNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        var refresh = credentialsValue.replace("Token ", "");
        try {
            var newToken = accountCases.relogin(refresh);
            auditCases.refresh(newToken.refreshToken(), userAgent, clientType);
            return mapper.apply(newToken);
        } catch (RefreshTokenExpired refreshTokenExpired) {
            auditCases.refreshExpired(refresh);
            throw new LoginException(LoginException.Type.REFRESH_EXPIRED);
        }
    }

    /**
     * Базовая аутентификация.
     *
     * @param credentialsValue креды.
     * @return токен в случае успешной аутентификации.
     */
    private <T> T basicAuthentication(String credentialsValue, String userAgent, String clientType, Function<ru.sber.transport.authentication.business.dto.Token, T> mapper) throws NoSuchAlgorithmException, InvalidKeySpecException {
        var rawCreds = credentialsValue.replace("Basic ", "");
        if (!rawCreds.contains(":")) {
            try {
                rawCreds = new String(Base64.getDecoder().decode(rawCreds));
            } catch (IllegalArgumentException e) {
                throw new WrongAuthenticationDataException(rawCreds);
            }
        }
        if (rawCreds.split(":").length != 2) {
            throw new WrongAuthenticationDataException(rawCreds);
        }
        var login = rawCreds.split(":")[0];
        var password = rawCreds.split(":")[1];
        try {
            var token = accountCases.login(login, password);
            auditCases.login(login, userAgent, clientType);
            return mapper.apply(token);
        } catch (AccountNotFoundException e) {
            auditCases.wrongLogin(login);
            throw new LoginException(LoginException.Type.WRONG_CREDENTIALS);
        } catch (WrongPasswordException e) {
            auditCases.wrongPassword(login);
            throw new LoginException(LoginException.Type.WRONG_CREDENTIALS);
        } catch (TooManyLoginTriesException e) {
            auditCases.tooManyLoginTries(login);
            throw new LoginException(LoginException.Type.TOO_MANY_LOGIN_TRIES);
        }
    }
}