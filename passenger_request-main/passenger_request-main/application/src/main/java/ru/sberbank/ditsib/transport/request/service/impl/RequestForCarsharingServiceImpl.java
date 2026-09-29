package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.CarsharingException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;
import ru.sberbank.ditsib.transport.request.service.CarsharingTariffService;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;
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
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class RequestForCarsharingServiceImpl extends AbstractTransportTypeService<RequestForCarsharing> {

    private final EmployeeService employeeService;

    private final DepartmentService departmentService;

    private final SQGenerator sqGenerator;

    private final TripPurposeRepository tripPurposeRepository;

    private final ReservationService reservationService;

    private final RequestForCarsharingRepository requestForCarsharingRepository;

    private final RequestHistoryRepository historyRepository;

    private final RequestSender<RequestForCarsharing> requestSender;

    private final AddressRepository addressRepository;

    private final GeoDataProcessingService geoDataProcessingService;

    private final IntegrationsCarsharingService carsharingService;

    private final CarsharingTripRepository carsharingTripRepository;

    private final CarsharingTariffService carSharingTariffService;

    private final ApprovalDeadlineCalculator approvalDeadlineCalculator;

    @Autowired
    public RequestForCarsharingServiceImpl(
            EmployeeService employeeService,
            DepartmentService departmentService,
            SQGenerator sqGenerator,
            TripPurposeRepository tripPurposeRepository,
            EntityDTOMapper mapper,
            ReservationService reservationService,
            RequestForCarsharingRepository requestForCarsharingRepository,
            CheckinSettingsService checkinSettingsService,
            RequestHistoryRepository historyRepository,
            RequestSender<RequestForCarsharing> requestSender,
            RegionDataResolver regionDataResolver,
            AddressRepository addressRepository,
            GeoDataProcessingService geoDataProcessingService,
            IntegrationsCarsharingService carsharingService,
            CarsharingTripRepository carsharingTripRepository,
            CarsharingTariffService carsharingTariffService,
            EasupGrpcService easupGrpcService,
            FraudService fraudService,
            RequestChecksGrpcService requestChecksGrpcService,
            DurationRequestCheckGrpcClient durationRequestCheckGrpcClient,
            OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient,
            FraudMonitoringService fraudMonitoringService
                                          ) {
        super(requestForCarsharingRepository, checkinSettingsService, regionDataResolver,
              mapper, easupGrpcService, fraudService, requestChecksGrpcService, durationRequestCheckGrpcClient, overrunRequestCheckGrpcClient, fraudMonitoringService);
        this.departmentService = departmentService;
        this.sqGenerator = sqGenerator;
        this.employeeService = employeeService;
        this.tripPurposeRepository = tripPurposeRepository;
        this.reservationService = reservationService;
        this.requestForCarsharingRepository = requestForCarsharingRepository;
        this.historyRepository = historyRepository;
        this.requestSender = requestSender;
        this.addressRepository = addressRepository;
        this.geoDataProcessingService = geoDataProcessingService;
        this.carsharingService = carsharingService;
        this.carsharingTripRepository = carsharingTripRepository;
        this.carSharingTariffService = carsharingTariffService;
        transportType = TransportTypeEnum.CARSHARING;
        this.approvalDeadlineCalculator = new CarsharingApprovalDeadlineCalculatorImpl();
    }

    @Override
    public Optional<RequestForCarsharing> get(UUID id) {
        return requestForCarsharingRepository.findById(id);
    }

    @Override
    public Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroupDTO) {

        final var last = requestForCarsharingRepository.findFirstByAuthorIdOrderByCreationTimeDesc(employee.getId());
        if (last.isPresent()) {
            final var status = last.get().getStatus();
            if (!(TripRequestStatus.CARSHARING_CANCELLED.equals(status) ||
                  TripRequestStatus.CARSHARING_TRIP_FINISHED.equals(status))) {
                throw new CarsharingException("Невозможно создать больше одной активной заявки");
            }
        }


        final var now = LocalDateTime.now(ZoneOffset.UTC);

        final var departmentId = employee.getDepartment().getId();
        final var organization = departmentService.get(departmentId)
                .map(Department::getOrganization)
                .orElseThrow(() -> new EntityNotFoundException(Organization.class, Map.of("departmentId", departmentId)));
        final var organizationDigitId = organization.getDigitId();
        final var organizationId = organization.getId();
        final var humanReadableId = sqGenerator.getNextId(Prefix.OT, organizationDigitId);
        final var tariff = carSharingTariffService.getTariffById(data.getTariffId());
        final var outcomeTariff = carSharingTariffService.getTariffById(data.getOutcomeTariffId());

        //прикрепление к корп тарифу
        carsharingService.joinToTariff(employee, organizationId);

        final var desiredDate = data.getDesiredDate() == null ||
                          Duration.between(now, data.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0 ?
                          now.plusMinutes(5) :
                          data.getDesiredDate();

        final var approvalDeadline = approvalDeadlineCalculator.getApprovalDeadline(now, desiredDate, data.getTimeZone());

        final var passengerId = data.getPassenger().id();
        final var expected = data.getExpected();
        var requestForCarsharing = RequestForCarsharing.builder()
                                                       .author(employee)
                                                       .segmentsJSON(expected.getSegments())
                                                       .waypoints(mapper.waypointDTOListToWaypointList(expected.getWaypoints()))
                                                       .expected(mapper.dtoToExpectedData(expected))
                                                       .passenger(employeeService.get(passengerId).orElseThrow(() -> new EntityNotFoundException(Employee.class, passengerId)))
                                                       .purpose(tripPurposeRepository.getReferenceById(data.getPurpose().getId()))
                                                       .tariffId(data.getTariffId())
                                                       .tariff(tariff)
                                                       .outcomeTariff(outcomeTariff)
                                                       .outcomeTariffId(data.getOutcomeTariffId())
                                                       .contractorId(data.getContractorId())
                                                       .creationTime(now).humanReadableId(humanReadableId)
                                                       .status(TripRequestStatus.CARSHARING_AWAITING_APPROVAL)
                                                       .approvalState(ApprovalState.AWAITING_APPROVAL)
                                                       .creationTime(now)
                                                       .desiredDate(desiredDate)
                                                       .requestOptions(data.getRequestOptions())
                                                       .timeZone(Optional.ofNullable(data.getTimeZone()).orElse(defaultTimezone))
                                                       .organizationId(organizationId)
                                                       .phoneNumber(employee.getMobilePhone())
                                                       .employeeDeviceTimeZone(data.getEmployeeDeviceTimeZone())
                                                       .approvalDeadline(approvalDeadline)
                                                       .approvalDeadlineState(DeadlineState.NONE)
                                                       .joinedPassengerIds(data.getJoinedPassengerIds())
                                                       .source(Optional.ofNullable(data.getSource()).orElse(RequestSourceEnum.UNDEFINED))
                                                       .minTariffTaxi(data.getMinTariffTaxi())
                                                       .commentForPurpose(data.getCommentForPurpose())
                                                       .executorGroupId(executorGroupDTO.getId())
                                                       .transportType(data.getTransportType())
                                                       .executorGroupName(executorGroupDTO.getName())
                                                       .build();
        requestForCarsharing.setPassengerCount(data.getPassengerCount());
        requestForCarsharing.setCarsharingClass(data.getCarsharingClass());
        requestForCarsharing.setCoopTrip(coop);
        requestForCarsharing.getHistoryItemsForCarsharing()
                            .add(RequestHistoryElementForCarsharing.builder()
                                                                   .changeDate(requestForCarsharing.getCreationTime())
                                                                   .requestForCarsharing(requestForCarsharing)
                                                                   .comment("Заявка зарегистрирована")
                                                                   .status(requestForCarsharing.getStatus())
                                                                   .initiator(requestForCarsharing.getAuthor().getId())
                                                                   .build());
        var isAbsent = checkOnAbsence(requestForCarsharing, employee, Optional.empty());
        enrichWaypointsWithRadius(requestForCarsharing, TransportTypeEnum.CARSHARING);
        try {
            checkDurationLimit(requestForCarsharing, Optional.empty());
            checkOverrunLimit(requestForCarsharing, Optional.empty());
            checkSingleTripDuration(requestForCarsharing);
            geoDataProcessingService.saveAddresses(requestForCarsharing.getWaypoints(),
                                                   requestForCarsharing.getPassenger());
            requestForCarsharing = requestForCarsharingRepository.saveAndFlush(requestForCarsharing);
            if (isAbsent) {
                createFraud(requestForCarsharing, FraudType.ABSENCE, ABSENT_EMPLOYEE_FRAUD_COMMENT);
            }
            checkMultipointLimit(requestForCarsharing, Optional.empty());
            reservationService.makeReservation(requestForCarsharing,
                                               requestForCarsharing.getPassenger(),
                                               requestForCarsharing.getExpected().getCost(),
                                               requestForCarsharing.getExpected().getBonusCost());
            requestForCarsharingRepository.saveAndFlush(requestForCarsharing);
            historyRepository.flush();
            requestForCarsharing.getHistoryItemsForCarsharing().sort(
                    Comparator.comparing(RequestHistoryElementForCarsharing::getChangeDate));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        addressRepository.flush();
        return requestForCarsharing;
    }

    @Override
    public Request update(RequestDTO newData, Employee activeUser, Request request, String token) {
        var requestForCarsharing = (RequestForCarsharing) request;
        if (requestForCarsharing.isCoopTrip()) {
            throw new CarsharingException("Request for shared ride cannot be edited. Cancel is the only " +
                                          "option");
        }
        if (requestForCarsharing.getExpected().getCost() != newData.getExpected().getCost()) {
            reservationService.makeReservation(requestForCarsharing,
                                               requestForCarsharing.getPassenger(),
                                               newData.getExpected().getCost(),
                                               newData.getExpected().getBonusCost());
        }
        requestForCarsharing.setApprovalDate(null);
        var now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        newData.setDesiredDate(newData.getDesiredDate() == null ||
                               Duration.between(now, newData.getDesiredDate()).compareTo(Duration.ofMinutes(5)) < 0 ?
                               now.plusMinutes(5) :
                               newData.getDesiredDate());
        requestForCarsharing.setDesiredDate(newData.getDesiredDate());
        var passenger = newData.getPassenger();
        requestForCarsharing.setPassenger(employeeService.get(passenger.id())
                                                         .orElseThrow(
                                                                 () -> new EntityNotFoundException(Employee.class,
                                                                                                   passenger.id())));
        requestForCarsharing.setPurpose(tripPurposeRepository.findById(newData.getPurpose().getId()).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, newData.getPurpose().getId())));
        var tariff = carSharingTariffService.getTariffById(newData.getTariffId());
        var outcomeTariff = carSharingTariffService.getTariffById(newData.getOutcomeTariffId());
        requestForCarsharing.setTariffId(newData.getTariffId());
        requestForCarsharing.setTariff(tariff);
        requestForCarsharing.setOutcomeTariff(outcomeTariff);
        requestForCarsharing.setOutcomeTariffId(newData.getOutcomeTariffId());
        requestForCarsharing.setExpected(mapper.dtoToExpectedData(newData.getExpected()));
        requestForCarsharing.getSegmentsJSON().clear();
        requestForCarsharing.getSegmentsJSON().addAll(newData.getExpected().getSegments());
        requestForCarsharing.setRequestOptions(newData.getRequestOptions());
        requestForCarsharing.setPassengerCount(newData.getPassengerCount());
        save(requestForCarsharing);
        geoDataProcessingService.saveAddresses(requestForCarsharing, mapper.waypointDTOListToWaypointList(
                newData.getExpected().getWaypoints()));
        var historyElement =
                RequestHistoryElementForCarsharing.builder()
                                                  .changeDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                                                  .requestForCarsharing(requestForCarsharing)
                                                  .comment("Заявка отправлена на согласование после редактирования")
                                                  .status(requestForCarsharing.getStatus())
                                                  .initiator(activeUser.getId())
                                                  .build();
        requestForCarsharing.getHistoryItemsForCarsharing().add(historyElement);

        return save(requestForCarsharing);
    }

    @Override
    public Request finish(Request request, Employee activeUser) {
        var requestForCarsharing = (RequestForCarsharing) request;
        requestForCarsharing.setFinishedTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        requestForCarsharing.setStatus(TripRequestStatus.CARSHARING_TRIP_FINISHED);

        requestForCarsharing.setStatusCode(TripRequestStatus.CarsharingStatusCode.CARSHARING_TRIP_FINISHED_NOT_RATED.getCode());
        var finished = requestForCarsharingRepository.save(requestForCarsharing);
        var historyElement =
                RequestHistoryElementForCarsharing.builder()
                                                  .requestForCarsharing(requestForCarsharing)
                                                  .code(finished.getStatusCode())
                                                  .comment("Заявка принудительно завершена пользователем " +
                                                           activeUser.getFIO())
                                                  .status(finished.getStatus())
                                                  .initiator(activeUser.getId())
                                                  .build();
        historyRepository.save(historyElement);
        requestSender.send(finished);
        return finished;
    }

    @Override
    public Request complete(Request request, Employee activeUser) {
        reservationService.spend(request, request.getExpected().getCost().intValue(), TransportTypeEnum.CARSHARING);
        changeState(request, TripRequestStatus.CARSHARING_TRIP_FINISHED, activeUser, null);
        return request;
    }

    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator) {
        var requestForCarsharing = (RequestForCarsharing) request;
        if (TripRequestStatus.TAXI_CANCELLED == requestForCarsharing.getStatus()) {
            return;
        }
        requestForCarsharing.setStatus(TripRequestStatus.CARSHARING_CANCELLED);
        requestForCarsharing.setStatusCode(cancelDTO.getCode());
        if (TripRequestStatus.CarsharingStatusCode.CARSHARING_DECLINED_BY_EXPIRATION_TIME.getCode() == cancelDTO.getCode()) {
            requestForCarsharing.setApprovalDeadlineState(DeadlineState.RED);
        }
        try {
            reservationService.cancel(requestForCarsharing);
            requestForCarsharing.getHistoryItemsForCarsharing()
                                .add(RequestHistoryElementForCarsharing.builder()
                                                                       .changeDate(LocalDateTime.now(ZoneId.of(
                                                                               ZoneOffset.UTC
                                                                                       .getId())))
                                                                       .requestForCarsharing(requestForCarsharing)
                                                                       .code(cancelDTO.getCode())
                                                                       .comment("Заявка отменена по причине: " +
                                                                                cancelDTO.getReason())
                                                                       .status(requestForCarsharing.getStatus())
                                                                       .initiator(initiator.getId())
                                                                       .build());
            // TODO Закомментировано до возникновения совместных поездок на каршеринге
            //if (requestForCarsharing.isCoopTrip() && requestForCarsharing.getMagentaOrderId() != null) {
            //    removeRequestFromSharedRide(requestForTaxi, magentaService, sharedRequestRepository);
            //}
            requestForCarsharing = requestForCarsharingRepository.save(requestForCarsharing);
            requestSender.send(requestForCarsharing);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Request approveRequest(Request request) {
        var requestForCarsharing = (RequestForCarsharing) request;
        //TODO до добавления совместных поездок по каршерингу этот код заменен на проставление статуса APPROVED
        /*if (requestForCarsharing.isCoopTrip() && !requestForCarsharing.isSharedRideOwner()) {
            requestForCarsharing.setStatus(TripRequestStatus.AWAITING_SHARED_RIDE_APPROVAL);
        } else {
            requestForCarsharing.setStatus(TripRequestStatus.APPROVED);
        }*/
        requestForCarsharing.setStatus(TripRequestStatus.CARSHARING_APPROVED);
        requestForCarsharing.setApprovalState(ApprovalState.APPROVED);
        requestForCarsharing.setApprovalDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        var historyElement =
                RequestHistoryElementForCarsharing.builder()
                                                  .changeDate(requestForCarsharing.getApprovalDate())
                                                  .requestForCarsharing(requestForCarsharing)
                                                  .comment("Заявка согласована " +
                                                           requestForCarsharing.getApprovedBy().getFIO())
                                                  .status(requestForCarsharing.getStatus())
                                                  .initiator(requestForCarsharing.getApprovedBy().getId())
                                                  .build();
        requestForCarsharing.getHistoryItemsForCarsharing().add(historyElement);
        autoHandle(requestForCarsharing);
        return requestForCarsharing;
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        var requestForCarsharing = (RequestForCarsharing) toChange;
        if (newStatus.ordinal() > requestForCarsharing.getStatus().ordinal()) {
            requestForCarsharing.setStatus(newStatus);
        } else {
            log.warn("Обновленный статус {} < статуса поездки {}, requestId {}. Пропускаем смену статуса", newStatus.name(),
                     requestForCarsharing.getStatus().name(),
                     requestForCarsharing.getId());
        }
        if (newStatus == TripRequestStatus.CARSHARING_AWAITING_TRIP_APPROVAL) {
            requestForCarsharing.setFinishedTime(LocalDateTime.now(ZoneId.of("UTC")));
        }
        if (newStatus == TripRequestStatus.CARSHARING_CANCELLED && changeStatusDTO != null) {
            requestForCarsharing.setStatusComment(changeStatusDTO.getComment());
        }
        requestForCarsharing = requestForCarsharingRepository.save(requestForCarsharing);
        historyRepository.save(RequestHistoryElementForCarsharing.builder()
                                                                 .requestForCarsharing(requestForCarsharing)
                                                                 .comment("Статус заявки изменен пользователем: " +
                                                                          activeUser.getFIO())
                                                                 .status(newStatus)
                                                                 .initiator(activeUser.getId())
                                                                 .build());
        requestSender.send(requestForCarsharing);
        return requestForCarsharing;
    }

    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        RequestForPersonal toApprove = (RequestForPersonal) request;
        if (toApprove.getStatus() != TripRequestStatus.CARSHARING_AWAITING_TRIP_APPROVAL) {
            log.warn("Skip approve final trip because request has status: {}", request.getStatus());
            return;
        }
        changeState(request, TripRequestStatus.CARSHARING_TRIP_FINISHED, request.getAuthor(), null);
    }

    /**
     * Автоматическая синхронизация поездки в случае, если заявка не была согласована до момента получения фактических данных
     *
     * @param request заявка
     */
    private void autoHandle(Request request) {
        log.info("Проверка наличия фактических данных для request: {}", request.getId());
        var carsharingTripOptional = carsharingTripRepository.findFirstByRequestId(request.getId());
        if (carsharingTripOptional.isEmpty()) {
            return;
        }
        var carsharingTrip = carsharingTripOptional.get();
        log.info("Фактические данные найдены для request: {}. Синхронизируем статус заявки с фактическими данными carshringTrip: {}",
                 request.getId(),
                 carsharingTrip.getId());
        if (carsharingTrip.getRentCreatedAt() != null) {
            log.info("Найдены фактические данные по началу поездки для request: {}", request.getId());
            changeState(request, TripRequestStatus.CARSHARING_TRIP_IN_PROGRESS, request.getAuthor(), null);
        }
        if (carsharingTrip.getRentFinishedAt() != null) {
            log.info("Найдены фактические данные по завершению поездки для request: {}", request.getId());
            complete(request, request.getAuthor());
        }
    }
}
