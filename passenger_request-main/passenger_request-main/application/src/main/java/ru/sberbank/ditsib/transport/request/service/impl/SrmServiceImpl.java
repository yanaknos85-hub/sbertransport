package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.messaging.resolvers.SrmResolver;
import ru.sberbank.ditsib.transport.request.service.SrmService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Имплементация сервиса для работы с маджента
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SrmServiceImpl implements SrmService {
    
    private final SrmResolver srmResolver;
    
    @Override
    public SrmSharedRideDTO postNewSharedRide(SrmRequestDTO sharedRidePostDTO, String token) {
        try {
            return srmResolver.addNew(sharedRidePostDTO, token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              "SrmService:postNewSharedRide error", e);
        }
    }
    
    @Override
    public SrmSharedRideDTO addRequestToSharedRide(UUID requestId, SrmRequestDTO sharedRidePostDTO, String token) {
        try {
            return srmResolver.joinRequest(requestId, sharedRidePostDTO, token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SrmService:addRequestToSharedRide error", e);
        }
    }
    
    @Override
    public List<SrmSharedRideDTO> getSuitableSharedRides(SrmRequestDTO sharedRidePostDTO, String token) {
        try {
            log.info("getSuitableSharedRides: start: " + sharedRidePostDTO);
            return srmResolver.findMatch(sharedRidePostDTO, token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SrmService:getSuitableSharedRides error", e);
        }
    }
    
    @Override
    public Optional<SrmSharedRideDTO> getSharedRideByRequestId(UUID requestId, String token) {
        try {
            return srmResolver.getSharedRideByRequestId(requestId, token).stream().findAny();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SrmService:getSharedRideByRequestId error", e);
        }
    }
}
