package ru.sber.transport.request.external.providers.fraud;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.Fraud;
import ru.sber.transport.database.external_request.tables.records.FraudRecord;

@Transactional
@RequiredArgsConstructor
public class FraudsDataProviderImpl implements FraudsProvider, JooqRepository<Fraud, FraudRecord, UUID> {

    @Override
    public Fraud table() {
        return Tables.FRAUD;
    }

    @Override
    public void save(ru.sber.transport.request.external.model.Fraud source) {
        context().insertInto(table())
                .set(table().ID, source.getId())
                .set(table().COMMENT, source.getComment())
                .set(table().TYPE, source.getType())
                .onConflict().doNothing().execute();
    }
}
