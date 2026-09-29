package ru.sber.transport.request.external.providers.grpc;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import ru.sber.transport.request.external.model.Assessment;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.Fraud;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.WaypointData;

@Builder
@Getter
public class TestOrderData implements TripOrderData {

    private UUID id;

    private String humanReadableId;

    private TestEmployee passenger;

    private TestEmployee approver;

    private URI link;

    private String receipt;

    private TestPriceData planned;

    private TestPriceData actual;

    private OffsetDateTime date;

    private List<WaypointData> waypoints;

    private UUID purposeId;

    private Tariff tariff;

    private String comment;

    private State status;

    private String reason;

    private TestAssessments assessments;

    private String costCenter;

    private Fraud fraud;

    private String timeZone;

    private OffsetDateTime approvalDate;

    private BigDecimal economy;

    private BigDecimal taxiCost;

    private String receiptLink;

    @Builder
    @Getter
    private static class TestPriceData implements OrderData {

        private BigDecimal cost;

        private Duration duration;

        private long distance;

    }

    @Getter
    @Builder
    private static class TestEmployee implements Employee {

        private UUID positionId;

        private UUID id;

        private UUID departmentId;

        private UUID organizationId;

        private String lastName;

        private String firstName;

        private String patronymic;

        private String personnelNumber;

        private String costCenter;

    }

    private record TestAssessments(TestAssessment getService) implements Assessments {

    }

    private record TestAssessment(String getComment, byte getRating) implements Assessment {

    }

}
