package ru.sberbank.ditsib.transport.request.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.FraudRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPublicRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapper;
import ru.sberbank.ditsib.transport.request.mappers.FraudMapper;
import ru.sberbank.ditsib.transport.request.mappers.PositionMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.RequestControllerService;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.service.RequestValidationService;
import ru.sberbank.ditsib.transport.request.service.TaxiPriceService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;
import ru.sberbank.ditsib.transport.request.service.search.RequestSearchService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_CANCELLED;

/**
 * Implementation of controller for working with requests.
 */
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class RequestControllerServiceImpl implements RequestControllerService {

    private final RequestService requestService;

    private final RequestValidationService requestValidationService;

    private final RequestSearchService requestSearchService;

    private final EntityDTOMapper mapper;

    private final EmployeeMapper employeeMapper;

    private final EmployeeService employeeService;

    private final PositionService positionService;

    private final PositionMapper positionMapper;

    private final DepartmentService departmentService;

    private final PersonalCarDataResolver personalCarDataResolver;

    private final List<TaxiPriceService> taxiPriceServiceList;

    private final Map<TransportTypeEnum, RequestSender<Request>> senders;

    private final RequestForTaxiRepository requestForTaxiRepository;

    private final RequestForPublicRepository requestForPublicRepository;

    private final Map<TransportTypeEnum, Evaluator> evaluators;

    private final FraudRepository fraudRepository;

    private final FraudMapper fraudMapper;

    @Value("${external-prices.timeout:2000}")
    private int externalPriceTimeout;

    @Value("${external-prices.include-zero-price:false}")
    private boolean includeZeroPrice;

    @Override
    public GetRequestDTO add(NewRequestDTO newRequest, Employee employee, String token, List<RegionDto> regions) {
        final var request = requestService.add(null, newRequest.isCoopTrip(), newRequest, employee, token, regions);
        senders.get(request.getTransportType()).send(request);
        return requestService.convertToDto(request);
    }

    @Override
    public void edit(UUID requestId, RequestDTO newData, Employee activeUser, String token) {
        var saved = requestValidationService.validateAndGetRequest(requestId);
        requestValidationService.checkUserEditPermission(saved, activeUser);
        Set<TripRequestStatus> allowed = Arrays.stream(TripRequestStatus.values())
                .filter(TripRequestStatus::isEditable)
                .collect(Collectors.toSet());
        if (saved.getTransportType() == TransportTypeEnum.PERSONAL) {
            allowed.add(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS);
        }
        requestValidationService.checkRequestStatusByAllowedStatuses(saved.getStatus(),
                allowed);
        requestValidationService.checkRequestStatusByIllegalStatuses(saved.getStatus(),
                Set.of(TripRequestStatus.TAXI_APPROVED,
                        TripRequestStatus.PERSONAL_APPROVED,
                        TripRequestStatus.CARSHARING_APPROVED));
        final var request = requestService.update(newData, activeUser, token);
        senders.get(request.getTransportType()).send(request);
    }

    @Override
    public GetRequestDTO addRequestToSharedRide(UUID rideId, NewRequestDTO newRequest, Employee employee, String token) {
        final var request = requestService.add(rideId, true, newRequest, employee, token);
        senders.get(request.getTransportType()).send(request);
        return requestService.convertToDto(request);
    }

    @Override
    public void editApproved(UUID requestId, ExpectedDataDTO newData, Employee callerEmployee) {
        var saved = requestValidationService.validateAndGetRequest(requestId);
        requestValidationService.checkUserEditPermission(saved, callerEmployee);
        requestValidationService.checkRequestStatusByAllowedStatuses(saved.getStatus(),
                TripRequestStatus.getApprovedStatuses().stream()
                        .collect(Collectors.toUnmodifiableSet()));
        requestService.updateApproved(saved, newData);
    }

    @Override
    public List<ShareRideResponseDTO> getSuitableSharedRide(NewRequestDTO newRequest, Employee loggedEmployee, String token) {
        if (newRequest.getExpected().getWaypoints().size() < 2
                || newRequest.getExpected().getWaypoints().get(0).equalsByCoords(newRequest.getExpected().getWaypoints().get(1))) {
            return Collections.emptyList();
        }
        return switch (newRequest.getTransportType()) {
            case TAXI ->
                    requestService.getSuitableSharedRide(mapper.newDTOToRequestForTaxi(newRequest), loggedEmployee, token);
            case PERSONAL ->
                    requestService.getSuitableSharedRide(mapper.newDTOToRequestForPersonal(newRequest), loggedEmployee, token);
            default -> new ArrayList<>();
        };
    }

    @Override
    public ShareRideResponseDTO getLinkedSharedRide(UUID requestId, String token) {
        return requestService.getLinkedSharedRide(requestId, token);
    }

    @Override
    public List<TaxiPriceDto> calculateRequestExternalPrices(
            ExternalPriceDTO externalPriceDTO,
            Employee employee, String token
    ) {
        var tasks = taxiPriceServiceList.stream()
                .filter(TaxiPriceService::isEnabled)
                .map(e -> getPrice(e, externalPriceDTO))
                .toList();
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        return tasks.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .filter(taxiPriceDto -> taxiPriceDto.getPrice() > 0 || includeZeroPrice)
                .toList();
    }

    @Override
    public GetRequestDTO getWithUpdate(UUID requestId, Employee callerEmployee) {
        var saved = requestValidationService.validateAndGetUpdateRequest(requestId);
        requestValidationService.checkUserEditPermission(saved.getRequest(), callerEmployee);

        final GetRequestDTO requestDTO = mapGetRequestDTO(saved.getRequest());
        if (requestDTO == null) {
            throw new RuntimeException("RequestDTO is null for requestId: " + requestId);
        }
        requestDTO.setExpected(mapper.expectedDataToDTO(saved.getExpected()));
        setList(requestDTO.getExpected().getSegments(), saved.getSegmentsJSON());
        setList(requestDTO.getExpected().getWaypoints(), saved.getWaypoints());
        return requestDTO;
    }

    @Override
    public void cancel(UUID requestId, UUID userId, Optional<CancelDTO> optionalCancelDTO, String token)
            throws NoSuchFieldException, IllegalAccessException {
        var toCancel = requestValidationService.validateAndGetRequest(requestId);
        var activeUser = employeeService.getByUserId(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
        if (!(activeUser.getId().equals(toCancel.getAuthor().getId())
                || activeUser.getId().equals(toCancel.getPassenger().getId()))) {
            throw new IllegalCallerResponseException("Only author or passenger can cancel request");
        }

        CancelDTO cancelDTO = requestValidationService.validateStatusAndGetCancelDto(toCancel.getStatus(),
                toCancel.getTransportType(),
                optionalCancelDTO);
        requestService.cancel(toCancel, cancelDTO, activeUser, false);
    }

    @Override
    public GetRequestDTO complete(UUID requestId, Employee activeUser) {
        var request = requestValidationService.validateAndGetRequest(requestId);
        return mapGetRequestDTO(requestService.complete(request, activeUser));
    }

    @Override
    public GetRequestDTO changeStatus(
            UUID requestId, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO,
            String token
    ) {
        var toChange = requestValidationService.validateAndGetRequest(requestId);

        requestValidationService.validateCurrentRequestStatus(toChange);
        requestValidationService.validateNewRequestStatus(toChange, newStatus);

        if (TripRequestStatus.getTripFinishStatuses().contains(newStatus)) {
            return mapGetRequestDTO(requestService.finish(toChange, activeUser));
        }
        if (TripRequestStatus.getCanceledStatuses().contains(newStatus)) {
            requestService.cancel(toChange, CancelDTO.builder()
                    .reason("Отмена через монитор")
                    .build(), activeUser, true);
            return mapGetRequestDTO(requestService.get(requestId).orElseThrow(() -> new EntityNotFoundException(Request.class, requestId)));
        }
        return mapGetRequestDTO(requestService.changeState(toChange, newStatus, activeUser, changeStatusDTO));
    }

    @Override
    public GetRequestDTO rate(@NotNull UUID requestId, RequestRatingDTO ratingDTO, Employee user) {
        var request = requestValidationService.validateAndGetRequest(requestId);
        final var evaluator = evaluators.get(request.getTransportType());
        final var requestRating = mapper.dtoToRequestRating(ratingDTO);
        request = evaluator.rate(request, requestRating, user);
        return mapGetRequestDTO(request);
    }

    @Override
    public List<? extends GetRequestDTO> getAll() {
        return mapGetRequestDTOs(requestService.getAll());
    }

    @Override
    public List<? extends GetRequestDTO> getAllByAuthor(UUID authorId) {
        return mapGetRequestDTOs(requestService.getAllByAuthor(authorId));
    }

    @Override
    public <R extends GetRequestDTO> R get(UUID requestId) {
        return mapGetRequestDTO(requestValidationService.validateAndGetRequest(requestId));
    }

    @Override
    public Page<? extends GetRequestDTO> getPersonalRequestAndStatus(boolean isTerminateStatus, Employee employee, Pageable page) {
        Page<? extends Request> requestSearch = requestService.getByTerminateStatusAndEmployee(isTerminateStatus, employee, page);

        return new PageImpl<>(
                mapGetRequestDTOs(requestSearch.stream()
                        .toList()),
                requestSearch.getPageable(),
                requestSearch.getTotalElements());
    }

    @Override
    public <R extends GetRequestDTO> R get(@NotNull UUID requestId, TransportTypeEnum transportType) {
        final var request = requestValidationService.validateAndGetRequest(requestId, transportType);
        return mapGetRequestDTO(request);
    }

    @Override
    public List<RequestHistoryElementDTO> getHistory(UUID requestId, TransportTypeEnum transportType) {
        var history = requestService.getHistory(requestId, transportType);
        var employeeByIdMap = employeeService.getByEmployeeIds(history.stream()
                .map(RequestHistoryElement::getInitiator)
                .collect(Collectors.toSet()));
        return history.stream()
                .map(h -> mapper.requestHistoryElementToDTO(h, employeeByIdMap))
                .toList();
    }

    @Override
    public Collection<PaymentStateResponseDTO> updatePaymentStatus(
            Collection<PaymentStateRequestDTO> paymentStateRequestDTOs, Employee employee
    ) {
        var result = new ArrayList<PaymentStateResponseDTO>();
        for (PaymentStateRequestDTO paymentStateRequestDTO : paymentStateRequestDTOs) {
            var requestOpt = requestService.get(paymentStateRequestDTO.getRequestId());
            if (requestOpt.isPresent()) {
                var request = requestOpt.get();
                TripRequestStatus status;
                if (paymentStateRequestDTO.isPayed()) {
                    status = (request.getTransportType().equals(TransportTypeEnum.PERSONAL) ?
                            TripRequestStatus.PERSONAL_PAYMENT_DONE :
                            TripRequestStatus.PUBLIC_PAYMENT_DONE);
                } else {
                    status = (request.getTransportType().equals(TransportTypeEnum.PERSONAL) ?
                            TripRequestStatus.PERSONAL_PAYMENT_DECLINED :
                            TripRequestStatus.PUBLIC_PAYMENT_NOT_DONE);
                }
                var resultRequest = requestService.changeState(request, status, employee);
                result.add(
                        new PaymentStateResponseDTO(resultRequest.getId(), resultRequest.getStatus().name(),
                                null));
            } else {
                result.add(new PaymentStateResponseDTO(paymentStateRequestDTO.getRequestId(), null,
                        "Заявка не существует"));
            }
        }
        return result;
    }

    @Override
    public Collection<? extends GetRequestDTO> getBySearchParam(
            Optional<UUID> authorId, Optional<UUID> passengerId, Optional<UUID> approvedById,
            Optional<Boolean> approvedFlag, Optional<Boolean> coopTrip, Optional<String> comment,
            Optional<UUID> requestId, Optional<Set<Integer>> statuses, Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
            Optional<Set<UUID>> tripPurposes, Optional<String> fio,
            Employee authenticated
    ) {
        return mapGetRequestDTOs(requestSearchService.getBySearchParam(authorId,
                passengerId,
                approvedById,
                approvedFlag,
                coopTrip,
                comment,
                requestId,
                statuses,
                dateFrom,
                dateTo,
                minRideCost,
                maxRideCost,
                tripPurposes,
                fio,
                authenticated));
    }

    @Override
    public Page<? extends GetRequestDTO> generalSearch(RequestSearchDTO requestSearchDTO, RequestProjection projection) {
        return generalSearch(requestSearchDTO, Pageable.unpaged(), projection);
    }

    @Override
    public Page<? extends GetRequestDTO> generalSearch(RequestSearchDTO requestSearchDTO, Pageable pageable, RequestProjection projection) {
        Page<? extends Request> requestSearch = requestSearchService.generalSearch(requestSearchDTO, pageable);

        var result = mapGetRequestDTOs(requestSearch.stream()
                .toList());
        if (RequestProjection.SELECT.equals(projection)) {
            result.forEach(reqDTO -> reqDTO.setExpected(reqDTO.getExpected().toBuilder().segments(null).build()));
        }

        return new PageImpl<>(
                result,
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<? extends GetRequestDTO> taxiSearch(RequestTaxiSearchDTO requestSearchDTO) {
        Page<RequestForTaxi> requestSearch = requestSearchService.taxiSearch(requestSearchDTO);

        return new PageImpl<>(
                mapGetRequestDTOs(requestSearch.stream()
                        .toList()),
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<? extends GetRequestDTO> taxiSearch(RequestTaxiSearchDTO requestSearchDTO, Pageable pageable) {
        Page<RequestForTaxi> requestSearch = requestSearchService.taxiSearch(requestSearchDTO, pageable);

        return new PageImpl<>(
                mapGetRequestDTOs(requestSearch.stream()
                        .toList()),
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<? extends GetRequestDTO> personalSearch(RequestPersonalSearchDTO requestSearchDTO, String token) {
        Page<RequestForPersonal> requestSearch = requestSearchService.personalSearch(requestSearchDTO);

        List<GetPersonalRequestDTO> personalRequestDTOList = requestSearch.stream()
                .map(mapper::requestToGetPersonalDTO)
                .toList();
        enrichEmployeesWithOrganizationIds(personalRequestDTOList);
        enrichEmployeesWithPositionName(personalRequestDTOList);

        for (var personalRequestDTO : personalRequestDTOList) {
            if (personalRequestDTO.getPersonalCarId() != null) {
                try {
                    var passenger = personalRequestDTO.getPassenger();
                    var personalCar = personalCarDataResolver.getPersonalCar(
                            passenger.organizationId(),
                            passenger.departmentId(),
                            passenger.id(),
                            personalRequestDTO.getPersonalCarId(),
                            token
                    );
                    personalRequestDTO.setPersonalCar(personalCar);
                } catch (Exception e) {
                    log.error("Error handle transportDataResolver for request '{}', personal car id '{}'", personalRequestDTO.getId(), personalRequestDTO.getPersonalCarId(), e);
                }
            }
        }

        return new PageImpl<>(
                personalRequestDTOList,
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<? extends GetRequestDTO> publicSearch(RequestPublicSearchDTO requestSearchDTO) {
        Page<RequestForPublic> requestSearch = requestSearchService.publicSearch(requestSearchDTO);

        return new PageImpl<>(
                mapGetRequestDTOs(requestSearch.stream()
                        .toList()),
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Page<? extends GetRequestDTO> carsharingSearch(RequestCarsharingSearchDTO requestSearchDTO) {
        Page<RequestForCarsharing> requestSearch = requestSearchService.carsharingSearch(requestSearchDTO);

        return new PageImpl<>(
                mapGetRequestDTOs(requestSearch.stream()
                        .toList()),
                requestSearch.getPageable(),
                requestSearch.getTotalElements()
        );
    }

    @Override
    public Boolean checkInAutomatic(CheckinDTO checkinDTO) {
        Request request = requestValidationService.validateAndGetRequest(checkinDTO.getRequestId());
        return requestService.checkInAutomatic(request, checkinDTO);
    }

    @Override
    public Boolean checkInManual(CheckinDTO checkinDTO) {
        Request request = requestValidationService.validateAndGetRequest(checkinDTO.getRequestId());
        return requestService.checkInManual(request, checkinDTO);
    }

    @Override
    public Boolean setAbsenceReason(CheckinDTO checkinDTO) {
        Request request = requestValidationService.validateAndGetRequest(checkinDTO.getRequestId());
        return requestService.setAbsenceReason(request, checkinDTO);
    }

    @Override
    public Boolean startTrip(CheckinDTO checkinDTO) {
        Request request = requestValidationService.validateAndGetRequest(checkinDTO.getRequestId());
        return requestService.startTrip(request, checkinDTO);
    }

    @Override
    public Boolean deleteWaypoint(CheckinDTO checkinDTO) {
        Request request = requestValidationService.validateAndGetRequest(checkinDTO.getRequestId());
        log.debug("Request for delete waypoint found, transport type is - {}", request.getTransportType());
        return requestService.deleteWaypoint(request, checkinDTO);
    }

    private Map<UUID, UUID> getDepartmentOrganizationUUIDMap(Set<UUID> departmentIds) {
        return departmentService.getByIds(departmentIds).stream().collect(Collectors.toMap(Department::getId,
                department -> department.getOrganization().getId()));
    }

    private List<? extends GetRequestDTO> mapGetRequestDTOs(List<? extends Request> result) {
        final var requestDTOS =
                result.stream().map(item ->
                                switch (item.getTransportType()) {
                                    case TAXI -> toTaxiResponse(item);
                                    case PERSONAL -> mapper.requestToGetPersonalDTO((RequestForPersonal) item);
                                    case PUBLIC -> {
                                        var getPublicRequestDTO = mapper.requestToGetPublicDTO((RequestForPublic) item);
                                        yield enrichTransportCompensation(getPublicRequestDTO, ((RequestForPublic) item));
                                    }
                                    case CARSHARING -> mapper.requestToGetCarsharingDTO((RequestForCarsharing) item);
                                    case GROUP_TRANSFER -> mapper.requestToGetGroupTransferDTO((RequestForGroupTransfer) item);
                                    default -> mapper.requestToGetDTO(item);
                                })
                        .toList();
        enrichEmployeesWithOrganizationIds(requestDTOS);
        enrichEmployeesWithPositionName(requestDTOS);
        enrichEmployeesFraudData(requestDTOS);
        return requestDTOS;
    }

    private void enrichEmployeesFraudData(List<GetRequestDTO> requestDTOS) {
        final var fraudMap = fraudRepository.findAllByRequestIdIn(requestDTOS.stream().map(GetRequestDTO::getId).toList())
                .stream()
                .collect(Collectors.toMap(it -> it.getRequest().getId(), List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));

        requestDTOS.forEach(dto ->
                Optional.ofNullable(fraudMap.get(dto.getId()))
                        .map(fraudMapper::constructFraudComments)
                        .ifPresent(dto::setFraudComment)
        );
    }

    private <R extends GetRequestDTO> R mapGetRequestDTO(Request request) {
        var requestDTO =
                switch (request.getTransportType()) {
                    case TAXI -> toTaxiResponse(request);
                    case PERSONAL -> mapper.requestToGetPersonalDTO((RequestForPersonal) request);
                    case PUBLIC -> {
                        var getPublicRequestDTO = mapper.requestToGetPublicDTO((RequestForPublic) request);
                        yield enrichTransportCompensation(getPublicRequestDTO, ((RequestForPublic) request));
                    }
                    case CARSHARING -> mapper.requestToGetCarsharingDTO((RequestForCarsharing) request);
                    case GROUP_TRANSFER -> toGroupTransferResponse((RequestForGroupTransfer) request);
                    default -> mapper.requestToGetDTO(request);
                };

        assert requestDTO != null;
        enrichApprover(List.of(requestDTO));
        enrichEmployeesWithOrganizationIds(List.of(requestDTO));
        enrichEmployeesWithPositionName(List.of(requestDTO));
        enrichJoinedPassengers(request, requestDTO);
        enrichKPI(request, requestDTO);
        enrichPayRequestIds(request, requestDTO);
        enrichFraudData(request, requestDTO);
        return ReflectionUtils.cast(requestDTO);
    }

    private void enrichFraudData(Request request, GetRequestDTO requestDTO) {
        requestDTO.setFraudComment(fraudMapper.constructFraudComments(fraudRepository.findAllByRequestIdIn(List.of(request.getId()))));
    }

    private GetPublicRequestDTO enrichTransportCompensation(GetPublicRequestDTO getPublicRequestDTO, RequestForPublic request) {
        getPublicRequestDTO.getTransportCompensation()
                .forEach(tc -> request
                        .getCompensationDocuments()
                        .stream()
                        .filter(cd -> Objects.equals(cd.getId(), tc.getAttachedDocumentId()))
                        .findFirst()
                        .map(cd -> CompensationDocumentDTO.builder()
                                .id(cd.getId())
                                .folder(cd.getFolder())
                                .fileName(cd.getFileName())
                                .fileFormat(cd.getFileFormat().getFileFormat())
                                .fileSize(cd.getFileSize())
                                .build())
                        .ifPresent(tc::setCompensationDocumentDTO));

        return getPublicRequestDTO;
    }

    private void enrichKPI(Request request, GetRequestDTO dto) {
        if (request instanceof AbstractRequestForTnPnC coop && coop.isCoopTrip()) {
            dto.setCostSharePart(coop.getCostSharePart());
        }
    }

    private void enrichPayRequestIds(Request request, GetRequestDTO dto) {
        if (request instanceof RequestForPersonal) {
            var ids = requestForPublicRepository.findAllByPayRequestId(request.getId());
            ((GetPersonalRequestDTO) dto).setPayRequestIds(ids);
        } else if (request instanceof RequestForPublic r) {
            ((GetPublicRequestDTO) dto).setPayRequestIds(r.getPayRequestId() != null ? List.of(r.getPayRequestId()) : Collections.emptyList());
        }
    }

    private void enrichJoinedPassengers(Request request, GetRequestDTO dto) {
        if (request.getJoinedPassengerIds() == null) {
            return;
        }
        var passengers = employeeService.getByEmployeeIds(request.getJoinedPassengerIds());
        passengers.values().stream().map(employeeMapper::toDto).forEach(p -> dto.getJoinedPassengers().add(p));
    }

    private GetRequestDTO toGroupTransferResponse(RequestForGroupTransfer request) {
        var requestDTO = mapper.requestToGetGroupTransferDTO(request);
        requestDTO.setDriverInfo(getDriverInfo(request));
        return requestDTO;
    }

    private GetRequestDTO toTaxiResponse(Request request) {
        var requestDTO = mapper.requestToGetDTO((RequestForTaxi) request);
        var requestForTaxi = requestForTaxiRepository.findById(request.getId());
        if (requestForTaxi.isPresent()) {
            Driver driver = requestForTaxi.get().getDriver();
            TaxiTrip taxiTrip = requestForTaxi.get().getTaxiTrip();
            if (driver != null) {
                requestDTO.setDriver(DriverDTO.builder()
                        .contactPhone(driver.getContactPhone())
                        .firstName(driver.getFirstName())
                        .lastName(driver.getLastName())
                        .patronymic(driver.getPatronymic())
                        .rating(driver.getRating())
                        .build());
            }
            if (taxiTrip != null && taxiTrip.getAssignedCar() != null) {
                requestDTO.setVehicle(VehicleDTO.builder()
                        .brand(taxiTrip.getAssignedCar().getBrandName())
                        .name(taxiTrip.getAssignedCar().getModel())
                        .color(taxiTrip.getAssignedCar().getColor())
                        .stateNumber(taxiTrip.getAssignedCar().getRegistrationNumber())
                        .build());
            }
            requestDTO.setDriverInfo(getDriverInfo(requestForTaxi.orElse(null)));
            if (request.getStatus() == TAXI_CANCELLED && request.getStatusCode() == 207) {
                requestDTO.setStatusCodeDescription("Отменено исполнителем");
            }
            return requestDTO;
        } else {
            return null;
        }
    }

    private DriverInfoDTO getDriverInfo(DriverInfo request) {
        // Если есть информация о водителе и транспорте от диспетчерской => возвращаем информацию от диспетчерской
        String driverName = null;
        String vehicleInfo = null;
        String registrationNumber = null;
        String driverPhone = null;
        if (request != null) {
            final var driver = request.getDriver();
            final var vehicle = request.getVehicle();
            if (driver != null) {
                driverName = getFio(driver.getLastName(), driver.getFirstName(), driver.getPatronymic());
                driverPhone = driver.getContactPhone();
            }
            if (vehicle != null) {
                vehicleInfo = getVehicleInfo(vehicle.getBrandName(), vehicle.getModel(), vehicle.getColor());
                registrationNumber = vehicle.getRegistrationNumber();
            }
        }
        if (!(driverName == null && vehicleInfo == null)) {
            return DriverInfoDTO.builder()
                    .driverName(driverName)
                    .vehicleInfo(vehicleInfo)
                    .driverPhone(driverPhone)
                    .registrationNumber(registrationNumber)
                    .build();
        }
        return null;
    }

    private String getVehicleInfo(String brand, String model, String color) {
        StringBuilder sb = new StringBuilder();
        sb.append(color != null && !color.isEmpty() ? color : "");
        sb.append(sb.isEmpty() ? "" : " ");
        sb.append(brand != null && !brand.isEmpty() ? brand : "");
        sb.append(sb.isEmpty() ? "" : " ");
        sb.append(model != null && !model.isEmpty() ? model : "");
        return sb.toString();
    }

    private String getFio(String lastName, String firstName, String patronymic) {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName != null && !lastName.isEmpty() ? lastName : "");
        sb.append(sb.isEmpty() ? "" : " ");
        sb.append(firstName != null && !firstName.isEmpty() ? firstName : "");
        sb.append(sb.isEmpty() ? "" : " ");
        sb.append(patronymic != null && !patronymic.isEmpty() ? patronymic : "");
        return sb.toString();
    }

    private void enrichApprover(List<GetRequestDTO> requestDTO) {
        // Заполняем только AWAITING_APPROVAL заявки
        var requestsToUpdate = requestDTO.stream()
                .filter(r -> TripRequestStatus.getAwaitingApprovalStatuses()
                        .contains(r.getStatus())).toList();
        // Создаем мапу подразделений
        var departmentIds = requestsToUpdate.stream()
                .map(r -> r.getPassenger().departmentId())
                .toList();
        Map<UUID, Department> idToDepartmentMap =
                departmentService.getWithApproversByIds(departmentIds).stream()
                        .collect(Collectors.toMap(Department::getId, Function.identity()));
        // Заполняем approvedBy
        requestsToUpdate.forEach(dto -> setApprover(dto, idToDepartmentMap));
        fillEmployeePositions(requestDTO);
    }

    /**
     * Установка head of limit в поле approvedBy
     *
     * @param dto               - заявка
     * @param idToDepartmentMap - мапа подразделений
     */
    private void setApprover(GetRequestDTO dto, Map<UUID, Department> idToDepartmentMap) {
        var departmentId = dto.getPassenger().departmentId();
        var department = idToDepartmentMap.get(departmentId);
        if (department == null) {
            log.warn("Not found department with id " + departmentId);
            return;
        }
        if (department.getApprovers() == null) {
            return;
        }
        // Ищем Head по незаполненному полю тип транспорта, так head не имеет этого ограничения
        department.getApprovers().stream()
                .filter(a -> a.getTransportType() == null)
                .findFirst().ifPresent(a -> {
                    Optional<Employee> employee = employeeService.get(a.getEmployeeId());
                    if (employee.isEmpty()) {
                        log.warn("Not found employee with id {}", a.getEmployeeId());
                    } else {
                        dto.setApprovedBy(employeeMapper.toDto(employee.get()));
                    }
                });
    }

    private void fillEmployeePositions(List<GetRequestDTO> dtos) {
        var approvedByIds =
                dtos.stream().filter(dto -> dto.getApprovedBy() != null)
                        .map(dto -> dto.getApprovedBy().positionId())
                        .collect(Collectors.toSet());
        if (approvedByIds.isEmpty()) {
            return;
        }
        Map<UUID, Position> positions = positionService.getByIds(approvedByIds).stream()
                .collect(Collectors.toMap(Position::getId, Function.identity()));

        dtos.stream()
                .filter(dto -> dto.getApprovedBy() != null)
                .forEach(dto -> {
                    var approvedBy = dto.getApprovedBy();

                    Position position = positions.get(approvedBy.positionId());
                    if (position != null) {
                        positionMapper.update(approvedBy, position);
                    }
                });
    }

    private void enrichEmployeesWithPositionName(List<? extends GetRequestDTO> requestDTOS) {
        Set<UUID> neededPositionIds =
                requestDTOS.stream().filter(requestDTO -> requestDTO.getAuthor() != null)
                        .map(requestDTO -> requestDTO.getAuthor().positionId()).collect(Collectors.toSet());
        neededPositionIds.addAll(requestDTOS.stream().filter(requestDTO -> requestDTO.getPassenger() != null)
                .map(requestDTO -> requestDTO.getPassenger().positionId()).collect(Collectors.toSet()));
        neededPositionIds.addAll(requestDTOS.stream().filter(requestDTO -> requestDTO.getApprovedBy() != null)
                .map(requestDTO -> requestDTO.getApprovedBy().positionId()).collect(
                        Collectors.toSet()));
        Map<UUID, String> positionId2NameMap = getpositionId2NameMap(neededPositionIds);
        for (var requestDTO : requestDTOS) {
            if (requestDTO.getAuthor() != null) {
                requestDTO.setAuthor(setPositionName(requestDTO.getAuthor(), positionId2NameMap.get(requestDTO.getAuthor().positionId())));
            }
            if (requestDTO.getPassenger() != null) {
                requestDTO.setPassenger(setPositionName(requestDTO.getPassenger(), positionId2NameMap.get(requestDTO.getPassenger().positionId())));
            }
            if (requestDTO.getApprovedBy() != null) {
                requestDTO.setApprovedBy(
                        setPositionName(requestDTO.getApprovedBy(), positionId2NameMap.get(requestDTO.getApprovedBy().positionId())));
            }
        }
    }

    Map<UUID, String> getpositionId2NameMap(Set<UUID> positionIds) {
        return positionService.getByIds(positionIds)
                .stream()
                .collect(Collectors.toMap(Position::getId, Position::getPositionName));
    }

    private EmployeeDTO setPositionName(EmployeeDTO emp, String positionName) {
        return new EmployeeDTO(emp.id(),
                emp.firstName(),
                emp.lastName(),
                emp.patronymic(),
                emp.personnelNumber(),
                emp.departmentId(),
                emp.userId(),
                emp.positionId(),
                emp.delegatedById(),
                positionName,
                emp.supervisorId(),
                emp.organizationId(),
                emp.organizationName(),
                emp.departmentName(),
                emp.mvz(),
                emp.humanReadableId(),
                emp.mobilePhone()
        );
    }

    private void enrichEmployeesWithOrganizationIds(List<? extends GetRequestDTO> requestDTOS) {
        Set<UUID> neededDepartmentIds =
                requestDTOS.stream().filter(requestDTO -> requestDTO.getAuthor() != null)
                        .map(requestDTO -> requestDTO.getAuthor().departmentId()).collect(
                                Collectors.toSet());
        neededDepartmentIds.addAll(requestDTOS.stream().filter(requestDTO -> requestDTO.getPassenger() != null)
                .map(requestDTO -> requestDTO.getPassenger().departmentId()).collect(
                        Collectors.toSet()));
        neededDepartmentIds.addAll(requestDTOS.stream().filter(requestDTO -> requestDTO.getApprovedBy() != null)
                .map(requestDTO -> requestDTO.getApprovedBy().departmentId()).collect(
                        Collectors.toSet()));
        Map<UUID, UUID> departmentOrganizationUUIDMap = getDepartmentOrganizationUUIDMap(neededDepartmentIds);
        for (var requestDTO : requestDTOS) {
            if (requestDTO.getAuthor() != null) {
                employeeMapper.updateOrganization(requestDTO.getAuthor(), departmentOrganizationUUIDMap.get(requestDTO.getAuthor().departmentId()));
            }
            if (requestDTO.getPassenger() != null) {
                employeeMapper.updateOrganization(requestDTO.getPassenger(),
                        departmentOrganizationUUIDMap.get(requestDTO.getPassenger().departmentId()));
            }
            if (requestDTO.getApprovedBy() != null) {
                employeeMapper.updateOrganization(requestDTO.getApprovedBy(),
                        departmentOrganizationUUIDMap.get(requestDTO.getApprovedBy().departmentId()));
            }
        }
    }

    private <A> void setList(Collection<A> list, Collection<A> newList) {
        list.clear();
        list.addAll(newList);
    }

    private CompletableFuture<List<TaxiPriceDto>> getPrice(TaxiPriceService taxiPriceService, ExternalPriceDTO dto) {
        return taxiPriceService.getPrice(dto).completeOnTimeout(taxiPriceService.getResponseWithZeroPrice(), externalPriceTimeout,
                TimeUnit.MILLISECONDS);
    }
}
