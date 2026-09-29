package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TestTripOrder(UUID getId, String getHumanReadableId, TestOrderData getPlanned, TestOrderData getActual,
                            State getStatus, String getComment, String getReason, String getReceipt, Tariff getTariff,
                            OffsetDateTime getDate, List<TestWaypointData> waypoints,
                            UUID getPurposeId, URI getLink, TestEmployee getPassenger, TestEmployee getApprover,
                            TestAssessments getAssessments, String getCostCenter, TestFraud getFraud, String getTimeZone, OffsetDateTime getApprovalDate, BigDecimal getEconomy, BigDecimal getTaxiCost, String getReceiptLink) implements TripOrderData {

        @Override
        public List<WaypointData> getWaypoints() {
            return waypoints.stream().map(WaypointData.class::cast).toList();
        }
    }