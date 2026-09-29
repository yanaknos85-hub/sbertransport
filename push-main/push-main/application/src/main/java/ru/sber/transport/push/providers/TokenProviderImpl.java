package ru.sber.transport.push.providers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.business.providers.TokenProvider;
import ru.sber.transport.push.database.push.Keys;
import ru.sber.transport.push.database.push.Tables;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;
import ru.sber.transport.push.providers.mapper.TokenMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
class TokenProviderImpl implements TokenProvider {

    private final DSLContext dslContext;

    private final TokenMapper tokenMapper;

    @Override
    public List<TokenData> get(UUID recipient) {
        return dslContext
                .selectFrom(Tables.TOKEN)
                .where(Tables.TOKEN.RECIPIENT_ID.eq(recipient))
                .fetchInto(Tables.TOKEN)
                .stream()
                .map(tokenMapper::toModel)
                .toList();
    }

    @Override
    public TokenRecord save(UUID id, UUID recipientId, TokenData tokenData) {
        var tokenRecord = tokenMapper.toRecord(recipientId, tokenData);
        tokenRecord.setId(id);
        if (!dslContext.fetchExists(dslContext.selectFrom(Tables.TOKEN).where(Tables.TOKEN.VALUE.eq(tokenData.getValue())))) {
            dslContext
                .insertInto(Tables.TOKEN)
                .set(tokenRecord)
                .onConflict(Keys.PUSH_TOKEN_PK.getFields())
                .doUpdate()
                .set(tokenRecord)
                .execute();
            log.info("A new token received");
        } else {
            dslContext.update(Tables.TOKEN).set(tokenRecord).where(Tables.TOKEN.VALUE.eq(tokenData.getValue())).execute();
            log.info("The received token was transferred to the recipient with id: {}", recipientId);
        }
        return tokenRecord;
    }

}
