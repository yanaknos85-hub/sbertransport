package ru.sber.transport.request.external.business.impl;

import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderData;

public record ChangeStateRequest(
    TripOrderData item,
    State newState
) implements EditTripOrderData {

    @Override
    public OrderData getActual() {
        return item.getActual();
    }

    @Override
    public State getStatus() {
        return newState;
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
        return "";
    }

}
