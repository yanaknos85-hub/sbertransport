package ru.sber.transport.request.external.providers.order.history;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.tables.TripOrderHistory;
import ru.sber.transport.database.external_request.tables.records.TripOrderHistoryRecord;
import ru.sber.transport.request.external.model.State;

@Transactional
@RequiredArgsConstructor
public class TripOrderHistoriesDataProviderImpl implements TripOrderHistoriesProvider, JooqRepository<TripOrderHistory, TripOrderHistoryRecord, Map<String, Object>>, Serializable {

    @Override
    public TripOrderHistory table() {
        return Tables.TRIP_ORDER_HISTORY;
    }

    @Override
    public void save(UUID orderId, State status, String comment, UUID modifiedBy, String reason, String receipt, UUID approver) {
        context().insertInto(table())
                .set(table().ORDER_ID, orderId)
                .set(table().STATUS, OrderState.valueOf(status.name()))
                .set(table().COMMENT, comment)
                .set(table().MODIFIED_BY, modifiedBy)
                .set(table().REASON, reason)
                .set(table().MODIFIED_AT, OffsetDateTime.now())
                .set(table().RECEIPT, receipt)
                .set(table().APPROVER_ID, approver)
                .execute();
    }

    @Override
    public Map<UUID, List<ru.sber.transport.request.external.model.TripOrderHistory>> get(List<UUID> orderIds) {
        return context().selectFrom(table())
                .where(table().ORDER_ID.in(orderIds))
                .fetchInto(TripOrderHistoryRecord.class)
                .stream()
                .map(it -> Map.entry(it.getOrderId(), new TripOrderHistoryDatabase(it)))
                .collect(Collectors.toMap(Map.Entry::getKey, it -> List.of(it.getValue()), (first, second) -> Stream.concat(first.stream(), second.stream()).toList()));
    }

    @Override
    public List<ru.sber.transport.request.external.model.TripOrderHistory> get(UUID orderId) {
        return context().selectFrom(table())
                .where(table().ORDER_ID.eq(orderId))
                .orderBy(table().MODIFIED_AT.desc())
                .fetchInto(TripOrderHistoryRecord.class)
                .stream()
                .map(TripOrderHistoryDatabase::new)
                .map(dto -> (ru.sber.transport.request.external.model.TripOrderHistory) dto)
                .toList();
    }
}
