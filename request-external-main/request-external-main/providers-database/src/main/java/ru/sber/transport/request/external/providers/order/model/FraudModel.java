package ru.sber.transport.request.external.providers.order.model;

import static ru.sber.transport.database.external_request.Tables.FRAUD;

import java.util.UUID;
import org.jooq.Record;
import ru.sber.transport.request.external.model.Fraud;

/**
 * Модель фрода
 */
public class FraudModel implements Fraud {

    private final Record source;

    public FraudModel(Record source) {
        this.source = source;
    }

    @Override
    public UUID getId() {
        return source.get(FRAUD.ID);
    }

    @Override
    public String getComment() {
        return source.get(FRAUD.COMMENT);
    }

    @Override
    public String getType() {
        return source.get(FRAUD.TYPE);
    }
}
