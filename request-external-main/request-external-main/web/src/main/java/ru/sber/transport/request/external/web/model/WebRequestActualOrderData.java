package ru.sber.transport.request.external.web.model;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.web.model.EditExternalRequest;

@RequiredArgsConstructor
public class WebRequestActualOrderData implements OrderData {

    private final EditExternalRequest delegatee;

    @Override
    public BigDecimal getCost() {
        return delegatee.getFactCost();
    }

    @Override
    public Duration getDuration() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public long getDistance() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
