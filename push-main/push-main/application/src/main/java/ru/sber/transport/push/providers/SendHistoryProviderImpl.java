package ru.sber.transport.push.providers;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.providers.SendHistoryProvider;
import ru.sber.transport.push.database.push.Tables;
import ru.sber.transport.push.providers.mapper.SendHistoryMapper;

@Component
@RequiredArgsConstructor
public class SendHistoryProviderImpl implements SendHistoryProvider {

    private final DSLContext dslContext;

    private final SendHistoryMapper sendHistoryMapper;

    @Override
    public void save(SendHistoryDto sendHistoryDto) {
        var record = sendHistoryMapper.toRecord(sendHistoryDto);
        dslContext.insertInto(Tables.SEND_HISTORY).set(record).execute();
    }
}
