package ru.sber.transport.request.external.messaging;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.WaypointData;

public record TestOrder(
            UUID getId,
            String getHumanReadableId,
            OffsetDateTime getDate,
            TestOrderData getPlanned,
            TestOrderData getActual,
            State getStatus,
            String getComment,
            String getReason,
            String getReceipt,
            Tariff getTariff,
            UUID getPurposeId,
            List<WaypointData> getWaypoints,
            URI getLink,
            TestEmployee getPassenger,
            TestEmployee getApprover,
            TestAssessments getAssessments,
            String getCostCenter,
            TestFraud getFraud,
            String getTimeZone,
            OffsetDateTime getApprovalDate,
            BigDecimal getEconomy,
            BigDecimal getTaxiCost,
            String getReceiptLink
    ) implements TripOrderData {
}