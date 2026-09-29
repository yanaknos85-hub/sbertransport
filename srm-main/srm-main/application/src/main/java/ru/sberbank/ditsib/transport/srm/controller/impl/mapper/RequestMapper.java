package ru.sberbank.ditsib.transport.srm.controller.impl.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmMultipleRequestDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmSingleRequestDTO;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestMapper {

    public SrmMultipleRequestDTO mapSrmRequestDtoToSrmMultipleRequestDto(SrmRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getWaypoints().isEmpty()) {
            return null;
        }
        SrmMultipleRequestDTO multipleRequestDTO = new SrmMultipleRequestDTO();
        multipleRequestDTO.setMultipleRequestId(UUID.randomUUID());
        multipleRequestDTO.setTransportType(requestDTO.getTransportType());
        multipleRequestDTO.setTariffId(requestDTO.getTariffId());
        multipleRequestDTO.setTimeZone(requestDTO.getTimeZone());
        multipleRequestDTO.setPointMatchingType(requestDTO.getPointMatchingType());
        multipleRequestDTO.setRequestPrice(requestDTO.getRequestPrice());
        multipleRequestDTO.setPickupTime(requestDTO.getPickupTime());

        List<SrmSingleRequestDTO> singleRequestDTOList = new ArrayList<>();

        SrmSingleRequestDTO singleRequestDTO = new SrmSingleRequestDTO();
        singleRequestDTO.setRequestId(requestDTO.getRequestId());
        singleRequestDTO.setRequiredPassengers(requestDTO.getRequiredPassengers());

        singleRequestDTO.setCandidate(false);

        List<SrmWaypointPostDTO> waypointPostDTOList = new ArrayList<>();
        for (SrmWaypointPostDTO waypointDTO : requestDTO.getWaypoints()) {
            SrmWaypointPostDTO waypointPostDTO = new SrmWaypointPostDTO();
            waypointPostDTO.setId(waypointDTO.getId());
            waypointPostDTO.setAddress(waypointDTO.getAddress());
            waypointPostDTO.setLatitude(waypointDTO.getLatitude());
            waypointPostDTO.setLongitude(waypointDTO.getLongitude());
            waypointPostDTO.setWaitingTime(waypointDTO.getWaitingTime() == null ? 0 : waypointDTO.getWaitingTime());
            waypointPostDTO.setLoaderNumber(waypointDTO.getLoaderNumber());
            waypointPostDTOList.add(waypointPostDTO);
        }
        singleRequestDTO.getWaypoints().addAll(waypointPostDTOList);

        singleRequestDTOList.add(singleRequestDTO);

        multipleRequestDTO.getRequestDTOList().addAll(singleRequestDTOList);
        return multipleRequestDTO;
    }
}
