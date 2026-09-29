package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.exceptions.StatusChangeException;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.validate.TripSplitCheckService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@Transactional
public class RequestForPersonalServiceImpl extends AbstractTransportTypeService<RequestForPersonal> {

    private final EmployeeService employeeService;
    private final TripPurposeRepository tripPurposeRepository;
    private final DepartmentService departmentService;
    private final SQGenerator sqGenerator;
    private final RequestForPersonalRepository requestForPersonalRepository;
    private final OrganizationService organizationService;
    private final AddressRepository addressRepository;
    private final GeoDataProcessingService geoDataProcessingService;
    private final MagentaAuxilaryService magentaAuxilaryService;
    private final ReservationService reservationService;
    private final RequestHistoryRepository historyRepository;
    private final RequestSender<RequestForPersonal> requestSender;
    private final PersonalCarDataResolver personalCarDataResolver;
    private final DepLimitRepository depLimitRepository;
    private final SrmGrpcClient srmGrpcClient;
    private final PositionService positionService;
    private final PersonalTariffService personalTariffService;
    private final PersonalApprovalDeadlineCalculatorImpl approvalDeadlineCalculator;
    private final PaymentDoneDeadlineCalculator paymentDoneDeadlineCalculator;
    @Value("${request_for_personal.additionalSumForDriver:10000}")
    private long additionalSumForDriver;
    private final ProductionCalendar productionCalendar = new ProductionCalendarImpl();
    private final Clock clock;
    private final TripSplitCheckService splitCheckService;
    FraudRepository fraudRepository;
    private final RequestPayoutSender requestPayoutSender;
    private final RequestMapper requestMapper;
    private final RequestForPersonalHistoryRepository requestForPersonalHistoryRepository;

    // Список статусов, при установке которых в заявке водителя, меняем статус в заявках попутчиков (пассажиров) на такой же
    private final Set<TripRequestStatus> suitableStatusesForUpdateLinkedRequest =
            Set.of(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS, TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL, TripRequestStatus.PERSONAL_SHARED_RIDE_APPROVED, TripRequestStatus.PERSONAL_SHARED_RIDE_DECLINED);
    
    // Список статусов, при установке которых в заявке водителя, меняем статус в заявках попутчиков (пассажиров) на завершение поездки
    private final Set<TripRequestStatus> suitableStatusesForFinishedLinkedRequest =
            Set.of(TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL, TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION, TripRequestStatus.PERSONAL_PAYMENT_AWAITING, TripRequestStatus.PERSONAL_PAYMENT_DONE, TripRequestStatus.PERSONAL_PAYMENT_DECLINED);

    @Autowired
    public RequestForPersonalServiceImpl(
            EmployeeService employeeService,
            TripPurposeRepository tripPurposeRepository,
            DepartmentService departmentService,
            SQGenerator sqGenerator,
            RequestForPersonalRepository requestForPersonalRepository,
            OrganizationService organizationService,
            EntityDTOMapper mapper,
            AddressRepository addressRepository,
            GeoDataProcessingService geoDataProcessingService,
            MagentaAuxilaryService magentaAuxilaryService,
            ReservationService reservationService,
            RequestHistoryRepository historyRepository,
            RequestSender<RequestForPersonal> requestSender,
            CheckinSettingsService checkinSettingsService,
            RegionDataResolver regionDataResolver,
            PersonalCarDataResolver personalCarDataResolver,
            DepLimitRepository depLimitRepository,
            SrmGrpcClient srmGrpcClient,
            PositionService positionService,
            PersonalTariffService personalTariffService,
            Clock clock, FraudService fraudService, TripSplitCheckService splitCheckService,
            EasupGrpcService easupGrpcService,
            FraudRepository fraudRepository,
            RequestPayoutSender requestPayoutSender,
            RequestMapper requestMapper,
            RequestForPersonalHistoryRepository requestForPersonalHistoryRepository,
            DurationRequestCheckGrpcClient durationRequestCheckGrpcClient,
            RequestChecksGrpcService requestChecksGrpcService,
            OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient,
            FraudMonitoringService fraudMonitoringService
    ) {
        super(requestForPersonalRepository, checkinSettingsService, regionDataResolver, mapper,
                easupGrpcService, fraudService, requestChecksGrpcService, durationRequestCheckGrpcClient, overrunRequestCheckGrpcClient, fraudMonitoringService);
        this.employeeService = employeeService;
        this.tripPurposeRepository = tripPurposeRepository;
        this.departmentService = departmentService;
        this.sqGenerator = sqGenerator;
        this.requestForPersonalRepository = requestForPersonalRepository;
        this.organizationService = organizationService;
        this.addressRepository = addressRepository;
        this.geoDataProcessingService = geoDataProcessingService;
        this.magentaAuxilaryService = magentaAuxilaryService;
        this.reservationService = reservationService;
        this.historyRepository = historyRepository;
        this.requestSender = requestSender;
        this.personalCarDataResolver = personalCarDataResolver;
        this.depLimitRepository = depLimitRepository;
        this.srmGrpcClient = srmGrpcClient;
        this.positionService = positionService;
        this.personalTariffService = personalTariffService;
        this.splitCheckService = splitCheckService;
        transportType = TransportTypeEnum.PERSONAL;
        this.approvalDeadlineCalculator = new PersonalApprovalDeadlineCalculatorImpl();
        this.paymentDoneDeadlineCalculator = new PersonalPaymentDoneDeadlineCalculatorImpl();
        this.clock = clock;
        this.fraudRepository = fraudRepository;
        this.requestPayoutSender = requestPayoutSender;
        this.requestMapper = requestMapper;
        this.requestForPersonalHistoryRepository = requestForPersonalHistoryRepository;
    }

