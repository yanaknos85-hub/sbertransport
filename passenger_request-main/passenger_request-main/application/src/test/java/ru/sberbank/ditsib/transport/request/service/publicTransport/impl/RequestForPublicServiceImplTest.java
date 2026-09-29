package ru.sberbank.ditsib.transport.request.service.publicTransport.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPublicRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestHistoryRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.ChangeStatusDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestDocumentSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.ApprovalsSettingsInjectionService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@ExtendWith(MockitoExtension.class)
class RequestForPublicServiceImplTest {

    @Test
    @DisplayName("Установка статуса PUBLIC_ORDER_PAYMENT_FORMATION при пустой дате начала формирования приказа")
    void changeState_orderPaymentFormationStartDate_is_null() {

        EmployeeService employeeService = null;
        RequestForPublicRepository repository = mock(RequestForPublicRepository.class);
        DepartmentService departmentService = null;
        SQGenerator sqGenerator = null;
        OrganizationService organizationService = null;
        EntityDTOMapper entityDTOMapper = null;
        AddressRepository addressRepository = null;
        GeoDataProcessingService geoDataProcessingService = null;
        ReservationService reservationService = null;
        RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);
        RequestSender requestSender = mock(RequestSender.class);
        RequestDocumentSender documentSender = null;
        ApprovalsSettingsInjectionService approvalsSettingsInjectionService = null;
        CheckinSettingsService checkinSettingsService = null;
        RegionDataResolver regionDataResolver = null;
        TransportCompensationRepository transportCompensationRepository = null;
        PublicTariffService publicTariffService = null;
        ApprovalsSettingsInjectionService injectionService = null;
        EasupGrpcService easupGrpcService = null;
        FraudService fraudService = null;
        RequestPayoutSender requestPayoutSender = mock(RequestPayoutSender.class);
        RequestMapper requestMapper = mock(RequestMapper.class);
        DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = null;
        OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);
        FraudMonitoringService fraudMonitoringService = mock(FraudMonitoringService.class);
        RequestChecksGrpcService requestChecksGrpcService = null;
        TravelCardValidationService travelCardValidationService = null;
        ReceiptScannerService receiptScannerService = null;

        var cut = new RequestForPublicServiceImpl(
                employeeService,
                repository,
                departmentService,
                sqGenerator,
                organizationService,
                entityDTOMapper,
                addressRepository,
                geoDataProcessingService,
                reservationService,
                historyRepository,
                requestSender,
                documentSender,
                approvalsSettingsInjectionService,
                checkinSettingsService,
                regionDataResolver,
                transportCompensationRepository,
                publicTariffService,
                injectionService, easupGrpcService, fraudService,
                requestPayoutSender, requestMapper,
                durationRequestCheckGrpcClient,
                requestChecksGrpcService,
                overrunRequestCheckGrpcClient,
                fraudMonitoringService,
                travelCardValidationService,
                receiptScannerService);

        var desiredDate = LocalDateTime.of(2023, 7, 25, 12, 0);
        RequestForPublic toChange = RequestForPublic.builder()
                .desiredDate(desiredDate)
                .timeZone("GMT+03")
                .build();

        when(repository.save(any())).thenReturn(toChange);

        var historyItem = Instancio.of(RequestHistoryElementForPublic.class).create();
        doReturn(historyItem).when(historyRepository).save(any(RequestHistoryElementForPublic.class));

        toChange = (RequestForPublic) cut.changeState(toChange,
                                                      TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION,
                                                      Employee.builder().build(),
                                                      ChangeStatusDTO.builder().build());
        assertEquals(toChange.getOrderPaymentFormationStartDate(), desiredDate, "Если дата начала формирования приказа не была установлена, то при установке статуса PUBLIC_ORDER_PAYMENT_FORMATION она должна " +
                "быть установлена равной желаемой дате поездки");
    }

    @Test
    @DisplayName("Установка статуса PUBLIC_ORDER_PAYMENT_FORMATION при установленной дате начала формирования приказа")
    void changeState_orderPaymentFormationStartDate_is_not_null() {

        RequestForPublicRepository repository = mock(RequestForPublicRepository.class);
        DepartmentService departmentService = null;
        SQGenerator sqGenerator = null;
        OrganizationService organizationService = null;
        EntityDTOMapper entityDTOMapper = null;
        AddressRepository addressRepository = null;
        GeoDataProcessingService geoDataProcessingService = null;
        ReservationService reservationService = null;
        RequestHistoryRepository historyRepository = mock(RequestHistoryRepository.class);
        RequestSender requestSender = mock(RequestSender.class);
        RequestDocumentSender documentSender = null;
        ApprovalsSettingsInjectionService approvalsSettingsInjectionService = null;
        CheckinSettingsService checkinSettingsService = null;
        RegionDataResolver regionDataResolver = null;
        TransportCompensationRepository transportCompensationRepository = null;
        PublicTariffService publicTariffService = null;
        ApprovalsSettingsInjectionService injectionService = null;
        EasupGrpcService easupGrpcService = null;
        FraudService fraudService = null;
        RequestPayoutSender requestPayoutSender = mock(RequestPayoutSender.class);
        RequestMapper requestMapper = mock(RequestMapper.class);
        DurationRequestCheckGrpcClient durationRequestCheckGrpcClient = null;
        OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient = mock(OverrunRequestCheckGrpcClient.class);
        FraudMonitoringService fraudMonitoringService = mock(FraudMonitoringService.class);
        RequestChecksGrpcService requestChecksGrpcService = null;
        TravelCardValidationService travelCardValidationService = null;
        ReceiptScannerService receiptScannerService = null;

        var cut = new RequestForPublicServiceImpl(
                null,
                repository,
                departmentService,
                sqGenerator,
                organizationService,
                entityDTOMapper,
                addressRepository,
                geoDataProcessingService,
                reservationService,
                historyRepository,
                requestSender,
                documentSender,
                approvalsSettingsInjectionService,
                checkinSettingsService,
                regionDataResolver,
                transportCompensationRepository,
                publicTariffService,
                injectionService, easupGrpcService, fraudService,
                requestPayoutSender, requestMapper,
                durationRequestCheckGrpcClient,
                requestChecksGrpcService,
                overrunRequestCheckGrpcClient,
                fraudMonitoringService,
                travelCardValidationService,
                receiptScannerService);

        var orderPaymentFormationStartDate = LocalDateTime.of(2023, 6, 25, 12, 0);
        var desiredDate = LocalDateTime.of(2023, 7, 25, 12, 0);
        RequestForPublic.builder()
                .desiredDate(desiredDate)
                .build();
        RequestForPublic toChange = RequestForPublic.builder()
                                   .orderPaymentFormationStartDate(orderPaymentFormationStartDate)
                                   .desiredDate(desiredDate)
                                   .timeZone("GMT+03")
                                   .build();
        when(repository.save(any())).thenReturn(toChange);

        var historyItem = Instancio.of(RequestHistoryElementForPublic.class).create();
        doReturn(historyItem).when(historyRepository).save(any(RequestHistoryElementForPublic.class));

        toChange = (RequestForPublic) cut.changeState(toChange,
                                                      TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION,
                                                      Employee.builder().build(),
                                                      ChangeStatusDTO.builder().build());
        verify(requestPayoutSender).send(any());
        assertEquals(orderPaymentFormationStartDate, toChange.getOrderPaymentFormationStartDate(),
                     "Если дата начала формирования приказа уже была установлена, то при установке статуса PUBLIC_ORDER_PAYMENT_FORMATION она " +
                     "не должна меняться");
    }
}