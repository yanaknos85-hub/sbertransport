package ru.sber.transport.authentication.business.providers;

import ru.sber.transport.authentication.business.dto.AccessTokenData;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.TwoFactor;

import java.util.UUID;

/**
 * Провайдер данных второго фактора аутентификации.
 */
public interface TwoFactorProvider {

    /**
     * Генерировать код второго фактора аутентификации.
     *
     * @param account УЗ для генерирования кода.
     * @param data данные токена доступа.
     * @return описание второго фактора.
     */
    TwoFactor generate(AccountDto account, AccessTokenData data);

    /**
     * Проверить корректность кода от пользователя.
     *
     * @param userId пользователь.
     * @param code код.
     * @return проверка кода.
     */
    boolean checkCode(UUID userId, String code);

    /**
     * Удалить использованный код второго фактора.
     *
     * @param userId идентификатор пользователя.
     */
    void removeCode(UUID userId);

    /**
     * Адрес для перехода на ввод кода.
     *
     * @return адрес.
     */
    String codeUrl();

}
