package ru.sber.transport.request.external.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.external.messaging.TestAssessment;
import ru.sber.transport.request.external.messaging.TestOrder;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.WaypointData;

class RequestMapperTest {

    private final WaypointMapper waypointMapper = new WaypointMapperImpl();
    private final RequestMapper requestMapper = new RequestMapperImpl(waypointMapper);

    @Test
    void toRequestPayoutMessage() {
        var editTripOrderData = Instancio.create(TestTripOrderSource.class);
        var tripOrder = Instancio.create(TestOrder.class);
        var modifiedAt = Instancio.create(OffsetDateTime.class);
        var actual = requestMapper.toRequestPayoutMessage(editTripOrderData, tripOrder, modifiedAt);

        assertThat(actual).isNotNull()
                .extracting(
                        RequestPayoutMessage::id,
                        RequestPayoutMessage::humanReadableId,
                        RequestPayoutMessage::costCenter,
                        RequestPayoutMessage::resource,
                        RequestPayoutMessage::organizationId,
                        RequestPayoutMessage::departmentId,
                        RequestPayoutMessage::actualCost,
                        RequestPayoutMessage::employeeId,
                        RequestPayoutMessage::changeDate,
                        RequestPayoutMessage::transportType
                )
                .containsExactly(
                        tripOrder.getId(),
                        tripOrder.getHumanReadableId(),
                        "4661",
                        "26511",
                        tripOrder.getPassenger().getOrganizationId(),
                        tripOrder.getPassenger().getDepartmentId(),
                        editTripOrderData.getActual().getCost(),
                        tripOrder.getPassenger().getId(),
                        modifiedAt,
                        "YANDEX_TAXI"
                );
    }

    private record TestTripOrderSource(
            UUID getId,
            String getHumanReadableId,
            TestTripOrderData getPlanned,
            TestTripOrderData getActual,
            OffsetDateTime getDate,
            UUID getPassengerId,
            State getStatus,
            String getComment,
            String getReason,
            ru.sber.transport.request.external.model.Tariff getTariff,
            UUID getPurposeId,
            List<WaypointData> getWaypointData,
            URI getLink,
            TestAssessments getAssessments,
            String getReceiptLink
    ) implements EditTripOrderData {
    }

    private record TestTripOrderData(BigDecimal getCost, Duration getDuration, long getDistance) implements OrderData {
    }

    private record TestAssessments(TestAssessment getService) implements Assessments {
    }
}
