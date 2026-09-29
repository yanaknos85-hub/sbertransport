package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.database.model.Approver;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.service.RequestValidationService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestValidationServiceImpl implements RequestValidationService {
    public static final String EDIT_REQUEST_ILLEGAL_STATUS_FORMAT = "Cannot edit request in %s status";
    private static final String APPROVE_REQUEST_ILLEGAL_STATUS_FORMAT = "Unable approve/decline request %s in status %s";
    public static final String CANCEL_REQUEST_ILLEGAL_STATUS_FORMAT = "Cannot cancel request in %s status";
    public static final String CHANGE_REQUEST_ILLEGAL_STATUS_FORMAT = "Cannot change state of request in %s state";
    public static final String DECREASE_REQUEST_ILLEGAL_STATUS_FORMAT = "Cannot decrease request state. Current state: %s ; New state: %s";
    
    private final RequestService requestService;
    
    private final DepartmentService departmentService;
    
    @Override
    public Request validateAndGetRequest(UUID requestId) {
        return requestService.get(requestId).orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
    }
    
    @Override
    public Request validateAndGetRequest(UUID requestId, TransportTypeEnum transportType) {
        return requestService.get(requestId, transportType)
                             .orElseThrow(() -> new EntityNotFoundException(Request.class, requestId));
    }
    
    @Override
    public CancelDTO validateStatusAndGetCancelDto(
            TripRequestStatus status, TransportTypeEnum transportType,
            Optional<CancelDTO> optionalCancelDTO
                                                  ) throws NoSuchFieldException
            , IllegalAccessException {
        if (!status.isCancelable()) {
            throw new IllegalStateResponseException(String.format(CANCEL_REQUEST_ILLEGAL_STATUS_FORMAT, status));
        } else {
            CancelDTO cancelDTO;
            Object reason = TripRequestStatus.getCanceledByEmployee(transportType);
            
            if (optionalCancelDTO.isPresent()) {
                cancelDTO = optionalCancelDTO.get();
            } else {
                cancelDTO = new CancelDTO();
                Field field = reason.getClass().getDeclaredField("description");
                field.setAccessible(true);
                cancelDTO.setReason((String) field.get(reason));
            }
            
            Field field = reason.getClass().getDeclaredField("code");
            field.setAccessible(true);
            cancelDTO.setCode((Integer) field.get(reason));
            
            return cancelDTO;
        }
    }
    
    @Override
    public UpdateRequest validateAndGetUpdateRequest(UUID requestId) {
        return requestService.findUpdateRequest(requestId).orElseThrow(() -> new EntityNotFoundException(UpdateRequest.class, requestId));
    }
    
    @Override
    public void validateCurrentRequestStatus(Request request) {
        if (request.getStatus().ordinal() <
            TripRequestStatus.getApprovedStatusByTransportType(request.getTransportType()).ordinal() ||
            request.getStatus().ordinal() >=
            TripRequestStatus.getCompletedStatusByTransportType(request.getTransportType()).ordinal()) {
            throw new IllegalStateResponseException(String.format(CHANGE_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus()));
        }
    }
    
    @Override
    public void validateNewRequestStatus(Request request, TripRequestStatus newStatus) {
        if (request.getStatus().ordinal() > newStatus.ordinal()) {
            throw new IllegalStateResponseException(String.format(DECREASE_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus(), newStatus));
        }
    }
    
    @Override
    public void checkRequestStatusByAllowedStatuses(TripRequestStatus actual, Set<TripRequestStatus> allowed) {
        if (actual == null || !allowed.contains(actual)) {
            throw new IllegalStateResponseException(String.format(EDIT_REQUEST_ILLEGAL_STATUS_FORMAT, actual));
        }
    }
    
    @Override
    public void checkRequestStatusByIllegalStatuses(TripRequestStatus actual, Set<TripRequestStatus> illegal) {
        if (actual == null || illegal.contains(actual)) {
            throw new IllegalStateResponseException(String.format(EDIT_REQUEST_ILLEGAL_STATUS_FORMAT, actual));
        }
    }
    
    @Override
    public void checkApprovable(Request request) {
        if (!request.getStatus().isApprovable()) {
            throw new IllegalStateResponseException(
                    String.format(APPROVE_REQUEST_ILLEGAL_STATUS_FORMAT, request.getId(), request.getStatus()));
        }
    }
    
    @Override
    public void checkUserEditPermission(Request saved, Employee callerEmployee) {
        if (!(callerEmployee.getId().equals(saved.getAuthor().getId())
              || callerEmployee.getId().equals(saved.getPassenger().getId())
              || isApprover(saved.getPassenger().getDepartment().getId(),
                            saved.getTransportType(),
                            callerEmployee))) {
            throw new IllegalCallerResponseException();
        }
    }
    
    @Override
    public void checkUserApprovePermissionByAuthor(Request saved, Employee callerEmployee) {
        if (!isApprover(saved.getAuthor().getDepartment().getId(),
                        saved.getTransportType(),
                        callerEmployee)) {
            throw new IllegalCallerResponseException();
        }
    }
    
    /**
     * Проверка является ли пользователь согласующим для данного подразделения и вида транспорта
     *
     * @param departmentId идентификатор подразделения
     * @param transportType вид транспорта
     * @param callerEmployee проверяемый сотрудник
     *
     * @return true/false
     */
    private boolean isApprover(UUID departmentId, TransportTypeEnum transportType, Employee callerEmployee) {
        // Берем всех согласующих для подразделения, подходящих под тип транспорта, и ищем среди них caller.
        return departmentService.getApprovals(departmentId).stream()
                                .filter(approver -> approver.getTransportType() == null ||
                                                    approver.getTransportType() == transportType)
                                .map(Approver::getEmployeeId)
                                .anyMatch(Predicate.isEqual(callerEmployee.getId()));
    }
}
