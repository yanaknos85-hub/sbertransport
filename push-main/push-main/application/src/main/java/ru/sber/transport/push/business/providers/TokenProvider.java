package ru.sber.transport.push.business.providers;

import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;

import java.util.List;
import java.util.UUID;

/**
 * Провейдер токенов.
 */
public interface TokenProvider {

    /**
     * Получение токенов получателя.
     *
     * @param recipient получатель.
     * @return токены.
     */
    List<TokenData> get(UUID recipient);

    /**
     * Сохранение токена получателя.
     *
     * @param id          идентификатор токена.
     * @param recipientId идентификатор получателя.
     * @param tokenData   токен.
     * @return запись токена.
     */
    TokenRecord save(UUID id, UUID recipientId, TokenData tokenData);
}
