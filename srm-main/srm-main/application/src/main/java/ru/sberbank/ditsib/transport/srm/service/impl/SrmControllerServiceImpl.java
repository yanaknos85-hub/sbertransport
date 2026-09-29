package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.srm.controller.impl.mapper.RequestMapper;
import ru.sberbank.ditsib.transport.srm.exception.SrmLogicException;
import ru.sberbank.ditsib.transport.srm.messaging.senders.SharedRideSender;
import ru.sberbank.ditsib.transport.srm.service.SrmControllerService;
import ru.sberbank.ditsib.transport.srm.service.SrmService;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of checkin controller service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SrmControllerServiceImpl implements SrmControllerService {
    private final SrmService srmService;
    private final SharedRideSender sharedRideSender;
    private final RequestMapper requestMapper;

    @Override
    public SrmSharedRideDTO addNew(SrmRequestDTO requestDTO) {
        log.info("SRM: addNew start for request {}", requestDTO);
        if (requestDTO.getRequestId() == null) {
            throw new SrmLogicException("addNew: requestId is null!");
        }
        if (requestDTO.getPickupTime() == null) {
            // Константы ниже - наследие. Зачем они нужны, почему именно такие - не знаю.
            final var dateTime = getTomorrowTime();
            requestDTO.setPickupTime(dateTime);
            log.debug("addNew: pickupTime was null. new pickupTime set to {}", dateTime);
        }
        final var multipleRequestDTO = requestMapper.mapSrmRequestDtoToSrmMultipleRequestDto(requestDTO);
        final var sharedRideDTO = srmService.addNew(multipleRequestDTO, null, true);
        if(sharedRideDTO == null) {
            return null;
        } else {
            sharedRideSender.send(sharedRideDTO);
            return sharedRideDTO;
        }
    }



    @Override
    public SrmSharedRideDTO joinRequest(UUID rideId, SrmRequestDTO requestDTO) {
        log.info("SRM: joinRequest start for request {}", requestDTO);
        if (requestDTO.getRequestId() == null) {
            throw new SrmLogicException("joinRequest: requestId is null!");
        }
        final var multipleRequestDTO = requestMapper.mapSrmRequestDtoToSrmMultipleRequestDto(requestDTO);
        final var sharedRideDTO = srmService.joinRequest(rideId, multipleRequestDTO, 0);
        sharedRideSender.send(sharedRideDTO);
        log.debug("SRM: joinRequest finish: {}", sharedRideDTO);
        return sharedRideDTO;
    }

    @Override
    public List<SrmSharedRideDTO> findMatch(SrmRequestDTO requestDTO) {
        final var time1 = LocalDateTime.now();
        log.info("SRM: findMatch1 start: {}", requestDTO);
        if (requestDTO.getRequestId() == null) {
            requestDTO.setRequestId(UUID.randomUUID());
        }
        final var multipleRequestDTO = requestMapper.mapSrmRequestDtoToSrmMultipleRequestDto(requestDTO);
        final var list = srmService.findMatch(multipleRequestDTO, null, true);
        final var timeDuration = Duration.between(LocalDateTime.now(), time1).abs();
        log.info("SRM: findMatch1 finish: duration = {}, list = {}", timeDuration, list);
        return list;
    }

    @Override
    public List<SrmSharedRideDTO> cancelRequest(UUID requestId) {
        final var sharedRideDTO = srmService.cancelRequest(requestId);
        sharedRideSender.send(sharedRideDTO);
        return sharedRideDTO.isActive() ? Collections.singletonList(sharedRideDTO) : Collections.emptyList();
    }

    @Override
    public List<SrmSharedRideDTO> getSharedRideByRequestId(UUID requestId) {
        return Optional.ofNullable(srmService.getByRequestId(requestId))
                .map(List::of)
                .orElseGet(Collections::emptyList);
    }

    private ZonedDateTime getTomorrowTime() {
        return ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(7, 0), ZoneOffset.UTC);
    }
}