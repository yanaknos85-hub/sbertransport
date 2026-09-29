package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.magenta.CoopRequest;
import ru.sberbank.ditsib.transport.request.dto.CheckinDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapper;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.UpdateRequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.publicTransport.impl.RequestForPublicServiceImpl;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;
import ru.sberbank.ditsib.transport.request.validate.request.impl.*;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Установка причины отсутствия")
class RequestServiceImplTest {

    private final RequestRepository requestRepository = mock(RequestRepository.class);
    private final RequestForPersonalHistoryRepository personalHistoryRepository = mock(RequestForPersonalHistoryRepository.class);
    private final RequestForPublicHistoryRepository publicHistoryRepository = mock(RequestForPublicHistoryRepository.class);
    private final RequestForTaxiHistoryRepository taxiHistoryRepository = mock(RequestForTaxiHistoryRepository.class);
    private final SrmService srmService = mock(SrmService.class);
    private final EntityDTOMapper mapper = mock(EntityDTOMapper.class);
    private final RequestSender<Request> requestSender = mock(RequestSender.class);
    private final UpdateRequestSender updateRequestSender = mock(UpdateRequestSender.class);
    private final RequestRatingSender requestRatingSender = mock(RequestRatingSender.class);
    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final List<AbstractTransportTypeService<? extends Request>> transportTypeService = List.of(
            mock(RequestForTaxiServiceImpl.class),
            mock(RequestForPersonalServiceImpl.class),
            mock(RequestForCarsharingServiceImpl.class),
            mock(RequestForPublicServiceImpl.class),
            mock(RequestForGroupTransferServiceImpl.class)
    );
    private final UpdateRequestRepository updateRequestRepository = mock(UpdateRequestRepository.class);
    private final GeoDataProcessingService geoDataProcessingService = mock(GeoDataProcessingService.class);
    private final RequestForTaxiRepository requestForTaxiRepository = mock(RequestForTaxiRepository.class);
    private final RequestForPersonalRepository requestForPersonalRepository = mock(RequestForPersonalRepository.class);
    private final EmployeeMapper employeeMapper = mock(EmployeeMapper.class);
    private final RequestMapper requestMapper = mock(RequestMapper.class);
    private final RequestForCarsharingHistoryRepository requestForCarsharingHistoryRepository = mock(RequestForCarsharingHistoryRepository.class);
    private final PositionRepository positionRepository = mock(PositionRepository.class);
    private final RequestForPublicRepository requestForPublicRepository = mock(RequestForPublicRepository.class);
    private final RequestForGroupTransferHistoryRepository requestForGroupTransferHistoryRepository =
            mock(RequestForGroupTransferHistoryRepository.class);
    private final TariffGrpcClient tariffGrpcClient = mock(TariffGrpcClient.class);
    private final PlatformService platformService = mock(PlatformService.class);
    private final List<NewRequestValidator> validators = List.of(
            new TransportTypeValidator(),
            new SumsValidator(),
            new OtherRequestsValidator(requestRepository, Set.of(TAXI), Set.of()),
            new MobilePhoneValidator(employeeService),
            new CarsharingPassengerValidator()
    );
    private final NewRequestValidation validation = new NewRequestValidationImpl(validators);

    private final RequestService cut = new RequestServiceImpl(
            Map.of(TAXI, requestSender, TransportTypeEnum.PERSONAL, requestSender, TransportTypeEnum.PUBLIC, requestSender, TransportTypeEnum.CARSHARING, requestSender, TransportTypeEnum.GROUP_TRANSFER, requestSender),
            updateRequestSender,
            requestRatingSender,
            srmService,
            employeeService,
            transportTypeService,
            geoDataProcessingService,
            mapper,
            employeeMapper,
            requestMapper,
            tariffGrpcClient,
            platformService,
            validation,
            personalHistoryRepository,
            publicHistoryRepository,
            taxiHistoryRepository,
            requestForTaxiRepository,
            requestForPersonalRepository,
            requestForGroupTransferHistoryRepository,
            requestForCarsharingHistoryRepository,
            requestForPublicRepository,
            positionRepository,
            updateRequestRepository,
            requestRepository,
            mock(FraudRepository.class)
    );

