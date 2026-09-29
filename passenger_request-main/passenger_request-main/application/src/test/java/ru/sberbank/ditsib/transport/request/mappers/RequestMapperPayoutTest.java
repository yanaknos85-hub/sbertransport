package ru.sberbank.ditsib.transport.request.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.database.model.BaseTariff;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Отдельные тесты для маппера RequestMapper — методы , связанные с payout и компенсациями.
 * Покрывает новые поля: ticketsCost, ticketsCount, compensationType, orderPaymentFormationStartDate,
 * employeeDriverId, additionalSum, expectedDistance и методы aggregate/first.
 */
class RequestMapperPayoutTest {

    private final RequestMapper mapper = new RequestMapperImpl(
            new WaypointsMapperImpl(new AddressMapperImpl()),
            new ExpectedDataMapperImpl(),
            new FraudMapperImpl());

    @Test
    @DisplayName("aggregateTicketsCost: агрегирует стоимость билетов из нескольких компенсаций")
    void aggregateTicketsCost_shouldSumAllTicketsCost() {
        var request = createRequestForPublicWithCompensations(
                List.of(30000, 20000, 15000));

        var result = mapper.aggregateTicketsCost(request);

        assertThat(result).isEqualTo(65000);
    }

    @Test
    @DisplayName("aggregateTicketsCost: возвращает null при null transportCompensation")
    void aggregateTicketsCost_shouldReturnNullWhenNull() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(null)
                .compensationDocuments(List.of())
                .build();

        var result = mapper.aggregateTicketsCost(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("aggregateTicketsCost: возвращает null при пустом списке компенсаций")
    void aggregateTicketsCost_shouldReturnNullWhenEmpty() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(List.of())
                .compensationDocuments(List.of())
                .build();

        var result = mapper.aggregateTicketsCost(request);

        assertThat(result).isNull();
    }


    @Test
    @DisplayName("aggregateTicketsCount: агрегирует количество билетов из нескольких компенсаций")
    void aggregateTicketsCount_shouldSumAllTicketsCount() {
        var request = createRequestForPublicWithCompensationsCount(List.of(2, 3, 1));

        var result = mapper.aggregateTicketsCount(request);

        assertThat(result).isEqualTo(6);
    }

    @Test
    @DisplayName("aggregateTicketsCount: возвращает null при null transportCompensation")
    void aggregateTicketsCount_shouldReturnNullWhenNull() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(null)
                .compensationDocuments(List.of())
                .build();

        var result = mapper.aggregateTicketsCount(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("aggregateTicketsCount: возвращает null при пустом списке компенсаций")
    void aggregateTicketsCount_shouldReturnNullWhenEmpty() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(List.of())
                .compensationDocuments(List.of())
                .build();

        var result = mapper.aggregateTicketsCount(request);

        assertThat(result).isNull();
    }


    @Test
    @DisplayName("firstCompensationType: возвращает тип первой компенсации")
    void firstCompensationType_shouldReturnFirstType() {
        var request = createRequestForPublicWithCompensationTypes(
                List.of(PublicCompensationType.SUBURB_TRIP_COMPENSATION,
                        PublicCompensationType.TRAVEL_CARD_COMPENSATION));

        var result = mapper.firstCompensationType(request);

        assertThat(result).isEqualTo(PublicCompensationType.SUBURB_TRIP_COMPENSATION.name());
    }

    @Test
    @DisplayName("firstCompensationType: возвращает null при null transportCompensation")
    void firstCompensationType_shouldReturnNullWhenNull() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(null)
                .compensationDocuments(List.of())
                .build();

        var result = mapper.firstCompensationType(request);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("firstCompensationType: возвращает null при пустом списке компенсаций")
    void firstCompensationType_shouldReturnNullWhenEmpty() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .transportCompensation(List.of())
                .compensationDocuments(List.of())
                .build();

        var result = mapper.firstCompensationType(request);

