package ru.sber.transport.request.external.providers.order.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.database.external_request.tables.records.WaypointRecord;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.Fraud;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.WaypointData;

/**
 * Модель заказа поездки.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class TripDatabaseModel implements TripOrderData {

    @Delegate
    private final TripOrderRecord delegatee;

    private String humanReadableId;

    private String costCenter;

    private OffsetDateTime approvalDate;

    private Assessments assessments;

    private List<WaypointData> waypoints;

    private Employee passenger;

    private Employee approver;

    private URI link;

    private Fraud fraud;

    @Override
    public String getHumanReadableId() {
        return humanReadableId;
    }

    public void humanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
    }

    @Override
    public OrderData getPlanned() {
        return new PlannedDataDatabaseModel(delegatee);
    }

    @Override
    public String getCostCenter() {
        return costCenter;
    }

    public void costCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    @Override
    public OrderData getActual() {
        return new ActualDataDatabaseModel(delegatee);
    }

    @Override
    public State getStatus() {
        return State.valueOf(delegatee.getStatus().name());
    }

    public void setWaypoints(List<? extends Map.Entry<WaypointRecord, Integer>> list) {
        this.waypoints = list.stream().sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(WaypointDatabaseModel::new)
                .map(WaypointData.class::cast)
                .toList();
    }

    @Override
    public Tariff getTariff() {
        return Tariff.valueOf(delegatee.getTariff().name());
    }

    @Override
    public BigDecimal getEconomy() {
        return delegatee.getEconomy();
    }
}
