package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitResponse;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestHistoryRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapperImpl;
import ru.sberbank.ditsib.transport.request.exceptions.DurationLimitExceededException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapperImpl;
import ru.sberbank.ditsib.transport.request.mappers.FraudMapperImpl;
import ru.sberbank.ditsib.transport.request.mappers.VehicleMapperImpl;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.CarsharingTariffService;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import ru.sberbank.ditsib.transport.request.service.FraudService;
import ru.sberbank.ditsib.transport.request.service.GeoDataProcessingService;
import ru.sberbank.ditsib.transport.request.service.IntegrationsCarsharingService;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.DURATION_FRAUD_COMMENT;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.SINGLE_TRIP_DURATION_FRAUD_COMMENT;

@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка работы сервиса по работе с каршерингом")
class RequestForCarsharingServiceImplTest {

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final DepartmentService departmentService = mock(DepartmentService.class);

    private final SQGenerator sqGenerator = mock(SQGenerator.class);

    private final TripPurposeRepository tripPurposeRepository = mock(TripPurposeRepository.class);

    private final EntityDTOMapper mapper = new EntityDTOMapperImpl(new EmployeeMapperImpl(), new VehicleMapperImpl(), new FraudMapperImpl());

    private final ReservationService reservationService = mock(ReservationService.class);

    private final RequestForCarsharingRepository requestForCarsharingRepository = mock(RequestForCarsharingRepository.class);

    private final CheckinSettingsService checkinSettingsService = mock(CheckinSettingsService.class);

