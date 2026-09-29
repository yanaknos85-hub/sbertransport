package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.service.RequestApprovementService;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.service.RequestValidationService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.UUID;

/**
 * Implementation of service for working with requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestApprovementServiceImpl implements RequestApprovementService {
    private final RequestRepository repository;
    
    private final EmployeeService employeeService;
    
    private final RequestValidationService requestValidationService;
    
    private final RequestService requestService;
    
    @Override
    public void approve(UUID requestId, UUID approvedById) {
        log.info("approve came for: " + requestId);
        Request request = getRequest(requestId);
        
        try {
            requestValidationService.checkApprovable(request);
            var approver = getEmployee(approvedById);
            request.setApprovedBy(approver);
            requestService.approve(request);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }
    
    @Override
    public void decline(UUID requestId, UUID approvedBy, String reason) {
        final Request request = getRequest(requestId);
        try {
            requestValidationService.checkApprovable(request);
            
            request.setApprovalState(ApprovalState.DECLINED);
            
            final CancelDTO cancelDTO = CancelDTO.builder()
                                                 .reason(reason)
                                                 .code(TripRequestStatus.getDeclinedByTransportType(request.getTransportType()))
                                                 .build();
            requestService.decline(request, cancelDTO, getEmployee(approvedBy), "token");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }
    
    @Override
    public void approveFinalTrip(UUID requestId, UUID actorEmployeeId) {
        Request request = repository.findById(requestId).orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
        requestService.approveFinalTrip(request, actorEmployeeId);
    }
    
    @Override
    public void declineFinalTrip(UUID requestId, UUID actorEmployeeId, String reason) {
        final Request request = getRequest(requestId);
        if (!TripRequestStatus.getAwaitingTripApprovalStatuses().contains(request.getStatus())) {
            log.warn("Skip decline final trip because request has status: " + request.getStatus());
            return;
        }
        final CancelDTO dto = CancelDTO.builder()
                                       .reason(reason)
                                       .code(TripRequestStatus.getNotApprovedStatusesByTransportType(request.getTransportType()))
                                       .build();
        requestService.decline(request, dto, getEmployee(actorEmployeeId), "token");
    }
    
    private Request getRequest(UUID requestId) {
        return repository.findById(requestId)
                         .orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
    }
    
    private Employee getEmployee(UUID employeeId) {
        return employeeService.get(employeeId).orElseThrow(
                () -> new EntityNotFoundException(Employee.class, employeeId));
    }
}
