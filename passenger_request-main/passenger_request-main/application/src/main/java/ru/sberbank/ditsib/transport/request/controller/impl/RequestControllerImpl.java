package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.request.controller.RequestController;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.helper.UserAuthorizationHelper;
import ru.sberbank.ditsib.transport.request.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of request controller service.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@E2EController
public class RequestControllerImpl implements RequestController {

    public static final String BEARER = "Bearer ";
    private final RequestControllerService requestControllerService;
    private final EmployeeService employeeService;
    private final RequestForTaxiServiceImpl requestForTaxiService;
    private final StatsService statsService;
    private final CarsharingInfoService carsharingInfoService;
    private final RegionDataResolver regionDataResolver;
    private final RequestMapper requestMapper;
    private final ContractorTripService contractorTripService;
    private final TripPurposeService tripPurposeService;
    
    @Override
    public TripPurposeDTO getFrequentlyUsedTripPurpose(@E2EUser("principal") JwtAuthenticationToken authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return tripPurposeService.getFrequentlyUsedTripPurpose(userId);
    }

    @Override
    public GetRequestDTO saveRequest(NewRequestDTO newRequest, @E2EUser("principal") JwtAuthenticationToken authentication) {
        final var employee = employeeService.getAuthenticatedEmployee(authentication);
        final var regionBranch = regionDataResolver.getRegionBranch(newRequest.getExpected().getWaypoints().getFirst());
        formatDesiredDateBasedOnRegionTimeZone(newRequest, regionBranch);
        return requestControllerService.add(newRequest, employee, BEARER + authentication.getToken().getTokenValue(), regionBranch);
    }
    
