package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.messaging.listeners.TripApproveHandler;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.NotificationCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Реализация слушателя согласований.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class TripApproveHandlerImpl implements TripApproveHandler {

    private final List<NotificationCommand<TripApprove>> notificationCommands;
    
    private final TripApproveRepository approveRepository;
    
    private final TripRequestRepository requestRepository;
    
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void handle(ApproveTripRequestMessage message) {
        var approverId = message.actorEmployeeId();
        var requestId = message.actionId();
        var tripRequest = requestRepository.findById(requestId);
        if(tripRequest.isEmpty()) {
            log.info("Request with ID {} is not found yet. Trying to restart search.", requestId);
            tripRequest = tryGetTripRequest(requestId, tripRequest);
        }
        var tripApprove = new TripApprove();
        tripApprove.setApproverIds(new ArrayList<>());
        TripApprove previousApprove = null;
        
        try {
            var tripApproveOpt = approveRepository.findByRequestId(requestId);
            if (tripApproveOpt.isPresent() && tripRequest.isPresent()) {
                tripApprove = tripApproveOpt.get();
                previousApprove =
                        objectMapper.readValue(objectMapper.writeValueAsString(tripApprove), TripApprove.class);
                previousApprove.setRequest(tripRequest.get());
            }
        } catch (JsonProcessingException ignore) {
            // ignore
        }
        fillApprovers(tripApprove, message, approverId);
        defineRequest(tripApprove, message, tripRequest);
        tripApprove.setApproverId(approverId);
        tripApprove.setRequestId(requestId);
        tripApprove.setStatus(message.approved());
        approveRepository.save(tripApprove);
        
        log.debug(String.format("A new approval data for request with ID %s received", requestId));
        
        TripApprove finalPreviousApprove = previousApprove;
        var finalApprove = tripApprove;

        if (finalApprove.getRequest() == null) {
            log.info("Request with ID {} is not found yet. Maybe it will appear later", finalApprove.getRequestId());
            return;
        }
        if (previousApprove != null && previousApprove.getRequest() == null) {
            log.info("Request with ID {} is not found yet. Maybe it will appear later", previousApprove.getRequestId());
            return;
        }
        
        try {
            notificationCommands.stream().filter(nc -> nc.validate(finalPreviousApprove, finalApprove))
                                .forEach(command -> {
                                    try {
                                        command.sendNotification(finalApprove);
                                    } catch (JsonProcessingException e) {
                                        log.error("Processing failed", e);
                                    }
                                });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void defineRequest(TripApprove target, ApproveTripRequestMessage message, Optional<TripRequest> source) {
        if (source.isPresent()) {
            target.setRequest(source.get());
            target.setPassengerId(source.get().getPassengerId());
            target.setDesiredDate(source.get().getDesiredDate());
            if(message.approved()!=null && message.approved()) {
                target.setApproveStatus(ApproveStatus.APPROVED);
                target.setApproveStatusDescription(ApproveStatus.APPROVED.getDescription());
            }
            if(message.approved()!=null && !message.approved()){
                target.setApproveStatus(ApproveStatus.DECLINED);
                target.setApproveStatusDescription(ApproveStatus.DECLINED.getDescription());
            }
        }
    }

    private void fillApprovers(TripApprove target, ApproveTripRequestMessage source, UUID defaultApproverId) {
        if(source.approverIds()!=null) {
            target.setApproverIds(source.approverIds());
            if(target.getApproverIds().isEmpty()){
                target.getApproverIds().add(defaultApproverId);
            }
        }
        else {
            target.getApproverIds().add(defaultApproverId);
        }
    }

    private Optional<TripRequest> tryGetTripRequest(UUID requestId, Optional<TripRequest> tripRequest) {
        for (var i = 0; i < 3; i++) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            tripRequest = requestRepository.findById(requestId);
            if(tripRequest.isPresent()){
                log.info("Request with ID {} found.", requestId);
                break;
            }
        }
        return tripRequest;
    }
}
