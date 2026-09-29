package ru.sber.transport.authentication.business.use_cases;

import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.OwnershipProofDto;
import ru.sber.transport.authentication.business.dto.RoleDto;
import ru.sber.transport.authentication.business.dto.Token;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.exceptions.CodeException;
import ru.sber.transport.authentication.business.exceptions.NonTransferException;
import ru.sber.transport.authentication.business.exceptions.PasswordCheckException;
import ru.sber.transport.authentication.business.exceptions.RefreshTokenExpired;
import ru.sber.transport.authentication.business.exceptions.TooManyLoginTriesException;
import ru.sber.transport.authentication.business.exceptions.WrongPasswordException;
import ru.sber.transport.authentication.web.model.RegistrationRequestDto;
import ru.sber.transport.authentication.web.model.RegistrationResponseDto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Пользовательские случаи использования УЗ.
 */
public interface AccountCases {

    /**
     * Вход в систему.
     *
     * @param login    логин.
     * @param password пароль.
     * @return токен доступа.
     * @throws AccountNotFoundException   УЗ не найдена.
     * @throws WrongPasswordException     пароль неверен.
     * @throws TooManyLoginTriesException большое количество неуспешных попыток авторизации.
     */
    Token login(String login, String password) throws AccountNotFoundException, WrongPasswordException, TooManyLoginTriesException, NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Вход в систему.
     *
     * @param userId идентификатор пользователя.
     * @param code   код проверки.
     * @return токен.
     */
    Token login(UUID userId, String code) throws CodeException, NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Выход из системы.
     *
     * @param userId идентификатор пользователя.
     * @param token  токен доступа, для которого производится выход.
     * @throws AccountNotFoundException УЗ не найдена.
     */
    void logout(UUID userId, String token) throws AccountNotFoundException;

    /**
     * Продолжение сессии.
     *
     * @param refresh токен обновления сессии.
     * @return токен доступа.
     * @throws RefreshTokenExpired токен обновления сессии истёк.
     */
    Token relogin(String refresh) throws RefreshTokenExpired, NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Сброс пароля.
     *
     * @param userId идентификатор пользователя для сброса пароля.
     * @throws AccountNotFoundException УЗ не найдена.
     */
    void resetPassword(UUID userId) throws AccountNotFoundException;

    /**
     * Смена пароля.
     *
     * @param login    логин.
     * @param password новый пароль.
     * @throws AccountNotFoundException УЗ не найдена.
     * @throws PasswordCheckException   ошибка проверки пароля.
     * @throws NonTransferException     ошибка проверки озможности смены транспортного пароля.
     */
    void changePassword(String login, String password) throws AccountNotFoundException, PasswordCheckException,
            NonTransferException;

    /**
     * Установка ролей.
     *
     * @param userId идентификатор пользователя.
     * @param roles  новые роли.
     * @throws AccountNotFoundException УЗ не найдена.
     */
    void setRoles(UUID userId, Set<String> roles) throws AccountNotFoundException;

    /**
     * Получение списка ролей пользователя.
     *
     * @param userId идентификатор пользователя.
     * @return список ролей.
     * @throws AccountNotFoundException УЗ не найдена.
     */
    Set<RoleDto> getRoles(UUID userId) throws AccountNotFoundException;

    /**
     * Деактивация пользователя.
     *
     * @param userId идентификатор пользователя.
     */
    void deactivate(Collection<UUID> userId);

    /**
     * Сохранить данные пользователя.
     *
     * @param accountDto УЗ полизователя.
     */
    void save(Collection<AccountDto> accountDto);

    /**
     * Регистрация нового УЗ
     *
     * @param dto Данные для регистрации
     * @return Данные регистрации
     */
    RegistrationResponseDto register(RegistrationRequestDto dto);

    /**
     * Отправка кода для подтверждения владения аккаунтом.
     *
     * @param ownershipProofDto данные для подтверждения владения аккаунтом.
     */
    void sendCodeForOwnershipProof(OwnershipProofDto ownershipProofDto) throws AccountNotFoundException;

    /**
     * Проверка кода для подтверждения владения аккаунтом и сброса пароля.
     *
     * @param code              код
     * @param ownershipProofDto данные для подтверждения владения аккаунтом.
     */
    void codeCheck(String code, OwnershipProofDto ownershipProofDto) throws AccountNotFoundException;
}