    @Override
    public List<TaxiPriceDto> calculateRequestExternalPrices(ExternalPriceDTO externalPriceDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.calculateRequestExternalPrices(externalPriceDTO, employee, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public GetRequestDTO addNewRequestTOSharedRide(
            UUID rideId,
            NewRequestDTO newRequest,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                  ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        var regionBranch = regionDataResolver.getRegionBranch(newRequest.getExpected().getWaypoints().getFirst());
        formatDesiredDateBasedOnRegionTimeZone(newRequest, regionBranch);
        return requestControllerService.addRequestToSharedRide(rideId, newRequest, employee, BEARER + authentication.getToken().getTokenValue());
    }
    
    
    @Override
    public void editRequest(UUID requestId, RequestDTO newData, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        requestControllerService.edit(requestId, newData, employee, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public GetRequestDTO rateRequest(
            UUID requestId, RequestRatingDTO ratingDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                    ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.rate(requestId, ratingDTO, employee);
    }
    
    @Override
    public void cancelRequest(
            UUID requestId,
            CancelDTO optionalCancelDTO,
            @E2EUser("principal") JwtAuthenticationToken authentication
                             ) throws NoSuchFieldException, IllegalAccessException {
        requestControllerService.cancel(requestId, ControllerUtils.currentUser(),
                                        Optional.ofNullable(optionalCancelDTO), BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public GetRequestDTO completeRequest(UUID requestId, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.complete(requestId, employee);
    }
    
    @Deprecated
    @Override
    public GetRequestDTO changeRequestStatus(
            UUID requestId, TripRequestStatus status, ChangeStatusDTO changeStatusDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                            ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.changeStatus(requestId, status, employee, changeStatusDTO, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public <R extends GetRequestDTO> R getRequest(
            UUID requestId,
            RequestProjection projection
                                                 ) {
        var result = requestControllerService.get(requestId);
        if (RequestProjection.SELECT.equals(projection)) {
            result.setExpected(result.getExpected().toBuilder().segments(null).build());
        }
        return ReflectionUtils.cast(result);
    }
    
    @Override
    public Page<? extends GetRequestDTO> postSearchRequestTerminal(
            RequestSearchDTO searchDTO, Pageable page, RequestProjection projection
                                                                  ) {
        if (searchDTO == null) {
            searchDTO = new RequestTaxiSearchDTO();
        }
        searchDTO.setTerminalStatus(true);
        return terminalSearch(searchDTO, page, projection);
    }
    
    @Override
    public Page<? extends GetRequestDTO> getPersonalRequestAndTerminalStatus(
            Pageable page,
            RequestProjection projection
                                                                            ) {
        RequestTaxiSearchDTO searchDTO = new RequestTaxiSearchDTO();
        searchDTO.setTerminalStatus(true);
        return terminalSearch(searchDTO, page, projection);
    }
    
    @Override
    public Page<? extends GetRequestDTO> postSearchRequestNonTerminal(
            RequestSearchDTO searchDTO, Pageable page,
            RequestProjection projection
                                                                     ) {
        searchDTO = Optional.ofNullable(searchDTO).orElse(new RequestSearchDTO());
        searchDTO.setTerminalStatus(false);
        return terminalSearch(searchDTO, page, projection);
    }
    
    @Override
    public Page<ShortGetRequestDTO> selfRequests(RequestSearchDTO searchDTO, Pageable page) {
        if (searchDTO.getTransportTypeSet() == null) {
            searchDTO.setTransportTypeSet(Set.of(TransportTypeEnum.PERSONAL));
        }
        if (searchDTO.getRequestStatusSet() == null) {
            searchDTO.setRequestStatusSet(Stream.of(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                                                    TripRequestStatus.PERSONAL_PAYMENT_AWAITING,
                                                    TripRequestStatus.PERSONAL_PAYMENT_DONE)
                                                .map(TripRequestStatus::name).collect(Collectors.toSet()));
        }
        if (searchDTO.getDesiredDate() == null) {
            searchDTO.setDesiredDate(new RequestSearchDTO.DateRange(LocalDateTime.now().minusMonths(3), LocalDateTime.now()));
        }
        
        return terminalSearch(searchDTO, page, RequestProjection.SELECT).map(requestMapper::toShortDTO);
    }
    
    @Override
    public Page<? extends GetRequestDTO> getPersonalRequestAndNonTerminalStatus(
            Pageable page, RequestProjection projection
                                                                               ) {
        RequestTaxiSearchDTO searchDTO = new RequestTaxiSearchDTO();
        searchDTO.setTerminalStatus(false);
        return terminalSearch(searchDTO, page, projection);
    }
    
    private void formatDesiredDateBasedOnRegionTimeZone(NewRequestDTO dto, List<RegionDto> regionBranch) {
        var regionTimeZone = regionBranch.stream()
                .map(RegionDto::getTimeZone)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (regionTimeZone == null) {
            throw new RuntimeException("Не удалось получить таймзону для региона");
        }
        /*
         * Приводим время относительно региона
         * C фронта приходит UTC 07:30
         * Приводим ко времени пользователя 10:30 для GMT+3
         * Меняем на зону для региона (например GMT+10), получаем 10:30 GMT+10
         * Приводим к UTC и получаем 00:30
         */
        var utcTripDateBasedOnRegionTimeZone = dto.getDesiredDate()
                                                  .atZone(ZoneOffset.UTC)
                                                  .withZoneSameInstant(ZoneId.of(dto.getTimeZone()))
                                                  .withZoneSameLocal(ZoneId.of(regionTimeZone))
                                                  .withZoneSameInstant(ZoneOffset.UTC)
                                                  .toLocalDateTime();
        
        log.debug("tripTime: {}, client time zone: {}, region time zone: {}, final time {}", dto.getDesiredDate(), dto.getTimeZone(),
                  regionTimeZone, utcTripDateBasedOnRegionTimeZone);
        
        //Сохраняем таймзону устройства
        dto.setEmployeeDeviceTimeZone(dto.getTimeZone());
        
        dto.setDesiredDate(utcTripDateBasedOnRegionTimeZone);
        dto.setTimeZone("GMT" + ZoneId.of(regionTimeZone).normalized());
    }
    
    private Page<? extends GetRequestDTO> terminalSearch(
            RequestSearchDTO searchDTO, Pageable page, RequestProjection projection
                                                        ) {
        var userId = ControllerUtils.currentUser();
        Employee employee = employeeService.getByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
        searchDTO.setPassengerId(employee.getId());
        if (searchDTO.getPageSetting() != null) {
            return requestControllerService.generalSearch(searchDTO, RequestSearchDTO.getPageRequest(searchDTO), projection);
        } else if (page == null) {
            return requestControllerService.generalSearch(searchDTO, projection);
        } else {
            return requestControllerService.generalSearch(searchDTO, page, projection);
        }
    }
    
    @Override
    public <R extends GetRequestDTO> R getRequest(UUID requestId, TransportTypeEnum transportType, RequestProjection projection) {
        var result = requestControllerService.get(requestId, transportType);
        if (RequestProjection.SELECT.equals(projection)) {
            result.setExpected(result.getExpected().toBuilder().segments(null).build());
        }
        return ReflectionUtils.cast(result);
    }
    
    @Override
    public ShareRideResponseDTO getLinkedSharedRide(UUID requestId, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.getLinkedSharedRide(requestId, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public List<RequestHistoryElementDTO> getRequestHistory(UUID requestId, TransportTypeEnum transportType) {
        return requestControllerService.getHistory(requestId, transportType);
    }
    
    @Override
    public List<? extends GetRequestDTO> getRequests() {
        return requestControllerService.getAll();
    }
    
    @Override
    public Page<? extends GetRequestDTO> getRequestsBySearchDTO(
            RequestTaxiSearchDTO requestSearchDTO,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                               ) {
        return requestControllerService.taxiSearch(requestSearchDTO);
    }
    
    @Override
    public Page<? extends GetRequestDTO> getRequestsByPersonalSearchDTO(
            RequestPersonalSearchDTO requestSearchDTO, @E2EUser("principal") JwtAuthenticationToken authentication
                                                                       ) {
        return requestControllerService.personalSearch(requestSearchDTO, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public Page<? extends GetRequestDTO> getRequestsByPublicSearchDTO(
            RequestPublicSearchDTO requestSearchDTO,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                                     ) {
        return requestControllerService.publicSearch(requestSearchDTO);
    }
    
    @Override
    public Collection<? extends GetRequestDTO> getRequestsBySearchParam(
            Optional<UUID> authorId, Optional<UUID> passengerId, Optional<UUID> approvedById,
            Optional<Boolean> approvedFlag, Optional<Boolean> coopTrip, Optional<String> comment,
            Optional<UUID> requestId, Optional<Set<Integer>> statuses, Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
            Optional<Set<UUID>> tripPurposes, Optional<String> fio, @E2EUser("principal") JwtAuthenticationToken authentication
                                                                       ) {
        var authenticated = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.getBySearchParam(authorId, passengerId, approvedById, approvedFlag, coopTrip,
                                                         comment, requestId, statuses, dateFrom, dateTo, minRideCost,
                                                         maxRideCost, tripPurposes, fio, authenticated);
    }
    
    @Override
    public List<ShareRideResponseDTO> getSuitable(
            NewRequestDTO newRequest,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                 ) {
        var userId = ControllerUtils.currentUser();
        log.info("RequestControllerService: getSuitable called");
        Employee activeEmployee = employeeService.getByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
        var regionBranch = regionDataResolver.getRegionBranch(newRequest.getExpected().getWaypoints().getFirst());
        formatDesiredDateBasedOnRegionTimeZone(newRequest, regionBranch);
        return requestControllerService.getSuitableSharedRide(newRequest, activeEmployee, BEARER + authentication.getToken().getTokenValue());
    }
    
    @Override
    public Collection<PaymentStateResponseDTO> updatePaymentStatus(
            Collection<PaymentStateRequestDTO> paymentStateRequestDTOs, @E2EUser("principal") JwtAuthenticationToken authentication
                                                                  ) {
        return requestControllerService.updatePaymentStatus(paymentStateRequestDTOs,
                                                            employeeService.getAuthenticatedEmployee(authentication));
    }
    
    
    @Override
    public void editApprovedRequest(UUID requestId, ExpectedDataDTO newData, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        requestControllerService.editApproved(requestId, newData, employee);
    }
    
    @Override
    public GetRequestDTO getRequestWithUpdate(UUID requestId, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return requestControllerService.getWithUpdate(requestId, employee);
    }
    
    @Override
    public Boolean checkInAutomatic(CheckinDTO checkinDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.checkInAutomatic(checkinDTO);
    }
    
    @Override
    public Boolean checkInManual(CheckinDTO checkinDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.checkInManual(checkinDTO);
    }
    
    @Override
    public Boolean setAbsenceReason(CheckinDTO checkinDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.setAbsenceReason(checkinDTO);
    }
    
    @Override
    public Boolean startTrip(CheckinDTO checkinDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.startTrip(checkinDTO);
    }
    
    @Override
    public Boolean deleteWaypoint(CheckinDTO checkinDTO, @E2EUser("principal") JwtAuthenticationToken authentication) {
        return requestControllerService.deleteWaypoint(checkinDTO);
    }
    
    @Override
    public GetRequestWithFactDataDTO getRequestTaxiWithFactData(UUID requestId) {
        return requestForTaxiService.getWithFactData(requestId);
    }
    
    @Override
    public List<StatsDTO> processStats(int month, int year) {
        return statsService.processStatsForMonthAndYear(month, year);
    }
    
    @Override
    public Map<String, String> getDeepLinkByRequestId(UUID requestId, @E2EUser("principal") JwtAuthenticationToken authentication) {
        var deeplink = carsharingInfoService.getDeeplinkByRequestId(requestId);
        return Collections.singletonMap("deepLink", deeplink);
    }
    
    @Override
    public void reintegration(ReintergrationRequestDTO dto, @E2EUser("principal") JwtAuthenticationToken authentication) {
        log.info("Пользователь {} запросил повторную интеграцию по заявкам: {}", authentication.getName(), dto.requestIds());
        contractorTripService.processTrips(dto.requestIds());
    }
}
