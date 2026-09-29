package ru.sberbank.ditsib.transport.request.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.magenta.model.KpiDTO;
import ru.sber.transport.magenta.model.MagentaWaypointResponseDTO;
import ru.sber.transport.magenta.model.OrderKpiDTO;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointFinalDTO;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TaxiStopType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.database.model.magenta.CoopRequest;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapper;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.UpdateRequestSender;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.utils.reflection.DoubleUtil;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of service for working with requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final Map<TransportTypeEnum, RequestSender<Request>> requestSenders;
    private final UpdateRequestSender updateRequestSender;
    private final RequestRatingSender requestRatingSender;
    private final SrmService srmService;
    private final EmployeeService employeeService;
    private final List<AbstractTransportTypeService<? extends Request>> transportTypeService;
    private final GeoDataProcessingService geoDataProcessingService;

    private final EntityDTOMapper mapper;
    private final EmployeeMapper employeeMapper;
    private final RequestMapper requestMapper;

    private final TariffGrpcClient tariffGrpcClient;

    private final PlatformService platformService;

    private final NewRequestValidation validation;
    private final RequestForPersonalHistoryRepository personalHistoryRepository;
    private final RequestForPublicHistoryRepository publicHistoryRepository;
    private final RequestForTaxiHistoryRepository taxiHistoryRepository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final RequestForPersonalRepository requestForPersonalRepository;
    private final RequestForGroupTransferHistoryRepository requestForGroupTransferHistoryRepository;
    private final RequestForCarsharingHistoryRepository requestForCarsharingHistoryRepository;
    private final RequestForPublicRepository requestForPublicRepository;
    private final PositionRepository positionRepository;
    private final UpdateRequestRepository updateRequestRepository;
    private final RequestRepository requestRepository;
    private final FraudRepository fraudRepository;

    @Value("${sharedRide.requests.cancelStatusDescription:Заявка отменена}")
    private String cancelStatusDescription;

    @Override
    public Request add(UUID rideId, boolean coop, NewRequestDTO data, Employee employee) {
        return add(rideId, coop, data, employee, null, Collections.emptyList());
    }

    @Override
    public Request add(UUID rideId, boolean coop, NewRequestDTO data, Employee employee, String token) {
        return add(rideId, coop, data, employee, token, Collections.emptyList());
    }

    @Override
    public Request add(UUID rideId, boolean coop, NewRequestDTO request, Employee employee, String token, List<RegionDto> regions) {
        validation.validate(request, employee);

        if (!isSpendingFromBonusAccountAllowed(request.getTransportType())) {
            request.getExpected().setBonusCost(null);
        }
        request.setDesiredDate(request.getDesiredDate().withSecond(0).withNano(0));

        return getServiceOpt(request.getTransportType())
                .map(
                        service -> {
                            var executorGroup =
                                    platformService.getExecutorGroup(employee.getId(), regions.stream()
                                            .map(RegionDto::getId)
                                            .toList(), token);
                            if (ObjectUtils.isEmpty(executorGroup)) {
                                log.info("Заявка от пользователя с id: {} не получила назначение группы исполнителей", employee.getId());
                                executorGroup = new ExecutorGroupDTO();
                            }
                            return service.add(rideId, coop, request, employee, token, executorGroup);
                        }
                ).orElseThrow(() -> new UnsupportedTransportTypeException(request.getTransportType()));
    }

    @Override
    public Request update(RequestDTO newData, Employee activeUser, String token) {
        var savedRequest = get(newData.getId()).orElseThrow(
                () -> new EntityNotFoundException(Request.class, newData.getId()));
        if (savedRequest.getTransportType() == TransportTypeEnum.PUBLIC) {
            throw new UnsupportedTransportTypeException(newData.getTransportType());
        }

        return getServiceOpt(savedRequest.getTransportType())
                .map(service -> service.update(newData, activeUser, savedRequest, token))
                .orElseThrow(() -> new UnsupportedTransportTypeException(savedRequest.getTransportType()));
    }

    @Override
    public Request rate(RequestForTaxi request, RequestRating rating, Employee activeUser) {
        request.setRequestRating(rating);
        request.getHistoryItemsForTaxi().add(RequestHistoryElementForTaxi.builder()
                .changeDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .requestForTaxi(request)
                .code(TripRequestStatus.TaxiStatusCode.TAXI_TRIP_FINISHED_RATED.getCode())
                .comment("Пользователь " + activeUser.getFIO() + " выставил оценку " + rating.getRating())
                .status(TripRequestStatus.TAXI_TRIP_FINISHED)
                .initiator(activeUser.getId())
                .build());
        var savedRequest = requestRepository.save(request);
        requestRatingSender.send(savedRequest.getId(), rating);
        return savedRequest;
    }

    @Override
    public Request finish(Request toFinish, Employee activeUser) {
        return transportTypeService.stream()
                .filter(service -> service.getTransportType() == toFinish.getTransportType())
                .map(service -> service.finish(toFinish, activeUser)).findFirst()
                .orElseThrow(() -> new UnsupportedTransportTypeException(toFinish.getTransportType()));
    }

    @Override
    public Request complete(Request requestForTnP, Employee activeUser) {
        getTransportTypeService(requestForTnP.getTransportType()).complete(requestForTnP, activeUser);
        return requestForTnP;
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser) {
        return changeState(toChange, newStatus, activeUser, null);
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus) {
        return changeState(toChange, newStatus, TechnicalUser.get(), null);
    }

    @Override
    public Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO) {
        return transportTypeService.stream()
                .filter(service -> service.getTransportType() == toChange.getTransportType())
                .map(service -> service.changeState(toChange, newStatus, activeUser, changeStatusDTO)).findFirst()
                .orElseThrow(() -> new UnsupportedTransportTypeException(toChange.getTransportType()));
    }

    @Override
    public void cancel(Request request, CancelDTO cancelDTO, Employee initiator, boolean cancelAnyway) {
        transportTypeService.stream()
                .filter(service -> service.getTransportType() == request.getTransportType())
                .forEach(service -> service.cancel(request, cancelDTO, initiator));
    }

    @Override
    public void decline(Request request, CancelDTO cancelDTO, Employee initiator, String token) {
        cancel(request, cancelDTO, initiator, true);
    }

    @Override
    public List<RequestHistoryElement> getHistory(UUID requestId, TransportTypeEnum transportType) {
        List<RequestHistoryElement> historyList = new ArrayList<>();

        if (transportType == null || TransportTypeEnum.PERSONAL.equals(transportType)) {
            historyList.addAll(personalHistoryRepository.getAllByRequestForPersonalIdOrderByChangeDate(requestId));
        }

        if (transportType == null || TransportTypeEnum.PUBLIC.equals(transportType)) {
            historyList.addAll(publicHistoryRepository.getAllByRequestForPublicIdOrderByChangeDate(requestId));
        }

        if (transportType == null || TransportTypeEnum.TAXI.equals(transportType)) {
            historyList.addAll(taxiHistoryRepository.getAllByRequestForTaxiIdOrderByChangeDate(requestId));
        }

        if (transportType == null || TransportTypeEnum.CARSHARING.equals(transportType)) {
            historyList.addAll(requestForCarsharingHistoryRepository.getAllByRequestForCarsharingIdOrderByChangeDate(requestId));
        }

        if (transportType == null || TransportTypeEnum.GROUP_TRANSFER.equals(transportType)) {
            historyList.addAll(requestForGroupTransferHistoryRepository.findAllByRequestForGroupTransferIdOrderByChangeDate(requestId));
        }

        return historyList;
    }

    @Override
    public Optional<? extends Request> get(UUID id) {
        return requestRepository.findById(id);
    }

    @Override
    public Page<? extends Request> getByTerminateStatusAndEmployee(
            boolean isTerminateStatus, Employee employee, Pageable page
    ) {
        Set<TripRequestStatus> terminalStatusSet = TripRequestStatus.getTerminalStatus(isTerminateStatus);
        return requestRepository.findByStatusSetAndEmployee(terminalStatusSet, employee, page);
    }

    @Override
    public Optional<? extends Request> get(UUID id, TransportTypeEnum transportType) {
        var requests = transportTypeService.stream()
                .filter(service -> service.getTransportType() == transportType)
                .map(service -> service.get(id)).toList();
        var request = requests.isEmpty() ? Optional.<Request>empty() : requests.getFirst();

        request.ifPresent(r -> {
            if (r instanceof CoopRequest coopRequest && coopRequest.getRideId() != null) {
                if (transportType == TransportTypeEnum.PERSONAL) {
                    var requestList = requestForPersonalRepository.findByRideId(coopRequest.getRideId());
                    r.setPassengersFromCoop(requestList.stream()
                            .map(Request::getPassenger)
                            .collect(Collectors.toSet()));
                }
                if (transportType == TransportTypeEnum.TAXI) {
                    var requestList = requestForTaxiRepository.findByRideId(coopRequest.getRideId());
                    r.setPassengersFromCoop(requestList.stream()
                            .map(Request::getPassenger)
                            .collect(Collectors.toSet()));
                }
            }
            r.setFraudData(fraudRepository.findAllByRequestIdIn(List.of(r.getId())));
        });
        return request;
    }

    @Override
    public List<? extends Request> getAll() {
        return requestRepository.findAll(Sort.by(Sort.Direction.DESC, "desiredDate"));
    }

    @Override
    public List<? extends Request> getAllByAuthor(@NotNull UUID authorId) {
        return requestRepository.findAllByAuthorId(authorId);
    }

    @Override
    public List<ShareRideResponseDTO> getSuitableSharedRide(Request request, Employee loggedEmployee, String token) {
        LocalDateTime minDesiredDateTime = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(5);
        LocalDateTime correctDesiredDate = minDesiredDateTime;
        if (request.getDesiredDate() != null && !minDesiredDateTime.isAfter(request.getDesiredDate())) {
            correctDesiredDate = request.getDesiredDate();
        }
        correctDesiredDate = correctDesiredDate.withSecond(0).withNano(0);
        request.setDesiredDate(correctDesiredDate);
        List<SrmSharedRideDTO> suitableSharedRides = srmService.getSuitableSharedRides(mapper.requestToSrmRequestDTO((AbstractRequestForTnP) request),
                token);
        return suitableSharedRides.stream()
                .filter(this::isVisible)
                .map(this::mapToSharedRideResponseDTO)
                .peek(this::enrichDTOWithPassengers)
                .peek(this::enrichSharedRideRequestDTO)
                .toList();
    }

    @Override
    public ShareRideResponseDTO getLinkedSharedRide(UUID requestId, String token) {
        final var sharedRideDTO = srmService.getSharedRideByRequestId(requestId, token)
                .orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
        final var getSharedRideResponceDTO = mapToSharedRideResponseDTO(sharedRideDTO);
        enrichDTOWithPassengers(getSharedRideResponceDTO);
        enrichSharedRideRequestDTO(getSharedRideResponceDTO);
        return getSharedRideResponceDTO;
    }

    @Override
    public void approve(Request request) {
        log.info("approve() start: request = {}", request.getId());
        request = innerApprove(request);
        request = requestRepository.save(request);
        requestSenders.get(request.getTransportType()).send(request);
        log.info("approve() finish: request = {}", request.getId());
    }

    @Override
    public void approveFinalTrip(Request request, UUID actorEmployeeId) {
        getTransportTypeService(request.getTransportType()).approveFinalTrip(request, actorEmployeeId);
    }

    @Override
    public void updateApproved(Request request, ExpectedDataDTO newData) {
        checkStatusForUpdateApprovedTrip(request);
        var prevUpdate = updateRequestRepository.findByRequestId(request.getId());
        if (prevUpdate != null) {
            log.info("Found UpdateRequest for request '" + request.getId() + "' will removed");
            updateRequestRepository.delete(prevUpdate);
            updateRequestSender.sendDeleted(prevUpdate);
        }
        var expectedData = ExpectedData.builder()
                .cost(newData.getCost())
                .bonusCost(newData.getBonusCost())
                .distance(newData.getDistance())
                .time(newData.getTime())
                .build();
        var update = UpdateRequest.builder()
                .request(request)
                .segmentsJSON(newData.getSegments())
                .waypoints(newData.getWaypoints())
                .expected(expectedData)
                .build();
        updateRequestRepository.save(update);
        updateRequestSender.send(update);

    }

    @Override
    public void approveUpdateTrip(UUID updateId, UUID approvedByEmployeeId) {
        UpdateRequest updateRequest = getUpdateRequest(updateId);
        Request request = updateRequest.getRequest();
        checkStatusForUpdateApprovedTrip(request);
        request.setExpected(updateRequest.getExpected());
        request.getSegmentsJSON().clear();
        request.getSegmentsJSON().addAll(updateRequest.getSegmentsJSON());
        geoDataProcessingService.saveAddresses(request, mapper.waypointDTOListToWaypointList(updateRequest.getWaypoints()));
        updateRequestRepository.delete(updateRequest);
    }

    @Override
    public void declineUpdateTrip(UUID updateId, UUID approvedByEmployeeId, String message) {
        UpdateRequest updateRequest = getUpdateRequest(updateId);
        log.info("Update request '" + updateRequest.getId() + "' declined and removed");
        updateRequestRepository.delete(updateRequest);
    }

    @Override
    public Optional<UpdateRequest> findUpdateRequest(UUID requestId) {
        return Optional.ofNullable(updateRequestRepository.findByRequestId(requestId));
    }

    @Override
    public Boolean checkInAutomatic(Request request, CheckinDTO checkinDTO) {
        AbstractRequestForTnP requestForTnP = (AbstractRequestForTnP) request;
        boolean bManual = checkinRequestAutomatic(requestForTnP, checkinDTO);
        update(requestForTnP);
        return bManual;
    }

    @Override
    public Boolean checkInManual(Request request, CheckinDTO checkinDTO) {
        return false;
    }

    @Override
    public Boolean setAbsenceReason(Request request, CheckinDTO checkinDTO) {
        for (Waypoint waypoint : request.getWaypoints()) {
            if (DoubleUtil.doubleEqualsWithPrecision(waypoint.getAddress().getLatitude(), checkinDTO.getLatitude(),
                    DoubleUtil.EPSILON) &&
                    DoubleUtil.doubleEqualsWithPrecision(waypoint.getAddress().getLongitude(), checkinDTO.getLongitude(),
                            DoubleUtil.EPSILON) &&
                    !waypoint.isCheckinAutomatic() &&
                    (checkinDTO.getOrderingIndex() == null || checkinDTO.getOrderingIndex().equals(waypoint.getOrderingIndex()))
            ) {
                log.debug("setAbsenceReason: waypoint found with chekin automatic false");
                waypoint.setAbsenceReason(checkinDTO.getAbsenceReason());
                waypoint.setCheckinManual(true);
                waypoint.setCheckinManualTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean startTrip(Request request, CheckinDTO checkinDTO) {
        if (request.getTransportType() == TransportTypeEnum.PERSONAL) {
            var requestForPersonal = (RequestForPersonal) get(request.getId(), TransportTypeEnum.PERSONAL)
                    .orElseThrow(() -> new EntityNotFoundException(Request.class, request.getId()));
            if (requestForPersonal.getStatus() != TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS) {
                requestForPersonal.setTripStartTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
                requestForPersonal.setTripStartLatitude(checkinDTO.getLatitude());
                requestForPersonal.setTripStartLongitude(checkinDTO.getLongitude());
                update(requestForPersonal);
                changeState(request, TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS, request.getPassenger());
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean deleteWaypoint(Request request, CheckinDTO checkinDTO) {
        long activeWaypointCount = request.getWaypoints().stream()
                .filter(Waypoint::isActive)
                .count();
        if (activeWaypointCount < 3) {
            throw new UpdateRequestException("Ошибка при обновление заявки, активных точек должно быть не меньше двух");
        }
        var deleted = false;
        for (Waypoint waypoint : request.getWaypoints()) {
            if (DoubleUtil.doubleEqualsWithPrecision(waypoint.getAddress().getLatitude(), checkinDTO.getLatitude(),
                    DoubleUtil.EPSILON) &&
                    DoubleUtil.doubleEqualsWithPrecision(waypoint.getAddress().getLongitude(), checkinDTO.getLongitude(),
                            DoubleUtil.EPSILON) &&
                    (checkinDTO.getOrderingIndex() == null || checkinDTO.getOrderingIndex().equals(waypoint.getOrderingIndex()))
            ) {
                waypoint.setActive(false);
                deleted = true;
            }
        }
        if (!deleted) {
            throw new UpdateRequestException("Ошибка при обновление заявки, не удалось найти точку");
        }
        try {
            log.debug("Request recalculating, transport type is - " + request.getTransportType());
            tariffGrpcClient.recalculate(request);
        } catch (Exception e) {
            log.error("Error while recalculate tariff", e);
            throw new UpdateRequestException("Ошибка при обновление заявки, не удалось пересчитать маршрут");
        }
        requestSenders.get(request.getTransportType()).send(request);

        return true;
    }

    /**
     * @return список заявок
     */
    @Override
    public List<RequestForTaxi> findRequestsForTaxiWithDriverArrivedDeadlineViolation(LocalDateTime now) {
        return requestRepository.findRequestsForTaxiWithDriverArrivedDeadlineViolation(now, DeadlineState.NONE, TripRequestStatus.TAXI_CANCELLED);
    }

    @Override
    public List<RequestForGroupTransfer> findRequestsForGroupTransferWithDriverArrivedDeadlineViolation(LocalDateTime now) {
        return requestRepository.findRequestsForGroupTransferWithDriverArrivedDeadlineViolation(now, DeadlineState.NONE,
                TripRequestStatus.GROUP_TRANSFER_CANCELLED);
    }

    /**
     * @param request - заявка
     */
    @Override
    public void updateRequestForTaxiWithDriverArrivedDeadlineViolation(RequestForTaxi request) {
        doUpdate(request, TransportTypeEnum.TAXI);
    }

    @Override
    public void updateRequestForGroupTransferWithDriverArrivedDeadlineViolation(RequestForGroupTransfer request) {
        doUpdate(request, TransportTypeEnum.GROUP_TRANSFER);
    }

    private <R extends Request & HasHumanReadableId & DriverArrivedInfo & DeadlineInfo> void doUpdate(R request, TransportTypeEnum transportTypeEnum) {
        try {
            log.debug("Контрольный срок по подаче ТС для заявки {} был нарушен (deadlineState будет установлен в 'RED')",
                    request.getHumanReadableId());
            log.debug("request.getDriverArrivedDeadline() = {}", request.getDriverArrivedDeadline());
            log.debug("request.getDriverArrivedDatetime() = {}", request.getDriverArrivedDatetime());
            log.debug("request.getDeadlineState() = {}", request.getDeadlineState());
            request.setDeadlineState(DeadlineState.RED);
            var service = ReflectionUtils.<TransportTypeService<R>>cast(getServiceOpt(transportTypeEnum).orElse(null));
            if (service != null) {
                service.save(request);
            }
        } catch (Exception e) {
            // Ошибку выводим в лог, но замаскируем, чтобы не прерывать транзакцию для остальных заявок из списка
            log.error("Произошла непредвиденная ошибка при установке признака нарушения контрольного срока подачи ТС для заявки {}",
                    Optional.ofNullable(request).map(HasHumanReadableId::getHumanReadableId).orElse("Н/Д"));
            log.error(e.getMessage());
        }
    }

    /**
     * @param now                   - текущие дата/время
     * @param driverArrivedDatetime - время подачи транспортного средства
     * @param driverArrivedDeadline - контрольный срок подачи транспортного средства
     * @return - рассчитанное состояние контрольного срока, RED - нарушен, NONE - не нарушен
     */
    @Override
    public DeadlineState calcDriverArrivedDeadline(
            TransportTypeEnum transportType,
            LocalDateTime now,
            LocalDateTime driverArrivedDatetime,
            LocalDateTime driverArrivedDeadline
    ) {
        var serv = transportTypeService.stream()
                .filter(service -> service.getTransportType() == transportType)
                .findFirst()
                .orElseThrow(() -> new UnsupportedTransportTypeException(transportType));
        return serv.calcDriverArrivedDeadline(now, driverArrivedDatetime, driverArrivedDeadline);
    }

    @Override
    public void approveRequestForPersonalWithTripApprovalDeadlineViolation(RequestForPersonal request) {
        var requestDb = requestForPersonalRepository.getReferenceById(request.getId());
        requestDb.setTripApprovalDeadlineState(DeadlineState.RED);
        requestDb = requestForPersonalRepository.save(requestDb);
        changeState(requestDb, TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION);
    }

    @Override
    public void setPaymentDoneDeadlineState(RequestForPersonal request, DeadlineState deadlineState) {
        var requestDb = requestForPersonalRepository.getReferenceById(request.getId());
        requestDb.setPaymentDoneDeadlineState(deadlineState);
        requestDb.setSlaExpired(DeadlineState.RED.equals(deadlineState));
        requestForPersonalRepository.save(requestDb);
    }

    @Override
    public void setPaymentDoneDeadlineState(RequestForPublic request, DeadlineState deadlineState) {
        var requestDb = requestForPublicRepository.getReferenceById(request.getId());
        requestDb.setPaymentDoneDeadlineState(deadlineState);
        requestDb.setSlaExpired(DeadlineState.RED.equals(deadlineState));
        requestForPublicRepository.save(requestDb);
    }

    @Override
    @Transactional
    public GetRequestDTO convertToDto(Request source) {
        ExpectedDataDTO expectedDataDTO = mapper.expectedDataToDTO(source.getExpected());
        expectedDataDTO.getWaypoints().addAll(mapper.waypointListToDTOList(source.getWaypoints()));
        expectedDataDTO.getSegments().addAll(source.getSegmentsJSON());

        GetRequestDTO.GetRequestDTOBuilder<?, ?> builder;
        switch (source.getTransportType()) {
            case TAXI -> {
                var sourceForTaxi = (RequestForTaxi) source;
                builder = GetRequestDTO.builder()
                        .sharedRideId(sourceForTaxi.getRideId())
                        .commentForDriver(sourceForTaxi.getCommentForDriver())
                        .desiredDate(sourceForTaxi.getDesiredDate())
                        .passengerCount(sourceForTaxi.getPassengerCount())
                        .resolution(sourceForTaxi.getResolution())
                        .taxiClass(sourceForTaxi.getTaxiClass())
                        .busCount(sourceForTaxi.getBusCount())
                        .busRentDuration(sourceForTaxi.getBusRentDuration())
                        .timeZone(sourceForTaxi.getTimeZone());
            }
            case PERSONAL -> {
                RequestForPersonal sourceForPersonal = (RequestForPersonal) source;
                builder = GetPersonalRequestDTO.builder()
                        .sharedRideId(sourceForPersonal.getRideId())
                        .commentForDriver(sourceForPersonal.getCommentForDriver())
                        .desiredDate(sourceForPersonal.getDesiredDate())
                        .passengerCount(sourceForPersonal.getPassengerCount())
                        .personalCar(sourceForPersonal.getPersonalCar())
                        .timeZone(sourceForPersonal.getTimeZone());
            }
            default -> builder = GetRequestDTO.builder();
        }

        return builder.id(source.getId())
                .humanReadableId(source.getHumanReadableId())
                .approvalState(source.getApprovalState())
                .approvedBy(source.getApprovedBy() != null
                        ? getEmployeeDto(source)
                        : null)
                .status(source.getStatus())
                .author(employeeMapper.toDto(employeeService.get(source.getAuthor().getId()).orElseThrow()))
                .creationTime(source.getCreationTime())
                .expected(expectedDataDTO)
                .passenger(employeeMapper.toDto(employeeService.get(source.getPassenger().getId()).orElseThrow()))
                .purpose(mapper.tripPurposeToDTO(source.getPurpose()))
                .tariffId(source.getTariffId())
                .outcomeTariffId(source.getOutcomeTariffId())
                .requestOptions(source.getRequestOptions())
                .transportType(source.getTransportType())
                .timeZone(source.getTimeZone())
                .build();
    }

    private Optional<AbstractTransportTypeService<?>> getServiceOpt(TransportTypeEnum transportType) {
        return transportTypeService.stream()
                .filter(service -> service.getTransportType() == transportType)
                .findFirst();
    }

    private Boolean checkinRequestAutomatic(AbstractRequestForTnP requestForTnP, CheckinDTO checkinDTO) {
        for (Waypoint waypoint : requestForTnP.getWaypoints()) {
            boolean checkinOnlyManual = waypoint.isCheckinOnlyManual();
            boolean checkinAutomatic = waypoint.isCheckinAutomatic();
            if (!checkinOnlyManual && !checkinAutomatic && isInsideRadius(checkinDTO, waypoint, waypoint.getRadius()) &&
                    (checkinDTO.getOrderingIndex() == null || checkinDTO.getOrderingIndex().equals(waypoint.getOrderingIndex()))) {
                waypoint.setCheckinAutomatic(true);
                waypoint.setCheckinAutomaticTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
                return true;
            }
        }
        return false;
    }

    private boolean isInsideRadius(CheckinDTO checkinDTO, Waypoint waypoint2, Integer radius) {
        double distance = calculateDistanceInMeter(checkinDTO.getLatitude(), checkinDTO.getLongitude(),
                waypoint2.getAddress().getLatitude(),
                waypoint2.getAddress().getLongitude());
        return distance <= Double.valueOf(radius);
    }

    private static final double AVERAGE_RADIUS_OF_EARTH_METER = 6371000;

    private double calculateDistanceInMeter(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lat1))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double result = (AVERAGE_RADIUS_OF_EARTH_METER * c);

        BigDecimal bd = BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void checkStatusForUpdateApprovedTrip(Request request) {
        if ((request.getStatus() == null) ||
                !request.getStatus().between(TripRequestStatus.getApprovedStatusByTransportType(request.getTransportType()),
                        TripRequestStatus.getCompletedStatusByTransportType(
                                request.getTransportType()))) {
            throw new IllegalStateResponseException(
                    String.format("Cannot edit request in %s status", request.getStatus()));
        }
    }

    private UpdateRequest getUpdateRequest(UUID updateId) {
        return updateRequestRepository.findById(updateId).orElseThrow(
                () -> new EntityNotFoundException(UpdateRequest.class, updateId));
    }

    /**
     * check and set statuses
     */
    private Request innerApprove(Request request) {
        return getTransportTypeService(request.getTransportType()).approveRequest(request);
    }

    private AbstractTransportTypeService<? extends Request> getTransportTypeService(TransportTypeEnum transportType) {
        return transportTypeService.stream()
                .filter(service -> service.getTransportType() == transportType)
                .findFirst()
                .orElseThrow(() -> new UnsupportedTransportTypeException(transportType));
    }

    private boolean isSpendingFromBonusAccountAllowed(TransportTypeEnum transportType) {
        return transportType == TransportTypeEnum.TAXI;
    }

    private void update(Request request) {
        transportTypeService.stream()
                .filter(service -> service.getTransportType() == request.getTransportType())
                .map(service -> service.save(ReflectionUtils.cast(request)))
                .forEach(it -> requestSenders.get(it.getTransportType()).send(it));
    }

    // requests that are not approved are not shown in getSuitable
    private boolean isVisible(SrmSharedRideDTO sharedRide) {
        var result = false;
        var approvedStatuses = Arrays.asList(TripRequestStatus.PERSONAL_APPROVED,
                TripRequestStatus.TAXI_APPROVED,
                TripRequestStatus.CARSHARING_APPROVED);
        for (SrmRequestKpiDTO requestKpiDTO : sharedRide.getRequestKpiList()) {
            var requestOpt = requestRepository.findById(requestKpiDTO.getId());
            if (requestOpt.isPresent() && isSharedRideOwner(requestOpt.get()) && approvedStatuses.contains(requestOpt.get().getStatus())) {
                result = true;
            }
        }
        return result;
    }

    private boolean isSharedRideOwner(Request request) {
        return ((request instanceof RequestForPersonal requestForPersonal && requestForPersonal.isSharedRideOwner()) ||
                (request instanceof RequestForTaxi requestForTaxi && requestForTaxi.isSharedRideOwner()) ||
                (request instanceof RequestForCarsharing requestForCarsharing && requestForCarsharing.isSharedRideOwner()));
    }

    private void enrichDTOWithPassengers(@NonNull ShareRideResponseDTO suitableSharedRide) {
        final var requests = suitableSharedRide.getKpi().getOrdersKpi()
                .stream()
                .map(OrderKpiDTO::getOrderId)
                .map(requestRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        var passengers = requests.stream().map(req -> requestMapper.employeeToDto(req.getPassenger())).toList();
        var positionIds =
                passengers.stream().filter(Objects::nonNull)
                        .map(ru.sber.transport.magenta.model.EmployeeDTO::getPositionId)
                        .filter(Objects::nonNull)
                        .toList();
        var positions = positionRepository.findAllByIdIn(positionIds)
                .stream().collect(Collectors.toMap(Position::getId, Function.identity()));
        passengers.stream()
                .filter(p -> Objects.nonNull(p) && Objects.nonNull(p.getId()))
                .forEach(p -> p.setPositionName(positions.get(p.getPositionId()).getPositionName()));

        suitableSharedRide.setEmployeePassengers(passengers);
        var joinedPassengerIds = requests.stream().map(Request::getJoinedPassengerIds)
                .flatMap(Collection::stream).collect(Collectors.toSet());
        var joinedPassengers = employeeService.getByEmployeeIds(joinedPassengerIds).values().stream().map(requestMapper::employeeToDto).toList();
        suitableSharedRide.setJoinedPassengers(joinedPassengers);
    }

    private void enrichSharedRideRequestDTO(ShareRideResponseDTO shareRideResponseDTO) {
        var requests = requestRepository.findAllById(shareRideResponseDTO.getNewOrdersIds().stream().map(UUID::fromString).toList());
        requests.stream().map((r -> mapToSharedRideResponseRequestDTO(shareRideResponseDTO, r)))
                .forEach(r -> shareRideResponseDTO.getRequests().add(r));
    }

    private ShareRideResponseDTO.SharedRideResponseRequestDTO mapToSharedRideResponseRequestDTO(ShareRideResponseDTO responseDTO, Request request) {
        var dto = ShareRideResponseDTO.SharedRideResponseRequestDTO.builder();
        dto
                .id(request.getId())
                .humanReadableId(request.getHumanReadableId())
                .employee(responseDTO.getEmployeePassengers().stream().filter(e -> e.getId().equals(request.getPassenger().getId())).findFirst()
                        .orElse(null))
                .status(request.getStatus())
                .statusCode(request.getStatusCode())
                .statusDescription(resolveStatusDescription(request));
        if (request instanceof RequestForPersonal requestForPersonal) {
            var sharedRideOwner = requestForPersonal.isSharedRideOwner();
            dto.sharedRideOwner(sharedRideOwner);
            if (sharedRideOwner) {
                dto.car(requestForPersonal.getPersonalCar());
            }
            dto.commentForDriver(requestForPersonal.getCommentForDriver());
        } else if (request instanceof RequestForTaxi requestForTaxi) {
            var sharedRideOwner = requestForTaxi.isSharedRideOwner();
            dto.sharedRideOwner(sharedRideOwner);
            if (sharedRideOwner) {
                responseDTO.setTaxiClass(requestForTaxi.getTaxiClass());
            }
        }
        return dto.build();
    }

    private @NonNull ShareRideResponseDTO mapToSharedRideResponseDTO(@NonNull SrmSharedRideDTO sharedRideDTO) {
        final var requestKpiList = sharedRideDTO.getRequestKpiList();
        requestKpiList.removeIf(Objects::isNull);

        final var sharedRide = new ShareRideResponseDTO();
        sharedRide.setId(sharedRideDTO.getId().toString());
        sharedRide.setTransportType(sharedRideDTO.getTransportType());
        sharedRide.setPassengers(requestKpiList.stream().mapToInt(SrmRequestKpiDTO::getRequiredPassengers).sum());

        final var newOrdersIds = requestKpiList.stream()
                .map(SrmRequestKpiDTO::getId)
                .map(UUID::toString)
                .toList();
        sharedRide.setNewOrdersIds(newOrdersIds);
        List<MagentaWaypointResponseDTO> stops = new ArrayList<>();
        for (SrmWaypointFinalDTO waypointGetDTO : sharedRideDTO.getWaypointsFinal()) {
            ZonedDateTime startTime = null;
            if (waypointGetDTO.getStartTime() != null) {
                startTime = waypointGetDTO.getStartTime().withZoneSameInstant(ZoneId.of(sharedRideDTO.getTimeZone()));
            }
            ZonedDateTime endTime = null;
            if (waypointGetDTO.getEndTime() != null) {
                endTime = waypointGetDTO.getEndTime().withZoneSameInstant(ZoneId.of(sharedRideDTO.getTimeZone()));
            }
            MagentaWaypointResponseDTO magentaWaypoint = new MagentaWaypointResponseDTO();
            magentaWaypoint.setAddress(trimAddress(waypointGetDTO.getAddress()));
            magentaWaypoint.setLatitude(waypointGetDTO.getLatitude());
            magentaWaypoint.setLongitude(waypointGetDTO.getLongitude());
            magentaWaypoint.setEventType(TaxiStopType.valueOf(waypointGetDTO.getEventType()).getRusName());
            magentaWaypoint.setStartTime(startTime);
            magentaWaypoint.setEndTime(endTime);

            stops.add(magentaWaypoint);
        }
        sharedRide.setStops(stops);

        KpiDTO kpiDTO = new KpiDTO();
        kpiDTO.setTotalCost(sharedRideDTO.getRideCost().doubleValue());
        kpiDTO.setTotalDistanceKm(sharedRideDTO.getRideDistance());
        kpiDTO.setTotalTimeMin(sharedRideDTO.getRideTime().intValue() / 60);

        final var ordersKpi = requestKpiList.stream().map(this::getOrderKpiDTO).toList();
        kpiDTO.setOrdersKpi(ordersKpi);
        sharedRide.setKpi(kpiDTO);

        return sharedRide;
    }

    private OrderKpiDTO getOrderKpiDTO(SrmRequestKpiDTO requestKpiDTO) {
        final var orderKpiDTO = new OrderKpiDTO();
        orderKpiDTO.setOrderId(requestKpiDTO.getId());
        orderKpiDTO.setOrderDistanceKm(requestKpiDTO.getRequestDistance().intValue());
        orderKpiDTO.setRideTimeMin(requestKpiDTO.getRequestTime().intValue() / 60);
        orderKpiDTO.setOrderPriceKop(requestKpiDTO.getRequestPrice());
        orderKpiDTO.setCostSharePart(requestKpiDTO.getCostSharePart());
        orderKpiDTO.setSavings((double) requestKpiDTO.getSavingsCash() / 100);
        orderKpiDTO.setSavingsPct(requestKpiDTO.getSavingsProcents());
        orderKpiDTO.setCandidate(requestKpiDTO.isCandidate());
        return orderKpiDTO;
    }

    private String resolveStatusDescription(Request request) {
        if (TripRequestStatus.getCanceledStatuses().contains(request.getStatus())) {
            return cancelStatusDescription;
        }
        return null;
    }

    private String trimAddress(String str) {
        List<String> list = new ArrayList<>();
        String[] strSplit = str.split(",");
        for (String s : strSplit) {
            if (s == null || "null".equals(s.trim())) {
                continue;
            }
            list.add(s);
        }
        return String.join(",", list);
    }

    private EmployeeDTO getEmployeeDto(Request source) {
        return employeeMapper.toDto(employeeService.get(source.getApprovedBy().getId()).orElseThrow());
    }

}
