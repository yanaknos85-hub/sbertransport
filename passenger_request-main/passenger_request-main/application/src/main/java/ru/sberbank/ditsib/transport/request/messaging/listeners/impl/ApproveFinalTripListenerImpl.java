package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveFinalTripMessage;
import ru.sberbank.ditsib.transport.request.service.RequestApprovementService;

import java.util.function.Consumer;

/**
 * Implementation of final trip approves listener.
 */
@RequiredArgsConstructor
@Slf4j
public class ApproveFinalTripListenerImpl implements Consumer<Message<ApproveFinalTripMessage>> {
    
    private final RequestApprovementService requestApprovementService;
    
    private void handleApprove(ApproveFinalTripMessage message) {
        var approve = message.getApproved();
        if (approve != null) {
            if (approve) {
                requestApprovementService.approveFinalTrip(message.getRequestId(), message.getActorEmployeeId());
            } else {
                requestApprovementService.declineFinalTrip(message.getRequestId(), message.getActorEmployeeId(),
                                                           message.getMessage());
            }
        }
    }
    
    public void accept(Message<ApproveFinalTripMessage> message) {
        handleApprove(message.getPayload());
    }
}
