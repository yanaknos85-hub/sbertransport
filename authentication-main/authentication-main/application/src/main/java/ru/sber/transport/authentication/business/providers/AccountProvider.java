package ru.sber.transport.authentication.business.providers;


import ru.sber.transport.authentication.business.dto.AccountDto;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер данных УЗ.
 */
public interface AccountProvider {
    
    /**
     * Получение УЗ по логину.
     *
     * @param login логин.
     * @return УЗ.
     */
    Optional<AccountDto> get(String login);
    
    /**
     * Получение УЗ по идентификатору.
     *
     * @param id идентификатор.
     * @return УЗ.
     */
    Optional<AccountDto> get(UUID id);

    /**
     * Получение УЗ по идентификатору.
     *
     * @param id идентификатор.
     * @return УЗ.
     */
    Collection<AccountDto> get(Collection<UUID> id);

    /**
     * Получение УЗ по идентификатору со всеми статусами активности.
     *
     * @param id идентификатор пользователя.
     * @return УЗ.
     */
    Optional<AccountDto> getAllActiveness(UUID id);
    
    /**
     * Установка хэша пароля УЗ.
     *
     * @param accountDto УЗ.
     * @param hash хэш пароля.
     * @param isTransfer флаг транспортного пароля.
     */
    void setPassword(AccountDto accountDto, String hash, boolean isTransfer);
    
    /**
     * Деактивация аккаунта.
     *
     * @param accountDto УЗ для деактивации.
     */
    void deactivate(AccountDto accountDto);

    /**
     * Деактивация аккаунта.
     *
     * @param accountDto УЗ для деактивации.
     */
    void deactivate(Collection<AccountDto> accountDto);
    
    /**
     * Сохранение аккаунта.
     *
     * @param accountDto УЗ.
     */
    void save(AccountDto accountDto);

    /**
     * Сохранение аккаунта.
     *
     * @param accountDto УЗ.
     */
    void save(Collection<AccountDto> accountDto);
    
    /**
     * Проверка пустой базы пользователей.
     *
     * @return <code>true</code> если база пуста.
     */
    boolean empty();

    /**
     * Получение аккаунта по почте.
     *
     * @return аккаунт.
     */
    Optional<AccountDto> getByEmail(String email);

    int getLoginsCount(String login);
}
