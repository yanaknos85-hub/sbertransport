package ru.sberbank.ditsib.transport.request.service;

import java.util.UUID;

public interface RequestApprovementService {
    void approve(UUID requestId, UUID approvedById);
    
    void decline(UUID requestId, UUID approvedBy, String reason);
    
    void approveFinalTrip(UUID requestId, UUID actorEmployeeId);
    
    void declineFinalTrip(UUID requestId, UUID actorEmployeeId, String reason);
}
