package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Внутренняя логика разделенная по типу транспорта
 */
public interface TransportTypeService<T extends Request> {
    
    Optional<T> get(UUID id);
    
    Request add(UUID sharedRideId, boolean coop, NewRequestDTO data, Employee employee, String token, ExecutorGroupDTO executorGroup);
    
    Request update(RequestDTO newData, Employee activeUser, Request request, String token);
    
    T save(T request);
    
    Request finish(Request toFinish, Employee activeUser);
    
    Request complete(Request request, Employee activeUser);
    
    void cancel(Request request, CancelDTO cancelDTO, Employee initiator);
    
    Request approveRequest(Request request);
    
    Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO);
    
    void approveFinalTrip(Request request, UUID actorEmployeeId);
    
    default void markSlaExpired(UUID requestId) {
    }
    
    default void finishByExpiration(T request, Employee author) {
    }
    
    default DeadlineState calcDriverArrivedDeadline(
            LocalDateTime now,
            LocalDateTime driverArrivedDatetime,
            LocalDateTime driverArrivedDeadline) {
        return DeadlineState.NONE;
    }
}
