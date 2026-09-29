package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveUpdateTripRequestMessage;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.util.function.Consumer;

/**
 * Implementation of approves listener.
 */
@RequiredArgsConstructor
@Slf4j
public class ApproveUpdateTripListenerImpl implements Consumer<Message<ApproveUpdateTripRequestMessage>> {
    
    private final RequestService requestService;
    
    private void handleApprove(ApproveUpdateTripRequestMessage message) {
        var approve = message.getApproved();
        if (approve != null) {
            if (approve) {
                requestService.approveUpdateTrip(message.getUpdateId(), message.getApprovedByEmployeeId());
            } else {
                requestService.declineUpdateTrip(message.getUpdateId(), message.getApprovedByEmployeeId(),
                                                 message.getMessage());
            }
        }
    }
    
    public void accept(Message<ApproveUpdateTripRequestMessage> message) {
        handleApprove(message.getPayload());
    }
}
