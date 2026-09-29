package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestForGroupTransferServiceImplTest {

    @InjectMocks
    private RequestForGroupTransferServiceImpl requestForGroupTransferService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TripPurposeRepository tripPurposeRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private RequestForGroupTransferRepository requestForGroupTransferRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private GeoDataProcessingService geoDataProcessingService;
    @Mock
    private RequestHistoryRepository historyRepository;
    @Mock
    private RequestSender<RequestForGroupTransfer> requestSender;
    @Mock
    private GroupTransferTripRepository groupTransferTripRepository;
    @Mock
    private GroupTransferTariffRepository groupTransferTariffRepository;
    @Mock
    private ApprovalDeadlineCalculator approvalDeadlineCalculator;
    @Mock
    private PublishTripService publishTripService;
    @Mock
    private CheckinSettingsService checkinSettingsService;
    @Mock
    private RegionDataResolver regionDataResolver;
    @Mock
    private EntityDTOMapper mapper;
    @Mock
    private EasupGrpcService easupGrpcService;
    @Mock
    private FraudService fraudService;
    @Captor
    private ArgumentCaptor<RequestForGroupTransfer> requestForGroupTransferArgumentCaptor;
    @Mock
    private Clock clock;
    @Mock
    private DurationRequestCheckGrpcClient durationRequestCheckGrpcClient;
    @Mock
    private OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient;
    @Mock
    private FraudMonitoringService fraudMonitoringService;

    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2024, 9, 17, 16, 10);
    private static final Clock FIXED_CLOCK = Clock.fixed(CURRENT_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    @DisplayName("Проверка добавления заявки. Организация не найдена")
    void test_add_organizationNotFound() {
        final var sharedRideId = UUID.randomUUID();
        final var coop = false;
        final var data = Instancio.create(NewRequestDTO.class);
        final var employee = Instancio.create(Employee.class);
        final var token = Instancio.create(String.class);
        final var group = Instancio.create(ExecutorGroupDTO.class);

        when(employeeService.get(employee.getId())).thenReturn(Optional.of(employee));

        try {
            requestForGroupTransferService.add(sharedRideId, coop, data, employee, token, group);
            fail("EntityNotFoundException expected");
        } catch (EntityNotFoundException e) {
            assertThat(e.getEntityName()).isEqualTo("Organization");
            assertThat(e.getEntityId()).isEqualTo(Map.of("departmentId", employee.getDepartment().getId()));
        }
    }

    @Test
    @DisplayName("Проверка добавления заявки. Пассажир не найден")
    void test_add_passengerNotFound() {
        final var sharedRideId = UUID.randomUUID();
        final var coop = false;
        final var data = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now().plusDays(10))
                .create();
        final var employee = Instancio.create(Employee.class);
        final var token = Instancio.create(String.class);
        final var group = Instancio.create(ExecutorGroupDTO.class);
        final var department = Instancio.create(Department.class);
        final var tariff = Instancio.create(GroupTransferTariff.class);

        when(employeeService.get(employee.getId())).thenReturn(Optional.of(employee));
        when(groupTransferTariffRepository.getReferenceById(data.getOutcomeTariffId())).thenReturn(tariff);
        when(departmentService.get(employee.getDepartment().getId())).thenReturn(Optional.of(department));
        when(organizationService.get(department.getOrganization().getId())).thenReturn(Optional.of(department.getOrganization()));
        when(groupTransferTariffRepository.findById(data.getTariffId())).thenReturn(Optional.of(tariff));

        try {
            requestForGroupTransferService.add(sharedRideId, coop, data, employee, token, group);
            fail("EntityNotFoundException expected");
        } catch (EntityNotFoundException e) {
            assertThat(e.getEntityName()).isEqualTo("Employee");
            assertThat(e.getEntityId()).isEqualTo(data.getPassenger().id());
        }
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
        final var tariff = Instancio.create(GroupTransferTariff.class);
        final var department = Instancio.create(Department.class);
        final var purpose = Instancio.create(TripPurpose.class);
        final var humanReadableId = "1234567890";
        final var expectedData = Instancio.create(ExpectedData.class);

        when(requestForGroupTransferRepository.saveAndFlush(any())).then(inv -> inv.getArgument(0));
        when(departmentService.get(employee.getDepartment().getId())).thenReturn(Optional.of(department));
        when(employeeService.get(data.getPassenger().id())).thenReturn(Optional.of(passenger));
        when(employeeService.get(employee.getId())).thenReturn(Optional.of(employee));
        when(sqGenerator.getNextId(eq(Prefix.OT), any())).thenReturn(humanReadableId);
        when(groupTransferTariffRepository.findById(data.getTariffId())).thenReturn(Optional.of(tariff));
        when(groupTransferTariffRepository.getReferenceById(data.getOutcomeTariffId())).thenReturn(tariff);
        when(organizationService.get(department.getOrganization().getId())).thenReturn(Optional.of(department.getOrganization()));
        doReturn(purpose).when(tripPurposeRepository).getReferenceById(data.getPurpose().getId());
        doReturn(Optional.empty()).when(easupGrpcService).resolveAbsence(any());
        doReturn(expectedData).when(mapper).dtoToExpectedData(any(ExpectedDataDTO.class));

        final var added = requestForGroupTransferService.add(sharedRideId, coop, data, employee, token, group);

        final var now = LocalDateTime.now(ZoneOffset.UTC);
        assertSoftly(it -> {
            it.assertThat(added.getApprovalDate()).isNull();
            it.assertThat(added.getApprovalDeadline().truncatedTo(ChronoUnit.MINUTES)).isEqualTo(now.plusDays(10).plusHours(2).truncatedTo(ChronoUnit.MINUTES));
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
            it.assertThat(added.getExpected().getBonusCost()).isEqualTo(expectedData.getBonusCost());
            it.assertThat(added.getExpected().getCost()).isEqualTo(expectedData.getCost());
            it.assertThat(added.getExpected().getDistance()).isEqualTo(expectedData.getDistance());
            it.assertThat(added.getExpected().getOutcomeCost()).isEqualTo(expectedData.getOutcomeCost());
            it.assertThat(added.getExpected().getTime()).isEqualTo(expectedData.getTime());
            it.assertThat(added.getHumanReadableId()).isEqualTo(humanReadableId);
            it.assertThat(added.getJoinedPassengerIds()).isEmpty();
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
            it.assertThat(added.getStatus()).isEqualTo(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL);
            it.assertThat(added.getStatusCode()).isZero();
        });
    }

    @Test
    void changeState() {
        var request1 = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getStatus), TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY)
                .set(field(RequestForGroupTransfer::getStatusCode), 0)
                .set(field(RequestForGroupTransfer::getDriverArrivedDatetime), null)
                .set(field(RequestForGroupTransfer::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForGroupTransfer::getTrip), Instancio.of(GroupTransferTrip.class)
                        .set(field(GroupTransferTrip::getStatus), InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT)
                        .create())
                .create();
        var request2 = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getStatus), TripRequestStatus.GROUP_TRANSFER_APPROVED)
                .set(field(RequestForGroupTransfer::getStatusCode), 0)
                .set(field(RequestForGroupTransfer::getDriverArrivedDatetime), null)
                .set(field(RequestForGroupTransfer::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForGroupTransfer::getTrip), null)
                .create();
        var request3 = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getStatus), TripRequestStatus.GROUP_TRANSFER_APPROVED)
                .set(field(RequestForGroupTransfer::getStatusCode), 0)
                .set(field(RequestForGroupTransfer::getDriverArrivedDatetime), null)
                .set(field(RequestForGroupTransfer::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForGroupTransfer::getTrip), Instancio.of(GroupTransferTrip.class)
                        .set(field(GroupTransferTrip::getStatus), InboundTaxiTripStatus.DRIVER_ARRIVED)
                        .create())
                .create();
        var request4 = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getStatus), TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY)
                .create();
        var savedRequest = Instancio.create(RequestForGroupTransfer.class);
        var savedRequestHistoryElement = Instancio.create(RequestHistoryElementForGroupTransfer.class);
        var employee = Instancio.create(Employee.class);
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(savedRequest).when(requestForGroupTransferRepository).save(requestForGroupTransferArgumentCaptor.capture());
        doReturn(savedRequestHistoryElement).when(historyRepository).save(any(RequestHistoryElementForGroupTransfer.class));
        doNothing().when(requestSender).send(savedRequest);
        var actual1 = requestForGroupTransferService.changeState(request1, TripRequestStatus.GROUP_TRANSFER_CANCELLED, employee, null);
        var actual2 = requestForGroupTransferService.changeState(request2, TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH, employee, null);
        var actual3 = requestForGroupTransferService.changeState(request3, TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED, employee, null);
        var actual4 = requestForGroupTransferService.changeState(request4, TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY, employee, null);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(savedRequest);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(savedRequest);
        assertThat(actual3)
                .usingRecursiveComparison()
                .isEqualTo(savedRequest);
        assertThat(actual4)
                .usingRecursiveComparison()
                .isEqualTo(request4);
        assertThat(requestForGroupTransferArgumentCaptor.getAllValues())
                .hasSize(3)
                .extracting(
                        RequestForGroupTransfer::getAwaitingSearchStartDate,
                        RequestForGroupTransfer::getDriverArrivedDatetime,
                        RequestForGroupTransfer::getDriverArrivedDeadline,
                        RequestForGroupTransfer::getDeadlineState,
                        RequestForGroupTransfer::getStatus,
                        RequestForGroupTransfer::getStatusCode
                ).containsExactly(
                        tuple(
                                request1.getAwaitingSearchStartDate(),
                                request1.getDriverArrivedDatetime(),
                                request1.getDriverArrivedDeadline(),
                                request1.getDeadlineState(),
                                TripRequestStatus.GROUP_TRANSFER_CANCELLED,
                                TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EMPLOYEE.getCode()
                        ),
                        tuple(
                                CURRENT_DATE_TIME,
                                request2.getDriverArrivedDatetime(),
                                request2.getDriverArrivedDeadline(),
                                request2.getDeadlineState(),
                                TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH,
                                0
                        ),
                        tuple(
                                request3.getAwaitingSearchStartDate(),
                                CURRENT_DATE_TIME,
                                request3.getDriverArrivedDeadline(),
                                request3.getDeadlineState(),
                                TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED,
                                0
                        )
                );
    }
}