package ru.sber.transport.authentication.business.providers;

import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.RefreshTokenData;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Поставщик токенов обновления.
 */
public interface RefreshProvider {
    
    /**
     * Сгенерировать токен.
     *
     * @param accessToken токен доступа.
     * @param accountDto УЗ для генерации токена.
     * @return токен.
     */
    RefreshTokenData generate(String accessToken, AccountDto accountDto);
    
    /**
     * Освободить токен обновления.
     *
     * @param accountDto УЗ для поиска токенов.
     * @param token токен доступа.
     */
    void release(AccountDto accountDto, String token);
    
    /**
     * Поиск УЗ с указанным токеном обновления.
     *
     * @param refresh токен обновления.
     * @return УЗ.
     */
    Optional<AccountDto> search(String refresh);
    
    /**
     * Освободить токен обновления.
     *
     * @param refresh токен обновления.
     */
    void release(String refresh);
    
    /**
     * Поиск данных токена.
     *
     * @param login УЗ для поиска данных.
     * @return карта токенов. Ключ - токен доступа, значение - токен обновления сессии.
     */
    Map<String, String> searchTokenData(AccountDto login);

    /**
     * Поиск данных токена.
     *
     * @param login УЗ для поиска данных.
     * @return карта токенов. Ключ - токен доступа, значение - токен обновления сессии.
     */
    Map<String, String> searchTokenData(Collection<AccountDto> login);
    
    /**
     * Поиск старого токена доступа.
     *
     * @param refresh токен обновления.
     * @return токен доступа.
     */
    String getAccessToken(String refresh);
    
    /**
     * Поиск УЗ по токену обновления сессии.
     *
     * @param refresh токен.
     * @return УЗ.
     */
    Optional<AccountDto> getAccount(String refresh);
}
