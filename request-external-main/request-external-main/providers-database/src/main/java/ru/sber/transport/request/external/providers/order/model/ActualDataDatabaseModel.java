package ru.sber.transport.request.external.providers.order.model;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.request.external.model.OrderData;

@RequiredArgsConstructor
public class ActualDataDatabaseModel implements OrderData {

    @Delegate
    private final TripOrderRecord delegatee;

    @Override
    public BigDecimal getCost() {
        return delegatee.getActualCost();
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
