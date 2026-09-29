package ru.sberbank.ditsib.transport.request.service.publicTransport.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPublicRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestHistoryRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.*;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.exceptions.ValidationRequestException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestDocumentSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.ApprovalsSettingsInjectionService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.impl.AbstractTransportTypeService;
import ru.sberbank.ditsib.transport.request.service.impl.PersonalPaymentDoneDeadlineCalculatorImpl;
import ru.sberbank.ditsib.transport.request.service.impl.PublicApprovalDeadlineCalculatorImpl;
import ru.sberbank.ditsib.transport.request.service.publicTransport.RequestForPublicService;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class RequestForPublicServiceImpl extends AbstractTransportTypeService<RequestForPublic> implements RequestForPublicService {

    private static final String CREATE_HISTORY_MSG = "Заявка зарегистрирована";
    private static final String UPDATE_HISTORY_MSG = "Заявка отредактирована до согласования";

    @Value("${request.validation.travel-card.one-time-trip.enabled}")
    private boolean isCheckOneTimeTripEnabled;
    @Value("${request.validation.travel-card.overlap.enabled}")
    private boolean isCheckOverlapEnabled;

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final SQGenerator sqGenerator;
    private final OrganizationService organizationService;
    private final AddressRepository addressRepository;
    private final GeoDataProcessingService geoDataProcessingService;
    private final ReservationService reservationService;
    private final RequestHistoryRepository historyRepository;
    private final RequestSender<RequestForPublic> requestSender;
    private final RequestDocumentSender documentSender;
    private final TransportCompensationRepository transportCompensationRepository;
    private final PublicTariffService publicTariffService;
    private final ApprovalDeadlineCalculator approvalDeadlineCalculator;
    private final ApprovalsSettingsInjectionService approvalsSettingsInjectionService;
    private final ApprovalsSettingsInjectionService injectionService;
    private final PaymentDoneDeadlineCalculator paymentDoneDeadlineCalculator;
    private final RequestPayoutSender requestPayoutSender;
    private final RequestMapper requestMapper;
    private final TravelCardValidationService travelCardValidationService;
    private final ReceiptScannerService receiptScannerService;

    public RequestForPublicServiceImpl(
            EmployeeService employeeService,
            RequestForPublicRepository repository,
            DepartmentService departmentService,
            SQGenerator sqGenerator,
            OrganizationService organizationService,
            EntityDTOMapper entityDTOMapper,
            AddressRepository addressRepository,
            GeoDataProcessingService geoDataProcessingService,
            ReservationService reservationService,
            RequestHistoryRepository historyRepository,
            RequestSender<RequestForPublic> requestSender,
            RequestDocumentSender documentSender,
            ApprovalsSettingsInjectionService approvalsSettingsInjectionService,
            CheckinSettingsService checkinSettingsService,
            RegionDataResolver regionDataResolver,
            TransportCompensationRepository transportCompensationRepository,
            PublicTariffService publicTariffService,
            ApprovalsSettingsInjectionService injectionService,
            EasupGrpcService easupGrpcService,
            FraudService fraudService,
            RequestPayoutSender requestPayoutSender,
            RequestMapper requestMapper,
            DurationRequestCheckGrpcClient durationRequestCheckGrpcClient,
            RequestChecksGrpcService requestChecksGrpcService,
            OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient,
            FraudMonitoringService fraudMonitoringService,
            TravelCardValidationService travelCardValidationService,
            ReceiptScannerService receiptScannerService
    ) {
        super(repository, checkinSettingsService, regionDataResolver,
                entityDTOMapper, easupGrpcService, fraudService, requestChecksGrpcService, durationRequestCheckGrpcClient, overrunRequestCheckGrpcClient, fraudMonitoringService);
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.sqGenerator = sqGenerator;
        this.organizationService = organizationService;
        this.addressRepository = addressRepository;
        this.geoDataProcessingService = geoDataProcessingService;
        this.reservationService = reservationService;
        this.historyRepository = historyRepository;
        this.requestSender = requestSender;
        this.documentSender = documentSender;
        this.transportCompensationRepository = transportCompensationRepository;
        this.approvalsSettingsInjectionService = approvalsSettingsInjectionService;
        this.publicTariffService = publicTariffService;
        this.injectionService = injectionService;
        this.receiptScannerService = receiptScannerService;
        transportType = TransportTypeEnum.PUBLIC;
        this.approvalDeadlineCalculator = new PublicApprovalDeadlineCalculatorImpl();
        this.paymentDoneDeadlineCalculator = new PersonalPaymentDoneDeadlineCalculatorImpl();
        this.requestPayoutSender = requestPayoutSender;
        this.requestMapper = requestMapper;
        this.travelCardValidationService = travelCardValidationService;
    }

    @Override
    public Optional<RequestForPublic> get(UUID id) {
        return getRepository().findById(id);
    }

    @Override
    public RequestForCompensationDTO addCompensationRequest(JwtAuthenticationToken authentication, NewRequestForCompensationDTO data) {
        var initiator = employeeService.getAuthenticatedEmployee(authentication);

        // проверяем прикрепленные документы только если есть "Компенсация междугородних поездок"
        checkDocumentsForCreatingRequest(data.getCompensationDocuments(), data.getTransportCompensation(), initiator);
        checkExpirationDatesForCreatingRequest(data.getTransportCompensation());
        checkMaxCountCompensationDocuments(data.getTransportCompensation());
        checkPeriodTravelCard(data.getTransportCompensation());
        checkOnlyPaidServices(data.getTransportCompensation());

        final var employeeTravelCardCompensationList = transportCompensationRepository
                .findByEmployeeAndCompensationType(data.getPassenger().id(), PublicCompensationType.TRAVEL_CARD_COMPENSATION);
        checkDuplicationTravelCard(mapper.newDtoToTransportCompensations(data.getTransportCompensation()),
                employeeTravelCardCompensationList);

        final var tariff = publicTariffService.getTariffById(data.getTariffId());

        final var now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        final var humanReadableId = getHumanReadableId(initiator);
        final var desiredDate = data.getDesiredDate() == null ||
                Duration.between(now, data.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0 ?
                now.plusMinutes(5) :
                data.getDesiredDate();
        final var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(now, desiredDate, data.getTimeZone());

        final var requestForPublic = mapper.newCompensationDtoToRequest(data);
        requestForPublic.setHumanReadableId(humanReadableId);
        requestForPublic.setStatus(TripRequestStatus.PUBLIC_AWAITING_APPROVAL);
        requestForPublic.setCreationTime(now);
        requestForPublic.setDesiredDate(desiredDate);
        //записать автора, полученного из Authentication
        requestForPublic.setAuthor(initiator);
        requestForPublic.setOrganizationId(data.getAuthor().organizationId());
        requestForPublic.setEmployeeDeviceTimeZone(data.getEmployeeDeviceTimeZone());
        requestForPublic.setTariff(tariff);
        requestForPublic.setPayRequestId(data.getPayRequestId());
        setRequestHistoryItem(requestForPublic, CREATE_HISTORY_MSG, requestForPublic.getCreationTime());
        createAddressesAndLimitReservationForCreate(requestForPublic);
        //если документы не были прикреплены - не пытаться отправить их в approvals
        if (requestForPublic.getCompensationDocuments() != null &&
                !requestForPublic.getCompensationDocuments().isEmpty()) {
            sendDocuments(requestForPublic, initiator);
        }
        requestForPublic.setApprovalDeadline(approvalDeadline);
        requestForPublic.setApprovalDeadlineState(DeadlineState.NONE);
        requestForPublic.setSource(data.getSource() == null ? RequestSourceEnum.UNDEFINED : data.getSource());
        requestForPublic.getTransportCompensation().forEach(it -> it.setRequest(requestForPublic));

        checkTravelCard(requestForPublic);
        var isAbsent = checkOnAbsence(requestForPublic, initiator, Optional.of(data.getEmployeeDeviceTimeZone()));
        getRepository().save(requestForPublic);
        if (isAbsent) {
            createFraud(requestForPublic, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);
            return mapper.compensationToPublicDto(requestForPublic); //отправка в кафку после сохранения фрода
        }
        checkDurationLimit(requestForPublic, Optional.of(data.getEmployeeDeviceTimeZone()));
        checkMultipointLimit(requestForPublic, Optional.of(data.getEmployeeDeviceTimeZone()));
        checkOverrunLimit(requestForPublic, Optional.of(data.getEmployeeDeviceTimeZone()));
        checkSingleTripDuration(requestForPublic);
        requestSender.send(requestForPublic);
        var compensationDocuments = requestForPublic.getCompensationDocuments();
        if (compensationDocuments != null && !compensationDocuments.isEmpty()) {
            var receiptScannerMessage = requestMapper.toReceiptScannerMessage(requestForPublic);
            receiptScannerService.sendToReceiptScanner(receiptScannerMessage);
        }
        return mapper.compensationToPublicDto(requestForPublic);
    }

    private void checkOnlyPaidServices(List<NewTransportCompensationDTO> compensations) {
        var compensationTypes =
                compensations.stream()
                        .map(NewTransportCompensationDTO::getCompensationType)
                        .map(PublicCompensationTypeDTO::getName)
                        .map(e -> Enum.valueOf(PublicCompensationType.class, e))
                        .collect(Collectors.toSet());
        if (compensationTypes.size() > 1 && compensationTypes.contains(PublicCompensationType.PAID_SERVICES_COMPENSATION)) {
            throw new ValidationRequestException("Заявка на компенсацию платных сервисов не должна содержать других типов компенсаций.");
        }
    }

    private void checkTravelCard(RequestForPublic request) {
        if (isCheckOneTimeTripEnabled) {
            travelCardValidationService.checkOneTimeTrip(request);
        }
        if (isCheckOverlapEnabled) {
            travelCardValidationService.checkOverlap(request);
        }
    }

    private void checkPeriodTravelCard(List<? extends NewTransportCompensationDTO> compensations) {
        final var minDate = LocalDate.now().minusDays(5);
        final var maxDate = LocalDate.now().plusYears(1L);

        if (compensations == null || compensations.isEmpty()) {
            return;
        }
        for (final var doc : compensations) {
            final var expirationStart = doc.getTicketsExpirationStart();
            final var expirationEnd = doc.getTicketsExpirationEnd();
            if (PublicCompensationType.TRAVEL_CARD_COMPENSATION.name().equalsIgnoreCase(doc.getCompensationType().getName())
                    && (expirationStart.isBefore(minDate) || expirationStart.isAfter(maxDate) || expirationEnd.isBefore(minDate) || expirationEnd.isAfter(maxDate))) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Период проездного билета не может попадать в даты ранее чем за 5 дней до текущего дня или более одного года в будущем");
            }

        }
    }

    private void checkMaxCountCompensationDocuments(List<? extends NewTransportCompensationDTO> compensations) {
        if (compensations != null && compensations.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "В одной заявке нельзя указать более 10 билетов/проездных");
        }
    }

    @Override
    public RequestForCompensationDTO getRequestForPublicCompensation(UUID requestId, RequestProjection projection) {
        var result = mapper.compensationToPublicDto(findRequest(requestId));
        if (RequestProjection.SELECT.equals(projection)) {
            result = result.toBuilder().segments(null).build();
        }
        return result;
    }

    //todo больше не используется для общественного. Удалить, когда сервис будет отвязан от абстрактного класса
    @Override
    @Deprecated
    public Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroupDTO) {
        throw new UnsupportedTransportTypeException(TransportTypeEnum.PUBLIC);
    }

    @Override
    public void edit(UUID requestId, Employee initiator, NewRequestForPublicDTO newData) {
        var requestFromDb = findRequest(requestId);
        checkRequestInitiatorIsAuthor(initiator, requestFromDb);
        checkStatusIsEqualToRequired(requestFromDb, TripRequestStatus.PUBLIC_AWAITING_APPROVAL);

        if (newData instanceof RequestForCompensationDTO data) {
            checkDocumentsForCreatingRequest(data.getCompensationDocuments(), data.getTransportCompensation(),
                    initiator);
            checkExpirationDatesForCreatingRequest(data.getTransportCompensation());
            checkMaxCountCompensationDocuments(data.getTransportCompensation());
            checkPeriodTravelCard(data.getTransportCompensation());
            checkDuplicationTravelCard(data.getTransportCompensation(), requestFromDb.getTransportCompensation());

            ArrayList<CompensationDocument> curDocumentList = null;
            ArrayList<CompensationDocument> prevDocumentList = null;
            if (data.getCompensationDocuments() != null && !data.getCompensationDocuments().isEmpty()) {
                curDocumentList = new ArrayList<>(mapper.dtoToDocuments(data.getCompensationDocuments()));
            }

            if (requestFromDb.getCompensationDocuments() != null && !requestFromDb.getCompensationDocuments().isEmpty()) {
                prevDocumentList = new ArrayList<>(requestFromDb.getCompensationDocuments());
            }

            if (curDocumentList != null && prevDocumentList != null) {
                sendNewDocuments(prevDocumentList, curDocumentList, data.getId(), initiator.getId());
                sendDeletedDocuments(prevDocumentList, curDocumentList, data.getId(), initiator.getId());
            }

            var tariff = publicTariffService.getTariffById(data.getTariffId());

            RequestForPublic updatedRequest = mapper.updatePublicRequestFromCompensationDto(data, requestFromDb);
            updatedRequest.setTariff(tariff);
            transportCompensationMerge(updatedRequest, mapper.dtoToTransportCompensations(data.getTransportCompensation()));

            setRequestHistoryItem(updatedRequest, UPDATE_HISTORY_MSG, LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            var waypoints = mapper.waypointDTOListToWaypointList(newData.getWaypoints());
            createAddressesAndLimitReservationForUpdate(updatedRequest, waypoints);
            requestSender.send(updatedRequest);
        }
    }

    //todo больше не используется для общественного. Удалить, когда сервис будет отвязан от абстрактного класса
    @Override
    @Deprecated
    public Request update(RequestDTO newData, Employee activeUser, Request request, String token) {
        throw new UnsupportedTransportTypeException(TransportTypeEnum.PUBLIC);
    }

    @Override
    public void confirm(UUID requestId, Employee initiator, List<CompensationDocumentDTO> newDocumentsDto) {
        var requestFromDb = findRequest(requestId);
        checkRequestInitiatorIsAuthor(initiator, requestFromDb);
        checkRequestCompensationType(requestFromDb);
        checkStatusIsEqualToRequired(requestFromDb, TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
        // если документы не были переданы, то не пытаться их прикрепить
        List<CompensationDocument> newDocuments = null;
        if (newDocumentsDto != null && !newDocumentsDto.isEmpty()) {
            newDocuments = mapper.dtoToDocuments(newDocumentsDto);
            checkDocsDuplicatesIntoRequestFromDb(newDocuments, requestFromDb);
            if (requestFromDb.getCompensationDocuments() == null) {
                requestFromDb.setCompensationDocuments(new ArrayList<>());
            }
            requestFromDb.getCompensationDocuments().addAll(newDocuments);
        }
        requestFromDb.setStatus(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        requestFromDb.setTripConfirmationDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        getRepository().save(requestFromDb);
        //отправить в kafka данные по измененной Заявке и прикрепленным подтверждающим Документам
        requestSender.send(requestFromDb);
        // если документы не были переданы, то не пытаться отправить их в approvals
        if (newDocuments != null) {
            sendConfirmationDocuments(newDocuments, requestFromDb, initiator);
        }

    }

    @Override
    public Request finish(Request toFinish, Employee activeUser) {
        throw new UnsupportedTransportTypeException(toFinish.getTransportType());
    }

    @Override
    public Request complete(Request request, Employee activeUser) {
        throw new UnsupportedTransportTypeException(request.getTransportType());
    }

    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator) {
        var requestForPublic = getRepository().getReferenceById(request.getId());
        if (TripRequestStatus.TAXI_CANCELLED == requestForPublic.getStatus()) {
            return;
        }
        if (!request.getStatus().isCancelable()) {
            throw new IllegalArgumentException("Невозможно отменить запрос %s в статусе %s".formatted(request.getHumanReadableId(),
                    request.getStatus().name()));
        }
        requestForPublic.setStatus(TripRequestStatus.PUBLIC_CANCELLED);
        requestForPublic.setStatusCode(cancelDTO.getCode());
        if (TripRequestStatus.PublicStatusCode.PUBLIC_DECLINED_AT_EXPIRATION.getCode() == cancelDTO.getCode()) {
            requestForPublic.setApprovalDeadlineState(DeadlineState.RED);
        }
        try {
            reservationService.cancel(requestForPublic);
            requestForPublic.getHistoryItemsForPublic().add(RequestHistoryElementForPublic.builder()
                    .changeDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                    .requestForPublic(requestForPublic)
                    .code(cancelDTO.getCode())
                    .comment("Заявка отменена по причине: " + cancelDTO.getReason())
                    .status(requestForPublic.getStatus())
                    .initiator(initiator.getId())
                    .build());
            requestForPublic = getRepository().save(requestForPublic);
            requestSender.send(requestForPublic);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Request approveRequest(Request request) {
        var publicRequest = (RequestForPublic) request;
        var transportCompensations = publicRequest.getTransportCompensation();
        boolean containsSuburb = transportCompensations.stream()
                .map(TransportCompensation::getTransportType)
                .map(PublicTransportType::getPublicCompensationType)
                .anyMatch(tc -> tc.equals(PublicCompensationType.SUBURB_TRIP_COMPENSATION));
        if (!containsSuburb) {
            publicRequest.setStatus(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
            publicRequest.setOrderPaymentFormationStartDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            setPaymentDoneDeadline(publicRequest);
        } else {
            if (injectionService.isPublicTrTripConfirmationRequired(publicRequest.getApprovedBy())) {
                publicRequest.setStatus(TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
            } else if (injectionService.isPublicTrTripAwaitingAffirmative(publicRequest.getApprovedBy())) {
                publicRequest.setStatus(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
            } else {
                publicRequest.setTripConfirmationDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
                publicRequest.setOrderPaymentFormationStartDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
                publicRequest.setStatus(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
                setPaymentDoneDeadline(publicRequest);
            }
        }
        publicRequest.setApprovalState(ApprovalState.APPROVED);
        publicRequest.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.getHistoryItemsForPublic().add(
                RequestHistoryElementForPublic.builder()
                        .changeDate(publicRequest.getApprovalDate())
                        .requestForPublic(publicRequest)
                        .comment("Заявка согласована " + publicRequest.getApprovedBy().getFIO())
                        .status(publicRequest.getStatus())
                        .initiator(publicRequest.getApprovedBy().getId())
                        .build());
        if (TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION.equals(publicRequest.getStatus())) {
            requestPayoutSender.send(requestMapper.requestToRequestPayoutMessage(publicRequest, publicRequest.getApprovalDate()));
        }
        return publicRequest;
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        var requestForPublic = (RequestForPublic) toChange;
        requestForPublic.setStatus(newStatus);
        if (newStatus == TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION) {
            if (requestForPublic.getOrderPaymentFormationStartDate() == null) {
                requestForPublic.setOrderPaymentFormationStartDate(requestForPublic.getDesiredDate());
            }
            setPaymentDoneDeadline(requestForPublic);
        }
        requestForPublic = getRepository().save(requestForPublic);
        var historyItem = historyRepository.save(RequestHistoryElementForPublic.builder()
                .requestForPublic(requestForPublic)
                .comment("Статус заявки изменен пользователем: " + activeUser.getFIO())
                .status(newStatus)
                .initiator(activeUser.getId())
                .build());
        requestSender.send(requestForPublic);
        if (TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION.equals(newStatus)) {
            requestPayoutSender.send(requestMapper.requestToRequestPayoutMessage(requestForPublic, historyItem.getChangeDate()));
        }
        return requestForPublic;
    }

    private void setPaymentDoneDeadline(RequestForPublic requestForPublic) {
        if (requestForPublic.getPaymentDoneDeadline() == null) {
            requestForPublic.setPaymentDoneDeadline(
                    paymentDoneDeadlineCalculator.getPaymentDoneDeadline(requestForPublic.getOrderPaymentFormationStartDate(),
                            requestForPublic.getTimeZone()));
        }
    }

    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        if (TransportTypeEnum.PUBLIC.equals(request.getTransportType())) {
            RequestForPublic toApprove = (RequestForPublic) request;
            if (toApprove.getStatus() != TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE) {
                log.warn("Skip approve final trip because request has status: {}", request.getStatus());
                return;
            }
            if (toApprove.getExpected().getCost() > 0) {
                //todo Костыль для показа списаний, заменить на нормальную обработку после интеграции
                reservationService
                        .spend(toApprove, toApprove.getExpected().getCost().intValue(), TransportTypeEnum.PUBLIC);
            }
            toApprove.setStatus(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
            toApprove.setOrderPaymentFormationStartDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            setPaymentDoneDeadline(toApprove);
            requestSender.send(toApprove);
            requestPayoutSender.send(requestMapper.requestToRequestPayoutMessage(toApprove, toApprove.getOrderPaymentFormationStartDate()));
        }
    }

    @Override
    public void markSlaExpired(UUID requestId) {
        var request = getRepository().findById(requestId)
                .orElseThrow(
                        () -> new EntityNotFoundException(Request.class, requestId));
        request.setSlaExpired(true);
        requestSender.send(request);
        getRepository().save(request);
    }

    /**
     * Установить человекочитаемый ID для организации по инициатору заявки
     *
     * @param initiator инициатор заявки
     * @return человекочитаемый ID для организации
     */
    private String getHumanReadableId(Employee initiator) {
        var organizationDigitId = departmentService.get(initiator.getDepartment().getId())
                .map(Department::getOrganization).map(Organization::getId).flatMap(organizationService::get)
                .map(Organization::getDigitId).orElse(null);

        return sqGenerator.getNextId(Prefix.OT, organizationDigitId);
    }

    /**
     * Записать HistoryItem
     *
     * @param requestForPublic RequestForPublic
     * @param message          сообщение
     * @param time             время изменения / создания заявки
     */
    private void setRequestHistoryItem(RequestForPublic requestForPublic, String message, LocalDateTime time) {
        requestForPublic.getHistoryItemsForPublic()
                .add(RequestHistoryElementForPublic.builder()
                        .changeDate(time)
                        .requestForPublic(requestForPublic)
                        .comment(message)
                        .status(requestForPublic.getStatus())
                        .initiator(requestForPublic.getAuthor().getId())
                        .build());
    }

    /**
     * Создать адреса из waypoints и зарезервировать лимит при создании новой Заявки
     *
     * @param requestForPublic RequestForPublic
     */
    private void createAddressesAndLimitReservationForCreate(RequestForPublic requestForPublic) {
        RequestForPublic requestWithId;
        try {
            requestForPublic.getWaypoints().forEach(it -> it.setRequest(requestForPublic));
            geoDataProcessingService.saveAddresses(requestForPublic.getWaypoints(), requestForPublic.getPassenger());
            requestWithId = getRepository().saveAndFlush(requestForPublic);
            reservationService.makeReservation(requestWithId,
                    requestWithId.getPassenger(),
                    requestWithId.getExpected().getCost(),
                    requestWithId.getExpected().getBonusCost());
            historyRepository.flush();
            requestWithId.getHistoryItemsForPublic()
                    .sort(Comparator.comparing(RequestHistoryElementForPublic::getChangeDate));
            addressRepository.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создать адреса из waypoints и перерезервировать лимит при редактировании Заявки
     *
     * @param requestForPublic RequestForPublic
     * @param waypoints        вейпойнты должны быть получены из DTO и не привязаны к Заявке (т.к. далее привязанные к заявке вейпойнты стираются)
     */
    private void createAddressesAndLimitReservationForUpdate(
            RequestForPublic requestForPublic, List<Waypoint> waypoints
    ) {
        try {
            //пересохранить вейпойнты / адреса
            geoDataProcessingService.saveAddresses(requestForPublic, waypoints);
            requestForPublic = getRepository().saveAndFlush(requestForPublic);
            //заново отправить новый запрос лимита (перерезервирование в мс лимитов)
            reservationService.makeReservation(requestForPublic,
                    requestForPublic.getPassenger(),
                    requestForPublic.getExpected().getCost(),
                    requestForPublic.getExpected().getBonusCost());
            getRepository().saveAndFlush(requestForPublic);
            historyRepository.flush();
            requestForPublic.getHistoryItemsForPublic()
                    .sort(Comparator.comparing(RequestHistoryElementForPublic::getChangeDate));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        addressRepository.flush();
    }

    /**
     * Обновить сохраненые ранее заявки на компенсацию ОТ из измененной поездки
     *
     * @param request             RequestForPublic
     * @param compensationNewList список новых заявок на компенсацию
     */
    private void transportCompensationMerge(@NonNull RequestForPublic request, List<TransportCompensation> compensationNewList) {
        if (request.getTransportCompensation() != null) {
            Iterator<TransportCompensation> iterator = request.getTransportCompensation().iterator();
            while (iterator.hasNext()) {
                TransportCompensation compensation = iterator.next();
                Optional<TransportCompensation> first = compensationNewList.stream()
                        .filter(dto -> compensation.getId().equals(dto.getId()))
                        .findFirst();
                if (first.isPresent()) {
                    mapper.updateTransportCompensation(first.get(), compensation);
                } else {
                    iterator.remove();
                }
            }
        }

        if (request.getTransportCompensation() == null) {
            request.setTransportCompensation(new ArrayList<>());
        }
        compensationNewList.stream()
                .filter(dto -> dto.getId() == null)
                .forEach(request.getTransportCompensation()::add);

        getRepository().saveAndFlush(request);
    }

    /**
     * Найти сохраненную Заявку на компенсацию за общественный транспорт в репозитории
     *
     * @param requestId ID Заявки на компенсацию за общественный транспорт
     */
    private RequestForPublic findRequest(UUID requestId) {
        return getRepository().findById(requestId).orElseThrow(() -> new EntityNotFoundException(
                Request.class, requestId));
    }

    /**
     * Проверить, является ли инициатор автором Заявки на компенсацию за общественный транспорт
     *
     * @param initiator инициатор
     * @param request   заявка на компенсацию за общественный транспорт
     */
    private void checkRequestInitiatorIsAuthor(Employee initiator, RequestForPublic request) {
        if (!(initiator.getId().equals(request.getAuthor().getId()) ||
                initiator.getId().equals(request.getPassenger().getId()))) {
            throw new IllegalCallerResponseException();
        }
    }

    /**
     * Проверить, имеет ли Заявка требуемый статус
     *
     * @param request        заявка на компенсацию за общественный транспорт
     * @param requiredStatus требуемый статус
     */
    private void checkStatusIsEqualToRequired(RequestForPublic request, TripRequestStatus requiredStatus) {
        if (!request.getStatus().equals(requiredStatus)) {
            throw new IllegalStateResponseException(
                    String.format("Невозможно отредактировать заявку в статусе %s", request.getStatus()));
        }
    }

    /**
     * Проверить список новых Документов на предмет того, что они уже присутствуют в заявке
     *
     * @param newDocuments  список новых Документов
     * @param requestFromDb Заявка из БД
     */
    private void checkDocsDuplicatesIntoRequestFromDb(
            List<CompensationDocument> newDocuments,
            RequestForPublic requestFromDb
    ) {
        if (newDocuments != null && requestFromDb != null && requestFromDb.getCompensationDocuments() != null) {
            for (CompensationDocument newDoc : newDocuments) {
                for (CompensationDocument docFromDb : requestFromDb.getCompensationDocuments()) {
                    if (newDoc.getId().equals(docFromDb.getId())) {
                        throw new IllegalStateResponseException("Документ %s уже есть в системе.".formatted(newDoc.getId()));
                    }
                }
            }
        }
    }

    /**
     * Проверить тип компенсации Заявки для прикрепления документа
     *
     * @param requestFromDb Заявка
     */
    private void checkRequestCompensationType(RequestForPublic requestFromDb) {
        for (TransportCompensation transportCompensation : requestFromDb.getTransportCompensation()) {
            if (PublicCompensationType.CITY_TRIP_COMPENSATION.equals(transportCompensation.getCompensationType())) {
                throw new IllegalStateResponseException("Compensation type 'CITY_TRIP_COMPENSATION' is incorrect!");
            }
        }
    }

    /**
     * Проверить заполнение срока действия билетов
     *
     * @param compensationList список компенсаций
     */
    private void checkExpirationDatesForCreatingRequest(List<? extends NewTransportCompensationDTO> compensationList) {
        for (final var compensation : compensationList) {
            final var compensationType = PublicCompensationType.getByName(compensation.getCompensationType().getName()).orElse(null);

            if (compensationType != null && compensationType.isExpirationDatesRequired()
                    && (compensation.getTicketsExpirationStart() == null || compensation.getTicketsExpirationEnd() == null)) {
                throw new IllegalStateResponseException(
                        String.format("Для %s необходимо указать дату начала и окончания действия проездного.", compensationType.getRusName()));
            }

        }
    }

    /**
     * Проверить дублирование заявки на компенсацию проездного
     *
     * @param compensationList список компенсаций
     */
    private void checkDuplicationTravelCard(
            List<TransportCompensationDTO> compensationList, List<TransportCompensation> transportCompensationDBList
    ) {
        checkDuplicationNewVsNew(compensationList);
        checkDuplicationNewVsDB(compensationList, transportCompensationDBList);
    }

    private void checkDuplicationNewVsNew(List<TransportCompensationDTO> compensationList) {
        if (Optional.ofNullable(compensationList).orElseGet(Collections::emptyList).isEmpty()) {
            return;
        }
        for (var dto1 : compensationList) {
            for (var dto2 : compensationList) {
                if (dto1 != dto2 &&
                        (dto1.getId() == null || dto2.getId() == null || !dto1.getId().equals(dto2.getId())) &&
                        ((dto1.getTransportType() != null &&
                                dto2.getTransportType() != null &&
                                dto1.getTransportType().getName().equalsIgnoreCase(dto2.getTransportType().getName())) ||
                                (dto1.getTransportType() != null &&
                                        dto1.getTransportType().getName().equalsIgnoreCase(PublicTransportType.TRAVEL_CARD_ALL_CITY_TRANSPORT.name())) ||
                                (dto2.getTransportType() != null &&
                                        dto2.getTransportType().getName().equalsIgnoreCase(PublicTransportType.TRAVEL_CARD_ALL_CITY_TRANSPORT.name()))) &&
                        dto1.getCompensationType() != null &&
                        dto1.getCompensationType().getName().equalsIgnoreCase(PublicCompensationType.TRAVEL_CARD_COMPENSATION.name()) &&
                        dto2.getCompensationType() != null &&
                        dto2.getCompensationType().getName().equalsIgnoreCase(PublicCompensationType.TRAVEL_CARD_COMPENSATION.name()) &&
                        !isDifferentPeriod(dto1, dto2)) {
                    throw new IllegalStateResponseException(
                            String.format(
                                    "Периоды действия проездных билетов в заявке пересекаются. Обратите внимание на проездной билет с датой начала " +
                                            "с %s по %s.\n" +
                                            "Необходимо выбрать другие даты",
                                    dto2.getTicketsExpirationStart(),
                                    dto2.getTicketsExpirationEnd()));
                }
            }
        }
    }

    private void checkDuplicationNewVsDB(List<TransportCompensationDTO> compensationList, List<TransportCompensation> transportCompensationDBList) {
        if (!Optional.ofNullable(compensationList).orElseGet(Collections::emptyList).isEmpty()) {
            for (var dto : compensationList) {
                for (var compensation : transportCompensationDBList) {
                    if (!compensation.getId().equals(dto.getId()) &&
                            (compensation.getCompensationType().equals(PublicCompensationType.TRAVEL_CARD_COMPENSATION) &&
                                    dto.getCompensationType().getName().equalsIgnoreCase(PublicCompensationType.TRAVEL_CARD_COMPENSATION.name())) &&
                            (dto.getTransportType().getName().equalsIgnoreCase(compensation.getTransportType().name()) ||
                                    dto.getTransportType().getName().equalsIgnoreCase(PublicTransportType.TRAVEL_CARD_ALL_CITY_TRANSPORT.name()) ||
                                    PublicTransportType.TRAVEL_CARD_ALL_CITY_TRANSPORT.equals(compensation.getTransportType())) &&
                            !isDifferentPeriod(dto, compensation)) {
                        throw new IllegalStateResponseException(
                                String.format(
                                        "Уже существует заявка %s с датой начала с %s по %s на данный вид компенсации.\n" +
                                                "Необходимо выбрать другие даты",
                                        Optional.ofNullable(compensation.getRequest()).map(RequestForPublic::getHumanReadableId).orElse(null),
                                        compensation.getTicketsExpirationStart(),
                                        compensation.getTicketsExpirationEnd()));
                    }
                }
            }
        }
    }

    private boolean isDifferentPeriod(TransportCompensationDTO one, TransportCompensation two) {
        return (one.getTicketsExpirationStart().isAfter(two.getTicketsExpirationStart()) &&
                one.getTicketsExpirationEnd().isAfter(two.getTicketsExpirationEnd())) ||
                (one.getTicketsExpirationStart().isBefore(two.getTicketsExpirationStart()) &&
                        one.getTicketsExpirationEnd().isBefore(two.getTicketsExpirationEnd()));
    }

    private boolean isDifferentPeriod(TransportCompensationDTO one, TransportCompensationDTO two) {
        return (one.getTicketsExpirationStart().isAfter(two.getTicketsExpirationStart()) &&
                one.getTicketsExpirationEnd().isAfter(two.getTicketsExpirationEnd())) ||
                (one.getTicketsExpirationStart().isBefore(two.getTicketsExpirationStart()) &&
                        one.getTicketsExpirationEnd().isBefore(two.getTicketsExpirationEnd()));
    }

    /**
     * Проверить прикрепление документов для создания заявки
     *
     * @param documentList     список документов
     * @param compensationList список компенсаций
     */
    private void checkDocumentsForCreatingRequest(
            List<CompensationDocumentDTO> documentList,
            List<? extends NewTransportCompensationDTO> compensationList,
            Employee initiator
    ) {
        for (final var compensationDTO : compensationList) {
            final var compensationType = PublicCompensationType.getByName(compensationDTO.getCompensationType().getName()).orElse(null);
            if (compensationType == null) {
                throw new IllegalStateException("Неизвестный тип компенсации.");
            }
            // проверяем прикрепление документов для междугородних поездок если необходимость прикрепления включена
            // в настройках согласований (approval_document_check)
            if (PublicCompensationType.SUBURB_TRIP_COMPENSATION.equals(compensationType) &&
                    approvalsSettingsInjectionService.isPublicTrCreationDocumentRequired(initiator) &&
                    isEmptyCompensationDocuments(documentList)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не были прикреплены подтверждающие документы.");
            }
        }
    }

    private boolean isEmptyCompensationDocuments(List<CompensationDocumentDTO> documentDTOList) {
        if (documentDTOList == null) {
            return true;
        }

        if (documentDTOList.isEmpty()) {
            return true;
        }
        for (CompensationDocumentDTO documentDTO : documentDTOList) {
            if (documentDTO.getId() == null) {
                return true;
            }
            if (documentDTO.getFolder() == null) {
                return true;
            }
            if (documentDTO.getFileName() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Отправить в Kafka сообщение о прикрепленных Документах при создании Заявки
     *
     * @param request   Заявка
     * @param initiator инициатор Заявки
     */
    private void sendDocuments(RequestForPublic request, Employee initiator) {
        request.getCompensationDocuments()
                .forEach(document -> documentSender.send(document, request.getId(), initiator.getId()));
    }

    /**
     * Отправить в Kafka сообщение о прикрепленных Документах при подтверждении Заявки
     *
     * @param documents список подтверждающих документов
     * @param request   Заявка на компенсацию за общественный транспорт
     * @param initiator инициатор Заявки
     */
    private void sendConfirmationDocuments(
            List<CompensationDocument> documents, RequestForPublic request, Employee initiator
    ) {
        documents.forEach(doc -> documentSender.send(doc, request.getId(), initiator.getId()));
    }

    private void sendDeletedDocuments(
            List<CompensationDocument> prevDocumentList,
            List<CompensationDocument> currentDocuments,
            UUID requestId, UUID employeeId
    ) {
        applyForDiff(currentDocuments, prevDocumentList, doc -> documentSender.sendDeleted(doc, requestId, employeeId));
    }

    private void sendNewDocuments(
            List<CompensationDocument> prevDocumentList,
            List<CompensationDocument> currentDocuments,
            UUID requestId, UUID employeeId
    ) {
        applyForDiff(prevDocumentList, currentDocuments, doc -> documentSender.send(doc, requestId, employeeId));
    }

    /**
     * Для элементов списка <b>secondList</b> вызывается consumer.accept, если элемент не содержится в
     * <b>firstList</b> или его id is null
     *
     * @param firstList  первый список
     * @param secondList второй список
     * @param consumer   консьюмер
     */
    private void applyForDiff(
            List<CompensationDocument> firstList,
            List<CompensationDocument> secondList,
            Consumer<CompensationDocument> consumer
    ) {
        final var firstListIds = firstList.stream()
                .map(CompensationDocument::getId)
                .filter(Objects::nonNull)
                .toList();
        secondList.stream().filter(doc -> doc.getId() == null || !firstListIds.contains(doc.getId())).forEach(consumer);
    }
}
