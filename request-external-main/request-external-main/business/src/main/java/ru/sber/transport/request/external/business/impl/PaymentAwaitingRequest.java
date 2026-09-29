package ru.sber.transport.request.external.business.impl;

import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderData;

/**
 * Объект запроса на изменеение статуса заявки на "Ожидание выплаты"
 *
 * @param item - данные о заявке
 */
public record PaymentAwaitingRequest(TripOrderData item) implements EditTripOrderData {

    @Override
    public OrderData getActual() {
        return item.getActual();
    }

    @Override
    public State getStatus() {
        return State.PAYMENT_AWAITING;
    }

    @Override
    public String getReason() {
        return item.getReason();
    }

    @Override
    public Assessments getAssessments() {
        return null;
    }

    @Override
    public String getReceiptLink() {
        return null;
    }
}