package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveTripRequestMessage;
import ru.sberbank.ditsib.transport.request.service.RequestApprovementService;

import java.util.function.Consumer;

/**
 * Implementation of approves listener.
 */
@RequiredArgsConstructor
@Slf4j
public class ApproveRequestListenerImpl implements Consumer<Message<ApproveTripRequestMessage>> {
    
    private final RequestApprovementService requestApprovementService;
    
    private void handleApprove(
            ApproveTripRequestMessage message
                             ) {
        log.info("ApproveRequestListener: handleApprove: actionId" + message.getActionId()
                 + ", approve = " + message.getApproved());
        try {
            var approve = message.getApproved();
            if (approve != null) {
                if (approve) {
                    requestApprovementService.approve(message.getActionId(), message.getActorEmployeeId());
                } else {
                    requestApprovementService.decline(message.getActionId(), message.getActorEmployeeId(),
                                                      message.getMessage());
                }
            }
        } catch (Throwable e) {
            log.error("Error handle approve for request " + message.getActionId(), e);
            throw e;
        }
    }
    
    public void accept(Message<ApproveTripRequestMessage> message) {
        handleApprove(message.getPayload());
    }
}
