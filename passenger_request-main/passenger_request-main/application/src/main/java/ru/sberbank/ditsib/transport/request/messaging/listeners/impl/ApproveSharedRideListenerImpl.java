package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveSharedRideMessage;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForPersonalServiceImpl;

import java.util.function.Consumer;

/**
 * Implementation of final trip approves listener.
 */
@RequiredArgsConstructor
@Slf4j
public class ApproveSharedRideListenerImpl implements Consumer<Message<ApproveSharedRideMessage>> {
    
    private final RequestForPersonalServiceImpl requestService;
    
    private void handleApprove(ApproveSharedRideMessage message) {
        var approve = message.getApproved();
        if (approve != null) {
            if (approve) {
                requestService.approveSharedRide(message.getAddRequestId());
            } else {
                requestService.declineSharedRide(message.getAddRequestId());
            }
        }
        
    }
    
    public void accept(Message<ApproveSharedRideMessage> message) {
        handleApprove(message.getPayload());
    }
    
}
