package ru.sber.transport.request.external.model.duration;

import lombok.Getter;
import ru.sber.transport.request.external.model.Fraud;

import java.util.UUID;

/**
 * Реализация Fraud для случаев превышения лимита по продолжительности поездки
 */
@Getter
public class DurationFraudData implements Fraud {

    public static final String DURATION_FRAUD_TYPE = "DURATION";

    /**
     * Идентификатор заявки
     **/
    private final UUID id;

    /**
     * Комментарий к фродовой заявке
     **/
    private final String comment;

    /**
     * Тип к фродовой заявке
     **/
    private final String type;

    public DurationFraudData(UUID id, String comment) {
        this.id = id;
        this.comment = comment;
        this.type = DURATION_FRAUD_TYPE;
    }

}
