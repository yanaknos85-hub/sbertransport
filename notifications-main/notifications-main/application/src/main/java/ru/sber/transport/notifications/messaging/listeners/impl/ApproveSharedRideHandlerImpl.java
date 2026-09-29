package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.approvals.messages.ApproveSharedRideMessage;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.messaging.listeners.ApproveSharedRideHandler;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.services.NotificationCommand;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
class ApproveSharedRideHandlerImpl implements ApproveSharedRideHandler {

    private final List<NotificationCommand<TripApprove>> notificationCommands;
    
    private final TripRequestRepository tripRequestRepository;
    
    private final EmployeeRepository employeeRepository;
    
    private final TripApproveRepository approveRepository;
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(ApproveSharedRideMessage message) {
        var requestId = message.addRequestId();
        var approvedByEmployeeId = message.approvedByEmployeeId();
        var tripRequest = tripRequestRepository.getById(requestId);
        var passengerId = tripRequest.getPassengerId();
        var passenger = employeeRepository.getById(passengerId);
        var approveStatus = ApproveStatus.valueOf(message.statusApprove());
    
        var tripApprove = new TripApprove();
        TripApprove previousApprove = null;
       
        try {
            var tripApproveOpt = approveRepository.findByRequestId(requestId);
            if (tripApproveOpt.isPresent()) {
                tripApprove = tripApproveOpt.get();
                previousApprove =
                        objectMapper.readValue(objectMapper.writeValueAsString(tripApprove), TripApprove.class);
                previousApprove.setRequest(tripRequest);
            }
        } catch (JsonProcessingException e) {
            log.warn("Approve processing failed. {}", e.getMessage());
        }
        tripApprove.setRequest(tripRequest);
        tripApprove.setApproverId(approvedByEmployeeId);
        if (approvedByEmployeeId != null) {
            tripApprove.setApprover(employeeRepository.findById(approvedByEmployeeId).orElse(null));
        }
        tripApprove.setRequestId(requestId);
        tripApprove.setOwnerRequestId(message.ownerRequestId());
        tripApprove.setStatus(message.approved());
        tripApprove.setApproveStatus(approveStatus);
        tripApprove.setDesiredDate(message.desiredDate());
        tripApprove.setPassenger(passenger);
        tripApprove.setPassengerId(passenger.getId());
        if (approveStatus.equals(ApproveStatus.DECLINED)){
            tripApprove.setMessage(message.message());
        }
        approveRepository.save(tripApprove);
        log.debug(String.format("A new approval data for request with ID %s received", requestId));
    
        try {
            TripApprove finalPreviousApprove = previousApprove;
            var finalApprove = tripApprove;
            Optional<NotificationCommand<TripApprove>> commandOpt =
                    notificationCommands.stream().filter(nc -> nc.validate(finalPreviousApprove, finalApprove))
                                        .findFirst();
            if (commandOpt.isPresent()) commandOpt.get().sendNotification(tripApprove);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