        assertThat(result).isNull();
    }


    @Test
    @DisplayName("requestToRequestPayoutMessage: корректно маппит RequestForPublic с новыми полями")
    void requestToRequestPayoutMessage_requestForPublic_withNewFields() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var orderPaymentStartDate = LocalDateTime.of(2025, 7, 1, 9, 0);

        var passenger = createEmployee(passengerId, departmentId, "COST_CENTER_123");
        var organization = Organization.builder()
                .id(organizationId)
                .officialName("Org Name")
                .build();
        passenger.setDepartment(createDepartment(departmentId, "Department", organization));

        var tc1 = TransportCompensation.builder()
                .ticketsCost(30000)
                .ticketsCount(2)
                .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                .build();
        var tc2 = TransportCompensation.builder()
                .ticketsCost(20000)
                .ticketsCount(3)
                .compensationType(PublicCompensationType.TRAVEL_CARD_COMPENSATION)
                .build();

        var expected = new ExpectedData();
        expected.setCost(100000.0);

        var request = RequestForPublic.builder()
                .id(requestId)
                .humanReadableId("REQ-001")
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .passenger(passenger)
                .organizationId(organizationId)
                .expected(expected)
                .transportCompensation(List.of(tc1, tc2))
                .compensationDocuments(List.of())
                .orderPaymentFormationStartDate(orderPaymentStartDate)
                .paymentDoneDeadlineState(DeadlineState.NONE)
                .build();

        var changeDate = LocalDateTime.of(2025, 8, 1, 12, 0);
        var result = mapper.requestToRequestPayoutMessage(request, changeDate);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(requestId);
        assertThat(result.costCenter()).isEqualTo("COST_CENTER_123");
        assertThat(result.resource()).isEqualTo("26511");
        assertThat(result.organizationId()).isEqualTo(organizationId);
        assertThat(result.actualCost()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.employeeId()).isEqualTo(passengerId);
        assertThat(result.changeDate()).isEqualTo(changeDate.atOffset(java.time.ZoneOffset.UTC));
        assertThat(result.transportType()).isEqualTo("PUBLIC");
        assertThat(result.ticketsCost()).isEqualTo(50000);
        assertThat(result.ticketsCount()).isEqualTo(5);
        assertThat(result.compensationType()).isEqualTo(PublicCompensationType.SUBURB_TRIP_COMPENSATION.name());
        assertThat(result.orderPaymentFormationStartDate()).isEqualTo(LocalDate.of(2025, 7, 1));
    }

    @Test
    @DisplayName("requestToRequestPayoutMessage: корректно маппит RequestForPublic без компенсаций")
    void requestToRequestPayoutMessage_requestForPublic_withoutCompensations() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();

        var passenger = createEmployee(passengerId, departmentId, "COST_CENTER_789");
        passenger.setDepartment(createDepartment(departmentId, "Department",
                Organization.builder().id(organizationId).build()));

        var expected = new ExpectedData();
        expected.setCost(5000.0);

        var request = RequestForPublic.builder()
                .id(requestId)
                .humanReadableId("REQ-NO-TC")
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 2, 10, 8, 0))
                .passenger(passenger)
                .organizationId(organizationId)
                .expected(expected)
                .transportCompensation(List.of())
                .compensationDocuments(List.of())
                .paymentDoneDeadlineState(DeadlineState.NONE)
                .build();

        var changeDate = LocalDateTime.of(2025, 8, 1, 12, 0);
        var result = mapper.requestToRequestPayoutMessage(request, changeDate);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(requestId);
        assertThat(result.transportType()).isEqualTo("PUBLIC");
        assertThat(result.ticketsCost()).isNull();
        assertThat(result.ticketsCount()).isNull();
        assertThat(result.compensationType()).isNull();
    }

    @Test
    @DisplayName("requestToRequestPayoutMessage: корректно маппит RequestForPersonal с новыми полями")
    void requestToRequestPayoutMessage_requestForPersonal_withNewFields() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var employeeDriverId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var additionalSum = 5000L;

        var passenger = createEmployee(passengerId, departmentId, "COST_CENTER_456");
        passenger.setDepartment(createDepartment(departmentId, "Department",
                Organization.builder().id(organizationId).build()));

        var expected = new ExpectedData();
        expected.setCost(50000.0);
        expected.setDistance(15.5);

        var request = RequestForPersonal.builder()
                .id(requestId)
                .humanReadableId("REQ-PER-001")
                .transportType(TransportTypeEnum.PERSONAL)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .passenger(passenger)
                .organizationId(organizationId)
                .expected(expected)
                .employeeDriverId(employeeDriverId)
                .additionalSum(additionalSum)
                .build();

        var changeDate = LocalDateTime.of(2025, 8, 1, 12, 0);
        var result = mapper.requestToRequestPayoutMessage(request, changeDate);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(requestId);
        assertThat(result.costCenter()).isEqualTo("COST_CENTER_456");
        assertThat(result.resource()).isEqualTo("29015");
        assertThat(result.organizationId()).isEqualTo(organizationId);
        assertThat(result.employeeId()).isEqualTo(passengerId);
        assertThat(result.changeDate()).isEqualTo(changeDate.atOffset(java.time.ZoneOffset.UTC));
        assertThat(result.transportType()).isEqualTo("PERSONAL");
        assertThat(result.employeeDriverId()).isEqualTo(employeeDriverId);
        assertThat(result.additionalSum()).isEqualTo(additionalSum);
        assertThat(result.expectedDistance()).isEqualTo(BigDecimal.valueOf(15.5));
    }


    @Test
    @DisplayName("toReceiptScannerMessage: корректно обрабатывает RequestForPublic с документами")
    void toReceiptScannerMessage_withDocuments() {
        var requestId = UUID.randomUUID();
        var docId = UUID.randomUUID();
        var folderId = UUID.randomUUID();

        var fileName = "ticket.pdf";
        int cost = 5000;
        var desiredDate = LocalDateTime.of(2025, 3, 20, 14, 0);

        var doc = CompensationDocument.builder()
                .id(docId)
                .folder(folderId)
                .fileName(fileName)
                .fileFormat(UploadFileFormats.PDF)
                .fileSize(1024)
                .creationTime(LocalDateTime.now())
                .build();

        var tc = TransportCompensation.builder()
                .attachedDocumentId(docId)
                .ticketsCost(cost)
                .ticketsCount(1)
                .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                .build();

        var expected = new ExpectedData();
        expected.setCost(5000.0);

        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .desiredDate(desiredDate)
                .expected(expected)
                .compensationDocuments(List.of(doc))
                .transportCompensation(List.of(tc))
                .build();

        var result = mapper.toReceiptScannerMessage(request);

        assertThat(result).isNotNull();
        assertThat(result.requestId()).isEqualTo(requestId);
        assertThat(result.transportType()).isEqualTo(TransportTypeEnum.PUBLIC.getName());
        assertThat(result.desiredDate()).isEqualTo(desiredDate);
        assertThat(result.cost()).isEqualTo(cost);
        assertThat(result.files()).hasSize(1);
        assertThat(result.files().getFirst().folderId()).isEqualTo(folderId);
        assertThat(result.files().getFirst().fileName()).isEqualTo(fileName);
        assertThat(result.files().getFirst().ticketCost()).isEqualTo(cost);
    }

    @Test
    @DisplayName("toReceiptScannerMessage: игнорирует компенсации с null attachedDocumentId")
    void toReceiptScannerMessage_ignoresNullAttachedDocumentId() {
        var requestId = UUID.randomUUID();
        var folderId = UUID.randomUUID();

        var doc1 = CompensationDocument.builder()
                .id(UUID.randomUUID())
                .folder(folderId)
                .fileName("valid.pdf")
                .fileFormat(UploadFileFormats.PDF)
                .fileSize(1024)
                .creationTime(LocalDateTime.now())
                .build();

        var validTc = TransportCompensation.builder()
                .attachedDocumentId(doc1.getId())
                .ticketsCost(3000)
                .ticketsCount(1)
                .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                .build();

        var nullTc = TransportCompensation.builder()
                .attachedDocumentId(null)
                .ticketsCost(9999)
                .ticketsCount(1)
                .compensationType(PublicCompensationType.PAID_SERVICES_COMPENSATION)
                .build();

        var expected = new ExpectedData();
        expected.setCost(3000.0);

        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 3, 20, 14, 0))
                .expected(expected)
                .compensationDocuments(List.of(doc1))
                .transportCompensation(List.of(validTc, nullTc))
                .build();

        var result = mapper.toReceiptScannerMessage(request);

        assertThat(result).isNotNull();
        assertThat(result.cost()).isEqualTo(3000);
        assertThat(result.files()).hasSize(1);
    }


    @Test
    @DisplayName("getTrustIdx: возвращает trustIdx из PersonalTariff")
    void getTrustIdx_shouldReturnTrustIdxFromPersonalTariff() {
        var tariff = PersonalTariff.builder()
                .trustIdx(0.5)
                .build();

        var result = mapper.getTrustIdx(tariff);

        assertThat(result).isEqualTo(0.5);
    }

    @Test
    @DisplayName("getTrustIdx: возвращает 0.0 когда trustIdx равен нулю")
    void getTrustIdx_shouldReturnZeroWhenTrustIdxIsZero() {
        var tariff = PersonalTariff.builder()
                .trustIdx(0.0)
                .build();

        var result = mapper.getTrustIdx(tariff);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getTrustIdx: возвращает null при null тарифе")
    void getTrustIdx_shouldReturnNullWhenNull() {
        var result = mapper.getTrustIdx(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getTrustIdx: возвращает null при tariff другого типа")
    void getTrustIdx_shouldReturnNullWhenTariffIsNotPersonal() {
        var result = mapper.getTrustIdx(new BaseTariff());

        assertThat(result).isNull();
    }

    private RequestForPublic createRequestForPublicWithCompensations(List<Integer> costs) {
        var requestId = UUID.randomUUID();
        var compensations = costs.stream()
                .map(cost -> TransportCompensation.builder()
                        .ticketsCost(cost)
                        .ticketsCount(1)
                        .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                        .build())
                .toList();

        var expected = new ExpectedData();
        expected.setCost(1000.0);

        return RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(expected)
                .transportCompensation(compensations)
                .compensationDocuments(List.of())
                .build();
    }

    private RequestForPublic createRequestForPublicWithCompensationsCount(List<Integer> counts) {
        var requestId = UUID.randomUUID();
        var compensations = counts.stream()
                .map(count -> TransportCompensation.builder()
                        .ticketsCost(1000)
                        .ticketsCount(count)
                        .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                        .build())
                .toList();

        var expected = new ExpectedData();
        expected.setCost(1000.0);

        return RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(expected)
                .transportCompensation(compensations)
                .compensationDocuments(List.of())
                .build();
    }

    private RequestForPublic createRequestForPublicWithCompensationTypes(List<PublicCompensationType> types) {
        var requestId = UUID.randomUUID();
        var compensations = types.stream()
                .map(type -> TransportCompensation.builder()
                        .ticketsCost(1000)
                        .ticketsCount(1)
                        .compensationType(type)
                        .build())
                .toList();

        var expected = new ExpectedData();
        expected.setCost(1000.0);

        return RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(expected)
                .transportCompensation(compensations)
                .compensationDocuments(List.of())
                .build();
    }

    private Employee createEmployee(UUID id, UUID departmentId, String costCenter) {
        var employee = new Employee();
        employee.setId(id);
        employee.setCostCenter(costCenter);
        employee.setDepartment(new Department());
        employee.getDepartment().setId(departmentId);
        return employee;
    }

    private Department createDepartment(UUID id, String name, Organization organization) {
        var dept = new Department();
        dept.setId(id);
        dept.setDepartmentName(name);
        dept.setOrganization(organization);
        return dept;
    }
}
