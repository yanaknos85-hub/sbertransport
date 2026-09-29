package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.InvalidDesireDateException;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
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
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RequestForGroupTransferServiceImpl extends AbstractTransportTypeService<RequestForGroupTransfer> {

    private final EmployeeService employeeService;
    private final TripPurposeRepository tripPurposeRepository;
    private final DepartmentService departmentService;
    private final SQGenerator sqGenerator;
    private final RequestForGroupTransferRepository requestForGroupTransferRepository;
    private final OrganizationService organizationService;
    private final AddressRepository addressRepository;
    private final GeoDataProcessingService geoDataProcessingService;
    private final RequestHistoryRepository historyRepository;
    private final RequestSender<RequestForGroupTransfer> requestSender;
    private final GroupTransferTripRepository groupTransferTripRepository;
    private final Clock clock;
    private final GroupTransferTariffRepository groupTransferTariffRepository;
    private final ApprovalDeadlineCalculator approvalDeadlineCalculator;
    private final PublishTripService publishTripService;

    private static final Map<InboundTaxiTripStatus, Integer> STATUS_CODE_MAPPING = Map.of(
            InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT, TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EMPLOYEE.getCode(),
            InboundTaxiTripStatus.ORDER_CANCELLED_BY_DRIVER, TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_DRIVER.getCode(),
            InboundTaxiTripStatus.ORDER_EXPIRED, TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EXPIRATION_TIME.getCode()
    );

    @Autowired
    public RequestForGroupTransferServiceImpl(
            EmployeeService employeeService,
            TripPurposeRepository tripPurposeRepository,
            DepartmentService departmentService,
            SQGenerator sqGenerator,
            RequestForGroupTransferRepository requestForGroupTransferRepository,
            OrganizationService organizationService,
            EntityDTOMapper mapper,
            AddressRepository addressRepository,
            GeoDataProcessingService geoDataProcessingService,
            RequestHistoryRepository historyRepository,
            RequestSender<RequestForGroupTransfer> requestSender,
            CheckinSettingsService checkinSettingsService,
            RegionDataResolver regionDataResolver,
            GroupTransferTripRepository groupTransferTripRepository,
            Clock clock,
            GroupTransferTariffRepository groupTransferTariffRepository,
            PublishTripService publishTripService,
            EasupGrpcService easupGrpcService,
            FraudService fraudService,
            DurationRequestCheckGrpcClient durationRequestCheckGrpcClient,
            RequestChecksGrpcService requestChecksGrpcService,
            OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient,
            FraudMonitoringService fraudMonitoringService
                                             ) {
        super(requestForGroupTransferRepository, checkinSettingsService, regionDataResolver,
              mapper, easupGrpcService, fraudService, requestChecksGrpcService, durationRequestCheckGrpcClient, overrunRequestCheckGrpcClient, fraudMonitoringService);
        this.tripPurposeRepository = tripPurposeRepository;
        this.departmentService = departmentService;
        this.sqGenerator = sqGenerator;
        this.requestForGroupTransferRepository = requestForGroupTransferRepository;
        this.organizationService = organizationService;
        this.addressRepository = addressRepository;
        this.employeeService = employeeService;
        this.geoDataProcessingService = geoDataProcessingService;
        this.historyRepository = historyRepository;
        this.requestSender = requestSender;
        this.clock = clock;
        this.groupTransferTripRepository = groupTransferTripRepository;
        this.groupTransferTariffRepository = groupTransferTariffRepository;
        transportType = TransportTypeEnum.GROUP_TRANSFER;
        this.approvalDeadlineCalculator = new GroupTransferApprovalDeadlineCalculatorImpl();
        this.publishTripService = publishTripService;
    }

    @Override
    public Optional<RequestForGroupTransfer> get(UUID id) {
        return requestForGroupTransferRepository.findById(id);
    }

    @Override
    public Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroupDTO) {
        final var now = LocalDateTime.now(ZoneOffset.UTC);
        final var author = employeeService.get(employee.getId()).orElseThrow(() -> new EntityNotFoundException(Employee.class, employee.getId()));

        final var authorDepartmentId = author.getDepartment().getId();
        final var organization = departmentService.get(authorDepartmentId)
                                            .map(Department::getOrganization).map(Organization::getId).flatMap(organizationService::get)
                                            .orElseThrow(() -> new EntityNotFoundException(Organization.class, Map.of("departmentId", authorDepartmentId)));
        final var organizationDigitId = organization.getDigitId();

        final var organizationId = organization.getId();

        final var humanReadableId = sqGenerator.getNextId(Prefix.OT, organizationDigitId);
        data.setDesiredDate(data.getDesiredDate() == null ||
                            Duration.between(now, data.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0 ?
                            now.plusMinutes(5) :
                            data.getDesiredDate());


        final var tariff =
                groupTransferTariffRepository.findById(data.getTariffId()).orElseThrow(() -> new EntityNotFoundException(GroupTransferTariff.class,
                                                                                                                         data.getTariffId()));
        final var outcomeTariff = groupTransferTariffRepository.getReferenceById(data.getOutcomeTariffId());

        validateDesireDate(data.getDesiredDate(), now, tariff.getMinCreateTime());

        final var desiredDate = data.getDesiredDate();
        final var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(now, desiredDate, data.getTimeZone());

        final var expected = data.getExpected();
        final var passengerId = data.getPassenger().id();

        var requestForGroupTransfer = RequestForGroupTransfer.builder()
                                                             .author(author)
                                                             .segmentsJSON(expected.getSegments())
                                                             .waypoints(mapper.waypointDTOListToWaypointList(expected.getWaypoints()))
                                                             .expected(mapper.dtoToExpectedData(expected))
                                                             .passenger(employeeService.get(passengerId).orElseThrow(() -> new EntityNotFoundException(Employee.class, passengerId)))
                                                             .purpose(tripPurposeRepository.getReferenceById(data.getPurpose().getId()))
                                                             .tariffId(data.getTariffId())
                                                             .tariff(tariff)
                                                             .outcomeTariff(outcomeTariff)
                                                             .outcomeTariffId(data.getOutcomeTariffId())
                                                             .humanReadableId(humanReadableId)
                                                             .timeZone(Optional.ofNullable(data.getTimeZone()).orElse(defaultTimezone))
                                                             .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL)
                                                             .approvalState(ApprovalState.AWAITING_APPROVAL)
                                                             .creationTime(now)
                                                             .transportType(data.getTransportType())
                                                             .desiredDate(desiredDate)
                                                             .autoCancelDeadlineMin(Optional.ofNullable(data.getAutoCancelDeadlineMin()).orElse(tariff.getTriggerTime()))
                                                             .requestOptions(data.getRequestOptions())
                                                             .information(data.getInformation())
                                                             .contractorId(outcomeTariff.getContractorId())
                                                             .triggerTime(tariff.getTriggerTime())
                                                             .busCount(data.getBusCount())
                                                             .busRentDuration(data.getBusRentDuration())
                                                             .organizationId(organizationId)
                                                             .employeeDeviceTimeZone(data.getEmployeeDeviceTimeZone())
                                                             .approvalDeadline(approvalDeadline)
                                                             .approvalDeadlineState(DeadlineState.NONE)
                                                             .vip(data.isVip())
                                                             .source(Optional.ofNullable(data.getSource()).orElse(RequestSourceEnum.UNDEFINED))
                                                             .minTariffTaxi(data.getMinTariffTaxi())
                                                             .commentForPurpose(data.getCommentForPurpose())
                                                             .executorGroupId(executorGroupDTO.getId())
                                                             .executorGroupName(executorGroupDTO.getName())
                                                             .build();
        requestForGroupTransfer.setCommentForDriver(data.getCommentForDriver());
        requestForGroupTransfer.setPassengerCount(data.getPassengerCount());
        requestForGroupTransfer.setGroupTransferClass(data.getGroupTransferClass());
        requestForGroupTransfer.getHistoryItemsForGroupTransfer()
                               .add(RequestHistoryElementForGroupTransfer.builder()
                                                                         .changeDate(requestForGroupTransfer.getCreationTime())
                                                                         .requestForGroupTransfer(requestForGroupTransfer)
                                                                         .comment("Заявка зарегистрирована")
                                                                         .code(requestForGroupTransfer.getStatusCode())
                                                                         .status(requestForGroupTransfer.getStatus())
                                                                         .initiator(requestForGroupTransfer.getAuthor().getId())
                                                                         .build());
        var isAbsent = checkOnAbsence(requestForGroupTransfer, author, Optional.empty());
        checkDurationLimit(requestForGroupTransfer, Optional.empty());
        geoDataProcessingService.saveAddresses(requestForGroupTransfer.getWaypoints(),
                                               requestForGroupTransfer.getPassenger());
        requestForGroupTransfer = requestForGroupTransferRepository.saveAndFlush(requestForGroupTransfer);
        var savedRequest = requestForGroupTransferRepository.saveAndFlush(requestForGroupTransfer);
        if (isAbsent) {
            createFraud(savedRequest, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);
        }
        checkMultipointLimit(requestForGroupTransfer, Optional.empty());
        checkOverrunLimit(requestForGroupTransfer, Optional.empty());
        historyRepository.flush();
        requestForGroupTransfer.getHistoryItemsForGroupTransfer()
                               .sort(Comparator.comparing(RequestHistoryElementForGroupTransfer::getChangeDate));
        addressRepository.flush();
        return requestForGroupTransfer;
    }

    private void validateDesireDate(LocalDateTime desiredDate, LocalDateTime now, int minCreateTime) {
        LocalDateTime minimalDateOrder = now.plusMinutes(minCreateTime);
        if (desiredDate.isBefore(minimalDateOrder)) {
            throw new InvalidDesireDateException(
                    String.format("Желаемая дата поездки должна быть не ранее, чем через %02d:%02d (час:мин)", minCreateTime / 60, minCreateTime % 60)
            );
        }
    }

    @Override
    public Request update(RequestDTO newData, Employee activeUser, Request request, String token) {
        var requestForGroupTransfer = (RequestForGroupTransfer) request;
        requestForGroupTransfer.setApprovalDate(null);
        var now = LocalDateTime.now(ZoneOffset.UTC);
        newData.setDesiredDate(newData.getDesiredDate() == null ||
                               Duration.between(now, newData.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0
                               ?
                               now.plusMinutes(5)
                               :
                               newData.getDesiredDate());
        requestForGroupTransfer.setDesiredDate(newData.getDesiredDate());

        requestForGroupTransfer.setPassenger(employeeService.get(newData.getPassenger().id())
                                                            .orElseThrow(
                                                                    () -> new EntityNotFoundException(Employee.class, newData.getPassenger().id())));
        requestForGroupTransfer.setPurpose(tripPurposeRepository.findById(newData.getPurpose().getId()).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, newData.getPurpose().getId())));
        GroupTransferTariff groupTransferTariff = groupTransferTariffRepository.findById(newData.getTariffId())
                                                                               .orElseThrow(
                                                                                       () -> new EntityNotFoundException(GroupTransferTariff.class,
                                                                                                                         newData.getTariffId()));
        var outcomeTariff = groupTransferTariffRepository.getReferenceById(newData.getOutcomeTariffId());

        requestForGroupTransfer.setTariffId(newData.getTariffId());
        requestForGroupTransfer.setTariff(groupTransferTariff);
        requestForGroupTransfer.setOutcomeTariff(outcomeTariff);
        requestForGroupTransfer.setOutcomeTariffId(newData.getOutcomeTariffId());
        requestForGroupTransfer.setTriggerTime(groupTransferTariff.getTriggerTime());
        requestForGroupTransfer.setExpected(mapper.dtoToExpectedData(newData.getExpected()));
        requestForGroupTransfer.getSegmentsJSON().clear();
        requestForGroupTransfer.getSegmentsJSON().addAll(newData.getExpected().getSegments());
        requestForGroupTransfer.setRequestOptions(newData.getRequestOptions());
        // Автора менять нельзя
        requestForGroupTransfer.setCommentForDriver(newData.getCommentForDriver());

        requestForGroupTransfer.setGroupTransferClass(newData.getGroupTransferClass());
        requestForGroupTransfer.setPassengerCount(newData.getPassengerCount());
        save(requestForGroupTransfer);
        geoDataProcessingService.saveAddresses(requestForGroupTransfer, mapper.waypointDTOListToWaypointList(newData.getExpected().getWaypoints()));
        requestForGroupTransfer.getHistoryItemsForGroupTransfer().add(RequestHistoryElementForGroupTransfer.builder()
                                                                                                           .changeDate(
                                                                                                                   LocalDateTime.now(ZoneOffset.UTC))
                                                                                                           .requestForGroupTransfer(
                                                                                                                   requestForGroupTransfer)
                                                                                                           .comment(
                                                                                                                   "Заявка отправлена на согласование после " +
                                                                                                                   "редактирования")
                                                                                                           .status(requestForGroupTransfer.getStatus())
                                                                                                           .initiator(activeUser.getId())
                                                                                                           .build());

        requestSender.send(requestForGroupTransfer);
        return save(requestForGroupTransfer);
    }

    @Override
    public Request finish(Request request, Employee activeUser) {
        var requestForGroupTransfer = (RequestForGroupTransfer) request;
        requestForGroupTransfer.setFinishedTime(LocalDateTime.now(ZoneOffset.UTC));
        requestForGroupTransfer.setStatus(TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED);
        var finished = requestForGroupTransferRepository.save(requestForGroupTransfer);
        historyRepository.save(RequestHistoryElementForGroupTransfer.builder()
                                                                    .requestForGroupTransfer(requestForGroupTransfer)
                                                                    .code(finished.getStatusCode())
                                                                    .comment(
                                                                            "Заявка принудительно завершена пользователем " +
                                                                            activeUser.getFIO())
                                                                    .status(finished.getStatus())
                                                                    .initiator(activeUser.getId())
                                                                    .build());
        requestSender.send(finished);
        return finished;
    }

    @Override
    public Request complete(Request request, Employee activeUser) {
        throw new UnsupportedTransportTypeException(request.getTransportType());
    }


    private void cancel(RequestForGroupTransfer requestToCancel, CancelDTO cancelDTO, Employee initiator) {

        if (TripRequestStatus.GROUP_TRANSFER_CANCELLED == requestToCancel.getStatus()) {
            return;
        }

        requestToCancel.setStatus(TripRequestStatus.GROUP_TRANSFER_CANCELLED);
        requestToCancel.setStatusCode(cancelDTO.getCode());
        requestToCancel.setRequestClosedDatetime(LocalDateTime.now());
        if (TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EXPIRATION_TIME.getCode() == cancelDTO.getCode()) {
            requestToCancel.setApprovalDeadlineState(DeadlineState.RED);
        }
        requestToCancel.getHistoryItemsForGroupTransfer().add(RequestHistoryElementForGroupTransfer.builder()
                                                                                                   .changeDate(LocalDateTime.now(ZoneOffset.UTC))
                                                                                                   .requestForGroupTransfer(requestToCancel)
                                                                                                   .code(cancelDTO.getCode())
                                                                                                   .comment("Заявка отменена по причине: " +
                                                                                                            cancelDTO.getReason())
                                                                                                   .status(requestToCancel.getStatus())
                                                                                                   .initiator(initiator.getId())
                                                                                                   .build());
        requestForGroupTransferRepository.save(requestToCancel);
        if (requestToCancel.getTrip() != null) {
            var tripOptional = groupTransferTripRepository.findById(requestToCancel.getTrip().getId());
            if (tripOptional.isPresent() && tripOptional.get().getGroupTransferId() != null) {
                var trip = tripOptional.get();
                trip.setStatus(InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT);
                groupTransferTripRepository.save(trip);
                publishTripService.publishGroupTransferTrip(trip);
                log.info("Отправили отмену поездки {} по трансферу контрагенту", trip.getHumanReadableId());
            }
        }

        log.debug("Trying to send requestForGroupTransfer {}", requestToCancel.getHumanReadableId());
        requestSender.send(requestToCancel);
        log.debug("Successfully sent requestForGroupTransfer {}", requestToCancel.getHumanReadableId());
    }

    @Transactional
    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator) {
        var requestToCancel = requestForGroupTransferRepository.getReferenceById(request.getId());
        if (!request.getStatus().isCancelable()) {
            throw new IllegalArgumentException("It is impossible to cancel a request %s with the status %s".formatted(request.getHumanReadableId(),
                                                                                                                      request.getStatus().name()));
        }
        if (requestToCancel.getTrip() != null && requestToCancel.getTrip().getGroupTransferId() == null) {
            throw new UpdateRequestException("Невозможно отменить заявку, так как не получен внешний идентификатор от контрагента");
        }
        cancel(requestToCancel, cancelDTO, initiator);
    }

    @Override
    public Request approveRequest(Request request) {
        var requestForGroupTransfer = (RequestForGroupTransfer) request;
        requestForGroupTransfer.setStatus(TripRequestStatus.GROUP_TRANSFER_APPROVED);
        requestForGroupTransfer.setApprovalState(ApprovalState.APPROVED);
        requestForGroupTransfer.setApprovalDate(LocalDateTime.now(ZoneOffset.UTC));
        var user = TechnicalUser.get();
        requestForGroupTransfer.getHistoryItemsForGroupTransfer().add(RequestHistoryElementForGroupTransfer.builder()
                                                                                                           .changeDate(
                                                                                                                   requestForGroupTransfer.getApprovalDate())
                                                                                                           .requestForGroupTransfer(
                                                                                                                   requestForGroupTransfer)
                                                                                                           .comment("Заявка согласована " +
                                                                                                                    user.getFIO())
                                                                                                           .status(requestForGroupTransfer.getStatus())
                                                                                                           .initiator(user.getId())
                                                                                                           .build());
        setupDriverArrivedDeadline(requestForGroupTransfer);
        return requestForGroupTransfer;
    }

    public void setupDriverArrivedDeadline(RequestForGroupTransfer requestForGroupTransfer) {
        log.debug("setupDriverArrivedDeadline() begin");
        log.debug("requestForGroupTransfer.getId() = {}", requestForGroupTransfer.getId());
        log.debug("requestForGroupTransfer.getApprovalDate() = {}", requestForGroupTransfer.getApprovalDate());
        log.debug("requestForGroupTransfer.getDriverArrivedDeadline() = {}", requestForGroupTransfer.getDriverArrivedDeadline());
        log.debug("requestForGroupTransfer.getDesiredDate() = {}", requestForGroupTransfer.getDesiredDate());

        if (requestForGroupTransfer.getApprovalDate() == null) {
            log.warn("Для заявки на групповой трансфер с id = {} не указана дата согласования - расчет контрольного срока прибытия водителя " +
                     "невозможен", requestForGroupTransfer.getId());
            return;
        }
        if (requestForGroupTransfer.getDriverArrivedDeadline() != null) {
            log.warn("Для заявки на групповой трансфер с id = {} уже указан контрольный срок - перерасчет контрольного срока прибытия водителя не " +
                     "делается", requestForGroupTransfer.getId());
            return;
        }
        var groupTransferDeadlineSettings = getTaxiDeadlineSettings();

        var duration = Duration.between(requestForGroupTransfer.getApprovalDate(), requestForGroupTransfer.getDesiredDate()).toMinutes();

        LocalDateTime driverArrivedDeadline = (duration < groupTransferDeadlineSettings.getGroupTransferMinTriggerTime()) ?
                                              requestForGroupTransfer.getApprovalDate()
                                                                     .plusMinutes(groupTransferDeadlineSettings.getGroupTransferMinTriggerTime())
                                                                     .plusMinutes(
                                                                             groupTransferDeadlineSettings.getGroupTransferDriverArrivalMaxDelay())
                                                                                                                          :
                                              requestForGroupTransfer.getDesiredDate()
                                                                     .plusMinutes(
                                                                             groupTransferDeadlineSettings.getGroupTransferDriverArrivalMaxDelay());

        requestForGroupTransfer.setDriverArrivedDeadline(driverArrivedDeadline);

        log.debug("duration = {}", duration);
        log.debug("groupTransferDeadlineSettings.getGroupTransferMinTriggerTime() = {}",
                  groupTransferDeadlineSettings.getGroupTransferMinTriggerTime());
        log.debug("groupTransferDeadlineSettings.getGroupTransferDriverArrivalMaxDelay() = {}",
                  groupTransferDeadlineSettings.getGroupTransferDriverArrivalMaxDelay());
        log.debug("driverArrivedDeadline = {}", driverArrivedDeadline);
        log.debug("requestForGroupTransfer.getDriverArrivedDeadline() = {}", requestForGroupTransfer.getDriverArrivedDeadline());
        log.debug("setupDriverArrivedDeadline() end");
    }

    private GroupTransferDeadlineSettings getTaxiDeadlineSettings() {
        return new GroupTransferDeadlineSettings();
    }

    static class GroupTransferDeadlineSettings {
        long groupTransferMinTriggerTime;
        long groupTransferDriverArrivalMaxDelay;

        GroupTransferDeadlineSettings() {
            groupTransferMinTriggerTime = 60L;
            groupTransferDriverArrivalMaxDelay = 15L;
        }

        long getGroupTransferMinTriggerTime() {
            return groupTransferMinTriggerTime;
        }

        long getGroupTransferDriverArrivalMaxDelay() {
            return groupTransferDriverArrivalMaxDelay;
        }
    }

    public DeadlineState calcDriverArrivedDeadline(
            LocalDateTime now,
            LocalDateTime driverArrivedDatetime,
            LocalDateTime driverArrivedDeadline
                                                  ) {
        if (now != null && driverArrivedDeadline != null) {
            // Если водитель прибыл в точку отправления позднее контрольного срока
            // или не приехал вообще, а время контрольного срока уже превышено,
            // тогда считаем это признаком нарушения контрольного срока (цвет Красный)
            if ((driverArrivedDatetime != null && driverArrivedDeadline.isBefore(driverArrivedDatetime)) ||
                (driverArrivedDatetime == null && driverArrivedDeadline.isBefore(now))) {
                return DeadlineState.RED;
            }
        }
        return DeadlineState.NONE;
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        var requestForGroupTransfer = (RequestForGroupTransfer) toChange;

        if (toChange.getStatus() == newStatus) {
            log.info("Статус у request не изменился. id = {}, toChange.getStatus() = {}, newStatus = {}",
                      requestForGroupTransfer.getId(), toChange.getStatus(), newStatus);
            return requestForGroupTransfer;
        }

        var now = LocalDateTime.now(clock);
        if (newStatus.equals(TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH)) {
            requestForGroupTransfer.setAwaitingSearchStartDate(now);
        }
        if (newStatus.equals(TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED) && requestForGroupTransfer.getDriverArrivedDatetime() == null) {
                requestForGroupTransfer.setDriverArrivedDatetime(now);
            }
        changeDefaultStatusCode(newStatus, requestForGroupTransfer);
        requestForGroupTransfer.setDeadlineState(calcDriverArrivedDeadline(
                now,
                requestForGroupTransfer.getDriverArrivedDatetime(),
                requestForGroupTransfer.getDriverArrivedDeadline()));

        requestForGroupTransfer.setStatus(newStatus);
        requestForGroupTransfer = requestForGroupTransferRepository.save(requestForGroupTransfer);
        historyRepository.save(RequestHistoryElementForGroupTransfer.builder()
                                                                    .requestForGroupTransfer(requestForGroupTransfer)
                                                                    .comment("Статус заявки изменен пользователем: " +
                                                                             activeUser.getFIO())
                                                                    .status(newStatus)
                                                                    .initiator(activeUser.getId())
                                                                    .build());
        log.info("Trying to send request {}", requestForGroupTransfer.getHumanReadableId());
        requestSender.send(requestForGroupTransfer);
        log.info("Successfully sent request {}", requestForGroupTransfer.getHumanReadableId());
        return requestForGroupTransfer;
    }

    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        log.error("Данная операция не существует для типа транспорта GROUP_TRANSFER");
    }

    /**
     * Смена статус кода на "Завершена без оценки" (выполняется по истечении КС)
     *
     * @param request заявка
     * @param author  автор
     */
    @Override
    public void finishByExpiration(RequestForGroupTransfer request, Employee author) {
        if (!request.getStatus().equals(TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED)) {
            throwWrongRequestStatusException(request.getId());
        }
        if (request.getRequestRating() != null) {
            throwRequestAlreadyRatedException(request.getId());
        }
        TripRequestStatus.GroupTransferStatusCode statusCode = TripRequestStatus.GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EXPIRATION_TIME;
        var expired = requestForGroupTransferRepository.save(request);
        historyRepository.save(
                RequestHistoryElementForGroupTransfer.builder()
                                                     .requestForGroupTransfer(request)
                                                     .code(expired.getStatusCode())
                                                     .comment("Установлен код заявки: " + statusCode.getCode() + " " +
                                                              statusCode.getDescription())
                                                     .status(expired.getStatus())
                                                     .initiator(author.getId())
                                                     .build());
        requestSender.send(expired);
    }

    @Override
    public RequestForGroupTransfer save(RequestForGroupTransfer request) {
        var savedRequest = super.save(request);
        requestSender.send(savedRequest);
        return savedRequest;
    }

    /**
     * Меняем statusCode, если он был пустым, и если мы получили новый статус заявки, по которому мы можем идентифицировать код
     * @param newStatus {@link TripRequestStatus}
     * @param request {@link RequestForGroupTransfer}
     */
    private static void changeDefaultStatusCode(TripRequestStatus newStatus, RequestForGroupTransfer request) {
        if (TripRequestStatus.getCanceledStatuses().contains(newStatus) && request.getStatusCode() == 0) {
            request.setStatusCode(STATUS_CODE_MAPPING.getOrDefault(request.getTrip().getStatus(), 0));
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
}
