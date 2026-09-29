package ru.sberbank.ditsib.transport.request.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.config.AllPointsMaxWaitTimeProperties;
import ru.sberbank.ditsib.transport.request.exceptions.InvalidDesireDateException;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.SENT_TO_CONTRACTOR;
import static ru.sberbank.ditsib.transport.request.database.model.FraudType.TAXI_WAITING_TIME;

@Slf4j
@Service
@Transactional
public class RequestForTaxiServiceImpl extends AbstractTransportTypeService<RequestForTaxi> {

    @Value("${request.triggerTime:60}")
    private Integer triggerTime;

    @Value("${request.bus.duration.minimal.hours:12}")
    private Integer minimalDurationHours;

    private final AllPointsMaxWaitTimeProperties maxWaitTimeProperties;

    private final EmployeeService employeeService;
    private final TripPurposeRepository tripPurposeRepository;
    private final DepartmentService departmentService;
    private final SQGenerator sqGenerator;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final OrganizationService organizationService;
    private final AddressRepository addressRepository;
    private final GeoDataProcessingService geoDataProcessingService;
    private final MagentaAuxilaryService magentaAuxilaryService;
    private final ReservationService reservationService;
    private final RequestHistoryRepository historyRepository;
    private final RequestSender<RequestForTaxi> requestSender;
    private final TaxiTripSender taxiTripSender;
    private final CoopTaxiTripRepository coopTaxiTripRepository;
    private final SingleTaxiTripRepository singleTaxiTripRepository;
    private final Clock clock;
    private final TaxiTariffService taxiTariffService;
    private final SrmGrpcClient srmGrpcClient;
    private final RequestRepository requestRepository;
    private final PublishTripService publishTripService;
    private final TransactionTemplate transactionTemplate;

    private static final List<TaxiClass> BUS_CLASSES = List.of(TaxiClass.VIP_BUS, TaxiClass.SMALL_BUS, TaxiClass.MIDDLE_BUS, TaxiClass.LARGE_BUS);
    private static final Map<InboundTaxiTripStatus, Integer> STATUS_CODE_MAPPING = Map.of(
            InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT, TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EMPLOYEE.getCode(),
            InboundTaxiTripStatus.ORDER_CANCELLED_BY_DRIVER, TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_DRIVER.getCode(),
            InboundTaxiTripStatus.ORDER_EXPIRED, TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EXPIRATION_TIME.getCode()
    );

    private final ApprovalDeadlineCalculator approvalDeadlineCalculator;
    private final Executor fraudAsyncExecutor;

    @Autowired
    public RequestForTaxiServiceImpl(
            EmployeeService employeeService,
            TripPurposeRepository tripPurposeRepository,
            DepartmentService departmentService,
            SQGenerator sqGenerator,
            RequestForTaxiRepository requestForTaxiRepository,
            OrganizationService organizationService,
            EntityDTOMapper mapper,
            AddressRepository addressRepository,
            GeoDataProcessingService geoDataProcessingService,
            MagentaAuxilaryService magentaAuxilaryService,
            ReservationService reservationService,
            RequestHistoryRepository historyRepository,
            RequestSender<RequestForTaxi> requestSender,
            TaxiTripSender taxiTripSender,
            CheckinSettingsService checkinSettingsService,
            RegionDataResolver regionDataResolver,
            CoopTaxiTripRepository coopTaxiTripRepository,
            SingleTaxiTripRepository singleTaxiTripRepository,
            Clock clock,
            TaxiTariffService taxiTariffService,
            SrmGrpcClient srmGrpcClient,
            RequestRepository requestRepository,
            PublishTripService publishTripService,
            TransactionTemplate transactionTemplate,
            EasupGrpcService easupGrpcService,
            FraudService fraudService,
            RequestChecksGrpcService requestChecksGrpcService,
            DurationRequestCheckGrpcClient durationRequestCheckGrpcClient,
            OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient,
            FraudMonitoringService fraudMonitoringService,
            Executor fraudAsyncExecutor,
            AllPointsMaxWaitTimeProperties maxWaitTimeProperties
    ) {
        super(requestForTaxiRepository, checkinSettingsService, regionDataResolver,
                mapper, easupGrpcService, fraudService, requestChecksGrpcService, durationRequestCheckGrpcClient, overrunRequestCheckGrpcClient, fraudMonitoringService);
        this.tripPurposeRepository = tripPurposeRepository;
        this.departmentService = departmentService;
        this.sqGenerator = sqGenerator;
        this.requestForTaxiRepository = requestForTaxiRepository;
        this.organizationService = organizationService;
        this.addressRepository = addressRepository;
        this.employeeService = employeeService;
        this.geoDataProcessingService = geoDataProcessingService;
        this.magentaAuxilaryService = magentaAuxilaryService;
        this.reservationService = reservationService;
        this.historyRepository = historyRepository;
        this.requestSender = requestSender;
        this.taxiTripSender = taxiTripSender;
        this.coopTaxiTripRepository = coopTaxiTripRepository;
        this.singleTaxiTripRepository = singleTaxiTripRepository;
        this.clock = clock;
        this.taxiTariffService = taxiTariffService;
        this.srmGrpcClient = srmGrpcClient;
        this.requestRepository = requestRepository;
        this.publishTripService = publishTripService;
        this.transactionTemplate = transactionTemplate;
        transportType = TransportTypeEnum.TAXI;
        this.approvalDeadlineCalculator = new TaxiApprovalDeadlineCalculatorImpl();
        this.fraudAsyncExecutor = fraudAsyncExecutor;
        this.maxWaitTimeProperties = maxWaitTimeProperties;
    }

