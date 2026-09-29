package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitResponse;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.config.AllPointsMaxWaitTimeProperties;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestHistoryRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationResultDto;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RegionDto;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.DurationLimitExceededException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import ru.sberbank.ditsib.transport.request.service.FraudService;
import ru.sberbank.ditsib.transport.request.service.GeoDataProcessingService;
import ru.sberbank.ditsib.transport.request.service.MagentaAuxilaryService;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.TaxiTariffService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;

import java.time.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.SINGLE_TRIP_DURATION_FRAUD_COMMENT;
import static ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService.TOTAL_WAIT_TIME_FRAUD_COMMENT;

@ExtendWith(MockitoExtension.class)
class RequestForTaxiServiceImplTest {

    public static final int METER_IN_KILOMETER = 1000;
    @InjectMocks
    private RequestForTaxiServiceImpl requestForTaxiService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TripPurposeRepository tripPurposeRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SQGenerator sqGenerator;
    @Mock
    private RequestForTaxiRepository requestForTaxiRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private GeoDataProcessingService geoDataProcessingService;
    @Mock
    private MagentaAuxilaryService magentaAuxilaryService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private RequestHistoryRepository historyRepository;
    @Mock
    private TaxiTariffService taxiTariffService;
    @Mock
    private EntityDTOMapper entityDTOMapper;
    @Mock
    private RegionDataResolver regionDataResolver;
    @Mock
    private CheckinSettingsService checkinSettingsService;
    @Mock
    private FraudService fraudService;
    @Mock
    private EasupGrpcService easupGrpcService;
    @Mock
    private RequestSender<RequestForTaxi> requestSender;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Captor
    private ArgumentCaptor<RequestForTaxi> requestForTaxiArgumentCaptor;
    @Mock
    private Clock clock;
    @Mock
    private DurationRequestCheckGrpcClient grpcDurationChecker;
    @Mock
    private OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient;
    @Mock
    private FraudMonitoringService fraudMonitoringService;
    @Mock
    private RequestChecksGrpcService requestChecksGrpcService;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private Executor fraudAsyncExecutor;
    @Mock
    private AllPointsMaxWaitTimeProperties properties;

    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2024, 9, 17, 16, 10);
    private static final Clock FIXED_CLOCK = Clock.fixed(CURRENT_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final Executor SYNCHRONOUS_EXECUTOR = Runnable::run;

    @Test
    void add() {
        //TODO тест не завершен, ввиду многообразия кейсов
        var sharedRideId = UUID.randomUUID();
        var token = Instancio.create(String.class);
        var executorGroup = Instancio.create(ExecutorGroupDTO.class);
        var request = Instancio.of(NewRequestDTO.class)
                .set(field(NewRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(NewRequestDTO::getTaxiClass), TaxiClass.COMFORT)
                .create();
        var author = Instancio.create(Employee.class);
        var department = Instancio.create(Department.class);
        var organization = Instancio.create(Organization.class);
        var humanReadableId = Instancio.create(String.class);
        var tariff = Instancio.create(TaxiTariff.class);
        var waypoints = Instancio.createList(Waypoint.class);
        var expectedData = Instancio.create(ExpectedData.class);
        var passenger = Instancio.create(Employee.class);
        var savedRequest = Instancio.create(RequestForTaxi.class);
        var limitReservationResultDto = Instancio.create(LimitReservationResultDto.class);
        var requestForTaxiList = Instancio.createList(RequestForTaxi.class);
        var regionDto = Instancio.create(RegionDto.class);
        var checkinSettings = Instancio.create(CheckinSettings.class);
        var tripPurpose = Instancio.create(TripPurpose.class);

        var expected = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getAuthor), author)
                .set(field(RequestForTaxi::getSegmentsJSON), request.getExpected().getSegments())
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getPassenger), passenger)
                .set(field(RequestForTaxi::getPurpose), tripPurpose)
                .set(field(RequestForTaxi::getTariffId), request.getTariffId())
                .set(field(RequestForTaxi::getTariff), tariff)
                .set(field(RequestForTaxi::getOutcomeTariffId), request.getOutcomeTariffId())
                .set(field(RequestForTaxi::getOutcomeTariff), tariff)
                .set(field(RequestForTaxi::getHumanReadableId), humanReadableId)
                .set(field(RequestForTaxi::getTimeZone), request.getTimeZone())
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_AWAITING_APPROVAL)
                .set(field(RequestForTaxi::getApprovalState), ApprovalState.AWAITING_APPROVAL)
                .set(field(RequestForTaxi::getCreationTime), CURRENT_DATE_TIME)
                .set(field(RequestForTaxi::getAutoCancelDeadlineMin), request.getAutoCancelDeadlineMin())
                .set(field(RequestForTaxi::getRequestOptions), request.getRequestOptions())
                .set(field(RequestForTaxi::getContractorId), tariff.getContractorId())
                .set(field(RequestForTaxi::getTriggerTime), tariff.getTriggerTime())
                .set(field(RequestForTaxi::getBusCount), request.getBusCount())
                .set(field(RequestForTaxi::getBusRentDuration), request.getBusRentDuration())
                .set(field(RequestForTaxi::getOrganizationId), organization.getId())
                .set(field(RequestForTaxi::getRequestPrice), request.getRequestPrice())
                .set(field(RequestForTaxi::getEmployeeDeviceTimeZone), request.getEmployeeDeviceTimeZone())
                .set(field(RequestForTaxi::getApprovalDeadlineState), DeadlineState.NONE)
                .set(field(RequestForTaxi::getJoinedPassengerIds), request.getJoinedPassengerIds())
                .set(field(RequestForTaxi::getSource), request.getSource())
                .set(field(RequestForTaxi::getMinTariffTaxi), request.getMinTariffTaxi())
                .set(field(RequestForTaxi::getExecutorGroupId), executorGroup.getId())
                .set(field(RequestForTaxi::getExecutorGroupName), executorGroup.getName())
                .set(field(RequestForTaxi::getCommentForDriver), request.getCommentForDriver())
                .set(field(RequestForTaxi::getPassengerCount), request.getPassengerCount())
                .set(field(RequestForTaxi::getTaxiClass), request.getTaxiClass())
                .set(field(RequestForTaxi::getCommentForPurpose), request.getCommentForPurpose())
                .set(field(RequestForTaxi::isCoopTrip), true)
                .set(field(RequestForTaxi::isActive), true)
                .set(field(RequestForTaxi::getApprovalDate), null)
                .set(field(RequestForTaxi::getApprovedBy), null)
                .set(field(RequestForTaxi::getCostSharePart), null)
                .set(field(RequestForTaxi::getDriver), null)
                .set(field(RequestForTaxi::getDriverArrivedDatetime), null)
                .set(field(RequestForTaxi::getDriverArrivedDeadline), null)
                .set(field(RequestForTaxi::getDriverAssignmentDeadline), null)
                .set(field(RequestForTaxi::getFactDistance), null)
                .set(field(RequestForTaxi::getFactWaitingTime), null)
                .set(field(RequestForTaxi::getFinishedTime), null)
                .set(field(RequestForTaxi::getId), null)
                .set(field(RequestForTaxi::getNumberPassengersJoined), null)
                .set(field(RequestForTaxi::getPassengersFromCoop), null)
                .set(field(RequestForTaxi::getRequestClosedDatetime), null)
                .set(field(RequestForTaxi::getRequestRating), null)
                .set(field(RequestForTaxi::getRideId), null)
                .set(field(RequestForTaxi::getSavingsCash), null)
                .set(field(RequestForTaxi::getSavingsProcents), null)
                .set(field(RequestForTaxi::isSentToContractor), false)
                .set(field(RequestForTaxi::getStatusCode), 0)
                .set(field(RequestForTaxi::getTaxiAwaitingSearchStartDate), null)
                .set(field(RequestForTaxi::getTaxiTrip), null)
                .set(field(RequestForTaxi::isSharedRideOwner), false)
                .set(field(RequestForTaxi::getResolution), null)
                .set(field(RequestForTaxi::getTransportType), TransportTypeEnum.TAXI)
                .ignore(field(RequestForPersonal::getFraudData))
                .create();

        ReflectionTestUtils.setField(requestForTaxiService, "triggerTime", 60);
        ReflectionTestUtils.setField(requestForTaxiService, "minimalDurationHours", 12);

        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(Optional.of(author)).when(employeeService).get(author.getId());
        doReturn(Optional.of(department)).when(departmentService).get(author.getDepartment().getId());
        doReturn(Optional.of(organization)).when(organizationService).get(department.getOrganization().getId());
        doReturn(humanReadableId).when(sqGenerator).getNextId(Prefix.OT, organization.getDigitId());
        doReturn(tariff).when(taxiTariffService).getTariffById(request.getTariffId());
        doReturn(tariff).when(taxiTariffService).getTariffById(request.getOutcomeTariffId());
        doReturn(waypoints).when(entityDTOMapper).waypointDTOListToWaypointList(request.getExpected().getWaypoints());
        doReturn(Optional.of(passenger)).when(employeeService).get(request.getPassenger().id());
        doReturn(expectedData).when(entityDTOMapper).dtoToExpectedData(request.getExpected());
        doNothing().when(geoDataProcessingService).saveAddresses(waypoints, passenger);
        doReturn(savedRequest).when(requestForTaxiRepository).save(requestForTaxiArgumentCaptor.capture());
        doReturn(null).when(magentaAuxilaryService).processCoopRequest(any(), any(), any());
        doReturn(limitReservationResultDto).when(reservationService).makeReservation(any(),
                any(),
                anyDouble(),
                anyLong());
        doNothing().when(historyRepository).flush();
        doNothing().when(addressRepository).flush();
        doReturn(requestForTaxiList).when(requestForTaxiRepository).findByRideId(sharedRideId);
        doReturn(regionDto).when(regionDataResolver).getRegion(any());
        doReturn(checkinSettings).when(checkinSettingsService).getByParams(any(), any(), any());
        doReturn(tripPurpose).when(tripPurposeRepository).getReferenceById(request.getPurpose().getId());

        var actual = (RequestForTaxi) requestForTaxiService.add(sharedRideId, true, request, author, token, executorGroup);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedRequest);
        assertThat(actual.isCoopTrip()).isFalse();
        assertThat(actual.getResolution()).isEqualTo("Заявка преобразована в одиночную по причине возникновения бизнес ошибки на SRM Magenta при создании совместной заявки");
        //проверяем только первое сохранение, их вообще должно быть не более одного
        var saved = requestForTaxiArgumentCaptor.getAllValues().getFirst();
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("approvalDeadline")
                .ignoringFields("deadlineState")
                .ignoringFields("historyItemsForTaxi")
                .ignoringFields("desiredDate")
                .isEqualTo(expected);
    }

    @Test
    void changeState() {
        var request1 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                .set(field(RequestForTaxi::getStatusCode), 0)
                .set(field(RequestForTaxi::getDriverArrivedDatetime), null)
                .set(field(RequestForTaxi::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForTaxi::getTaxiTrip), Instancio.of(SingleTaxiTrip.class)
                        .set(field(SingleTaxiTrip::getStatus), InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT)
                        .create())
                .create();
        var request2 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .set(field(RequestForTaxi::getStatusCode), 0)
                .set(field(RequestForTaxi::getDriverArrivedDatetime), null)
                .set(field(RequestForTaxi::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForTaxi::getTaxiTrip), null)
                .create();
        var request3 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                .set(field(RequestForTaxi::getStatusCode), 0)
                .set(field(RequestForTaxi::getDriverArrivedDatetime), null)
                .set(field(RequestForTaxi::getDriverArrivedDeadline), CURRENT_DATE_TIME.plusDays(1))
                .set(field(RequestForTaxi::getTaxiTrip), Instancio.of(SingleTaxiTrip.class)
                        .set(field(SingleTaxiTrip::getStatus), InboundTaxiTripStatus.DRIVER_ARRIVED)
                        .create())
                .create();
        var requestForTaxi4 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                .create();
        var savedRequest = Instancio.create(RequestForTaxi.class);
        var savedRequestHistoryElement = Instancio.create(RequestHistoryElementForTaxi.class);
        var employee = Instancio.create(Employee.class);
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(savedRequest).when(requestForTaxiRepository).save(requestForTaxiArgumentCaptor.capture());
        doReturn(savedRequestHistoryElement).when(historyRepository).save(any(RequestHistoryElementForTaxi.class));
        doNothing().when(requestSender).send(savedRequest);
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        var actual1 = requestForTaxiService.changeState(request1, TripRequestStatus.TAXI_CANCELLED, employee, null);
        var actual2 = requestForTaxiService.changeState(request2, TripRequestStatus.TAXI_AWAITING_SEARCH, employee, null);
        var actual3 = requestForTaxiService.changeState(request3, TripRequestStatus.TAXI_DRIVER_ARRIVED, employee, null);
        var actual4 = requestForTaxiService.changeState(requestForTaxi4, TripRequestStatus.TAXI_DRIVER_ON_THE_WAY, employee, null);
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
                .isEqualTo(requestForTaxi4);
        assertThat(requestForTaxiArgumentCaptor.getAllValues())
                .hasSize(3)
                .extracting(
                        RequestForTaxi::getTaxiAwaitingSearchStartDate,
                        RequestForTaxi::getDriverArrivedDatetime,
                        RequestForTaxi::getDriverArrivedDeadline,
                        RequestForTaxi::getDeadlineState,
                        RequestForTaxi::getStatus,
                        RequestForTaxi::getStatusCode
                ).containsExactly(
                        tuple(
                                request1.getTaxiAwaitingSearchStartDate(),
                                request1.getDriverArrivedDatetime(),
                                request1.getDriverArrivedDeadline(),
                                request1.getDeadlineState(),
                                TripRequestStatus.TAXI_CANCELLED,
                                TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EMPLOYEE.getCode()
                        ),
                        tuple(
                                CURRENT_DATE_TIME,
                                request2.getDriverArrivedDatetime(),
                                request2.getDriverArrivedDeadline(),
                                request2.getDeadlineState(),
                                TripRequestStatus.TAXI_AWAITING_SEARCH,
                                0
                        ),
                        tuple(
                                request3.getTaxiAwaitingSearchStartDate(),
                                CURRENT_DATE_TIME,
                                request3.getDriverArrivedDeadline(),
                                request3.getDeadlineState(),
                                TripRequestStatus.TAXI_DRIVER_ARRIVED,
                                0
                        )
                );
    }

    @Test
    void checkDurationLimitWhenNullExpectedTime() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForTaxiService.checkDurationLimit(request, Optional.empty());

        verifyNoInteractions(grpcDurationChecker);
    }

    @Test
    void checkDurationLimitWhenExpectedTimeNotNull() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getTimeZone), "Europe/Moscow")
                .set(field(RequestForTaxi::getDesiredDate), CURRENT_DATE_TIME)
                .create();

        doNothing().when(grpcDurationChecker).checkDurationLimit(
                any(UUID.class),
                any(OffsetDateTime.class),
                anyLong(),
                any(String.class)
        );

        requestForTaxiService.checkDurationLimit(request, Optional.empty());

        verify(grpcDurationChecker).checkDurationLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toOffsetDateTime(),
                request.getExpected().getTime().toMillis(),
                "Europe/Moscow"
        );
    }

    @Test
    void checkDurationLimitWhenDurationLimitExceededException_shouldNotPropagateException() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getTimeZone), "Europe/Moscow")
                .set(field(RequestForTaxi::getDesiredDate), CURRENT_DATE_TIME)
                .create();

        doThrow(new DurationLimitExceededException()).when(grpcDurationChecker).checkDurationLimit(
                any(UUID.class),
                any(OffsetDateTime.class),
                anyLong(),
                any(String.class)
        );

        requestForTaxiService.checkDurationLimit(request, Optional.empty());

        verify(grpcDurationChecker).checkDurationLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toOffsetDateTime(),
                request.getExpected().getTime().toMillis(),
                "Europe/Moscow"
        );
    }

    @Test
    void checkOverrunLimitWhenNullExpectedTime() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForTaxiService.checkOverrunLimit(request, Optional.empty());

        verifyNoInteractions(overrunRequestCheckGrpcClient);
    }

    @Test
    void checkOverrunLimitWhenNullExpectedDistance() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getDistance), null)
                        .create())
                .create();

        requestForTaxiService.checkOverrunLimit(request, Optional.empty());

        verifyNoInteractions(overrunRequestCheckGrpcClient);
    }

    @Test
    void checkOverrunLimitWhenOverrunNotExceeded() {
        var waypoints = Arrays.asList(
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class),
                Instancio.create(Waypoint.class)
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .set(field(ExpectedData::getDistance), 100.0)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getTimeZone), "Europe/Moscow")
                .create();
        doReturn(null).when(overrunRequestCheckGrpcClient).checkOverrunLimit(
                any(UUID.class),
                any(LocalDateTime.class),
                anyInt(),
                any(String.class)
        );

        requestForTaxiService.checkOverrunLimit(request, Optional.empty());

        verify(overrunRequestCheckGrpcClient).checkOverrunLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toLocalDateTime(),
                (int) Math.round(request.getExpected().getDistance() * METER_IN_KILOMETER),
                request.getTimeZone()
        );
        verifyNoInteractions(fraudService);
    }

    @Test
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
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getTimeZone), "Europe/Moscow")
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

        requestForTaxiService.checkOverrunLimit(request, Optional.empty());

        verify(overrunRequestCheckGrpcClient).checkOverrunLimit(
                request.getPassenger().getId(),
                request.getDesiredDate().atZone(ZoneId.of(request.getTimeZone())).toLocalDateTime(),
                (int) Math.round(request.getExpected().getDistance() * METER_IN_KILOMETER),
                request.getTimeZone()
        );

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.OVERRUN, "Превышен лимит 500.5 км");

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.OVERRUN);
        assertThat(fraudData.getComment()).isEqualTo("Превышен лимит 500.5 км");
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void checkSingleTripDurationWhenNullExpectedTime() {
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getTime), null)
                        .create())
                .create();

        requestForTaxiService.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkSingleTripDurationWhenNotExceeded() {
        ReflectionTestUtils.setField(requestForTaxiService, "singleTripDurationLimitMs", 28800000L);
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(5))
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkSingleTripDuration(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkSingleTripDurationWhenExceededAndFraudCreated() {
        ReflectionTestUtils.setField(requestForTaxiService, "singleTripDurationLimitMs", 28800000L);
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getTime), java.time.Duration.ofHours(10))
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkSingleTripDuration(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(request.getId(), FraudType.SINGLE_TRIP_DURATION, SINGLE_TRIP_DURATION_FRAUD_COMMENT);

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.SINGLE_TRIP_DURATION);
        assertThat(fraudData.getComment()).isEqualTo(SINGLE_TRIP_DURATION_FRAUD_COMMENT);
        assertThat(fraudData.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void checkTotalWaitTimeWhenNullWaypoints() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), null)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkTotalWaitTimeWhenEmptyWaypoints() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), Arrays.asList())
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkTotalWaitTimeWhenNotExceeded() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        var waypoints = Arrays.asList(
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(20))
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(30))
                        .create()
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getDistance), 100.0)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkTotalWaitTimeWhenExceededAndDistanceBelowThreshold() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        setWaitTimeLimits();
        var waypoints = Arrays.asList(
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(40))
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(30))
                        .create()
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getDistance), 30.0)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(
                any(UUID.class),
                eq(FraudType.TAXI_WAITING_TIME),
                eq(String.format(TOTAL_WAIT_TIME_FRAUD_COMMENT, 60))
        );

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.TAXI_WAITING_TIME);
        assertThat(fraudData.getRequest().getId()).isNotNull();
    }

    @Test
    void checkTotalWaitTimeWhenExceededAndDistanceAboveThresholdWithSingleCity() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        setWaitTimeLimits();
        var address1 = Instancio.of(Address.class)
                .set(field(Address::getCity), "Москва")
                .create();
        var address2 = Instancio.of(Address.class)
                .set(field(Address::getCity), "Москва")
                .create();
        var waypoints = Arrays.asList(
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(40))
                        .set(field(Waypoint::getAddress), address1)
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(30))
                        .set(field(Waypoint::getAddress), address2)
                        .create()
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getDistance), 100.0)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(
                any(UUID.class),
                eq(FraudType.TAXI_WAITING_TIME),
                eq(String.format(TOTAL_WAIT_TIME_FRAUD_COMMENT, 60))
        );

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.TAXI_WAITING_TIME);
        assertThat(fraudData.getRequest().getId()).isNotNull();
    }

    @Test
    void checkTotalWaitTimeWhenExceededAndDistanceAboveThresholdWithMultipleCities() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        setWaitTimeLimits();
        var address1 = Instancio.of(Address.class)
                .set(field(Address::getCity), "Москва")
                .create();
        var address2 = Instancio.of(Address.class)
                .set(field(Address::getCity), "Санкт-Петербург")
                .create();
        var waypoints = Arrays.asList(
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(40))
                        .set(field(Waypoint::getAddress), address1)
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(30))
                        .set(field(Waypoint::getAddress), address2)
                        .create()
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getDistance), 100.0)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        verifyNoInteractions(fraudService);
        verifyNoInteractions(fraudMonitoringService);
    }

    @Test
    void checkTotalWaitTimeWhenNullWaitTimeFiltered() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        setWaitTimeLimits();
        var waypoints = Arrays.asList(
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(40))
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), null)
                        .create(),
                Instancio.of(Waypoint.class)
                        .set(field(Waypoint::getWaitTime), java.time.Duration.ofMinutes(30))
                        .create()
        );
        var expectedData = Instancio.of(ExpectedData.class)
                .set(field(ExpectedData::getDistance), 30.0)
                .create();
        var requestId = UUID.randomUUID();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getExpected), expectedData)
                .set(field(RequestForTaxi::getId), requestId)
                .create();

        requestForTaxiService.checkTotalWaitTime(request);

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.TAXI_WAITING_TIME);
    }

    @Test
    void createFraudAsyncWhenRequestNotFound() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        var requestId = UUID.randomUUID();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getId), requestId)
                .create();

        requestForTaxiService.createFraud(request, FraudType.TAXI_WAITING_TIME, "test");

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        assertThat(fraudDataCaptor.getValue().getType()).isEqualTo(FraudType.TAXI_WAITING_TIME);
    }

    @Test
    void createFraudAsyncWhenRequestFound() {
        ReflectionTestUtils.setField(requestForTaxiService, "fraudAsyncExecutor", SYNCHRONOUS_EXECUTOR);
        var requestId = UUID.randomUUID();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getId), requestId)
                .create();

        requestForTaxiService.createFraud(
                request,
                FraudType.TAXI_WAITING_TIME,
                "test comment"
        );

        var fraudDataCaptor = ArgumentCaptor.forClass(FraudData.class);
        verify(fraudService).saveAndSend(fraudDataCaptor.capture());
        verify(fraudMonitoringService).send(requestId, FraudType.TAXI_WAITING_TIME, "test comment");

        var fraudData = fraudDataCaptor.getValue();
        assertThat(fraudData.getType()).isEqualTo(FraudType.TAXI_WAITING_TIME);
        assertThat(fraudData.getRequest().getId()).isEqualTo(requestId);
        assertThat(fraudData.getComment()).isEqualTo("test comment");
    }

    private void setWaitTimeLimits() {
        when(properties.getLimitInMinutes()).thenReturn(60);
        when(properties.getDistanceThreshold()).thenReturn(50000);
    }
}