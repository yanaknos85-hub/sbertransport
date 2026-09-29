package ru.sberbank.ditsib.transport.srm.service;

import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с маджентой
 */
public interface SrmControllerService {
    
    SrmSharedRideDTO addNew(SrmRequestDTO requestDTO);

    SrmSharedRideDTO joinRequest(UUID rideId, SrmRequestDTO requestDTO);
    
    List<SrmSharedRideDTO> findMatch(SrmRequestDTO requestDTO);

    List<SrmSharedRideDTO> cancelRequest(UUID requestId);
    
    List<SrmSharedRideDTO> getSharedRideByRequestId(UUID requestId);
}