    @Override
    public Optional<RequestForPersonal> get(UUID id) {
        return requestForPersonalRepository.findById(id);
    }

    @Override
    public Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroupDTO) {
        var department = departmentService.get(employee.getDepartment().getId()).orElseThrow();
        var organizationDigitId = organizationService.get(department.getOrganization().getId()).map(Organization::getDigitId).orElse(null);
        var humanReadableId = sqGenerator.getNextId(Prefix.OT, organizationDigitId);
        var organizationId = organizationService.get(department.getOrganization().getId()).map(Organization::getId).orElse(null);
        var tariff = personalTariffService.getTariffById(data.getTariffId());
        var outcomeTariff = personalTariffService.getTariffById(data.getOutcomeTariffId());

        var requestForPersonal = RequestForPersonal.builder()
                .author(employee)
                .segmentsJSON(data.getExpected().getSegments())
                .waypoints(mapper.waypointDTOListToWaypointList(
                        data.getExpected().getWaypoints()))
                .expected(mapper.dtoToExpectedData(data.getExpected()))
                .passenger(employeeService.get(data.getPassenger().id()).orElseThrow())
                .purpose(tripPurposeRepository.getReferenceById(data.getPurpose().getId()))
                .tariffId(data.getTariffId())
                .tariff(tariff)
                .outcomeTariff(outcomeTariff)
                .outcomeTariffId(data.getOutcomeTariffId())
                .humanReadableId(humanReadableId)
                .timeZone(Optional.ofNullable(data.getTimeZone()).orElse(defaultTimezone))
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL)
                .approvalState(ApprovalState.AWAITING_APPROVAL)
                .creationTime(LocalDateTime.now(clock))
                .desiredDate(data.getDesiredDate())
                .requestOptions(data.getRequestOptions())
                .organizationId(organizationId)
                .requestPrice(data.getRequestPrice())
                .employeeDeviceTimeZone(data.getEmployeeDeviceTimeZone())
                .joinedPassengerIds(data.getJoinedPassengerIds())
                .source(data.getSource() == null ? RequestSourceEnum.UNDEFINED : data.getSource())
                .minTariffTaxi(data.getMinTariffTaxi())
                .commentForPurpose(data.getCommentForPurpose())
                .executorGroupId(executorGroupDTO.getId())
                .executorGroupName(executorGroupDTO.getName())
                .build();
        requestForPersonal.setOccupiedPlacesCount(data.getOccupiedPlacesCount());
        if (data.getPersonalCarId() != null) {
            requestForPersonal.setPersonalCarId(data.getPersonalCarId());
            requestForPersonal.setPersonalCar(personalCarDataResolver.getPersonalCar(department.getOrganization().getId(),
                                                                                     department.getId(),
                                                                                     data.getPassenger().id(),
                                                                                     data.getPersonalCarId(),
                                                                                     token
                                                                                    ));
        }
        requestForPersonal.setCommentForDriver(data.getCommentForDriver());
        requestForPersonal.setPassengerCount(data.getPassengerCount());
        requestForPersonal.setCoopTrip(coop);
        requestForPersonal.setTransportType(data.getTransportType());
        requestForPersonal.getHistoryItemsForPersonal()
                          .add(RequestHistoryElementForPersonal.builder()
                                                               .changeDate(requestForPersonal.getCreationTime())
                                                               .requestForPersonal(requestForPersonal)
                                                               .comment("Заявка зарегистрирована")
                                                               .status(requestForPersonal.getStatus())
                                                               .initiator(requestForPersonal.getAuthor().getId())
                                                               .build());
        enrichWaypointsWithRadius(requestForPersonal, TransportTypeEnum.PERSONAL);
        try {
            var isAbsent = checkOnAbsence(requestForPersonal, employee, Optional.empty());
            var checkOnSplitIsValid = checkOnSplit(requestForPersonal);

            geoDataProcessingService.saveAddresses(requestForPersonal.getWaypoints(), requestForPersonal.getPassenger());
            requestForPersonal.setEmployeeDriverId(requestForPersonal.getPassenger().getId());
            requestForPersonal = requestForPersonalRepository.saveAndFlush(requestForPersonal);
            if (isAbsent) {
                createFraud(requestForPersonal, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);
            }
            if (!checkOnSplitIsValid) {
                createFraud(requestForPersonal, FraudType.SPLIT, SPLIT_DETECTED_FRAUD_COMMENT);
            }
            checkDurationLimit(requestForPersonal,Optional.empty());
            checkMultipointLimit(requestForPersonal, Optional.empty());
            checkOverrunLimit(requestForPersonal, Optional.empty());
            checkSingleTripDuration(requestForPersonal);
            long additionalSumPerPassenger = 0;
            if (coop) { // поставить EmployeeDriverId от водителя транспорта (sharedRideOwner)
                SrmSharedRideDTO sharedRideDTO = processCoopRequest(sharedRideId, requestForPersonal, token);
                if (sharedRideDTO != null) {
                    var requestForPersonalOpt =
                            requestForPersonalRepository.findById(sharedRideDTO.getRequestKpiList().get(0).getId());
                    if (requestForPersonalOpt.isPresent()) {
                        requestForPersonal.setEmployeeDriverId(requestForPersonalOpt.get().getEmployeeDriverId());
                        additionalSumPerPassenger = getAdditionalSumPerPassenger();
                    }
                }
            }
            requestForPersonal.setApprovalDeadline(approvalDeadlineCalculator.getApprovalDeadline(requestForPersonal));
            requestForPersonal.setApprovalDeadlineState(DeadlineState.NONE);

            var isDriver = !requestForPersonal.isCoopTrip() || requestForPersonal.isSharedRideOwner();
            if (isDriver && requestForPersonal.getPersonalCarId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан автомобиль (personalCarId = null)");
            }
            reservationService.makeReservation(requestForPersonal,
                                               requestForPersonal.getPassenger(),
                                               requestForPersonal.getExpected().getCost() + additionalSumPerPassenger,
                                               requestForPersonal.getExpected().getBonusCost());
            requestForPersonalRepository.saveAndFlush(requestForPersonal);
            historyRepository.flush();
            requestForPersonal.getHistoryItemsForPersonal()
                              .sort(Comparator.comparing(RequestHistoryElementForPersonal::getChangeDate));
        } catch (Exception e) {
            log.debug("Словили ошибку...");
            try {
                log.error("Произошла ошибка при создании заявки id={}, humanreadableid={}",
                          requestForPersonal.getId(), requestForPersonal.getHumanReadableId());
                removeRequestFromSharedRide(requestForPersonal);
                log.debug("Отправлен запрос в SRM на исключение заявки из совместной поездки, id = {}", requestForPersonal.getId());
            } catch (Exception ignore) {
                // ignore
            }
            log.error(e.getMessage(), e);
            throw e;
        }
        addressRepository.flush();
        updateNumberPassengersJoinedAndAdditionalSum(sharedRideId);
        refreshFraudData(requestForPersonal);
        return requestForPersonal;
    }


    private void refreshFraudData(RequestForPersonal entity) {
        List<FraudData> allByRequestIdIn = fraudRepository.findAllByRequestIdIn(List.of(entity.getId()));
        log.debug("[refreshFraudData] Step-1. Get Fraud data from DB for requestId: {}, size: {}", entity.getId(), allByRequestIdIn.size());
        entity.setFraudData(allByRequestIdIn);
        requestForPersonalRepository.saveAndFlush(entity);
        log.debug("[refreshFraudData] Step-2. Save Fraud data to DB for requestId: {}, size: {}", entity.getId(), entity.getFraudData().size());
    }

    private boolean checkOnSplit(RequestForPersonal entity) {
        final var desireDate = entity.getDesiredDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        final var timeZone = entity.getTimeZone();
        final var employeeId = entity.getPassenger().getId();
        final var expectedCost = entity.getExpected().getCost().longValue();
        final var expectedDuration = entity.getExpected().getTime().toMillis();

        return splitCheckService.check(new TripSplitCheckDTO(desireDate, timeZone, employeeId, expectedCost, expectedDuration)).isValid();
    }

    private SrmSharedRideDTO processCoopRequest(UUID sharedRideId, RequestForPersonal requestForPersonal, String token) {
        try {
            var srmSharedRideDTO = magentaAuxilaryService.processCoopRequest(sharedRideId, requestForPersonal, token);
            if (srmSharedRideDTO == null) {
                log.info("ЛИЧНЫЙ ТРАНСПОРТ: Заявка преобразована в одиночную по причине возникновения бизнес ошибки на стороне SRM при создании совместной заявки");
                requestForPersonal.setCoopTrip(false);
            } else {
                return srmSharedRideDTO;
            }
        } catch (ResponseStatusException e) {
            log.error("ЛИЧНЫЙ ТРАНСПОРТ: Заявка преобразована в одиночную по причине возникновения системной ошибки на стороне SRM при создании совместной заявки", e);
            requestForPersonal.setCoopTrip(false);
        }
        return null;
    }
    
    @Override
    public Request update(RequestDTO newData, Employee activeUser, Request request, String token) {
        var requestForPersonal = (RequestForPersonal) request;
        if (!requestForPersonal.getStatus().isEditable()) {
            throw new UpdateRequestException(
                    "Невозможно обновить данные заявки в статусе %s".formatted(request.getStatus().getDescription()));
        }
        if (requestForPersonal.isCoopTrip()) {
            throw new UpdateRequestException("Невозможно отредактировать данные запроса на совместную поездку. Возможна только отмена");
        }
        if (requestForPersonal.getExpected().getCost() != newData.getExpected().getCost()) {
            reservationService.makeReservation(requestForPersonal,
                                               requestForPersonal.getPassenger(),
                                               newData.getExpected().getCost(),
                                               newData.getExpected().getBonusCost());
        }
        
        var tariff = personalTariffService.getTariffById(newData.getTariffId());
        var outcomeTariff = personalTariffService.getTariffById(newData.getOutcomeTariffId());
        
        requestForPersonal.setApprovalDate(null);
        requestForPersonal.setDesiredDate(newData.getDesiredDate());
        var passenger = newData.getPassenger();
        var passengerId = passenger.id();
        requestForPersonal.setPassenger(employeeService.get(passengerId)
                                                       .orElseThrow(() -> new EntityNotFoundException(Employee.class, passengerId)));
        requestForPersonal.setPurpose(tripPurposeRepository.findById(newData.getPurpose().getId()).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, newData.getPurpose().getId())));
        requestForPersonal.setTariffId(newData.getTariffId());
        requestForPersonal.setTariff(tariff);
        requestForPersonal.setOutcomeTariff(outcomeTariff);
        requestForPersonal.setOutcomeTariffId(newData.getOutcomeTariffId());
        requestForPersonal.setExpected(mapper.dtoToExpectedData(newData.getExpected()));
        requestForPersonal.getSegmentsJSON().clear();
        requestForPersonal.getSegmentsJSON().addAll(newData.getExpected().getSegments());
        requestForPersonal.setRequestOptions(newData.getRequestOptions());
        requestForPersonal.setCommentForDriver(newData.getCommentForDriver());
        requestForPersonal.setPassengerCount(newData.getPassengerCount());
        if (newData.getPersonalCarId() != null) {
            Department department = departmentService.get(passenger.departmentId()).orElseThrow();
            requestForPersonal.setPersonalCar(personalCarDataResolver.getPersonalCar(department.getOrganization().getId(),
                                                                                     department.getId(),
                                                                                     passenger.id(),
                                                                                     newData.getPersonalCarId(),
                                                                                     token));
        } else {
            log.debug("No personal car specified");
        }
        save(requestForPersonal);
        geoDataProcessingService.saveAddresses(requestForPersonal, mapper.waypointDTOListToWaypointList(
                newData.getExpected().getWaypoints()));
        var historyElement =
                RequestHistoryElementForPersonal.builder()
                                                .changeDate(LocalDateTime.now(clock))
                                                .requestForPersonal(requestForPersonal)
                                                .comment("Заявка отправлена на согласование после редактирования")
                                                .status(requestForPersonal.getStatus())
                                                .initiator(activeUser.getId())
                                                .build();
        requestForPersonal.getHistoryItemsForPersonal().add(historyElement);
        
        return save(requestForPersonal);
    }
    
    @Override
    public Request finish(Request request, Employee activeUser) {
        throw new UnsupportedTransportTypeException(request.getTransportType());
    }
    
    @Override
    public Request complete(Request request, Employee activeUser) {
        AbstractRequestForTnP requestForTnP = (AbstractRequestForTnP) request;
        boolean allSet = true;
        for (Waypoint waypoint : requestForTnP.getWaypoints()) {
            if ((!waypoint.isCheckinAutomatic()) &&
                (!waypoint.isCheckinManual()) ||
                //Если на любой точке указана причина отсутствия, то отправляем на утверждение маршрута
                (waypoint.isCheckinManual() && StringUtils.hasText(waypoint.getAbsenceReason()))) {
                log.debug("Waypoint {} had checkinAutomatic = {} , checkinManual = {} , absenceReason={}",
                          waypoint.getId(), waypoint.isCheckinAutomatic(), waypoint.isCheckinManual(),
                          waypoint.getAbsenceReason());
                allSet = false;
                break;
            }
        }
        UUID depId = activeUser.getDepartment().getId();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        DepLimit depLimit = depLimitRepository.findByDepartmentIdAndYearAndActive(depId, year, true).orElse(null);
        var position =
                positionService.get(activeUser.getPositionId())
                               .orElseThrow(() -> new EntityNotFoundException(Position.class, activeUser.getPositionId()));
        var isSelfApproved = position.isSelfApproved();
        if (depLimit != null && !isSelfApproved) {
            var isLimitOwner = depLimit.getOwnerId().equals(activeUser.getId());
            log.debug("Is employee {} is limit owner of department {}: {}", activeUser.getId(), depId, isLimitOwner);
            if (isLimitOwner) {
                Department department = departmentService.get(depId)
                                                         .orElseThrow(() -> new EntityNotFoundException(Department.class, depId));
                if (department.getParent() == null) {
                    isSelfApproved = true;
                } else {
                    var headOfParentDepartment = departmentService.get(department.getParent()).map(Department::getDepartmentHead).orElse(null);
                    if (headOfParentDepartment == null) {
                        isSelfApproved = true;
                    } else {
                        request.setApprovedBy(employeeService.getEmployee(headOfParentDepartment));
                    }
                }
            }
            
        }
        var requestForPersonal = (RequestForPersonal) request;
        var now = LocalDateTime.now(clock);
        var tripApprovalDeadline = productionCalendar.addWorkingMinutes(now, 5295, requestForPersonal.getTimeZone());
        requestForPersonal.setApprovalDeadlineState(DeadlineState.NONE);
        requestForPersonal.setTripApprovalDeadline(tripApprovalDeadline);
        
        if (allSet || isSelfApproved) {
            requestForPersonal.setTripApprovalDatetime(now);
            requestForPersonal = requestForPersonalRepository.save(requestForPersonal);
            changeState(requestForPersonal, TripRequestStatus.GENAI_CHECK, activeUser, null);
        } else {
            requestForPersonal.setTripApprovalDatetime(null);
            requestForPersonal = requestForPersonalRepository.save(requestForPersonal);
            changeState(requestForPersonal, TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL, activeUser, null);
        }
        return request;
    }
    
    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator) {
        var requestForPersonal = requestForPersonalRepository.getReferenceById(request.getId());
        if (TripRequestStatus.PERSONAL_CANCELLED == requestForPersonal.getStatus()) {
            return;
        }
        if (!request.getStatus().isCancelable()) {
            throw new IllegalArgumentException("It is impossible to cancel a request %s with the status %s".formatted(request.getHumanReadableId(),
                                                                                                                      request.getStatus().name()));
        }
        requestForPersonal.setStatus(TripRequestStatus.PERSONAL_CANCELLED);
        requestForPersonal.setStatusCode(cancelDTO.getCode());
        requestForPersonal.setFinishedTime(LocalDateTime.now(clock));
        if (TripRequestStatus.PersonalStatusCode.PERSONAL_DECLINED_BY_EXPIRATION_TIME.getCode() == cancelDTO.getCode()) {
            requestForPersonal.setApprovalDeadlineState(DeadlineState.RED);
        }
        try {
            reservationService.cancel(requestForPersonal);
            requestForPersonal.getHistoryItemsForPersonal()
                              .add(RequestHistoryElementForPersonal.builder()
                                                                   .changeDate(LocalDateTime.now(clock))
                                                                   .requestForPersonal(requestForPersonal)
                                                                   .code(cancelDTO.getCode())
                                                                   .comment("Заявка отменена по причине: " +
                                                                            cancelDTO.getReason())
                                                                   .status(requestForPersonal.getStatus())
                                                                   .initiator(initiator.getId())
                                                                   .build());
            
            requestForPersonal = requestForPersonalRepository.save(requestForPersonal);
            if (requestForPersonal.isCoopTrip()) {
                if (requestForPersonal.isSharedRideOwner()) {
                    cancelLinkedRequests(requestForPersonal, initiator);
                    requestForPersonal = requestForPersonalRepository.getReferenceById(request.getId());
                    requestForPersonal.setAdditionalSum(null);
                    requestForPersonal.setNumberPassengersJoined(null);
                    requestForPersonal = requestForPersonalRepository.save(requestForPersonal);
                } else {
                    updateNumberPassengersJoinedAndAdditionalSum(requestForPersonal.getRideId());
                }
                removeRequestFromSharedRide(requestForPersonal);
            }
            requestSender.send(requestForPersonal);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private void removeRequestFromSharedRide(RequestForPersonal requestForPersonal) {
        if (!requestForPersonal.isCoopTrip()) {
            log.warn("removeRequestFromSharedRide: Unable remove requestForPersonal from shared ride because it is not defined as coopTrip");
            return;
        }
        srmGrpcClient.cancelRequestGrpc(requestForPersonal.getId());
    }
    
    @Override
    public Request approveRequest(Request request) {
        var requestForPersonal = (RequestForPersonal) request;
        if (requestForPersonal.isCoopTrip() && !requestForPersonal.isSharedRideOwner()) {
            requestForPersonal.setStatus(TripRequestStatus.PERSONAL_APPROVED);
        } else {
            requestForPersonal.setStatus(TripRequestStatus.PERSONAL_APPROVED);
        }
        requestForPersonal.setApprovalState(ApprovalState.APPROVED);
        requestForPersonal.setApprovalDate(LocalDateTime.now(clock));
        var historyElement =
                RequestHistoryElementForPersonal.builder()
                                                .changeDate(requestForPersonal.getApprovalDate())
                                                .requestForPersonal(requestForPersonal)
                                                .comment("Заявка согласована " +
                                                         requestForPersonal.getApprovedBy().getFIO())
                                                .status(requestForPersonal.getStatus())
                                                .initiator(requestForPersonal.getApprovedBy().getId())
                                                .build();
        requestForPersonal.getHistoryItemsForPersonal().add(historyElement);
        return requestForPersonal;
    }
    
    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        var request = (RequestForPersonal) toChange;
        request = changeStateForRequest(request, newStatus, activeUser);
        cancelUnapprovedLinkedRequests(request, newStatus, activeUser);
        changeStateForLinkedRequests(request, newStatus, activeUser);
        return request;
    }
    
    private void cancelUnapprovedLinkedRequests(RequestForPersonal request, TripRequestStatus newStatus, Employee activeUser) {
        if (newStatus != TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS) {
            return;
        }
        var linkedCancelDTO = CancelDTO.builder()
                                       .reason("Отменено (водитель уже начал поездку, но заявка пассажира была не согласована)")
                                       .code(TripRequestStatus.PersonalStatusCode.PERSONAL_CANCELLED_BY_DRIVER.getCode())
                                       .build();
        getLinkedRequests(request)
                .stream()
                .filter(req -> TripRequestStatus.PERSONAL_AWAITING_APPROVAL.equals(req.getStatus()))
                .forEach(linkedRequest -> cancel(linkedRequest, linkedCancelDTO, activeUser));
    }
    
    private void ifNecessaryUpdateFinishedTime(TripRequestStatus newStatus, RequestForPersonal requestForPersonal) {
        if (newStatus == TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL ||
            newStatus == TripRequestStatus.PERSONAL_PAYMENT_DONE ||
            newStatus == TripRequestStatus.PERSONAL_PAYMENT_DECLINED) {
            requestForPersonal.setFinishedTime(LocalDateTime.now(clock));
        }
    }
    
    private void updateHistory(TripRequestStatus newStatus, Employee activeUser, RequestForPersonal requestForPersonal) {
        historyRepository.save(RequestHistoryElementForPersonal.builder()
                                                               .requestForPersonal(requestForPersonal)
                                                               .comment("Статус заявки изменен пользователем: " +
                                                                        activeUser.getFIO())
                                                               .status(newStatus)
                                                               .initiator(activeUser.getId())
                                                               .build());
    }
    
    private void ifNecessarySpendLimit(TripRequestStatus newStatus, RequestForPersonal requestForPersonal) {
        if (newStatus == TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION ||
            newStatus == TripRequestStatus.PERSONAL_TRIP_FINISHED) {
            
            int sumSpend = requestForPersonal.getExpected().getCost().intValue();
            if (requestForPersonal.isCoopTrip()) {
                sumSpend = sumSpend +
                           Optional.ofNullable(requestForPersonal.getAdditionalSum())
                                   .map(Long::intValue)
                                   .orElse(0);
            }
            reservationService
                    .spend(requestForPersonal,
                           sumSpend,
                           requestForPersonal.isCoopTrip(),
                           requestForPersonal.isDriver(),
                           TransportTypeEnum.PERSONAL,
                           0);
        }
    }
    
    private void changeStateForLinkedRequests(RequestForPersonal requestForPersonal, TripRequestStatus newStatus, Employee activeUser) {
        TripRequestStatus linkedRequestsStatus = null;
        if (suitableStatusForUpdateLinkedRequest(newStatus)) {
            linkedRequestsStatus = newStatus;
        }
        if (suitableStatusForFinishedLinkedRequest(newStatus)) {
            linkedRequestsStatus = TripRequestStatus.PERSONAL_TRIP_FINISHED;
        }
        if (linkedRequestsStatus != null) {
            TripRequestStatus newLinkedRequestsStatus = linkedRequestsStatus;
            getLinkedRequests(requestForPersonal)
                    .stream()
                    .filter(req -> req.getStatus() != null && !req.getStatus().isTerminal())
                    .forEach(linkedRequest -> changeStateForRequest(linkedRequest, newLinkedRequestsStatus, activeUser));
        }
    }
    
    private boolean suitableStatusForFinishedLinkedRequest(TripRequestStatus newStatus) {
        return (newStatus != null && suitableStatusesForFinishedLinkedRequest.contains(newStatus));
    }
    
    private boolean suitableStatusForUpdateLinkedRequest(TripRequestStatus newStatus) {
        return (newStatus != null && suitableStatusesForUpdateLinkedRequest.contains(newStatus));
    }
    
    private RequestForPersonal changeStateForRequest(RequestForPersonal request, TripRequestStatus newStatus, Employee activeUser) {
        canStatusChange(request, newStatus);
        request.setStatus(newStatus);
        ifNecessaryUpdateFinishedTime(newStatus, request);
        ifNecessaryUpdatePaymentDoneDeadline(newStatus, request);
        ifNecessarySpendLimit(newStatus, request);
        request = requestForPersonalRepository.save(request);
        updateHistory(newStatus, activeUser, request);
        requestSender.send(request);
        if (TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.equals(newStatus)) {
            var changeAt = requestForPersonalHistoryRepository.getAllByRequestForPersonalIdOrderByChangeDate(request.getId()).stream()
                    .filter(it -> TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.equals(it.getStatus()))
                    .findFirst()
                    .map(RequestHistoryElementForPersonal::getChangeDate)
                    .orElse(null);
            requestPayoutSender.send(requestMapper.requestToRequestPayoutMessage(request, changeAt));
        }
        return request;
    }
    
    private void ifNecessaryUpdatePaymentDoneDeadline(TripRequestStatus newStatus, RequestForPersonal request) {
        if (newStatus == TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION) {
            if (request.getOrderPaymentFormationStartDate() == null) {
                request.setOrderPaymentFormationStartDate(LocalDateTime.now(clock));
            }
            if (request.getPaymentDoneDeadline() == null) {
                request.setPaymentDoneDeadline(
                        paymentDoneDeadlineCalculator.getPaymentDoneDeadline(request.getOrderPaymentFormationStartDate(),
                                                                             request.getTimeZone()));
            }
        }
    }
    
    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        if (request == null) {
            log.debug("В метод обработки утверждения заявки метод approveFinalTrip( ) передан параметр request = null");
            return;
        }
        log.debug("Начало обработки утверждения заявки - метод approveFinalTrip( ); humanReadableId = {}, actorEmployeeId = {}",
                  request.getHumanReadableId(),
                  actorEmployeeId);
        
        if (request.getStatus() != TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL) {
            log.warn("Skip approve final trip because request has status: " + request.getStatus());
            return;
        }
        if (actorEmployeeId == null) {
            throw new NullPointerException(String.format("При утверждении маршрута заявки %s не был указан id сотрудника, утвердившего маршрут",
                                                         request.getHumanReadableId()));
        }
        var actorEmployee = employeeService.get(actorEmployeeId).orElseThrow(
                () -> new StatusChangeException(
                        String.format(
                                "При утверждении маршрута заявки %s был указан id = %s сотрудника, утвердившего маршрут, но он не был найден в базе данных",
                                request.getHumanReadableId(),
                                actorEmployeeId)));
        
        changeState(request, TripRequestStatus.GENAI_CHECK, actorEmployee, null);
        
        log.debug("Начало обработки утверждения заявки - метод approveFinalTrip( ); humanReadableId = {}",
                  request.getHumanReadableId());
    }
    
    @Override
    public void markSlaExpired(UUID requestId) {
        var request = requestForPersonalRepository.findById(requestId)
                                                  .orElseThrow(
                                                          () -> new EntityNotFoundException(Request.class, requestId));
        request.setSlaExpired(true);
        requestSender.send(request);
        requestForPersonalRepository.save(request);
    }
    
    public void approveSharedRide(UUID addRequestId) {
        var request = requestForPersonalRepository.findById(addRequestId)
                                                  .orElseThrow(
                                                          () -> new EntityNotFoundException(Request.class, addRequestId));
        
        if (request.getStatus() != TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL) {
            log.warn("Skip approve shared ride because request has status: " + request.getStatus());
            return;
        }
        request.setStatus(TripRequestStatus.PERSONAL_APPROVED);
        requestForPersonalRepository.save(request);
    }
    
    public void declineSharedRide(UUID addRequestId) {
        var request = requestForPersonalRepository.findById(addRequestId)
                                                  .orElseThrow(
                                                          () -> new EntityNotFoundException(Request.class, addRequestId));
        if (TransportTypeEnum.PERSONAL.equals(request.getTransportType())
            && (request.isCoopTrip() && request.getRideId() != null)) {
            request.setStatusCode(TripRequestStatus.PersonalStatusCode.PERSONAL_CANCELLED_BY_EMPLOYEE.getCode());
        }
        
        removeRequestFromSharedRide(request);
        request.setStatus(TripRequestStatus.PERSONAL_SHARED_RIDE_DECLINED);
        requestForPersonalRepository.save(request);
    }
    
    /**
     * Отмена поездок в составе текущей если она совместная и изначальная.
     *
     * @param request текущая поездка.
     * @param initiator инициатор.
     */
    private void cancelLinkedRequests(RequestForPersonal request, Employee initiator) {
        List<RequestForPersonal> linkedRequests = getLinkedRequests(request);
        var linkedCancelDTO = CancelDTO.builder()
                                       .reason(TripRequestStatus.PersonalStatusCode.PERSONAL_CANCELLED_BY_DRIVER.getDescription())
                                       .code(TripRequestStatus.PersonalStatusCode.PERSONAL_CANCELLED_BY_DRIVER.getCode())
                                       .build();
        linkedRequests.forEach(item -> cancel(item, linkedCancelDTO, initiator));
    }
    
    private List<RequestForPersonal> getLinkedRequests(RequestForPersonal request) {
        List<RequestForPersonal> linkedRequests = new ArrayList<>();
        if (request.isCoopTrip() && request.getRideId() != null && request.isSharedRideOwner()) {
            linkedRequests = requestForPersonalRepository.findByRideId(request.getRideId())
                                                         .stream()
                                                         .filter(r -> !r.getId().equals(request.getId()) && !r.isSharedRideOwner())
                                                         .toList();
        }
        return linkedRequests;
    }
    
    public void updateNumberPassengersJoinedAndAdditionalSum(UUID rideId) {
        if (rideId == null) {
            log.debug("rideId is null => NumberPassengersJoined/AdditionalSum will not update");
            return;
        }
        
        var requestsByRide = requestForPersonalRepository.findByRideId(rideId);
        
        RequestForPersonal driverRequest = requestsByRide.stream()
                                                         .filter(AbstractRequestForTnPnC::isSharedRideOwner)
                                                         .findFirst()
                                                         .orElse(null);
        if (driverRequest == null) {
            log.error("Для rideId = {} не удалось найти заявку инициатора (sharedRideOwner = true). Пересчет доплаты и кол-ва присоединившихся " +
                      "пассажиров не возможен", rideId);
            return;
        }
        
        int numberPassengersJoined = (int) requestsByRide
                .stream()
                .filter(requestForPersonal ->
                                !TripRequestStatus.PERSONAL_SHARED_RIDE_DECLINED.equals(requestForPersonal.getStatus()) &&
                                !TripRequestStatus.PERSONAL_CANCELLED.equals(requestForPersonal.getStatus()) &&
                                !requestForPersonal.isSharedRideOwner())
                .count();
        
        long additionalSum = getAdditionalSum(numberPassengersJoined);
        
        if ((driverRequest.getAdditionalSum() == null ||
             driverRequest.getNumberPassengersJoined() == null ||
             !driverRequest.getAdditionalSum().equals(additionalSum) ||
             !driverRequest.getNumberPassengersJoined().equals(numberPassengersJoined))) {
            driverRequest.setAdditionalSum(additionalSum);
            driverRequest.setAdditionalSumReason("Доплата за совместную поездку");
            driverRequest.setNumberPassengersJoined(numberPassengersJoined);
            driverRequest = requestForPersonalRepository.save(driverRequest);
            log.debug("Для rideId = {} в заявке инициатора {} пересчитана доплата за присоединившихся пассажиров: сумма доплаты = {} копеек, кол-во" +
                      " присоединившихся = {}", rideId, driverRequest.getHumanReadableId(), driverRequest.getAdditionalSum(),
                      driverRequest.getNumberPassengersJoined());
            requestSender.send(driverRequest);
        }
    }
    
    private long getAdditionalSum(int numberPassengersJoined) {
        return numberPassengersJoined * getAdditionalSumPerPassenger();
    }
    
    private long getAdditionalSumPerPassenger() {
        log.debug("Сумма доплаты за пассажира в тарифах пока не реализована. Используем размер доплаты по умолчанию: request_for_personal" +
                  ".additionalSumForDriver = {} ", additionalSumForDriver);
        return additionalSumForDriver;
    }
    
    private void canStatusChange(@NonNull Request request, @NonNull TripRequestStatus newStatus) {
        if (!request.getStatus().isEditable() && newStatus.ordinal() < request.getStatus().ordinal()) {
            throw new StatusChangeException("Невозможно изменить статус %s на %s для заявки %s"
                                                    .formatted(request.getStatus().getDescription(), newStatus.getDescription(),
                                                               request.getHumanReadableId()));
        }
    }
}
