package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.ChangeStatusDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;
import ru.sberbank.ditsib.transport.request.service.validate.TripSplitCheckService;
import ru.sberbank.ditsib.transport.request.service.validate.impl.TripSplitCheckServiceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Изменение статуса пассажирской заявки")
class RequestForPersonalServiceImplChangeStateTest {

    @Test
    @DisplayName("Поездка началась, пассажирская заявка не согласована")
    void changeState() {

        EmployeeService employeeService = mock(EmployeeService.class);
        TripPurposeRepository tripPurposeRepository = mock(TripPurposeRepository.class);
        DepartmentService departmentService = null;
        SQGenerator sqGenerator = mock(SQGenerator.class);
        RequestForPersonalRepository requestForPersonalRepository = mock(RequestForPersonalRepository.class);
        OrganizationService organizationService = mock(OrganizationService.class);
        EntityDTOMapper mapper = mock(EntityDTOMapper.class);
        AddressRepository addressRepository = mock(AddressRepository.class);
        GeoDataProcessingService geoDataProcessingService = mock(GeoDataProcessingService.class);
        MagentaAuxilaryService magentaAuxilaryService = mock(MagentaAuxilaryService.class);
        ReservationService reservationService = mock(ReservationService.class);
        RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);
        RequestSender requestSender = mock(RequestSender.class);
        PersonalCarDataResolver personalCarDataResolver = null;
        DepLimitRepository depLimitRepository = null;
        SrmGrpcClient srmGrpcClient = mock(SrmGrpcClient.class);
        PositionService positionService = null;
        PersonalTariffService personalTariffService = null;
        CheckinSettingsService checkinSettingsService = null;
        RegionDataResolver regionDataResolver = null;
        LocalDateTime localDateTime = LocalDateTime.of(2024, 9, 17, 16, 10);
        Clock clock = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        FraudService fraudService = mock(FraudService.class);
        TripSplitCheckService splitCheckService = mock(TripSplitCheckServiceImpl.class);
        FraudRepository fraudRepository = mock(FraudRepository.class);
        EasupGrpcService easupGrpcService = null;
        RequestChecksGrpcService requestChecksGrpcService = null;
        DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = null;
        OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);
        var fraudMonitoringService = mock(FraudMonitoringService.class);
        var requestPayoutSender = mock(RequestPayoutSender.class);
        var requestMapper = mock(RequestMapper.class);
        var requestForPersonalHistoryRepository = mock(RequestForPersonalHistoryRepository.class);

        var cut = new RequestForPersonalServiceImpl(
                employeeService,
                tripPurposeRepository,
                departmentService,
                sqGenerator,
                requestForPersonalRepository,
                organizationService,
                mapper,
                addressRepository,
                geoDataProcessingService,
                magentaAuxilaryService,
                reservationService,
                historyRepository,
                requestSender,
                checkinSettingsService,
                regionDataResolver,
                personalCarDataResolver,
                depLimitRepository,
                srmGrpcClient,
                positionService,
                personalTariffService,
                clock, fraudService, splitCheckService, easupGrpcService, fraudRepository,
                requestPayoutSender, requestMapper, requestForPersonalHistoryRepository,
                durationRequestCheckGrpcClient, requestChecksGrpcService, overrunRequestCheckGrpcClient, fraudMonitoringService);

        RequestForPersonal toChange = RequestForPersonal.builder()
                .id(UUID.randomUUID())
                .status(TripRequestStatus.PERSONAL_APPROVED)
                .coopTrip(true)
                .rideId(UUID.randomUUID())
                .sharedRideOwner(true)
                .build();

        var requestFromPass = RequestForPersonal.builder()
                .id(UUID.randomUUID())
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL)
                .coopTrip(true)
                .rideId(toChange.getRideId())
                .sharedRideOwner(false)
                .build();

        List<RequestForPersonal> linkedRequests = new ArrayList<>();
        linkedRequests.add(requestFromPass);

        when(requestForPersonalRepository.getReferenceById(toChange.getId())).thenReturn(toChange);
        when(requestForPersonalRepository.getReferenceById(requestFromPass.getId())).thenReturn(requestFromPass);

        when(requestForPersonalRepository.save(any())).thenReturn(toChange);
        when(requestForPersonalRepository.findByRideId(any())).thenReturn(linkedRequests);

        TripRequestStatus newStatus = TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS;
        Employee activeUser = Employee.builder().id(UUID.randomUUID()).build();
        ChangeStatusDTO changeStatusDTO = null;

        cut.changeState(toChange, newStatus, activeUser, changeStatusDTO);

        assertEquals(TripRequestStatus.PERSONAL_CANCELLED, requestFromPass.getStatus(),
                "Не согласованная заявка пассажира при начале поездки должна отменяться");
    }

    @Test
    @DisplayName("Переход из статуса GENAI_CHECK в PERSONAL_ORDER_PAYMENT_FORMATION должен быть разрешён")
    void changeState_fromGenaiCheckToPersonalOrderPaymentFormation() {
        EmployeeService employeeService = mock(EmployeeService.class);
        TripPurposeRepository tripPurposeRepository = mock(TripPurposeRepository.class);
        DepartmentService departmentService = null;
        SQGenerator sqGenerator = mock(SQGenerator.class);
        RequestForPersonalRepository requestForPersonalRepository = mock(RequestForPersonalRepository.class);
        OrganizationService organizationService = mock(OrganizationService.class);
        EntityDTOMapper mapper = mock(EntityDTOMapper.class);
        AddressRepository addressRepository = mock(AddressRepository.class);
        GeoDataProcessingService geoDataProcessingService = mock(GeoDataProcessingService.class);
        MagentaAuxilaryService magentaAuxilaryService = mock(MagentaAuxilaryService.class);
        ReservationService reservationService = mock(ReservationService.class);
        RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);
        RequestSender requestSender = mock(RequestSender.class);
        PersonalCarDataResolver personalCarDataResolver = null;
        DepLimitRepository depLimitRepository = null;
        SrmGrpcClient srmGrpcClient = mock(SrmGrpcClient.class);
        PositionService positionService = null;
        PersonalTariffService personalTariffService = null;
        CheckinSettingsService checkinSettingsService = null;
        RegionDataResolver regionDataResolver = null;
        LocalDateTime localDateTime = LocalDateTime.of(2024, 9, 17, 16, 10);
        Clock clock = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        FraudService fraudService = mock(FraudService.class);
        TripSplitCheckService splitCheckService = mock(TripSplitCheckServiceImpl.class);
        FraudRepository fraudRepository = mock(FraudRepository.class);
        EasupGrpcService easupGrpcService = null;
        RequestChecksGrpcService requestChecksGrpcService = null;
        DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = null;
        OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);
        var fraudMonitoringService = mock(FraudMonitoringService.class);
        var requestPayoutSender = mock(RequestPayoutSender.class);
        var requestMapper = mock(RequestMapper.class);
        var requestForPersonalHistoryRepository = mock(RequestForPersonalHistoryRepository.class);

        var cut = new RequestForPersonalServiceImpl(
                employeeService,
                tripPurposeRepository,
                departmentService,
                sqGenerator,
                requestForPersonalRepository,
                organizationService,
                mapper,
                addressRepository,
                geoDataProcessingService,
                magentaAuxilaryService,
                reservationService,
                historyRepository,
                requestSender,
                checkinSettingsService,
                regionDataResolver,
                personalCarDataResolver,
                depLimitRepository,
                srmGrpcClient,
                positionService,
                personalTariffService,
                clock, fraudService, splitCheckService, easupGrpcService, fraudRepository,
                requestPayoutSender, requestMapper, requestForPersonalHistoryRepository,
                durationRequestCheckGrpcClient, requestChecksGrpcService, overrunRequestCheckGrpcClient, fraudMonitoringService);

        RequestForPersonal request = RequestForPersonal.builder()
                .id(UUID.randomUUID())
                .humanReadableId("OT-0001-00027042")
                .status(TripRequestStatus.GENAI_CHECK)
                .timeZone("+03:00")
                .expected(ExpectedData.builder()
                        .cost(1000D)
                        .build())
                .employeeDriverId(UUID.randomUUID())
                .build();

        when(requestForPersonalRepository.getReferenceById(request.getId())).thenReturn(request);
        when(requestForPersonalRepository.save(any())).thenReturn(request);

        TripRequestStatus newStatus = TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION;
        Employee activeUser = Employee.builder().id(UUID.randomUUID()).build();
        ChangeStatusDTO changeStatusDTO = null;

        assertDoesNotThrow(() -> cut.changeState(request, newStatus, activeUser, changeStatusDTO));

        assertEquals(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION, request.getStatus(),
                "Статус заявки должен измениться на PERSONAL_ORDER_PAYMENT_FORMATION");
    }

    @Test
    @DisplayName("Переход из статуса GENAI_CHECK в PERSONAL_ORDER_PAYMENT_FORMATION через changeStateForRequest должен быть разрешён")
    void changeStateForRequest_fromGenaiCheckToPersonalOrderPaymentFormation_shouldNotThrow() {
        EmployeeService employeeService = mock(EmployeeService.class);
        TripPurposeRepository tripPurposeRepository = mock(TripPurposeRepository.class);
        DepartmentService departmentService = null;
        SQGenerator sqGenerator = mock(SQGenerator.class);
        RequestForPersonalRepository requestForPersonalRepository = mock(RequestForPersonalRepository.class);
        OrganizationService organizationService = mock(OrganizationService.class);
        EntityDTOMapper mapper = mock(EntityDTOMapper.class);
        AddressRepository addressRepository = mock(AddressRepository.class);
        GeoDataProcessingService geoDataProcessingService = mock(GeoDataProcessingService.class);
        MagentaAuxilaryService magentaAuxilaryService = mock(MagentaAuxilaryService.class);
        ReservationService reservationService = mock(ReservationService.class);
        RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);
        RequestSender requestSender = mock(RequestSender.class);
        PersonalCarDataResolver personalCarDataResolver = null;
        DepLimitRepository depLimitRepository = null;
        SrmGrpcClient srmGrpcClient = mock(SrmGrpcClient.class);
        PositionService positionService = null;
        PersonalTariffService personalTariffService = null;
        CheckinSettingsService checkinSettingsService = null;
        RegionDataResolver regionDataResolver = null;
        LocalDateTime localDateTime = LocalDateTime.of(2024, 9, 17, 16, 10);
        Clock clock = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        FraudService fraudService = mock(FraudService.class);
        TripSplitCheckService splitCheckService = mock(TripSplitCheckServiceImpl.class);
        FraudRepository fraudRepository = mock(FraudRepository.class);
        EasupGrpcService easupGrpcService = null;
        RequestChecksGrpcService requestChecksGrpcService = null;
        DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = null;
        OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);
        var fraudMonitoringService = mock(FraudMonitoringService.class);
        var requestPayoutSender = mock(RequestPayoutSender.class);
        var requestMapper = mock(RequestMapper.class);
        var requestForPersonalHistoryRepository = mock(RequestForPersonalHistoryRepository.class);

        var cut = new RequestForPersonalServiceImpl(
                employeeService,
                tripPurposeRepository,
                departmentService,
                sqGenerator,
                requestForPersonalRepository,
                organizationService,
                mapper,
                addressRepository,
                geoDataProcessingService,
                magentaAuxilaryService,
                reservationService,
                historyRepository,
                requestSender,
                checkinSettingsService,
                regionDataResolver,
                personalCarDataResolver,
                depLimitRepository,
                srmGrpcClient,
                positionService,
                personalTariffService,
                clock, fraudService, splitCheckService, easupGrpcService, fraudRepository,
                requestPayoutSender, requestMapper, requestForPersonalHistoryRepository,
                durationRequestCheckGrpcClient, requestChecksGrpcService, overrunRequestCheckGrpcClient, fraudMonitoringService);

        RequestForPersonal request = RequestForPersonal.builder()
                .id(UUID.randomUUID())
                .humanReadableId("OT-0001-00027042")
                .status(TripRequestStatus.GENAI_CHECK)
                .timeZone("+03:00")
                .expected(ExpectedData.builder()
                        .cost(1000D)
                        .build())
                .build();

        when(requestForPersonalRepository.getReferenceById(request.getId())).thenReturn(request);
        when(requestForPersonalRepository.save(any())).thenReturn(request);
        when(requestForPersonalHistoryRepository.getAllByRequestForPersonalIdOrderByChangeDate(any())).thenReturn(List.of());

        TripRequestStatus newStatus = TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION;
        assertDoesNotThrow(() -> {
            cut.changeState(request, newStatus, Employee.builder().id(UUID.randomUUID()).build(), null);
        });

        assertEquals(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION, request.getStatus(),
                "Статус должен измениться на PERSONAL_ORDER_PAYMENT_FORMATION");
        verify(requestPayoutSender).send(any());
    }
}
