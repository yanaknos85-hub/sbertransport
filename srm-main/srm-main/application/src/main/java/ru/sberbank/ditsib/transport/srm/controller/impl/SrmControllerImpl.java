package ru.sberbank.ditsib.transport.srm.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.srm.controller.SrmController;
import ru.sberbank.ditsib.transport.srm.service.SrmControllerService;
import ru.sberbank.ditsib.transport.srm.service.SrmService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of checkin controller service.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class SrmControllerImpl implements SrmController {
    
    private final SrmService srmService;

    private final SrmControllerService srmControllerService;
    
    @Override
    public SrmSharedRideDTO addNew(SrmRequestDTO requestDTO) {
        return srmControllerService.addNew(requestDTO);
    }

    @Override
    public SrmSharedRideDTO joinRequest(UUID rideId, SrmRequestDTO requestDTO) {
        return srmControllerService.joinRequest(rideId, requestDTO);
    }

    @Override
    public List<SrmSharedRideDTO> findMatch(SrmRequestDTO requestDTO) {
        return srmControllerService.findMatch(requestDTO);
    }
    
    @Override
    public List<SrmSharedRideDTO> cancelRequest(UUID requestId) {
        return srmControllerService.cancelRequest(requestId);
    }
    
    @Override
    public SrmSharedRideDTO get(UUID rideId) {
        return srmService.get(rideId);
    }
    
    @Override
    public List<SrmSharedRideDTO> getSharedRideByRequestId(UUID requestId) {
        return srmControllerService.getSharedRideByRequestId(requestId);
    }
    
    @Override
    public List<SrmSharedRideDTO> getAll() {
        return srmService.getAll();
    }
    
    @Override
    public SrmSharedRideDTO finishSharedRide(UUID rideId) {
        return srmService.finishSharedRide(rideId);
    }
}