    private final RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);

    private final RequestSender<RequestForCarsharing> requestSender = mock(RequestSender.class);

    private final RegionDataResolver regionDataResolver = mock(RegionDataResolver.class);

    private final AddressRepository addressRepository = mock(AddressRepository.class);

    private final GeoDataProcessingService geoDataProcessingService = mock(GeoDataProcessingService.class);

    private final IntegrationsCarsharingService carsharingService = mock(IntegrationsCarsharingService.class);

    private final CarsharingTripRepository carsharingTripRepository = mock(CarsharingTripRepository.class);

    private final CarsharingTariffService carSharingTariffService = mock(CarsharingTariffService.class);
    private final FraudService fraudService = mock(FraudService.class);
    private final EasupGrpcService easupGrpcService = mock(EasupGrpcService.class);
    private final RequestChecksGrpcService requestChecksGrpcService = mock(RequestChecksGrpcService.class);
    private final DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = mock(DurationRequestCheckGrpcClient.class);

    private final FraudMonitoringService fraudMonitoringService = mock(FraudMonitoringService.class);
    private final OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);

    private final AbstractTransportTypeService<RequestForCarsharing> service = new RequestForCarsharingServiceImpl(
            employeeService,
            departmentService,
            sqGenerator,
            tripPurposeRepository,
            mapper,
            reservationService,
            requestForCarsharingRepository,
            checkinSettingsService,
            historyRepository,
            requestSender,
            regionDataResolver,
            addressRepository,
            geoDataProcessingService,
            carsharingService,
            carsharingTripRepository,
            carSharingTariffService,
            easupGrpcService,
            fraudService,
            requestChecksGrpcService,
            durationRequestCheckGrpcClient,
            overrunRequestCheckGrpcClient,
            fraudMonitoringService
            );

    @Test
    @DisplayName("Проверка добавления заявки. Организация не найдена")
    void test_add_organizationNotFound() {
        final var sharedRideId = UUID.randomUUID();
        final var coop = false;
        final var data = Instancio.create(NewRequestDTO.class);
        final var employee = Instancio.create(Employee.class);
        final var token = Instancio.create(String.class);
        final var group = Instancio.create(ExecutorGroupDTO.class);

        try {
            service.add(sharedRideId, coop, data, employee, token, group);
            fail("EntityNotFoundException expected");
        } catch (EntityNotFoundException e) {
            assertThat(e.getEntityName()).isEqualTo("Organization");
            assertThat(e.getEntityId()).isEqualTo(Map.of("departmentId", employee.getDepartment().getId()));
        }
        verifyNoInteractions(durationRequestCheckGrpcClient);
    }

    @Test
    @DisplayName("Проверка добавления заявки. Пассажир не найден")
    void test_add_passengerNotFound() {
        final var sharedRideId = UUID.randomUUID();
        final var coop = false;
        final var data = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .create();
        final var employee = Instancio.create(Employee.class);
        final var token = Instancio.create(String.class);
        final var group = Instancio.create(ExecutorGroupDTO.class);
        final var department = Instancio.create(Department.class);

        when(departmentService.get(employee.getDepartment().getId())).thenReturn(Optional.of(department));

        try {
            service.add(sharedRideId, coop, data, employee, token, group);
            fail("EntityNotFoundException expected");
        } catch (EntityNotFoundException e) {
            assertThat(e.getEntityName()).isEqualTo("Employee");
            assertThat(e.getEntityId()).isEqualTo(data.getPassenger().id());
        }
        verifyNoInteractions(durationRequestCheckGrpcClient);
    }

    @Test
    @DisplayName("Проверка добавления заявки")
    void test_add() {
        final var sharedRideId = UUID.randomUUID();
        final var coop = false;
        final var data = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now().plusDays(10))
                .create();
        final var employee = Instancio.create(Employee.class);
        final var passenger = Instancio.create(Employee.class);
        final var token = Instancio.create(String.class);
        final var group = Instancio.create(ExecutorGroupDTO.class);
        final var tariff = Instancio.create(CarsharingTariff.class);
        final var department = Instancio.create(Department.class);
        final var purpose = Instancio.create(TripPurpose.class);
        final var humanReadableId = "1234567890";

        when(requestForCarsharingRepository.saveAndFlush(any())).then(inv -> inv.getArgument(0));
        when(departmentService.get(employee.getDepartment().getId())).thenReturn(Optional.of(department));
        when(employeeService.get(data.getPassenger().id())).thenReturn(Optional.of(passenger));
        when(sqGenerator.getNextId(eq(Prefix.OT), any())).thenReturn(humanReadableId);
        when(carSharingTariffService.getTariffById(data.getOutcomeTariffId())).thenReturn(tariff);
        doReturn(purpose).when(tripPurposeRepository).getReferenceById(data.getPurpose().getId());

        final var added = service.add(sharedRideId, coop, data, employee, token, group);

        final var now = LocalDateTime.now(ZoneOffset.UTC);
        assertSoftly(it -> {
            it.assertThat(added.getApprovalDate()).isNull();
            it.assertThat(added.getApprovalDeadlineState()).isEqualTo(DeadlineState.NONE);
            it.assertThat(added.getApprovalState()).isEqualTo(ApprovalState.AWAITING_APPROVAL);
            it.assertThat(added.getApprovedBy()).isNull();
            it.assertThat(added.getAuthor().getId()).isEqualTo(employee.getId());
            it.assertThat(added.getCommentForPurpose()).isEqualTo(data.getCommentForPurpose());
            it.assertThat(added.getCreationTime().truncatedTo(ChronoUnit.MINUTES)).isEqualTo(now.truncatedTo(ChronoUnit.MINUTES));
            it.assertThat(added.getDesiredDate().truncatedTo(ChronoUnit.MINUTES)).isEqualTo(data.getDesiredDate().truncatedTo(ChronoUnit.MINUTES));
            it.assertThat(added.getEmployeeDeviceTimeZone()).isEqualTo(data.getEmployeeDeviceTimeZone());
            it.assertThat(added.getExecutorGroupId()).isEqualTo(group.getId());
            it.assertThat(added.getExecutorGroupName()).isEqualTo(group.getName());
            it.assertThat(added.getExpected().getBonusCost()).isEqualTo(data.getExpected().getBonusCost());
            it.assertThat(added.getExpected().getCost()).isEqualTo(data.getExpected().getCost());
            it.assertThat(added.getExpected().getDistance()).isEqualTo(data.getExpected().getDistance());
            it.assertThat(added.getExpected().getOutcomeCost()).isEqualTo(data.getExpected().getOutcomeCost());
            it.assertThat(added.getExpected().getTime()).isEqualTo(data.getExpected().getTime());
            it.assertThat(added.getHumanReadableId()).isEqualTo(humanReadableId);
            it.assertThat(added.getJoinedPassengerIds()).isEqualTo(data.getJoinedPassengerIds());
            it.assertThat(added.getMinTariffTaxi().getCost()).isEqualTo(data.getMinTariffTaxi().getCost());
            it.assertThat(added.getMinTariffTaxi().getTariffId()).isEqualTo(data.getMinTariffTaxi().getTariffId());
            it.assertThat(added.getOrganizationId()).isEqualTo(department.getOrganization().getId());
            it.assertThat(added.getOutcomeTariff().getDepartmentId()).isEqualTo(tariff.getDepartmentId());
            it.assertThat(added.getOutcomeTariff().getHumanReadableId()).isEqualTo(tariff.getHumanReadableId());
            it.assertThat(added.getOutcomeTariff().getId()).isEqualTo(tariff.getId());
            it.assertThat(added.getOutcomeTariff().getOrganizationId()).isEqualTo(tariff.getOrganizationId());
            it.assertThat(added.getOutcomeTariff().getRegionId()).isEqualTo(tariff.getRegionId());
            it.assertThat(added.getOutcomeTariff().getServiceType()).isEqualTo(tariff.getServiceType());
            it.assertThat(added.getOutcomeTariff().getTransportType()).isEqualTo(tariff.getTransportType());
            it.assertThat(added.getOutcomeTariffId()).isEqualTo(data.getOutcomeTariffId());
            it.assertThat(added.getPassenger().getId()).isEqualTo(passenger.getId());
            it.assertThat(added.getPassengersFromCoop()).isNull();
            it.assertThat(added.getPurpose()).isEqualTo(purpose);
            it.assertThat(added.getRequestOptions()).isEqualTo(data.getRequestOptions());
            it.assertThat(added.getSegmentsJSON()).isEqualTo(data.getExpected().getSegments());
            it.assertThat(added.getSource()).isEqualTo(data.getSource());
            it.assertThat(added.getStatus()).isEqualTo(TripRequestStatus.CARSHARING_AWAITING_APPROVAL);
            it.assertThat(added.getStatusCode()).isZero();
        });
        verify(durationRequestCheckGrpcClient).checkDurationLimit(
                eq(added.getPassenger().getId()),
                any(),
                eq(added.getExpected().getTime().toMillis()),
                any()
        );
    }

    @Test
    @DisplayName("Проверка превышения лимита длительности поездки")
    void checkDurationLimitWhenExceeded() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getWaypoints), waypoints)
                .set(field(RequestForCarsharing::getExpected), expectedData)
                .set(field(RequestForCarsharing::getTimeZone), ZoneOffset.UTC.getId())
                .create();

        doThrow(new DurationLimitExceededException()).when(durationRequestCheckGrpcClient).checkDurationLimit(
                any(UUID.class),
                any(),
                anyLong(),
                any(String.class)
        );

        service.checkDurationLimit(request, Optional.empty());

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.DURATION, DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.DURATION);
        assertThat(fraudData.getComment()).isEqualTo(DURATION_FRAUD_COMMENT);
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    @DisplayName("Проверка превышения лимита суммарного километража")
    void checkOverrunLimitWhenOverrunExceededAndFraudCreated() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .set(field(ExpectedData::getDistance), 500.0)
                .create();
        var request = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getWaypoints), waypoints)
                .set(field(RequestForCarsharing::getExpected), expectedData)
                .set(field(RequestForCarsharing::getTimeZone), ZoneOffset.UTC.getId())
                .create();

        var response = CheckOverrunLimitResponse.newBuilder()
                .setComment("Превышен лимит")
                .setTotalDistance(500500)
                .build();

        doReturn(response).when(overrunRequestCheckGrpcClient).checkOverrunLimit(
                any(UUID.class),
                any(LocalDateTime.class),
                anyInt(),
                any(String.class)
        );

        service.checkOverrunLimit(request, Optional.empty());

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.OVERRUN, "Превышен лимит 500.5 км");

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.OVERRUN);
        assertThat(fraudData.getComment()).isEqualTo("Превышен лимит 500.5 км");
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    @DisplayName("Проверка длительности одной заявки: expected time null")
    void checkSingleTripDurationWhenNullExpectedTime() {
        var request = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        service.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    @DisplayName("Проверка длительности одной заявки: не превышена")
    void checkSingleTripDurationWhenNotExceeded() {
        ReflectionTestUtils.setField(service, "singleTripDurationLimitMs", 28800000L);
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getExpected), expectedData)
                .create();

        service.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    @DisplayName("Проверка превышения длительности одной заявки")
    void checkSingleTripDurationWhenExceededAndFraudCreated() {
        ReflectionTestUtils.setField(service, "singleTripDurationLimitMs", 28800000L);
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(10))
                .create();
        var request = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getExpected), expectedData)
                .create();

        service.checkSingleTripDuration(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.SINGLE_TRIP_DURATION, SINGLE_TRIP_DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.SINGLE_TRIP_DURATION);
        assertThat(fraudData.getComment()).isEqualTo(SINGLE_TRIP_DURATION_FRAUD_COMMENT);
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }
}