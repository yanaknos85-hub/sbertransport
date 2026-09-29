package ru.sber.transport.authentication.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.*;
import ru.sber.transport.authentication.business.exceptions.*;
import ru.sber.transport.authentication.business.providers.*;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.messaging.senders.EmailSender;
import ru.sber.transport.authentication.messaging.senders.SmsSender;
import ru.sber.transport.authentication.providers.grpc.notification.NotificationsGrpcClient;
import ru.sber.transport.authentication.providers.grpc.notification.impl.NotificationsGrpcClientImpl;
import ru.sber.transport.authentication.providers.reset.dao.ResetCodeRepository;
import ru.sber.transport.authentication.web.cache.RefreshTokenCacheService;
import ru.sber.transport.authentication.web.exceptions.MaxAttemptsResetCodeException;
import ru.sber.transport.authentication.web.exceptions.WrongCodeException;
import ru.sber.transport.database.authentication.tables.records.ResetCodeRecord;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка бизнес-случаев работы с УЗ")
class AccountCasesImplTest {

    private final AccountProvider accountProvider = mock(AccountProvider.class);

    private final AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);

    private final RefreshProvider refreshProvider = mock(RefreshProvider.class);

    private final RoleProvider roleProvider = mock(RoleProvider.class);

    private final EmailSender emailSender = mock(EmailSender.class);

    private final SmsSender smsSender = mock(SmsSender.class);

    private final TextProvider textProvider = mock(TextProvider.class);

    private final ResetCodeRepository resetCodeRepository = mock(ResetCodeRepository.class);

    private final TwoFactorProvider twoFactorProvider = mock(TwoFactorProvider.class);

    private final TransferPasswordProvider transferPasswordProvider = mock(TransferPasswordProvider.class);

    private final RefreshTokenCacheService refreshTokenCacheService = mock(RefreshTokenCacheService.class);

    private final NotificationsGrpcClient notificationsGrpcClient = mock(NotificationsGrpcClientImpl.class);

    private final AccountCases cases = new AccountCasesImpl(accountProvider,
            accessTokenProvider,
            refreshProvider,
            roleProvider,
            emailSender,
            smsSender,
            textProvider,
            resetCodeRepository,
            twoFactorProvider,
            transferPasswordProvider,
            refreshTokenCacheService,
            notificationsGrpcClient) {
        {
            setTransferPasswordLength(12);
            setMaxNumberOfAttempts(5);
            setMaxNumberOfAttemptsResetCode(3);
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка подтверждения владения аккаунтом (почта)")
    void test_sendCodeForOwnershipProofEmail() throws AccountNotFoundException {
        var confirmationText = Instancio.ofList(String.class).size(1).create().getFirst();
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.EMAIL);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(textProvider.getEmailConfirmation()).thenReturn(confirmationText);

        cases.sendCodeForOwnershipProof(dto);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        var resetCodeCaptor = ArgumentCaptor.forClass(ResetCodeRecord.class);

        verify(notificationsGrpcClient).send(eq(account.getId()), eq(confirmationText), dataCaptor.capture(), eq(dto.getChannel()));
        verify(resetCodeRepository).save(resetCodeCaptor.capture());

        assertThat(resetCodeCaptor.getValue().getSendingTarget()).isEqualTo(account.getEmail());

        assertThat(dataCaptor.getValue()).containsKey("code");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка подтверждения владения аккаунтом (почта), перезапись кода")
    void test_sendCodeForOwnershipProofEmailOverride() throws AccountNotFoundException {
        var confirmationText = Instancio.ofList(String.class).size(1).create().getFirst();
        var account = Instancio.of(AccountDto.class).create();
        var resetCode = new ResetCodeRecord(account.getId(), BCrypt.hashpw(String.valueOf(1234), BCrypt.gensalt()), account.getEmail(), SendingChannel.EMAIL.name(),0);
        account.setLogin("LoginL-L");
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.EMAIL);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.of(resetCode));
        when(textProvider.getEmailConfirmation()).thenReturn(confirmationText);

        cases.sendCodeForOwnershipProof(dto);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        var resetCodeCaptor = ArgumentCaptor.forClass(ResetCodeRecord.class);

        verify(notificationsGrpcClient).send(eq(account.getId()), eq(confirmationText), dataCaptor.capture(), eq(dto.getChannel()));
        verify(resetCodeRepository).save(resetCodeCaptor.capture());

        assertThat(resetCodeCaptor.getValue().getSendingTarget()).isEqualTo(account.getEmail());

        assertThat(dataCaptor.getValue()).containsKey("code");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка подтверждения владения аккаунтом (смс)")
    void test_sendCodeForOwnershipProofSms() throws AccountNotFoundException {
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.SMS);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        var text = "Ваш код подтверждения аккаунта в АС СберТранспорт: {code}";

        cases.sendCodeForOwnershipProof(dto);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        var resetCodeCaptor = ArgumentCaptor.forClass(ResetCodeRecord.class);

        verify(notificationsGrpcClient).send(eq(account.getId()), eq(text), dataCaptor.capture(), eq(dto.getChannel()));
        verify(resetCodeRepository).save(resetCodeCaptor.capture());

        assertThat(resetCodeCaptor.getValue().getSendingTarget()).isEqualTo(account.getPhone());

        assertThat(dataCaptor.getValue()).containsKey("code");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка кода подтверждения владения аккаунтом и сброс пароля (смс)")
    void test_codeCheckSms() throws AccountNotFoundException {
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var resetCode = new ResetCodeRecord(account.getId(), BCrypt.hashpw(String.valueOf(1234), BCrypt.gensalt()), account.getPhone(), SendingChannel.SMS.name(), 0);
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.SMS);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(accountProvider.get(any(UUID.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.of(resetCode));
        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());

        cases.codeCheck("1234", dto);

        var phoneCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var hashCaptor = ArgumentCaptor.forClass(String.class);
        var transferCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(resetCodeRepository).delete(any());
        verify(accountProvider).setPassword(accountCaptor.capture(), hashCaptor.capture(), transferCaptor.capture());
        verify(smsSender).send(phoneCaptor.capture(), textCaptor.capture(), dataCaptor.capture());

        assertThat(accountCaptor.getValue().getId()).isEqualTo(account.getId());

        assertThat(phoneCaptor.getValue()).isEqualTo(account.getPhone());
        assertThat(textCaptor.getValue()).isEqualTo("""
                Ваш транспортный пароль для авторизации в АС СберТранспорт: {password}
                Пожалуйста, измените его
                """);
        assertThat((Map<String, String>) dataCaptor.getValue()).containsKey("password");
        assertThat(BCrypt.checkpw(((Map<String, String>) dataCaptor.getValue()).get("password"), hashCaptor.getValue())).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка кода подтверждения владения аккаунтом и сброс пароля (почта)")
    void test_codeCheckEmail() throws AccountNotFoundException {
        var confirmationText = Instancio.ofList(String.class).size(1).create().getFirst();
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var resetCode = new ResetCodeRecord(account.getId(), BCrypt.hashpw(String.valueOf(1234), BCrypt.gensalt()), account.getEmail(), SendingChannel.EMAIL.name(), 0);
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.EMAIL);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(accountProvider.get(any(UUID.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.of(resetCode));
        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        when(textProvider.getResetPasswordEmailV2()).thenReturn(confirmationText);

        cases.codeCheck("1234", dto);

        var emailCaptor = ArgumentCaptor.forClass(List.class);
        var subjectCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var hashCaptor = ArgumentCaptor.forClass(String.class);
        var transferCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(resetCodeRepository).delete(any());
        verify(accountProvider).setPassword(accountCaptor.capture(), hashCaptor.capture(), transferCaptor.capture());
        verify(emailSender).send(emailCaptor.capture(), subjectCaptor.capture(), textCaptor.capture(), dataCaptor.capture());

        assertThat(accountCaptor.getValue().getId()).isEqualTo(account.getId());

        assertThat(emailCaptor.getValue()).hasSameElementsAs(List.of(account.getEmail()));
        assertThat(subjectCaptor.getValue()).isNull();
        assertThat(textCaptor.getValue()).isEqualTo(confirmationText);
        assertThat((Map<String, String>) dataCaptor.getValue()).containsKey("password");
        assertThat(BCrypt.checkpw(((Map<String, String>) dataCaptor.getValue()).get("password"), hashCaptor.getValue())).isTrue();
    }

    @Test
    @DisplayName("Проверка увеличения счетчика попыток при неверном коде")
    void test_codeCheckWrongCode() throws AccountNotFoundException {
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var resetCode = new ResetCodeRecord(account.getId(), BCrypt.hashpw(String.valueOf(1234), BCrypt.gensalt()), account.getEmail(), SendingChannel.EMAIL.name(), 0);
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.EMAIL);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(accountProvider.get(any(UUID.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.of(resetCode));

        var ex = assertThrows(WrongCodeException.class,
                () -> cases.codeCheck("4321", dto));

        assertThat(ex.getMessage()).isNotBlank();

        var resetCodeCaptor = ArgumentCaptor.forClass(ResetCodeRecord.class);

        verify(resetCodeRepository, never()).delete(any());

        verify(resetCodeRepository).save(resetCodeCaptor.capture());
        assertThat(resetCodeCaptor.getValue().getNumberOfLoginAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Проверка превышения количества попыток ввода неверного кода")
    void test_codeCheckMaxAttemptsCode() throws AccountNotFoundException {
        var account = Instancio.of(AccountDto.class).create();
        account.setLogin("LoginL-L");
        var resetCode = new ResetCodeRecord(account.getId(), BCrypt.hashpw(String.valueOf(1234), BCrypt.gensalt()), account.getEmail(), SendingChannel.EMAIL.name(), 3);
        var dto = new OwnershipProofDto(account.getLogin(), SendingChannel.EMAIL);

        when(accountProvider.get(any(String.class))).thenReturn(Optional.of(account));
        when(accountProvider.get(any(UUID.class))).thenReturn(Optional.of(account));
        when(resetCodeRepository.findById(any(UUID.class))).thenReturn(Optional.of(resetCode));

        var ex = assertThrows(MaxAttemptsResetCodeException.class,
                () -> cases.codeCheck("4321", dto));

        assertThat(ex.getMessage()).isNotBlank();

        verify(resetCodeRepository).delete(resetCode);

        verify(resetCodeRepository, never()).save(any());
    }

    @DisplayName("Вход")
    @Test
    void test_login() throws AccountNotFoundException, WrongPasswordException, TooManyLoginTriesException, NoSuchAlgorithmException, InvalidKeySpecException {
        var userLogin = "Login";
        var userPassword = "Password";

        var account = AccountDto.builder().login(userLogin)
                .hash(BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));
        when(accessTokenProvider.generate(account, false)).thenReturn(new AccessTokenData("JWT", OffsetDateTime.now()));
        when(refreshProvider.generate("JWT", account)).thenReturn(new RefreshTokenData("Refresh", OffsetDateTime.now().plusDays(7)));

        var actual = cases.login(userLogin, userPassword);

        assertThat(actual.accessToken()).isEqualTo("JWT");
        assertThat(actual.refreshToken()).isEqualTo("Refresh");
        assertThat(actual.transferPassword()).isFalse();
    }

    @DisplayName("Вход. Большое количсетво попыток авторизации.")
    @Test
    void test_loginTooManyTimes() throws NoSuchAlgorithmException, InvalidKeySpecException {
        var userLogin = "Login";
        var userPassword = "Password";

        var account = AccountDto.builder().login(userLogin)
                .hash(BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));
        when(accessTokenProvider.generate(account, false)).thenReturn(new AccessTokenData("JWT", OffsetDateTime.now()));
        when(refreshProvider.generate("JWT", account)).thenReturn(new RefreshTokenData("Refresh", OffsetDateTime.now().plusDays(7)));

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> cases.login(userLogin, userPassword + "WRONG"))
                    .isInstanceOf(WrongPasswordException.class);
        }

        assertThatThrownBy(() -> cases.login(userLogin, userPassword + "WRONG"))
                .isInstanceOf(TooManyLoginTriesException.class);
    }

    @Test
    @DisplayName("Проверка входа с текстовыми данными. Обнуление счетчика после успешной авторизации.")
    void login_clearLoginTriesAfterSuccess() throws TooManyLoginTriesException, AccountNotFoundException, WrongPasswordException, NoSuchAlgorithmException, InvalidKeySpecException {
        var userLogin = "Login";
        var userPassword = "Password";

        var account = AccountDto.builder().login(userLogin)
                .hash(BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));
        when(accessTokenProvider.generate(account, false)).thenReturn(new AccessTokenData("JWT", OffsetDateTime.now()));
        when(refreshProvider.generate("JWT", account)).thenReturn(new RefreshTokenData("Refresh", OffsetDateTime.now().plusDays(7)));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> cases.login(userLogin, userPassword + "WRONG"))
                    .isInstanceOf(WrongPasswordException.class);
        }

        cases.login(userLogin, userPassword);

        assertEquals(0, account.getNumberOfLoginAttempts());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Вход. Нужен второй фактор")
    @Test
    void test_login_2nd_factor_needed() throws AccountNotFoundException, WrongPasswordException, TooManyLoginTriesException, NoSuchAlgorithmException, InvalidKeySpecException {
        var userLogin = "Login";
        var userPassword = "Password";

        var account = Instancio.of(AccountDto.class)
                .set(Select.field(AccountDto::getAuthType), AuthType.TWO_FA)
                .set(Select.field(AccountDto::getHash), BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .set(Select.field(AccountDto::getNumberOfLoginAttempts), 0)
                .create();

        var text = Instancio.create(String.class);
        var token = Instancio.create(AccessTokenData.class);
        var twoFactor = Instancio.create(TwoFactor.class);

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));
        when(accessTokenProvider.generate(account, false)).thenReturn(token);
        when(twoFactorProvider.generate(account, token)).thenReturn(twoFactor);
        when(twoFactorProvider.codeUrl()).thenReturn("Some url");
        when(textProvider.getSecondFactorEmailText()).thenReturn(text);

        var actual = cases.login(userLogin, userPassword);

        var emailCaptor = ArgumentCaptor.forClass(List.class);
        var subjectCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(emailSender).send(emailCaptor.capture(), subjectCaptor.capture(), textCaptor.capture(), dataCaptor.capture());

        assertThat(actual.accessToken()).isEqualTo(token.value());
        assertThat(actual.refreshToken()).isNull();
        List<String> emails = emailCaptor.getValue();
        assertThat(emails).hasSize(1);
        assertThat(emails.getFirst()).isEqualTo(account.getEmail());
        assertThat(subjectCaptor.getValue()).isNull();
        assertThat(textCaptor.getValue()).isEqualTo(text);

        var data = (Map<String, Object>) dataCaptor.getValue();
        assertThat(data)
                .containsEntry("token", twoFactor.token())
                .containsEntry("code", twoFactor.code())
                .containsEntry("expiration", twoFactor.expiration())
        ;
    }

    @DisplayName("Вход. Второй фактор")
    @Test
    void test_login_2nd_factor() throws CodeException, NoSuchAlgorithmException, InvalidKeySpecException {
        var userPassword = "Password";

        var account = Instancio.of(AccountDto.class)
                .set(Select.field(AccountDto::getAuthType), AuthType.TWO_FA)
                .set(Select.field(AccountDto::getHash), BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .create();

        var twoFactor = Instancio.create(TwoFactor.class);
        var token = Instancio.create(AccessTokenData.class);
        var sessionId = UUID.randomUUID().toString();

        when(twoFactorProvider.checkCode(account.getId(), twoFactor.code())).thenReturn(true);
        when(accountProvider.get(account.getId())).thenReturn(Optional.of(account));
        when(accessTokenProvider.generate(account, true)).thenReturn(token);
        when(refreshProvider.generate(token.value(), account)).thenReturn(new RefreshTokenData(sessionId, OffsetDateTime.now().plusDays(7)));

        var actual = cases.login(account.getId(), twoFactor.code());

        var userIdCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(twoFactorProvider).removeCode(userIdCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(account.getId());
        assertThat(actual.accessToken()).isEqualTo(token.value());
        assertThat(actual.refreshToken()).isEqualTo(sessionId);
    }

    @DisplayName("Вход. Неверный пароль")
    @Test
    void test_login_wrongPassword() {
        var userLogin = "Login";
        var userPassword = "Password";

        var account = AccountDto.builder().login(userLogin)
                .hash(BCrypt.hashpw(userPassword, BCrypt.gensalt()))
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.login(userLogin, "Wrong password"))
                .isInstanceOf(WrongPasswordException.class);
    }

    @DisplayName("Вход. Нет сотрудника")
    @Test
    void test_login_noLogin() {
        var userLogin = "Login";
        var userPassword = "Password";

        assertThatThrownBy(() -> cases.login(userLogin, userPassword))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("AccountDto with login 'Login' not found");
    }

    @DisplayName("Проверка выхода")
    @Test
    void test_logout() throws AccountNotFoundException {
        var userId = UUID.randomUUID();
        var userLogin = "Login";
        var token = "JWT";

        var account = AccountDto.builder().login(userLogin)
                .build();

        when(accountProvider.get(userId)).thenReturn(Optional.of(account));

        cases.logout(userId, token);

        var blTokenCaptor = ArgumentCaptor.forClass(String.class);
        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var relTokenCaptor = ArgumentCaptor.forClass(String.class);

        verify(accessTokenProvider).toBlackList(blTokenCaptor.capture());
        verify(refreshProvider).release(accountCaptor.capture(), relTokenCaptor.capture());

        var blToken = blTokenCaptor.getValue();
        var relToken = relTokenCaptor.getValue();
        var actual = accountCaptor.getValue();

        assertThat(blToken)
                .isEqualTo(relToken)
                .isEqualTo(token);
        assertThat(relToken).isEqualTo(token);
        assertThat(actual).isEqualTo(account);
    }

    @DisplayName("Проверка выхода. Нет пользователя")
    @Test
    void test_logout_noUser() {
        var userId = UUID.randomUUID();

        assertThatThrownBy(() -> cases.logout(userId, "anyToken"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("AccountDto with ID '" + userId + "' not found");
    }

    @Test
    @DisplayName("Проверка сброса пароля. Нет email")
    void test_resetPassword_noEmail() throws AccountNotFoundException {
        var userId = UUID.randomUUID();
        var userLogin = "Login";

        var account = AccountDto.builder().login(userLogin)
                .hash("Password")
                .build();

        when(accountProvider.get(userId)).thenReturn(Optional.of(account));

        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        cases.resetPassword(userId);

        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var hashCaptor = ArgumentCaptor.forClass(String.class);
        var transferCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(accountProvider).setPassword(accountCaptor.capture(), hashCaptor.capture(), transferCaptor.capture());

        assertThat(accountCaptor.getValue()).isEqualTo(account);
        assertThat(BCrypt.checkpw("Password", hashCaptor.getValue())).isFalse();
        assertThat(transferCaptor.getValue()).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка сброса пароля")
    void test_resetPassword() throws AccountNotFoundException {
        var userId = UUID.randomUUID();
        var userLogin = "Login";

        var account = AccountDto.builder().login(userLogin)
                .hash("Password")
                .email("Email")
                .numberOfLoginAttempts(3)
                .build();

        var template = "Text";

        when(accountProvider.get(userId)).thenReturn(Optional.of(account));
        when(textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE)).thenReturn(template);
        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        cases.resetPassword(userId);

        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var hashCaptor = ArgumentCaptor.forClass(String.class);
        var transferCaptor = ArgumentCaptor.forClass(Boolean.class);
        var emailsCaptor = ArgumentCaptor.forClass(List.class);
        var subjectCaptor = ArgumentCaptor.forClass(String.class);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(accountProvider).setPassword(accountCaptor.capture(), hashCaptor.capture(), transferCaptor.capture());
        verify(emailSender).send(emailsCaptor.capture(), subjectCaptor.capture(), templateCaptor.capture(),
                dataCaptor.capture());

        var actualEmails = emailsCaptor.getValue();
        var actualSubject = subjectCaptor.getValue();
        var actualTemplate = templateCaptor.getValue();
        var actualData = dataCaptor.getValue();

        assertThat(accountCaptor.getValue()).isEqualTo(account);
        assertThat(accountCaptor.getValue().getNumberOfLoginAttempts()).isEqualTo(0);
        assertThat(BCrypt.checkpw("Password", hashCaptor.getValue())).isFalse();
        assertThat(transferCaptor.getValue()).isTrue();
        assertThat(actualEmails).hasSize(1);
        assertThat(String.valueOf(actualEmails.getFirst())).isEqualTo(account.getEmail());
        assertThat(actualSubject).isNull();
        assertThat(actualTemplate).isEqualTo(template);
        assertThat(actualData.entrySet()).hasSize(2);
        assertThat((Map<String, Object>) actualData).containsKey("login")
                .containsEntry("login", account.getLogin())
                .containsKey("password");
    }

    @Test
    @DisplayName("Проверка сброса пароля. Аккаунт не найден")
    void test_resetPassword_noAccount() {
        var userId = UUID.randomUUID();

        assertThatThrownBy(() -> cases.resetPassword(userId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("AccountDto with ID '" + userId + "' not found");
    }

    @Test
    @DisplayName("Проверка смены пароля")
    void test_changePassword() throws PasswordCheckException, AccountNotFoundException, NonTransferException {
        var userLogin = "Login";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        var tokens = new LinkedHashMap<String, String>();
        tokens.put("JWT0", "Token0");
        tokens.put("JWT1", "Token1");
        tokens.put("JWT2", "Token2");

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));
        when(refreshProvider.searchTokenData(account)).thenReturn(tokens);

        cases.changePassword(userLogin, "NewPassword");

        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var hashCaptor = ArgumentCaptor.forClass(String.class);
        var transferCaptor = ArgumentCaptor.forClass(Boolean.class);
        var jwtCaptor = ArgumentCaptor.forClass(String.class);
        var refreshCaptor = ArgumentCaptor.forClass(String.class);

        verify(accountProvider).setPassword(accountCaptor.capture(), hashCaptor.capture(), transferCaptor.capture());
        verify(accessTokenProvider, times(3)).toBlackList(jwtCaptor.capture());
        verify(refreshProvider, times(3)).release(refreshCaptor.capture());

        assertThat(accountCaptor.getValue()).isEqualTo(account);
        assertThat(BCrypt.checkpw("NewPassword", hashCaptor.getValue())).isTrue();
        assertThat(transferCaptor.getValue()).isFalse();
        for (var i = 0; i < 3; i++) {
            assertThat(jwtCaptor.getAllValues().get(i)).isEqualTo("JWT" + i);
            assertThat(refreshCaptor.getAllValues().get(i)).isEqualTo("Token" + i);
        }

    }

    @Test
    @DisplayName("Проверка смены пароля без УЗ")
    void test_changePassword_noAccount() {
        assertThatThrownBy(() -> cases.changePassword("Any login", "New password"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("AccountDto with login 'Any login' not found");
    }

    @Test
    @DisplayName("Проверка смены пароля. Пароль равен логину")
    void test_changePassword_passwordEqualsLogin() {
        var userLogin = "Login1login";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, userLogin))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Password equals login")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() ==
                        CheckType.PASSWORD_EQUALS_LOGIN, "Check type (0)");
    }

    @Test
    @DisplayName("Проверка смены пароля. Пароль слишком маленький")
    void test_changePassword_shortPassword() {
        var userLogin = "Login1";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, "Ajokl1"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Short password")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SHORT_PASSWORD,
                        "Check " +
                                "type (0)");
    }

    @Test
    @DisplayName("Проверка смены пароля. Есть одинаковые символы")
    void test_changePassword_sameSymbols() {
        var userLogin = "Login1";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, "Aggggggg11"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Same symbols")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SAME_SYMBOLS,
                        "Check " +
                                "type (0)");
    }

    @Test
    @DisplayName("Проверка смены пароля. Только буквы и цифры")
    void test_changePassword_alphaNumbric() {
        var userLogin = "Login1";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, "!@#$%^&*()a"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Letters or numbers only")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SYMBOLS,
                        "Check type (0)");
    }

    @Test
    @DisplayName("Проверка смены пароля. При наличии в пароле :")
    void test_changePassword_alphaNumbric_splitter() {
        var splitter = ":";

        var userLogin = "Login1";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, splitter + "password"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Letters or numbers only")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SYMBOLS,
                        "Check type (0)");

        assertThatThrownBy(() -> cases.changePassword(userLogin, "pass" + splitter + "word"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Letters or numbers only")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SYMBOLS,
                        "Check type (0)");

        assertThatThrownBy(() -> cases.changePassword(userLogin, "password" + splitter))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Letters or numbers only")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.SYMBOLS,
                        "Check type (0)");
    }

    @Test
    @DisplayName("Проверка смены пароля. Пароль слишком простой")
    void test_changePassword_passwordTooEasy() {
        var userLogin = "Login1";

        var account = AccountDto.builder()
                .login(userLogin)
                .transferPassword(true)
                .hash("Password")
                .build();

        when(accountProvider.get(userLogin)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> cases.changePassword(userLogin, "qwerty12345678"))
                .isInstanceOf(PasswordCheckException.class)
                .hasMessage("Next checks was failed: Weak password")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().size() == 1, "Size of checks")
                .matches(check -> ((PasswordCheckException) check).getFailedChecks().getFirst() == CheckType.WEAK_SEQUENCE,
                        "Check " +
                                "type (0)");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Установка ролей")
    void test_setRoles() throws AccountNotFoundException {
        var userId = UUID.randomUUID();
        var userLogin = "Login";

        var account = AccountDto.builder().login(userLogin)
                .hash("Password")
                .build();

        var tokens = new LinkedHashMap<String, String>();
        tokens.put("JWT0", "Token0");
        tokens.put("JWT1", "Token1");
        tokens.put("JWT2", "Token2");

        when(accountProvider.get(userId)).thenReturn(Optional.of(account));
        when(refreshProvider.searchTokenData(account)).thenReturn(tokens);

        cases.setRoles(userId, Set.of("Role1", "Role2", "Role3"));

        var rolesCaptor = ArgumentCaptor.forClass(Set.class);

        var accountCaptor = ArgumentCaptor.forClass(AccountDto.class);
        var jwtCaptor = ArgumentCaptor.forClass(String.class);

        verify(roleProvider).setRoles(accountCaptor.capture(), rolesCaptor.capture());
        verify(accessTokenProvider, times(3)).toBlackList(jwtCaptor.capture());

        assertThat(accountCaptor.getValue()).isEqualTo(account);
        assertThat((Set<String>) rolesCaptor.getValue()).contains("Role1")
                .contains("Role2")
                .contains("Role3");
        for (var i = 0; i < 3; i++) {
            assertThat(jwtCaptor.getAllValues().get(i)).isEqualTo("JWT" + i);
        }
    }

    @Test
    @DisplayName("Установка ролей. Нет пользователя")
    void test_setRoles_noAccount() {
        var userId = UUID.randomUUID();
        var roles = Set.of("Role1", "Role2", "Role3");
        assertThatThrownBy(() -> cases.setRoles(userId, roles))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("AccountDto with ID '" + userId + "' not found");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка деактивации")
    void test_deactivate() {
        var userId = UUID.randomUUID();
        var userLogin = "Login";

        var account = AccountDto.builder().login(userLogin)
                .hash("Password")
                .build();

        var tokens = new LinkedHashMap<String, String>();
        tokens.put("JWT0", "Token0");
        tokens.put("JWT1", "Token1");
        tokens.put("JWT2", "Token2");

        when(accountProvider.get(List.of(userId))).thenReturn(List.of(account));
        when(refreshProvider.searchTokenData(anyCollection())).thenReturn(tokens);

        cases.deactivate(List.of(userId));

        var jwtCaptor = ArgumentCaptor.forClass(String.class);
        var refreshCaptor = ArgumentCaptor.forClass(String.class);
        var accountsCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountProvider).deactivate(accountsCaptor.capture());
        verify(accessTokenProvider, times(3)).toBlackList(jwtCaptor.capture());
        verify(refreshProvider, times(3)).release(refreshCaptor.capture());

        assertThat(accountsCaptor.getValue().getFirst()).isEqualTo(account);
        for (var i = 0; i < 3; i++) {
            assertThat(jwtCaptor.getAllValues().get(i)).isEqualTo("JWT" + i);
            assertThat(refreshCaptor.getAllValues().get(i)).isEqualTo("Token" + i);
        }

    }

    @Test
    @DisplayName("Получение роли по УЗ")
    void test_getRoles() throws AccountNotFoundException {
        var userId = UUID.randomUUID();
        var userLogin = "Login";

        var account = AccountDto.builder().login(userLogin)
                .hash("Password")
                .build();

        var roles = new HashSet<RoleDto>();

        for (var i = 0; i < 3; i++) {
            roles.add(RoleDto.builder()
                    .code("RoleDto" + i)
                    .name("Name" + i)
                    .description("Description" + i)
                    .build());
        }

        when(accountProvider.getAllActiveness(userId)).thenReturn(Optional.of(account));
        when(roleProvider.getRoles(account)).thenReturn(roles);

        var actual = cases.getRoles(userId);

        assertThat(actual).hasSize(3);
        assertThat(actual.stream().anyMatch(role -> role.getCode().equals("RoleDto0"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getCode().equals("RoleDto1"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getCode().equals("RoleDto2"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getName().equals("Name0"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getName().equals("Name1"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getName().equals("Name2"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getDescription().equals("Description0"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getDescription().equals("Description1"))).isTrue();
        assertThat(actual.stream().anyMatch(role -> role.getDescription().equals("Description2"))).isTrue();
    }

    @DisplayName("Проверка обновления сессии")
    @Test
    void test_relogin() throws RefreshTokenExpired, NoSuchAlgorithmException, InvalidKeySpecException {
        var account = AccountDto.builder().login("Login").build();
        var refreshToken = "Refresh";
        var oldAccessToken = "JWT";
        var newAccessToken = new AccessTokenData("New JWT", OffsetDateTime.now());
        var newRefreshToken = new RefreshTokenData("Refreshed", OffsetDateTime.now().plusDays(7));

        when(refreshProvider.search(refreshToken)).thenReturn(Optional.of(account));
        when(refreshProvider.getAccessToken(refreshToken)).thenReturn(oldAccessToken);
        when(accessTokenProvider.generate(account, true)).thenReturn(newAccessToken);
        when(refreshProvider.generate(newAccessToken.value(), account)).thenReturn(newRefreshToken);

        var actual = cases.relogin(refreshToken);

        var oldJwtCaptor = ArgumentCaptor.forClass(String.class);
        var oldRefreshCaptor = ArgumentCaptor.forClass(String.class);

        verify(accessTokenProvider).toBlackList(oldJwtCaptor.capture());
        verify(refreshProvider).release(oldRefreshCaptor.capture());

        assertThat(oldJwtCaptor.getValue()).isEqualTo(oldAccessToken);
        assertThat(oldRefreshCaptor.getValue()).isEqualTo(refreshToken);
        assertThat(actual.accessToken()).isEqualTo(newAccessToken.value());
        assertThat(actual.accessExpiration()).isEqualTo(newAccessToken.expiration());
        assertThat(actual.refreshToken()).isEqualTo(newRefreshToken.value());
        assertThat(actual.refreshExpiration()).isEqualTo(newRefreshToken.expiration());
    }

    @DisplayName("Проверка обновления сессии. Токен истек")
    @Test
    void test_relogin_expired() {
        assertThatThrownBy(() -> cases.relogin("Any token"))
                .isInstanceOf(RefreshTokenExpired.class)
                .hasMessage(String.format("Refresh token '%s' was expired", "Any token"));
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Проверка сохранения")
    @Test
    void test_save() {
        var account = AccountDto.builder().login("Login")
                .hash("hash").build();

        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        cases.save(List.of(account));

        var accountsCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountProvider).save(accountsCaptor.capture());

        var actual = (AccountDto) accountsCaptor.getValue().getFirst();

        assertThat(actual.getHash()).isEqualTo(account.getHash());
        assertThat(actual.getLogin()).isEqualTo(account.getLogin());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Проверка сохранения сгенерированных данных. Email")
    @Test
    void test_save_generated_email() {
        var account = AccountDto.builder().login("Login").hash("hash").email("email").build();
        var template = "Text";

        when(textProvider.getResetPasswordEmail(UserMessage.Scope.EMPLOYEE)).thenReturn(template);
        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        cases.save(List.of(account));

        var accountsCaptor = ArgumentCaptor.forClass(List.class);
        var emailsCaptor = ArgumentCaptor.forClass(List.class);
        var subjectCaptor = ArgumentCaptor.forClass(String.class);
        var templateCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(accountProvider).save(accountsCaptor.capture());
        verify(emailSender).send(emailsCaptor.capture(), subjectCaptor.capture(), templateCaptor.capture(),
                dataCaptor.capture());

        var actual = (AccountDto) accountsCaptor.getValue().getFirst();
        var actualEmails = emailsCaptor.getValue();
        var actualSubject = subjectCaptor.getValue();
        var actualTemplate = templateCaptor.getValue();
        var actualData = dataCaptor.getValue();

        assertThat(actual.getHash()).isEqualTo(account.getHash());
        assertThat(actual.getLogin()).isEqualTo(account.getLogin());
        assertThat(actualEmails).hasSize(1);
        assertThat(String.valueOf(actualEmails.getFirst())).isEqualTo(account.getEmail());
        assertThat(actualSubject).isNull();
        assertThat(actualTemplate).isEqualTo(template);
        assertThat(actualData.entrySet()).hasSize(2);
        assertThat(actualData).containsKey("login")
                .containsEntry("login", account.getLogin())
                .containsKey("password");
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Проверка сохранения сгенерированных данных. Нет email")
    @Test
    void test_save_generated_no_email() {
        var account = AccountDto.builder().login("Login").hash("hash").build();

        when(transferPasswordProvider.generateTransferPassword(anyInt())).thenReturn(generateTransferPassword());
        cases.save(List.of(account));

        var accountsCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountProvider).save(accountsCaptor.capture());

        var actual = (AccountDto) accountsCaptor.getValue().getFirst();

        assertThat(actual.getHash()).isEqualTo(account.getHash());
        assertThat(actual.getLogin()).isEqualTo(account.getLogin());
    }


    private char[] generateTransferPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefhijkmnprstuvwxyz23456789!@#$%^*_=.?)";
        char[] otp = new char[12];
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 12; ++i) {
            otp[i] = chars.charAt(random.nextInt(chars.length()));
        }

        return otp;
    }
}