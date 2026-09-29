package ru.sber.transport.request.external.providers.order;

import static ru.sber.database.model.Type.DELETE;
import static ru.sber.database.model.Type.INSERT;
import static ru.sber.database.model.Type.UPDATE;
import static ru.sber.transport.database.external_request.Tables.TRIP_ORDER;

import jakarta.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.Field;
import org.jooq.Query;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.database.listeners.ModifiedListener;
import ru.sber.transport.business.providers.CurrentUser;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.TripOrder;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.request.external.model.State;
import ru.sberbank.utils.reflection.ReflectionUtils;

@RequiredArgsConstructor
public class TripOrderListener implements ModifiedListener<TripOrderRecord, TripOrder> {

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider;

    private final CurrentUser userDataProvider;

    private transient final ObjectProvider<DSLContext> contextProvider;

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final DSLContext context = contextProvider.getObject();

    @SneakyThrows(SQLException.class)
    @Override
    public void afterQueryExecute(ExecuteContext ctxt, ru.sber.database.model.Query<Query, TripOrder, TripOrderRecord> query) {
        if (List.of(UPDATE, INSERT, DELETE).contains(query.getType())) {
            createHistoryRecord(ctxt, query);
        }
        ModifiedListener.super.beforeQueryExecute(ctxt, query);
    }

    @Override
    public TripOrder getTable() {
        return Tables.TRIP_ORDER;
    }

    @Override
    public Field<Object> getId(TripOrder tripOrder) {
        return ReflectionUtils.cast(tripOrder.ID);
    }

    @Override
    public Field<String> getHash(TripOrder tripOrder) {
        return tripOrder.HASH;
    }

    @Override
    public Field<OffsetDateTime> getModifiedAt(TripOrder tripOrder) {
        return tripOrder.MODIFIED_AT;
    }

    private void createHistoryRecord(ExecuteContext ctxt, ru.sber.database.model.Query<Query, TripOrder, TripOrderRecord> query) throws SQLException {
        var orderId = query.getValue(TRIP_ORDER.ID);

        if (orderId == null) {
            saveWithSelect(ctxt, query);
        } else {
            directlySave(null, query);
        }
    }

    @SneakyThrows(SQLException.class)
    private void directlySave(@Nullable ResultSet result, @NonNull ru.sber.database.model.Query<Query, TripOrder, TripOrderRecord> query) {
        var orderId = query.getValue(TRIP_ORDER.ID);
        var state = Optional.ofNullable(query.getValue(TRIP_ORDER.STATUS)).map(Enum::name).map(State::valueOf).orElse(null);
        var comment = query.getValue(TRIP_ORDER.COMMENT);
        var reason = query.getValue(TRIP_ORDER.REASON);
        var receipt = query.getValue(TRIP_ORDER.RECEIPT);
        var approver = query.getValue(TRIP_ORDER.APPROVER_ID);

        orderId = orderId == null && result != null ? result.getObject(TRIP_ORDER.ID.getName().toLowerCase(), UUID.class) : orderId;
        state = state == null && result != null ? State.valueOf(result.getString(TRIP_ORDER.STATUS.getName().toLowerCase())) : state;
        comment = comment == null && result != null ? result.getObject(TRIP_ORDER.COMMENT.getName().toLowerCase(), String.class) : comment;
        reason = reason == null && result != null ? result.getObject(TRIP_ORDER.REASON.getName().toLowerCase(), String.class) : reason;
        receipt = receipt == null && result != null ? result.getObject(TRIP_ORDER.RECEIPT.getName().toLowerCase(), String.class) : receipt;
        approver = approver == null && result != null ? result.getObject(TRIP_ORDER.APPROVER_ID.getName().toLowerCase(), UUID.class) : approver;
        final var modifiedBy = userDataProvider.get();
        tripOrderHistoriesProvider.save(orderId, state, comment, modifiedBy, reason, receipt, approver);
    }

    private void saveWithSelect(ExecuteContext ctxt, ru.sber.database.model.Query<Query, TripOrder, TripOrderRecord> query) throws SQLException {
        try (final var connection = ctxt.connection();
             final var statement = connection.createStatement()) {
            var where = query.getWhere();
            where = Objects.equals(where, "") ? "true = true" : where;

            var sql = "SELECT * FROM %s WHERE %s".formatted(query.getTable().toString().toLowerCase(), where);
            for (final var wheredValue : query.getWhereValues()) {
                sql = sql.replaceFirst("\\?", "'%s'".formatted(wheredValue.toString()));
            }
            final var result = statement.executeQuery(sql);
            while (result.next()) {
                directlySave(result, query);
            }
        }
    }
}
