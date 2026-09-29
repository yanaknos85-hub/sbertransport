package ru.sberbank.ditsib.transport.request.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service of request processing
 */
public interface RequestService {
    
    /**
     * Добавление заявки
     *
     * @param data данные новой заявки
     */
    Request add(UUID rideId, boolean coop, NewRequestDTO data, Employee employee);
    
    /**
     * Добавление заявки
     *
     * @param data данные новой заявки
     */
    Request add(UUID rideId, boolean coop, NewRequestDTO data, Employee employee, String token);
    
    /**
     * Добавление заявки
     *
     * @param data данные новой заявки
     */
    Request add(UUID rideId, boolean coop, NewRequestDTO data, Employee employee, String token, List<RegionDto> regions);
    
    /**
     * Обновление заявки
     *
     * @param newData данные новой заявки
     */
    Request update(RequestDTO newData, Employee activeUser, String token);
    
    /**
     * /** Получение списка подходящих совместных поездок
     *
     * @param request данные новой заявки для поиска подходящих
     * @param loggedEmployee залогиненый сотрудник
     */
    List<ShareRideResponseDTO> getSuitableSharedRide(Request request, Employee loggedEmployee, String token);
    
    /**
     * Получить связанную совместную поездку
     *
     * @param requestId заявки
     *
     * @return Данные совместной поездки
     */
    ShareRideResponseDTO getLinkedSharedRide(UUID requestId, String token);

    /**
     * Cancel request.
     *
     * @param request request.
     * @param cancelDTO object with cancel reasons
     * @param initiator active employee
     */
    void cancel(Request request, CancelDTO cancelDTO, Employee initiator, boolean cancelAnyway);
    
    /**
     * Decline request.
     *
     * @param toDecline request to decline.
     * @param cancelDTO object with decline reasons
     * @param initiator active employee
     */
    void decline(Request toDecline, CancelDTO cancelDTO, Employee initiator, String token);
    
    /**
     * Get request.
     *
     * @param id ID of request.
     *
     * @return request.
     */
    Optional<? extends Request> get(UUID id);
    
    /**
     * Get request.
     *
     * @param isTerminateStatus terminal status for request
     * @param employee for search request
     *
     * @return request.
     */
    Page<? extends Request> getByTerminateStatusAndEmployee(boolean isTerminateStatus, Employee employee, Pageable page);
    
    /**
     * Get request.
     *
     * @param id ID of request.
     *
     * @return request.
     */
    Optional<? extends Request> get(UUID id, TransportTypeEnum transportType);
    
    /**
     * Get list of all requests.
     *
     * @return request.
     */
    List<? extends Request> getAll();
    
    /**
     * Get list of all requests authored by specified employee.
     *
     * @param authorId employee id of author of requests
     *
     * @return request.
     */
    List<? extends Request> getAllByAuthor(@NotNull UUID authorId);
    
    /**
     * Get request history of status changes
     *
     * @param requestId id of request
     *
     * @return list of history records
     */
    List<RequestHistoryElement> getHistory(UUID requestId, TransportTypeEnum transportType);
    
    /**
     * Edit request.
     *
     * @param request request to rate.
     * @param activeUser active employee
     */
    Request rate(RequestForTaxi request, RequestRating rating, Employee activeUser);
    
    /**
     * Validate and save trip with finished status
     *
     * @param toFinish request to finish.
     * @param activeUser active employee
     */
    Request finish(Request toFinish, Employee activeUser);
    
    /**
     * Complete request
     *
     * @param toFinish request to finish.
     * @param activeUser active employee
     */
    Request complete(Request toFinish, Employee activeUser);
    
    /**
     * Validate and save trip with finished status
     *
     * @param toChange request to change status.
     * @param newStatus new status
     * @param activeUser active employee
     */
    Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser, ChangeStatusDTO changeStatusDTO);
    
    Request changeState(Request toChange, TripRequestStatus newStatus, Employee activeUser);
    
    Request changeState(Request toChange, TripRequestStatus newStatus);
    
    void approve(Request request);
    
    void approveFinalTrip(Request request, UUID actorEmployeeId);
    
    void updateApproved(Request request, ExpectedDataDTO newData);
    
    void approveUpdateTrip(UUID updateId, UUID approvedByEmployeeId);
    
    void declineUpdateTrip(UUID updateId, UUID approvedByEmployeeId, String message);
    
    Optional<UpdateRequest> findUpdateRequest(UUID requestId);
    
    /**
     * Автоматический чекин
     *
     * @param request заявка
     * @param checkinDTO данные чекина
     */
    Boolean checkInAutomatic(Request request, CheckinDTO checkinDTO);
    
    /**
     * Ручной чекин
     *
     * @param request заявка
     * @param checkinDTO данные чекина
     */
    Boolean checkInManual(Request request, CheckinDTO checkinDTO);
    
    /**
     * Установка причины отсутствия
     *
     * @param request заявка
     * @param checkinDTO данные чекина
     *
     * @return operation success status
     */
    Boolean setAbsenceReason(Request request, CheckinDTO checkinDTO);
    
    /**
     * Начать поездку
     *
     * @param request заявка
     * @param checkinDTO данные чекина
     *
     * @return operation success status
     */
    Boolean startTrip(Request request, CheckinDTO checkinDTO);
    
    /**
     * Удалить точку
     *
     * @param request заявка
     * @param checkinDTO данные точки
     *
     * @return operation success status
     */
    Boolean deleteWaypoint(Request request, CheckinDTO checkinDTO);
    
    /**
     * Конвертация в ДТО
     *
     * @param source заявка
     *
     * @return ДТО
     */
    GetRequestDTO convertToDto(Request source);

    List<RequestForTaxi> findRequestsForTaxiWithDriverArrivedDeadlineViolation(LocalDateTime now);
    
    List<RequestForGroupTransfer> findRequestsForGroupTransferWithDriverArrivedDeadlineViolation(LocalDateTime now);
    
    void updateRequestForTaxiWithDriverArrivedDeadlineViolation(RequestForTaxi request);
    
    void updateRequestForGroupTransferWithDriverArrivedDeadlineViolation(RequestForGroupTransfer request);
    
    DeadlineState calcDriverArrivedDeadline(
            TransportTypeEnum transportType,
            LocalDateTime now,
            LocalDateTime driverArrivedDatetime,
            LocalDateTime driverArrivedDeadline
                                           );
    
    /**
     * Утверждение маршрута в заявке на личный транспорт с нарушением контрольного срока утверждения маршрута
     *
     * @param request - заявка, маршрут которой необходимо утвердить
     */
    void approveRequestForPersonalWithTripApprovalDeadlineViolation(RequestForPersonal request);
    
    /**
     * Установка состояния проверки контрольного срока выплаты компенсации по заявке на личный транспорт
     *
     * @param request - заявка на личный транспорт
     * @param deadlineState - устанавливаемое состояние проверки контрольного срока
     */
    void setPaymentDoneDeadlineState(RequestForPersonal request, DeadlineState deadlineState);
    
    /**
     * Установка состояния проверки контрольного срока выплаты компенсации по заявке на общественный транспорт
     *
     * @param request - заявка на общественный транспорт
     * @param deadlineState - устанавливаемое состояние проверки контрольного срока
     */
    void setPaymentDoneDeadlineState(RequestForPublic request, DeadlineState deadlineState);
}
