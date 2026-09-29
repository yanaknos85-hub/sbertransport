package ru.sber.transport.authentication.web.cache;

import ru.sber.transport.authentication.business.dto.Token;

public interface RefreshTokenCacheService {

    /**
     * Кеширование токена
     * @param key ключ
     * @param value значение
     */
    void add(String key, Token value);

    /**
     * Получение токена
     * @param key ключ
     */
    Token get(String key);

}
