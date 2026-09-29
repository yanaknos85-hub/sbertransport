package ru.sber.transport.trips.cargo.web.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapper;
import ru.sber.transport.trips.cargo.web.controller.TripController;
import ru.sber.transport.trips.cargo.web.service.RequestService;
import ru.sber.transport.trips.cargo.web.service.ResultProcessor;
import ru.sber.transport.trips.cargo.web.service.TripService;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
class TripControllerImpl implements TripController {

    private final RequestService requestService;

    private final TripService tripService;

    private final ResultProcessor resultProcessor;

    @CheckOrganizationAccess
    @Override
    public Iterable<TripV2Dto> getTrips(@Organization UUID contractorId, RequestSearchDto searchDto, List<TripStatus> statuses,
                                        JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var result = tripService.getAll(contractorId, searchDto, getEffectiveStatuses(statuses), userId);
        return resultProcessor.process(result);
    }

    @CheckOrganizationAccess
    @Override
    public void patchTrip(@Organization UUID contractorId, UUID tripId, List<PatchData> data,
                          JwtAuthenticationToken authentication) {
        tripService.update(contractorId, tripId, data.stream().collect(Collectors.toMap(PatchData::field, PatchData::value, (o, o2) -> o2)), authentication);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<TripV2Dto> getAll(
            @Organization UUID contractorId,
            UUID dispatcherId, List<TripStatus> statuses,
            RequestSearchDto searchDto) throws NoSuchFieldException {
        var result = requestService.find(contractorId, dispatcherId, getEffectiveStatuses(statuses), searchDto);
        return resultProcessor.process(result);
    }

    @Override
    public TripDataDto getTrip(UUID contractorId, UUID tripId, int version, RequestSearchDto searchDto) {
        return resultProcessor.process(requestService.find(contractorId, tripId));
    }

    @Override
    public Iterable<TripV2Dto> getAllDriver(UUID contractorId, UUID driverId, List<TripStatus> statuses, int version, RequestSearchDto searchDto) throws NoSuchFieldException {
        var result = requestService.findByDriver(driverId, Optional.ofNullable(statuses)
                .orElse(List.of(
                        TripStatus.SENT_TO_CONTRACTOR,
                        TripStatus.WAITING_FOR_ASSIGNMENT,
                        TripStatus.DRIVER_ASSIGNED,
                        TripStatus.DRIVER_ON_THE_WAY,
                        TripStatus.DRIVER_ARRIVED,
                        TripStatus.TRIP_IN_PROGRESS,
                        TripStatus.ORDER_FINISHED
                )), searchDto);
        return resultProcessor.process(result);
    }

    @Override
    public TripDataDto getTripDriver(UUID contractorId, UUID driverId, UUID tripId, int version, RequestSearchDto searchDto) {
        return resultProcessor.process(requestService.find(contractorId, tripId));
    }

    @Override
    public CheckinResponseDtoV2 getTripCheckins(UUID contractorId, UUID tripId) {
        return tripService.getTripCheckins(contractorId, tripId);
    }

    private List<TripStatus> getEffectiveStatuses(List<TripStatus> statuses) {
        return Optional.ofNullable(statuses).orElse(List.of(
                TripStatus.SENT_TO_CONTRACTOR,
                TripStatus.WAITING_FOR_ASSIGNMENT,
                TripStatus.DRIVER_ASSIGNED,
                TripStatus.DRIVER_ON_THE_WAY,
                TripStatus.DRIVER_ARRIVED,
                TripStatus.TRIP_IN_PROGRESS,
                TripStatus.ORDER_EXPIRED
        ));
    }
}