    @BeforeEach
    void setup() {
        when(transportTypeService.get(0).getTransportType()).thenReturn(TAXI);
        when(transportTypeService.get(1).getTransportType()).thenReturn(PERSONAL);
        when(transportTypeService.get(2).getTransportType()).thenReturn(CARSHARING);
        when(transportTypeService.get(3).getTransportType()).thenReturn(PUBLIC);
        when(transportTypeService.get(4).getTransportType()).thenReturn(GROUP_TRANSFER);
    }

    @DisplayName("Автоматический чекин. Без индекса")
    @Test
    void checkInAutomatic__without_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).build();

        Boolean result = cut.checkInAutomatic(request, checkinDTO);

        assertTrue(result, "При совпадении координат, отсутствии пометки об автоматическом чекине и отсутствии пометки о только ручном чекине " +
                "автоматический чекин должен завершаться успехом");
    }

    @DisplayName("Автоматический чекин. Индекс и координаты совпадают с точкой маршрута")
    @Test
    void checkInAutomatic_with_the_correct_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(2).build();

        Boolean result = cut.checkInAutomatic(request, checkinDTO);

        assertTrue(result, "При совпадении координат с индексом, отсутствии пометки об автоматическом чекине и отсутствии отметки о только ручном " +
                "чекине автоматический чекин должен завершаться успехом");
    }

    @DisplayName("Автоматический чекин. Индекс и координаты не совпадают с точкой маршрута")
    @Test
    void checkInAutomatic_with_the_wrong_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(3).build();

        Boolean result = cut.checkInAutomatic(request, checkinDTO);

        assertFalse(result, "При совпадении координат, отсутствии пометки об автоматическом чекине, отсутствии отметки о только ручном " +
                "чекине и несовпадении индекса автоматический чекин должен завершаться провалом");
    }

    @DisplayName("Установка причины отсутствия. Без индекса")
    @Test
    void setAbsenceReason_without_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).build();

        Boolean result = cut.setAbsenceReason(request, checkinDTO);

        assertTrue(result, "При совпадении координат и отсутствии пометки об автоматическом чекине установка причины отсутствия должна завершаться " +
                "успехом");
    }

    @DisplayName("Установка причины отсутствия. Индекс и координаты совпадают с точкой маршрута")
    @Test
    void setAbsenceReason_with_the_correct_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(2).build();

        Boolean result = cut.setAbsenceReason(request, checkinDTO);

        assertTrue(result, "При совпадении координат с индексом и отсутствии пометки об автоматическом чекине установка причины отсутствия должна " +
                "завершаться успехом");
    }

    @DisplayName("Установка причины отсутствия. Индекс и координаты не совпадают с точкой маршрута")
    @Test
    void setAbsenceReason_with_the_wrong_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(3).build();

        Boolean result = cut.setAbsenceReason(request, checkinDTO);

        assertFalse(result, "При совпадении координат, не совпадении с индексом и отсутствии пометки об автоматическом чекине установка причины " +
                "отсутствия должна завершаться провалом");
    }

    @DisplayName("Удаление точки. Без индекса")
    @Test
    void deleteWaypoint_without_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .transportType(TransportTypeEnum.PERSONAL)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).build();

        Boolean result = cut.deleteWaypoint(request, checkinDTO);

        assertTrue(result, "При совпадении координат и активности удаление точки маршрута должно завершаться успехом");
    }

    @DisplayName("Удаление точки. Индекс и координаты совпадают с точкой маршрута")
    @Test
    void deleteWaypoint_with_the_correct_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .transportType(TransportTypeEnum.PERSONAL)
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(2).build();

        Boolean result = cut.deleteWaypoint(request, checkinDTO);

        assertTrue(result, "При совпадении координат с индексом и активности удаление точки маршрута должно завершаться успехом");
    }

    @DisplayName("Удаление точки. Индекс и координаты не совпадают с точкой маршрута")
    @Test
    void deleteWaypoint_with_the_wrong_index() {
        Address addressA = Address.builder().latitude(55.88859000423021).longitude(37.5963876602002).build();
        Address addressB = Address.builder().latitude(55.78930364731193).longitude(37.6284788059155).build();
        Address addressC = Address.builder().latitude(55.74015998570335).longitude(37.51985739636868).build();

        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(Waypoint.builder().orderingIndex(0).address(addressA).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(1).address(addressB).checkinAutomatic(true).build());
        waypoints.add(Waypoint.builder().orderingIndex(2).address(addressA).build());
        waypoints.add(Waypoint.builder().orderingIndex(3).address(addressC).build());
        waypoints.add(Waypoint.builder().orderingIndex(4).address(addressA).build());

        Request request = RequestForPersonal.builder()
                .waypoints(waypoints)
                .build();
        CheckinDTO checkinDTO = CheckinDTO.builder().latitude(addressA.getLatitude()).longitude(addressA.getLongitude()).orderingIndex(3).build();

        assertThrows(UpdateRequestException.class, () -> cut.deleteWaypoint(request, checkinDTO));
    }

    @Test
    @DisplayName("Предлагаются к присоединению только СП, заявка владельцев которых согласована")
    void getSuitableSharedRide() {

        Request request = RequestForPersonal.builder().build();
        Employee loggedEmployee = Employee.builder().build();
        String token = "";

        List<SrmSharedRideDTO> srmSharedRideDTOS = new ArrayList<>();


        RequestForPersonal requestForPersonalOwnerApproved =
                RequestForPersonal.builder()
                        .id(UUID.randomUUID())
                        .sharedRideOwner(true)
                        .status(TripRequestStatus.PERSONAL_APPROVED)
                        .joinedPassengerIds(Set.of(UUID.randomUUID(), UUID.randomUUID()))
                        .build();

        RequestForPersonal requestForPersonalOwnerApprovedButNotExist =
                RequestForPersonal.builder()
                        .id(UUID.randomUUID())
                        .sharedRideOwner(true)
                        .status(TripRequestStatus.PERSONAL_APPROVED)
                        .build();

        RequestForPersonal requestForPersonalOwnerInProgress =
                RequestForPersonal.builder()
                        .id(UUID.randomUUID())
                        .sharedRideOwner(true)
                        .status(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS)
                        .build();

        RequestForPersonal requestForPersonalPassenger =
                RequestForPersonal.builder()
                        .id(UUID.randomUUID())
                        .sharedRideOwner(false)
                        .status(TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL)
                        .joinedPassengerIds(Set.of(UUID.randomUUID(), UUID.randomUUID()))
                        .build();

        List<SrmRequestKpiDTO> srmRequestKpiDTOListVisible = new ArrayList<>();
        srmRequestKpiDTOListVisible.add(SrmRequestKpiDTO.builder()
                .id(requestForPersonalOwnerApproved.getId())
                .oldId(11)
                .requestDistance(1.0)
                .requestPrice(100000L)
                .requestTime(1L)
                .costSharePart(50.0)
                .orderingIndex(1)
                .requiredPassengers(1)
                .savingsCash(50000L)
                .savingsProcents(50.0)
                .build());

        srmRequestKpiDTOListVisible.add(SrmRequestKpiDTO.builder()
                .id(requestForPersonalOwnerApprovedButNotExist.getId())
                .oldId(11)
                .requestDistance(1.0)
                .requestPrice(100000L)
                .requestTime(1L)
                .costSharePart(50.0)
                .orderingIndex(1)
                .requiredPassengers(1)
                .savingsCash(50000L)
                .savingsProcents(50.0)
                .build());

        srmRequestKpiDTOListVisible.add(SrmRequestKpiDTO.builder()
                .id(requestForPersonalPassenger.getId())
                .oldId(12)
                .requestDistance(1.0)
                .requestPrice(100000L)
                .requestTime(1L)
                .costSharePart(50.0)
                .orderingIndex(2)
                .requiredPassengers(1)
                .savingsCash(50000L)
                .savingsProcents(50.0)
                .build());

        srmSharedRideDTOS.add(SrmSharedRideDTO.builder()
                .id(UUID.randomUUID())
                .transportType(TransportTypeEnum.PERSONAL)
                .requestKpiList(srmRequestKpiDTOListVisible)
                .rideCost(100L)
                .rideDistance(100.0)
                .rideTime(600L)
                .build());

        List<SrmRequestKpiDTO> srmRequestKpiDTOListInvisible = new ArrayList<>();
        srmRequestKpiDTOListInvisible.add(SrmRequestKpiDTO.builder()
                .id(requestForPersonalOwnerInProgress.getId())
                .oldId(11)
                .requestDistance(1.0)
                .requestPrice(100000L)
                .requestTime(1L)
                .costSharePart(50.0)
                .orderingIndex(1)
                .requiredPassengers(1)
                .savingsCash(50000L)
                .savingsProcents(50.0)
                .build());

        srmRequestKpiDTOListInvisible.add(SrmRequestKpiDTO.builder()
                .id(requestForPersonalPassenger.getId())
                .oldId(12)
                .requestDistance(1.0)
                .requestPrice(100000L)
                .requestTime(1L)
                .costSharePart(50.0)
                .orderingIndex(2)
                .requiredPassengers(1)
                .savingsCash(50000L)
                .savingsProcents(50.0)
                .build());

        srmSharedRideDTOS.add(SrmSharedRideDTO.builder()
                .id(UUID.randomUUID())
                .transportType(TransportTypeEnum.PERSONAL)
                .requestKpiList(srmRequestKpiDTOListInvisible)
                .rideCost(100L)
                .rideDistance(100.0)
                .rideTime(600L)
                .build());

        when(requestRepository.findById(eq(requestForPersonalOwnerApproved.getId()))).thenReturn(Optional.of(requestForPersonalOwnerApproved));
        when(requestRepository.findById(eq(requestForPersonalOwnerApprovedButNotExist.getId()))).thenReturn(Optional.empty());
        when(requestRepository.findById(eq(requestForPersonalOwnerInProgress.getId()))).thenReturn(Optional.of(requestForPersonalOwnerInProgress));
        when(requestRepository.findById(eq(requestForPersonalPassenger.getId()))).thenReturn(Optional.of(requestForPersonalPassenger));
        when(srmService.getSuitableSharedRides(any(), anyString())).thenReturn(srmSharedRideDTOS);
        when(positionRepository.findAllByIdIn(any())).thenReturn(List.of(Position.builder().build()));


        var result = cut.getSuitableSharedRide(request, loggedEmployee, token);

        final var joinedCapture = ArgumentCaptor.forClass(Set.class);
        Mockito.verify(employeeService).getByEmployeeIds(joinedCapture.capture());
        assertEquals(4, joinedCapture.getValue().size());

        assertEquals(1, result.size(), "В качестве СП для присоединения должна попасть только одна СП, статус у основной заявки " +
                "(заявка с sharedRideOwner = true) которой равен PERSONAL_APPROVED");
    }

    @Test
    @DisplayName("Установка признака нарушения КС выплаты компенсации у заявки на ЛТ")
    void setPaymentDoneDeadlineStatePersonal() {

        var request = RequestForPersonal.builder().id(UUID.randomUUID()).build();

        when(requestForPersonalRepository.getReferenceById(any())).thenReturn(request);

        cut.setPaymentDoneDeadlineState(request, DeadlineState.RED);

        assertTrue(request.isSlaExpired(), "При нарушении КС оплаты флаг isSlaExpired должен быть равен true, т.к. это используется для расчета SLA для аналитической " +
                "отчетности");
        assertEquals(DeadlineState.RED, request.getPaymentDoneDeadlineState(),
                "При нарушении КС оплаты состояние проверки КС оплаты должно быть переведено в RED");
    }

    @Test
    @DisplayName("Установка признака нарушения КС выплаты компенсации у заявки на ОТ")
    void setPaymentDoneDeadlineStatePublic() {

        var request = RequestForPublic.builder().id(UUID.randomUUID()).build();

        when(requestForPublicRepository.getReferenceById(any())).thenReturn(request);

        cut.setPaymentDoneDeadlineState(request, DeadlineState.RED);

        assertTrue(request.isSlaExpired(), "При нарушении КС оплаты флаг isSlaExpired должен быть равен true, т.к. это используется для расчета SLA для аналитической " +
                "отчетности");
        assertEquals(DeadlineState.RED, request.getPaymentDoneDeadlineState(),
                "При нарушении КС оплаты состояние проверки КС оплаты должно быть переведено в RED");
    }

    @Test
    @DisplayName("Получение истории по заявке без указания типа транспорта")
    void getHistoryShouldReturnAllTypeHistoryElementWithoutTransportType() {
        var requestHistoryElementForPersonal = RequestHistoryElementForPersonal.builder().build();
        var requestHistoryElementForPublic = RequestHistoryElementForPublic.builder().build();
        var requestHistoryElementForTaxi = RequestHistoryElementForTaxi.builder().build();
        var requestHistoryElementForCarsharing = RequestHistoryElementForCarsharing.builder().build();
        var requestHistoryElementForGroupTransfer = RequestHistoryElementForGroupTransfer.builder().build();

        var requestId = UUID.randomUUID();
        when(personalHistoryRepository.getAllByRequestForPersonalIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForPersonal));
        when(publicHistoryRepository.getAllByRequestForPublicIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForPublic));
        when(taxiHistoryRepository.getAllByRequestForTaxiIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForTaxi));
        when(requestForCarsharingHistoryRepository.getAllByRequestForCarsharingIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForCarsharing));
        when(requestForGroupTransferHistoryRepository.findAllByRequestForGroupTransferIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForGroupTransfer));


        var result = cut.getHistory(requestId, null);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Получение истории по заявке с указанием типа транспорта - ТРАНСФЕР")
    void getHistoryShouldReturnAllTypeHistoryElementWithTransportTypeTransfer() {
        var requestHistoryElementForPersonal = RequestHistoryElementForPersonal.builder().build();
        var requestHistoryElementForPublic = RequestHistoryElementForPublic.builder().build();
        var requestHistoryElementForTaxi = RequestHistoryElementForTaxi.builder().build();
        var requestHistoryElementForCarsharing = RequestHistoryElementForCarsharing.builder().build();
        var requestHistoryElementForGroupTransfer = RequestHistoryElementForGroupTransfer.builder().build();

        var requestId = UUID.randomUUID();
        when(personalHistoryRepository.getAllByRequestForPersonalIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForPersonal));
        when(publicHistoryRepository.getAllByRequestForPublicIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForPublic));
        when(taxiHistoryRepository.getAllByRequestForTaxiIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForTaxi));
        when(requestForCarsharingHistoryRepository.getAllByRequestForCarsharingIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForCarsharing));
        when(requestForGroupTransferHistoryRepository.findAllByRequestForGroupTransferIdOrderByChangeDate(requestId))
                .thenReturn(Collections.singletonList(requestHistoryElementForGroupTransfer));


        var result = cut.getHistory(requestId, TransportTypeEnum.GROUP_TRANSFER);
        assertEquals(1, result.size());
    }

    public static Stream<Arguments> getSource() {
        return Stream.of(
                Arguments.of(TAXI, RequestForTaxi.class, (Function<Request, Employee>) Request::getPassenger, 0),
                Arguments.of(PERSONAL, RequestForPersonal.class, (Function<Request, Employee>) Request::getPassenger, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("getSource")
    @DisplayName("Получение данных заявки")
    void test_get(TransportTypeEnum transportType, Class<? extends CoopRequest> requestClass, Function<Request, Employee> passengerFunc, int serviceIndex) {
        final var requestId = UUID.randomUUID();
        final var request = Instancio.create(requestClass);
        final var coopedRequests = Instancio.createList(requestClass);
        final var repositories = Map.<TransportTypeEnum, CoopedRequestRepository<? extends Request>>of(
                TAXI, requestForTaxiRepository,
                PERSONAL, requestForPersonalRepository
        );

        when(transportTypeService.get(serviceIndex).get(requestId)).thenReturn(Optional.of(ReflectionUtils.cast(request)));
        when(repositories.get(transportType).findByRideId(request.getRideId())).thenReturn(ReflectionUtils.cast(coopedRequests));

        final var actualOpt = cut.get(requestId, transportType);

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();

        assertThat(actual.getPassengersFromCoop()).hasSameSizeAs(coopedRequests.stream().map(Request.class::cast).map(passengerFunc).collect(Collectors.toUnmodifiableSet()));
    }
}