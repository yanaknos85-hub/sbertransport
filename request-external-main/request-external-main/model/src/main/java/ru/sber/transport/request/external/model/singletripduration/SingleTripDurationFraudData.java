package ru.sber.transport.request.external.model.singletripduration;

import lombok.Getter;
import ru.sber.transport.request.external.model.Fraud;

import java.util.UUID;

/**
 * Реализация Fraud для случаев превышения лимита длительности одной заявки (более 8 часов в сутки)
 */
@Getter
public class SingleTripDurationFraudData implements Fraud {

    public static final String SINGLE_TRIP_DURATION_FRAUD_TYPE = "SINGLE_TRIP_DURATION";

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

    public SingleTripDurationFraudData(UUID id, String comment) {
        this.id = id;
        this.comment = comment;
        this.type = SINGLE_TRIP_DURATION_FRAUD_TYPE;
    }

}
