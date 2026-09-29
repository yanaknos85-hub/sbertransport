package ru.sber.transport.request.external.web.model;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.web.model.EditExternalRequest;

/**
 * Класс-обертка для создания модели заказа на основе модели запроса.
 */
@RequiredArgsConstructor
public class WebRequestTripOrderData implements EditTripOrderData {

    @Delegate
    private final EditExternalRequest delegatee;

    @Override
    public OrderData getActual() {
        return new WebRequestActualOrderData(delegatee);
    }

    @Override
    public State getStatus() {
        return Optional.ofNullable(delegatee.getStatus()).map(Enum::name).map(State::valueOf).orElse(null);
    }

    @Override
    public Assessments getAssessments() {
        return Optional.ofNullable(delegatee.getAssessments()).map(WebRequestAssessments::new).orElse(null);
    }

}
