package ru.sber.transport.request.external.providers.order.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.request.external.model.OrderData;

@RequiredArgsConstructor
public class PlannedDataDatabaseModel implements OrderData {

    @Delegate
    private final TripOrderRecord delegatee;

    @Override
    public BigDecimal getCost() {
        return delegatee.getPlannedCost();
    }

    @Override
    public Duration getDuration() {
        return Optional.ofNullable(delegatee.getPlannedDuration())
                .map(it -> Duration.ofNanos(it.getNano()).plusSeconds(it.getSeconds()).plusMinutes(it.getMinutes()).plusHours(it.getHours()).plusDays(it.getDays()))
                .orElse(Duration.ZERO);
    }

    @Override
    public long getDistance() {
        return delegatee.getPlannedDistance();
    }
}
