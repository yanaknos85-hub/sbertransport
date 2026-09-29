package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.database.model.AbstractRequestForTnP;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.service.MagentaAuxilaryService;
import ru.sberbank.ditsib.transport.request.service.SrmService;

import java.util.UUID;

/**
 * Имплементация сервиса для работы с маджента
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class MagentaAuxilaryServiceImpl implements MagentaAuxilaryService {

    private final EntityDTOMapper entityDTOMapper;
    private final SrmService srmService;

    @Override
    public SrmSharedRideDTO processCoopRequest(UUID rideId, AbstractRequestForTnP request, String token) {
        if (rideId == null) {
            return processCoopRequestNewRide(request, token);
        } else {
            return processCoopRequestUpdateRide(rideId, request, token);
        }
    }

    @NotNull
    private SrmSharedRideDTO processCoopRequestUpdateRide(UUID rideId, AbstractRequestForTnP request, String token) {
        log.debug("processCoopRequest.updateSrmNewSharedRide rideId = " + rideId + ", requestId = " + request.getId());
        var sharedRideDTO = srmService.addRequestToSharedRide(rideId, entityDTOMapper.requestToSrmRequestDTO(request), token);
        var requestKpiDTO = sharedRideDTO.getRequestKpiList().stream()
                .filter(e -> e.getId().equals(request.getId()))
                .findAny()
                .orElse(null);
        request.setRideId(sharedRideDTO.getId());
        if (requestKpiDTO != null) {
            log.debug("processCoopRequest.updateSrmNewSharedRide updating economy from request = " + requestKpiDTO.getId());
            request.setCostSharePart(requestKpiDTO.getCostSharePart());
            request.setSavingsCash(requestKpiDTO.getSavingsCash());
            request.setSavingsProcents(requestKpiDTO.getSavingsProcents().longValue());
        }
        return sharedRideDTO;
    }

    @Nullable
    private SrmSharedRideDTO processCoopRequestNewRide(AbstractRequestForTnP request, String token) {
        log.debug("processCoopRequest.postSrmNewSharedRide: requestId = {}", request.getId());
        var sharedRideDTO = srmService.postNewSharedRide(entityDTOMapper.requestToSrmRequestDTO(request), token);
        if (sharedRideDTO != null) {
            final var requestKpiList = sharedRideDTO.getRequestKpiList();
            if (!requestKpiList.isEmpty()) {
                var requestKpiDTO = requestKpiList.get(0);
                request.setRideId(sharedRideDTO.getId());
                request.setSharedRideOwner(request.getId().equals(requestKpiDTO.getId()));
                request.setCostSharePart(requestKpiDTO.getCostSharePart());
                request.setSavingsCash(requestKpiDTO.getSavingsCash());
                request.setSavingsProcents(requestKpiDTO.getSavingsProcents().longValue());
                return sharedRideDTO;
            }
        }
        return null;
    }
}