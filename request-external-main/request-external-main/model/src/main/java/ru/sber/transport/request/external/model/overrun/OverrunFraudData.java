package ru.sber.transport.request.external.model.overrun;

import ru.sber.transport.request.external.model.Fraud;

import java.util.UUID;

/**
 * Реализация Fraud для случаев превышения лимита километража
 */
public class OverrunFraudData implements Fraud {

    public static final String FRAUD_TYPE = "OVERRUN";

    /**Идентификатор заявки**/
    private final UUID id;

    /**Комментарий к фродовой заявке**/
    private final String comment;

    /**Тип к фродовой заявке**/
    private final String type;

    public OverrunFraudData(UUID id, String comment) {
        this.id = id;
        this.comment = comment;
        this.type = FRAUD_TYPE;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getType() {
        return type;
    }
}
