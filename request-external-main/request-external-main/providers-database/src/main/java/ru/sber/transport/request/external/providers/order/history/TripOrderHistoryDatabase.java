package ru.sber.transport.request.external.providers.order.history;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.external_request.tables.records.TripOrderHistoryRecord;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderHistory;

@RequiredArgsConstructor
class TripOrderHistoryDatabase implements TripOrderHistory {

    @Delegate
    private final TripOrderHistoryRecord delegatee;

    @Override
    public State getStatus() {
        return State.valueOf(delegatee.getStatus().name());
    }
}