    @Override
    public Optional<RequestForTaxi> get(UUID id) {
        return requestForTaxiRepository.findById(id);
    }

    @Override
    public Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroup) {
        log.debug("add(): sharedRideId = {}, coop = {}, data = {}, employee = {}", sharedRideId, coop, data, employee.getId());
        final var now = LocalDateTime.now(clock);
        final var author = employeeService.get(employee.getId())
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, employee.getId()));

        final var organization = departmentService.get(author.getDepartment().getId())
                .map(Department::getOrganization).map(Organization::getId).flatMap(organizationService::get)
                .orElseThrow();
        final var organizationDigitId = organization.getDigitId();

        final var organizationId = organization.getId();

        final var humanReadableId = sqGenerator.getNextId(Prefix.OT, organizationDigitId);
        data.setDesiredDate(data.getDesiredDate() == null ||
                Duration.between(now, data.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0 ?
                now.plusMinutes(5) :
                data.getDesiredDate());

        validateDesireDate(data.getTaxiClass(), data.getDesiredDate(), now);

        final var tariff = taxiTariffService.getTariffById(data.getTariffId());
        final var outcomeTariff = taxiTariffService.getTariffById(data.getOutcomeTariffId());
        final var creationTime = LocalDateTime.now(clock);
        final var desiredDate = data.getDesiredDate();
        final var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(creationTime, desiredDate, data.getTimeZone());

        var requestForTaxi = RequestForTaxi.builder()
                .author(author)
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
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL)
                .approvalState(ApprovalState.AWAITING_APPROVAL)
                .creationTime(creationTime)
                .desiredDate(desiredDate)
                .autoCancelDeadlineMin(
                        data.getAutoCancelDeadlineMin() == null ? triggerTime : data.getAutoCancelDeadlineMin())
                .requestOptions(data.getRequestOptions())
                .contractorId(outcomeTariff.getContractorId())
                .triggerTime(tariff.getTriggerTime())
                .busCount(data.getBusCount())
                .busRentDuration(data.getBusRentDuration())
                .organizationId(organizationId)
                .requestPrice(data.getRequestPrice())
                .employeeDeviceTimeZone(data.getEmployeeDeviceTimeZone())
                .approvalDeadline(approvalDeadline)
                .approvalDeadlineState(DeadlineState.NONE)
                .joinedPassengerIds(data.getJoinedPassengerIds())
                .source(data.getSource() == null ? RequestSourceEnum.UNDEFINED : data.getSource())
                .minTariffTaxi(data.getMinTariffTaxi())
                .commentForPurpose(data.getCommentForPurpose())
                .executorGroupId(executorGroup.getId())
                .executorGroupName(executorGroup.getName())
                .transportType(data.getTransportType())
                .commentForDriver(data.getCommentForDriver())
                .passengerCount(data.getPassengerCount())
                .taxiClass(data.getTaxiClass())
                .coopTrip(coop)
                .build();
        requestForTaxi.getHistoryItemsForTaxi()
                .add(RequestHistoryElementForTaxi.builder()
                        .changeDate(requestForTaxi.getCreationTime())
                        .requestForTaxi(requestForTaxi)
                        .comment("Заявка зарегистрирована")
                        .code(requestForTaxi.getStatusCode())
                        .status(requestForTaxi.getStatus())
                        .initiator(requestForTaxi.getAuthor().getId())
                        .build());
        var isAbsent = checkOnAbsence(requestForTaxi, author, Optional.empty());
        enrichWaypointsWithRadius(requestForTaxi, TransportTypeEnum.TAXI);
        try {
            geoDataProcessingService.saveAddresses(requestForTaxi.getWaypoints(), requestForTaxi.getPassenger());
            requestForTaxi = requestForTaxiRepository.save(requestForTaxi);
            if (coop) {
                processCoopRequest(sharedRideId, token, requestForTaxi);
            }
            requestForTaxi = requestForTaxiRepository.save(requestForTaxi);
            reservationService.makeReservation(requestForTaxi,
                    requestForTaxi.getPassenger(),
                    requestForTaxi.getExpected().getCost(),
                    requestForTaxi.getExpected().getBonusCost());
            final var savedRequest = requestForTaxiRepository.save(requestForTaxi);
            if (isAbsent) {
                createFraud(savedRequest, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);
            }
            checkDurationLimit(requestForTaxi, Optional.empty());
            checkMultipointLimit(requestForTaxi, Optional.empty());
            checkOverrunLimit(requestForTaxi, Optional.empty());
            checkSingleTripDuration(requestForTaxi);
            historyRepository.flush();
            requestForTaxi.getHistoryItemsForTaxi().sort(Comparator.comparing(RequestHistoryElementForTaxi::getChangeDate));
            log.debug("RequestForTaxiService: for request with id {} desiredDate2 = {}", savedRequest.getId(), savedRequest.getDesiredDate());
        } catch (Exception e) {
            log.debug("Словили ошибку...");
            try {
                log.error("Произошла ошибка при создании заявки id={}, humanreadableid={}",
                        requestForTaxi.getId(), requestForTaxi.getHumanReadableId());
                removeRequestFromSharedRide(requestForTaxi);
                log.debug("Отправлен запрос в SRM на исключение заявки из совместной поездки, id = {}", requestForTaxi.getId());
            } catch (Exception ignore) {
                // ignore
            }
            log.error(e.getMessage(), e);
            throw e;
        }
        addressRepository.flush();
        updateNumberPassengersJoined(sharedRideId);
        RequestForTaxi finalRequestForTaxi = requestForTaxi;
        CompletableFuture.runAsync(() -> checkTotalWaitTime(finalRequestForTaxi), fraudAsyncExecutor);
        return requestForTaxi;
    }

    public void updateNumberPassengersJoined(UUID rideId) {
        if (rideId == null) {
            log.debug("rideId is null => NumberPassengersJoined will not update");
            return;
        }
        var requestsByRide = requestForTaxiRepository.findByRideId(rideId);
        RequestForTaxi ownerRequest = requestsByRide.stream()
                .filter(AbstractRequestForTnPnC::isSharedRideOwner)
                .findFirst()
                .orElse(null);
        if (ownerRequest == null) {
            log.error("Для rideId = {} не удалось найти заявку инициатора (sharedRideOwner = true). Пересчет кол-ва присоединившихся " +
                    "пассажиров (заявок) не возможен", rideId);
            return;
        }
        int numberPassengersJoined = (int) requestsByRide.stream()
                .filter(requestForPersonal ->
                        !TripRequestStatus.TAXI_CANCELLED.equals(requestForPersonal.getStatus()) &&
                                !requestForPersonal.isSharedRideOwner())
                .count();
        if (ownerRequest.getNumberPassengersJoined() == null || !ownerRequest.getNumberPassengersJoined().equals(numberPassengersJoined)) {
            ownerRequest.setNumberPassengersJoined(numberPassengersJoined);
            requestForTaxiRepository.save(ownerRequest);
            log.debug("Для rideId = {} в заявке инициатора {} пересчитана кол-во присоединившихся пассажиров (заявок) = {}",
                    rideId, ownerRequest.getHumanReadableId(), ownerRequest.getNumberPassengersJoined());
            requestSender.send(ownerRequest);
        }
    }

    private void validateDesireDate(TaxiClass taxiClass, LocalDateTime desiredDate, LocalDateTime now) {
        LocalDateTime minimalDateOrder = now.plusHours(minimalDurationHours);
        if (BUS_CLASSES.contains(taxiClass) && desiredDate.isBefore(minimalDateOrder)) {
            throw new InvalidDesireDateException(
                    String.format("Плановая дата заказа автобуса должна отстоять от текущей более чем на %s (час)", minimalDurationHours)
            );
        }
    }

    private void processCoopRequest(UUID sharedRideId, String token, RequestForTaxi requestForTaxi) {
        try {
            var srmSharedRideDTO = magentaAuxilaryService.processCoopRequest(sharedRideId, requestForTaxi, token);
            if (srmSharedRideDTO == null) {
                var resolution = "Заявка преобразована в одиночную по причине возникновения бизнес ошибки на SRM Magenta при создании совместной заявки";
                log.info("ТАКСИ: {}", resolution);
                requestForTaxi.setCoopTrip(false);
                requestForTaxi.setResolution(resolution);
            }
        } catch (ResponseStatusException e) {
            var resolution = "Заявка преобразована в одиночную по причине возникновения системной ошибки на SRM Magenta при создании совместной заявки";
            log.error("ТАКСИ: {}. {}", resolution, e.getMessage(), e);
            requestForTaxi.setCoopTrip(false);
            requestForTaxi.setResolution(resolution);
        }
    }

    @Override
    public Request update(RequestDTO newData, Employee activeUser, Request request, String token) {
        var requestForTaxi = (RequestForTaxi) request;
        if (requestForTaxi.isCoopTrip()) {
            throw new IllegalStateResponseException(
                    "Request for shared ride cannot be edited. Cancel is the only " +
                            "option");
        }
        if (requestForTaxi.getExpected().getCost() != newData.getExpected().getCost()) {
            reservationService.makeReservation(requestForTaxi,
                    requestForTaxi.getPassenger(),
                    newData.getExpected().getCost(),
                    newData.getExpected().getBonusCost());
        }
        requestForTaxi.setApprovalDate(null);
        var now = LocalDateTime.now(clock);
        newData.setDesiredDate(newData.getDesiredDate() == null ||
                Duration.between(now, newData.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0
                ?
                now.plusMinutes(5)
                :
                newData.getDesiredDate());
        requestForTaxi.setDesiredDate(newData.getDesiredDate());
        requestForTaxi.setPassenger(employeeService.get(newData.getPassenger().id())
                .orElseThrow(
                        () -> new EntityNotFoundException(Employee.class, newData.getPassenger().id())));
        requestForTaxi.setPurpose(tripPurposeRepository.findById(newData.getPurpose().getId()).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, newData.getPurpose().getId())));
        TaxiTariff taxiTariff = taxiTariffService.getTariffById(newData.getTariffId());
        var outcomeTariff = taxiTariffService.getTariffById(newData.getOutcomeTariffId());

        requestForTaxi.setTariffId(newData.getTariffId());
        requestForTaxi.setTariff(taxiTariff);
        requestForTaxi.setOutcomeTariff(outcomeTariff);
        requestForTaxi.setOutcomeTariffId(newData.getOutcomeTariffId());
        requestForTaxi.setTriggerTime(taxiTariff.getTriggerTime());
        requestForTaxi.setExpected(mapper.dtoToExpectedData(newData.getExpected()));
        requestForTaxi.getSegmentsJSON().clear();
        requestForTaxi.getSegmentsJSON().addAll(newData.getExpected().getSegments());
        requestForTaxi.setRequestOptions(newData.getRequestOptions());
        // Автора менять нельзя
        requestForTaxi.setCommentForDriver(newData.getCommentForDriver());

        requestForTaxi.setTaxiClass(newData.getTaxiClass());
        requestForTaxi.setPassengerCount(newData.getPassengerCount());
        save(requestForTaxi);
        geoDataProcessingService.saveAddresses(requestForTaxi, mapper.waypointDTOListToWaypointList(newData.getExpected().getWaypoints()));
        requestForTaxi.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi.builder()
                .changeDate(LocalDateTime.now(clock))
                .requestForTaxi(requestForTaxi)
                .comment("Заявка отправлена на согласование после редактирования")
                .status(requestForTaxi.getStatus())
                .initiator(activeUser.getId())
                .build());

        requestSender.send(requestForTaxi);
        return save(requestForTaxi);
    }

    @Override
    public Request finish(Request request, Employee activeUser) {
        var requestForTaxi = (RequestForTaxi) request;
        requestForTaxi.setFinishedTime(LocalDateTime.now(clock));
        requestForTaxi.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        var finished = saveRequestOutOfTransaction(requestForTaxi);
        historyRepository.save(RequestHistoryElementForTaxi.builder()
                .requestForTaxi(requestForTaxi)
                .code(finished.getStatusCode())
                .comment("Заявка принудительно завершена пользователем " + activeUser.getFIO())
                .status(finished.getStatus())
                .initiator(activeUser.getId())
                .build());
        requestSender.send(finished);
        reservationService.spend(requestForTaxi, requestForTaxi.getExpected().getCost().intValue(), requestForTaxi.isCoopTrip(), false,
                TransportTypeEnum.TAXI, 0);
        return finished;
    }

    @Override
    public Request complete(Request request, Employee activeUser) {
        throw new UnsupportedTransportTypeException(request.getTransportType());
    }


    private void cancelSingle(RequestForTaxi requestToCancel, CancelDTO cancelDTO, Employee initiator) {

        boolean sendToTrip = requestToCancel.getTaxiTrip() == null
                || !requestToCancel.getTaxiTrip().getStatus().equals(SENT_TO_CONTRACTOR);

        if (TripRequestStatus.TAXI_CANCELLED == requestToCancel.getStatus()) {
            return;
        }
        if (TripRequestStatus.TAXI_DRIVER_ARRIVED.equals(requestToCancel.getStatus())) {
            spendMinRideCost(requestToCancel, requestToCancel.getPassenger());

        } else if (TripRequestStatus.TAXI_TRIP_IN_PROGRESS.equals(requestToCancel.getStatus())) {
            reservationService.spend(requestToCancel, requestToCancel.getExpected().getCost().intValue(), requestToCancel.isCoopTrip(), false,
                    TransportTypeEnum.TAXI, 0);
        } else {
            reservationService.cancel(requestToCancel);
        }

        requestToCancel.setStatus(TripRequestStatus.TAXI_CANCELLED);
        requestToCancel.setStatusCode(cancelDTO.getCode());
        requestToCancel.setRequestClosedDatetime(LocalDateTime.now(clock));
        if (TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EXPIRATION_TIME.getCode() == cancelDTO.getCode()) {
            requestToCancel.setApprovalDeadlineState(DeadlineState.RED);
        }
        requestToCancel.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi.builder()
                .changeDate(LocalDateTime.now(clock))
                .requestForTaxi(requestToCancel)
                .code(cancelDTO.getCode())
                .comment("Заявка отменена по причине: " + cancelDTO.getReason())
                .status(requestToCancel.getStatus())
                .initiator(initiator.getId())
                .build());
        requestForTaxiRepository.save(requestToCancel);
        //У исполнителя можем отменить только по присвоенному в чужой системе идентификатору
        if (requestToCancel.isSentToContractor() && requestToCancel.getTaxiTrip() != null && sendToTrip) {
            var taxiTrip = singleTaxiTripRepository.findById(requestToCancel.getTaxiTrip().getId()).orElse(null);
            if (taxiTrip == null) {
                log.error("taxi trip with id {} for request for taxi with id {} was not found",
                        requestToCancel.getTaxiTrip().getId(),
                        requestToCancel.getId());
            } else {
                taxiTrip.setStatus(ORDER_CANCELLED_BY_CLIENT);
                singleTaxiTripRepository.save(taxiTrip);
                log.info("Trying to send taxiTrip {}", taxiTrip.getHumanReadableId());
                taxiTripSender.send(taxiTrip);
                var taxiTariff = taxiTariffService.getOptionalById(getFirstRequestFromSingleTaxiTrip(taxiTrip).getOutcomeTariffId()).orElse(null);
                publishTripService.publishSingleTrip(taxiTrip, getFirstRequestFromSingleTaxiTrip(taxiTrip), taxiTariff);
                log.info("Successfully sent taxiTrip {}", taxiTrip.getHumanReadableId());
            }
        }
        log.debug("Trying to send requestForTaxi {}", requestToCancel.getHumanReadableId());
        requestSender.send(requestToCancel);
        log.debug("Successfully sent requestForTaxi {}", requestToCancel.getHumanReadableId());
    }

    private RequestForTaxi getFirstRequestFromSingleTaxiTrip(SingleTaxiTrip taxiTrip) {
        if (taxiTrip.getRequests() == null || taxiTrip.getRequests().isEmpty()) {
            return null;
        }
        return taxiTrip.getRequests().getFirst();
    }

    private void cancelCoop(RequestForTaxi requestToCancel, CancelDTO cancelDTO, Employee initiator) {

        boolean sentToContractor = requestToCancel.isSentToContractor();
        List<RequestForTaxi> cancelList = new ArrayList<>();
        if (sentToContractor && (requestToCancel.getRideId() != null)) {
            var activeRequests = requestForTaxiRepository.findActiveByRideId(requestToCancel.getRideId());
            cancelList.addAll(activeRequests);
        } else {
            cancelList.add(requestToCancel);
        }
        try {
            cancelList.forEach(requestForTaxi -> {

                if (TripRequestStatus.TAXI_CANCELLED == requestForTaxi.getStatus()) {
                    return;
                }

                if (TripRequestStatus.TAXI_DRIVER_ARRIVED.equals(requestToCancel.getStatus()) &&
                        requestToCancel.getId().equals(requestForTaxi.getId())) {
                    spendMinRideCost(requestToCancel, requestToCancel.getPassenger());
                } else if (TripRequestStatus.TAXI_TRIP_IN_PROGRESS.equals(requestToCancel.getStatus())) {
                    reservationService.spend(requestForTaxi, requestForTaxi.getExpected().getCost().intValue(), requestForTaxi.isCoopTrip(), false,
                            TransportTypeEnum.TAXI, 0);
                } else {
                    reservationService.cancel(requestToCancel);
                }

                requestForTaxi.setStatus(TripRequestStatus.TAXI_CANCELLED);
                requestForTaxi.setStatusCode(cancelDTO.getCode());
                if (TripRequestStatus.TaxiStatusCode.TAXI_CANCELLED_BY_EXPIRATION_TIME.getCode() == cancelDTO.getCode()) {
                    requestForTaxi.setApprovalDeadlineState(DeadlineState.RED);
                }
                requestForTaxi.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi.builder()
                        .changeDate(LocalDateTime.now(clock))
                        .requestForTaxi(requestForTaxi)
                        .code(cancelDTO.getCode())
                        .comment("Заявка отменена по причине: " + cancelDTO.getReason())
                        .status(requestForTaxi.getStatus())
                        .initiator(initiator.getId())
                        .build());
                requestForTaxiRepository.save(requestForTaxi);
                log.debug("cancelCoop: removing requestForTaxi {} from shared ride", requestToCancel.getHumanReadableId());
                if (requestForTaxi.isCoopTrip()) {
                    if (requestForTaxi.isSharedRideOwner()) {
                        cancelLinkedRequests(requestForTaxi, cancelDTO, initiator);
                        requestForTaxi = requestForTaxiRepository.getReferenceById(requestForTaxi.getId());
                        requestForTaxi.setNumberPassengersJoined(null);
                        requestForTaxi = requestForTaxiRepository.save(requestForTaxi);
                    } else {
                        updateNumberPassengersJoined(requestForTaxi.getRideId());
                    }
                    removeRequestFromSharedRide(requestForTaxi);
                }
                log.debug("cancelCoop: removed requestForTaxi {} from shared ride", requestForTaxi.getHumanReadableId());
            });
            CoopTaxiTrip coopTaxiTrip;
            //Для отправленных на исполнение заявок точно существует связанная CoopTaxiTrip, отправим заявку на отмену и обновим информацию в кафке
            if (sentToContractor) {
                coopTaxiTrip = coopTaxiTripRepository.findById(requestToCancel.getTaxiTrip().getId()).orElseThrow(
                        () -> new EntityNotFoundException(CoopTaxiTrip.class, requestToCancel.getTaxiTrip().getId()));
                coopTaxiTrip.setStatus(ORDER_CANCELLED_BY_CLIENT);
                taxiTripSender.send(coopTaxiTrip);
                var taxiTariff = taxiTariffService.getOptionalById(coopTaxiTrip.getOutcomeTariffId()).orElse(null);
                var activeRequests = requestForTaxiRepository.findActiveByRideId(coopTaxiTrip.getRideId()); //TODO: change to trip.getRequests()
                publishTripService.publishCoopTrip(coopTaxiTrip, activeRequests, taxiTariff);
                coopTaxiTripRepository.save(coopTaxiTrip);
            }
            cancelList.forEach(requestSender::send);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void spendMinRideCost(RequestForTaxi requestToCancel, Employee initiator) {
        var tariff = taxiTariffService.getTariffById(requestToCancel.getTariffId());
        if (tariff.getMinRideDistanceCost() == null) {
            throw new IllegalArgumentException("MinRideDistanceCost in tariff %s is null".formatted(tariff.getId()));
        }
        spend(requestToCancel, initiator, tariff.getMinRideDistanceCost());
    }

    private void spend(RequestForTaxi requestToCancel, Employee initiator, int sum) {
        reservationService.makeReservation(requestToCancel, initiator, sum, 0L);
        reservationService.spend(requestToCancel, sum, TransportTypeEnum.TAXI);
    }

    @Transactional
    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator) {
        var requestToCancel = requestForTaxiRepository.getReferenceById(request.getId());
        if (!request.getStatus().isCancelable()) {
            throw new IllegalArgumentException("It is impossible to cancel a request %s with the status %s".formatted(request.getHumanReadableId(),
                    request.getStatus().name()));
        }

        if (requestToCancel.isCoopTrip()) {
            cancelCoop(requestToCancel, cancelDTO, initiator);
        } else {
            cancelSingle(requestToCancel, cancelDTO, initiator);
        }
    }

    @Override
    public Request approveRequest(Request request) {
        var requestForTaxi = (RequestForTaxi) request;
        requestForTaxi.setStatus(TripRequestStatus.TAXI_APPROVED);
        requestForTaxi.setApprovalState(ApprovalState.APPROVED);
        requestForTaxi.setApprovalDate(LocalDateTime.now(clock));
        requestForTaxi.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi.builder()
                .changeDate(requestForTaxi.getApprovalDate())
                .requestForTaxi(requestForTaxi)
                .comment("Заявка согласована " + requestForTaxi.getApprovedBy().getFIO())
                .status(requestForTaxi.getStatus())
                .initiator(requestForTaxi.getApprovedBy().getId())
                .build());
        setupDriverArrivedDeadline(requestForTaxi);
        return requestForTaxi;
    }

    /**
     * Расчет и установка контрольного срока прибытия водителя в точку отправления
     *
     * @param requestForTaxi - заявка на такси
     */
    public void setupDriverArrivedDeadline(RequestForTaxi requestForTaxi) {
        log.debug("setupDriverArrivedDeadline() begin");
        log.debug("requestForTaxi.getId() = {}", requestForTaxi.getId());
        log.debug("requestForTaxi.getApprovalDate() = {}", requestForTaxi.getApprovalDate());
        log.debug("requestForTaxi.getDriverArrivedDeadline() = {}", requestForTaxi.getDriverArrivedDeadline());
        log.debug("requestForTaxi.getDesiredDate() = {}", requestForTaxi.getDesiredDate());

        if (requestForTaxi.getApprovalDate() == null) {
            log.warn("Для заявки на такси с id = {} не указана дата согласования - расчет контрольного срока прибытия водителя невозможен",
                    requestForTaxi.getId());
            return;
        }
        if (requestForTaxi.getDriverArrivedDeadline() != null) {
            log.warn("Для заявки на такси с id = {} уже указан контрольный срок - перерасчет контрольного срока прибытия водителя не делается",
                    requestForTaxi.getId());
            return;
        }
        var taxiDeadlineSettings = getTaxiDeadlineSettings();

        var duration = Duration.between(requestForTaxi.getApprovalDate(), requestForTaxi.getDesiredDate()).toMinutes();


        LocalDateTime driverArrivedDeadline = (duration < taxiDeadlineSettings.getTaxiMinTriggerTime()) ?
                requestForTaxi.getApprovalDate()
                        .plusMinutes(taxiDeadlineSettings.getTaxiMinTriggerTime())
                        .plusMinutes(taxiDeadlineSettings.getTaxiDriverArrivalMaxDelay())
                :
                requestForTaxi.getDesiredDate()
                        .plusMinutes(taxiDeadlineSettings.getTaxiDriverArrivalMaxDelay());

        requestForTaxi.setDriverArrivedDeadline(driverArrivedDeadline);

        log.debug("duration = {}", duration);
        log.debug("taxiDeadlineSettings.getTaxiMinTriggerTime() = {}", taxiDeadlineSettings.getTaxiMinTriggerTime());
        log.debug("taxiDeadlineSettings.getTaxiDriverArrivalMaxDelay() = {}", taxiDeadlineSettings.getTaxiDriverArrivalMaxDelay());
        log.debug("driverArrivedDeadline = {}", driverArrivedDeadline);
        log.debug("requestForTaxi.getDriverArrivedDeadline() = {}", requestForTaxi.getDriverArrivedDeadline());
        log.debug("setupDriverArrivedDeadline() end");
    }

    private TaxiDeadlineSettings getTaxiDeadlineSettings() {
        return new TaxiDeadlineSettings();
    }

    public Optional<RequestForTaxi> findByHumanReadableId(String hrId) {
        return requestRepository.findByHumanReadableId(hrId);
    }

    static class TaxiDeadlineSettings {
        long taxiMinTriggerTime;
        long taxiDriverArrivalMaxDelay;

        TaxiDeadlineSettings() {
            taxiMinTriggerTime = 60L;
            taxiDriverArrivalMaxDelay = 15L;
        }

        long getTaxiMinTriggerTime() {
            return taxiMinTriggerTime;
        }

        long getTaxiDriverArrivalMaxDelay() {
            return taxiDriverArrivalMaxDelay;
        }
    }

    @Override
    public DeadlineState calcDriverArrivedDeadline(
            LocalDateTime now,
            LocalDateTime driverArrivedDatetime,
            LocalDateTime driverArrivedDeadline
    ) {
        if (now != null && driverArrivedDeadline != null && ((driverArrivedDatetime != null && driverArrivedDeadline.isBefore(driverArrivedDatetime)) ||
                (driverArrivedDatetime == null && driverArrivedDeadline.isBefore(now)))) {
            return DeadlineState.RED;
        }

        return DeadlineState.NONE;
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        var requestForTaxi = (RequestForTaxi) toChange;

        if (toChange.getStatus() == newStatus) {
            log.info("Статус у request не изменился. id = {}, toChange.getStatus() = {}, newStatus = {}",
                    requestForTaxi.getId(), toChange.getStatus(), newStatus);
            return requestForTaxi;
        }

        var now = LocalDateTime.now(clock);
        if (newStatus.equals(TripRequestStatus.TAXI_AWAITING_SEARCH)) {
            requestForTaxi.setTaxiAwaitingSearchStartDate(now);
        }
        // Если водитель прибыл в точку отправления позднее контрольного срока
        // или не приехал вообще, а время контрольного срока уже превышено,
        // тогда считаем это признаком нарушения контрольного срока (цвет Красный)
        if (newStatus.equals(TripRequestStatus.TAXI_DRIVER_ARRIVED) && requestForTaxi.getDriverArrivedDatetime() == null) {
            requestForTaxi.setDriverArrivedDatetime(now);
        }
        changeDefaultStatusCode(newStatus, requestForTaxi);
        requestForTaxi.setDeadlineState(calcDriverArrivedDeadline(
                now,
                requestForTaxi.getDriverArrivedDatetime(),
                requestForTaxi.getDriverArrivedDeadline()));

        requestForTaxi.setStatus(newStatus);
        requestForTaxi = saveRequestOutOfTransaction(requestForTaxi);
        historyRepository.save(RequestHistoryElementForTaxi.builder()
                .requestForTaxi(requestForTaxi)
                .comment("Статус заявки изменен пользователем: " + activeUser.getFIO())
                .status(newStatus)
                .initiator(activeUser.getId())
                .build());
        log.info("Trying to send request {}", requestForTaxi.getHumanReadableId());
        requestSender.send(requestForTaxi);
        log.info("Successfully sent request {}", requestForTaxi.getHumanReadableId());
        return requestForTaxi;
    }

    private RequestForTaxi saveRequestOutOfTransaction(RequestForTaxi requestForTaxi) {
        return transactionTemplate.execute(status -> requestForTaxiRepository.save(requestForTaxi));
    }

    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        log.error("Данная операция не существует для типа транспорта TAXI");
    }

    /**
     * Смена статус кода на "Завершена без оценки" (выполняется по истечении КС)
     *
     * @param request заявка
     * @param author  автор
     */
    @Override
    public void finishByExpiration(RequestForTaxi request, Employee author) {
        if (!request.getStatus().equals(TripRequestStatus.TAXI_TRIP_FINISHED)) {
            throwWrongRequestStatusException(request.getId());
        }
        if (request.getRequestRating() != null) {
            throwRequestAlreadyRatedException(request.getId());
        }
        TripRequestStatus.TaxiStatusCode statusCode = TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_NOT_RATED;
        var expired = requestForTaxiRepository.save(request);
        historyRepository.save(
                RequestHistoryElementForTaxi.builder()
                        .requestForTaxi(request)
                        .code(expired.getStatusCode())
                        .comment("Установлен код заявки: " + statusCode.getCode() + " " +
                                statusCode.getDescription())
                        .status(expired.getStatus())
                        .initiator(author.getId())
                        .build());
        requestSender.send(expired);
    }

    @Override
    public RequestForTaxi save(RequestForTaxi request) {
        var savedRequest = super.save(request);
        requestSender.send(savedRequest);
        return savedRequest;
    }

    /**
     * Меняем statusCode, если он был пустым, и если мы получили новый статус заявки, по которому мы можем идентифицировать код
     *
     * @param newStatus {@link TripRequestStatus}
     * @param request   {@link RequestForTaxi}
     */
    private static void changeDefaultStatusCode(TripRequestStatus newStatus, RequestForTaxi request) {
        if (TripRequestStatus.getCanceledStatuses().contains(newStatus) && request.getStatusCode() == 0) {
            request.setStatusCode(STATUS_CODE_MAPPING.getOrDefault(request.getTaxiTrip().getStatus(), 0));
        }
    }

    /**
     * Бросить исключение BAD_REQUEST - не тот статус заявки
     *
     * @param requestId ID заявки
     */
    private void throwWrongRequestStatusException(UUID requestId) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, String.format("Заявка ID '%s' не находится в нужном статусе!", requestId));
    }

    /**
     * Бросить исключение BAD_REQUEST - не тот статус код заявки
     *
     * @param requestId ID заявки
     */
    private void throwRequestAlreadyRatedException(UUID requestId) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, String.format("Заявка ID '%s' уже содержит оценку!", requestId));
    }

    /**
     * Получить заявку такси с фактическими данными по поездке.
     *
     * @param id ID заявки.
     * @return request.
     */
    public GetRequestWithFactDataDTO getWithFactData(UUID id) {
        return requestForTaxiRepository.getWithFactData(id)
                .map(mapper::requestToGetRequestWithFactDataDTO).orElse(null);
    }

    /**
     * Отмена поездок в составе текущей если она совместная и изначальная.
     *
     * @param request   текущая поездка.
     * @param cancelDTO объект отмены.
     * @param initiator инициатор.
     */
    private void cancelLinkedRequests(RequestForTaxi request, CancelDTO cancelDTO, Employee initiator) {
        if (request.isCoopTrip() && request.getRideId() != null) {
            cancelDTO.setReason("Отменена родительская заявка: " + cancelDTO.getReason());
            requestForTaxiRepository.findByRideId(request.getRideId())
                    .stream()
                    .filter(r -> !r.getId().equals(request.getId()))
                    .filter(r -> !TripRequestStatus.getCanceledStatuses().contains(r.getStatus()))
                    .filter(item -> !request.equals(item))
                    .forEach(item -> cancel(item, cancelDTO, initiator));
        }
    }

    private void removeRequestFromSharedRide(RequestForTaxi requestForTaxi) {
        if (!requestForTaxi.isCoopTrip()) {
            log.warn("removeRequestFromSharedRide: Unable remove requestForTaxi from shared ride because it is not defined as coopTrip");
            return;
        }
        srmGrpcClient.cancelRequestGrpc(requestForTaxi.getId());
    }

    /**
     * Проверка превышения суммарного времени ожидания на всех точках маршрута.
     * При превышении лимита создаётся fraud-запись с типом TAXI_WAITING_TIME.
     * <p>
     * Логика: если расстояние ниже порога — fraud всегда.
     * Если расстояние выше порога — только при одном уникальном городе.
     * <p>
     * Выполняется асинхронно, чтобы не блокировать создание заявки.
     * Маркирующая проверка.
     *
     * @param request Заявка
     */
    protected void checkTotalWaitTime(@NotNull Request request) {
        var waypoints = request.getWaypoints();
        if (waypoints == null || waypoints.isEmpty()) {
            return;
        }

        var totalWaitMinutes = waypoints.stream()
                .map(Waypoint::getWaitTime)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();

        int limitInMinutes = maxWaitTimeProperties.getLimitInMinutes();
        if (totalWaitMinutes > limitInMinutes) {
            var distance = request.getExpected().getDistance().intValue() * METER_IN_KILOMETER;
            var comment = String.format(TOTAL_WAIT_TIME_FRAUD_COMMENT, limitInMinutes);

            if (distance <= maxWaitTimeProperties.getDistanceThreshold()) {
                createFraud(request, TAXI_WAITING_TIME, comment);
            } else {
                var uniqueCities = waypoints.stream()
                        .map(Waypoint::getAddress)
                        .map(Address::getCity)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                if (uniqueCities <= 1) {
                    createFraud(request, TAXI_WAITING_TIME, comment);
                }
            }
        }
    }
}
