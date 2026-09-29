package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service of request processing
 */
public interface RequestValidationService {
    Request validateAndGetRequest(UUID requestId);
    
    Request validateAndGetRequest(UUID requestId, TransportTypeEnum transportType);
    
    /**
     * Проверка на допустимость отмены
     * @param status текущий статус заявки
     * @param transportType тип транспорта
     * @param optionalCancelDTO dto
     */
    CancelDTO validateStatusAndGetCancelDto(TripRequestStatus status, TransportTypeEnum transportType,
                                            Optional<CancelDTO> optionalCancelDTO) throws NoSuchFieldException
            , IllegalAccessException;

    UpdateRequest validateAndGetUpdateRequest(UUID requestId);
    
    void validateCurrentRequestStatus(Request request);
    
    void validateNewRequestStatus(Request request, TripRequestStatus newStatus);
    
    /**
     * Проверить статус изменяемой заявки в списке <b>разрешенных статусов</b>
     * @param actual статус изменяемой заявки
     * @param allowed спискок разрешенных статусов
     */
    void checkRequestStatusByAllowedStatuses(TripRequestStatus actual, Set<TripRequestStatus> allowed);
    
    /**
     * Проверить статус изменяемой заявки в списке <b>запрещенных статусов</b>
     * @param actual статус изменяемой заявки
     * @param illegal спискок запрещенных статусов
     */
    void checkRequestStatusByIllegalStatuses(TripRequestStatus actual, Set<TripRequestStatus> illegal);
    
    /**
     * Проверить заявку на допустимость согласования
     * @param request изменяемая заявка
     */
    void checkApprovable(Request request);
    
    /**
     * проверяет пользователя на наличие прав на редактирование данной заявки
     *
     * @param saved заявка
     * @param callerEmployee проверяемый сотрудник
     */
    void checkUserEditPermission(Request saved, Employee callerEmployee);
    
    /**
     * проверяет пользователя на наличие прав на согласование данной заявки
     *
     * @param saved заявка
     * @param callerEmployee проверяемый сотрудник
     */
    void checkUserApprovePermissionByAuthor(Request saved, Employee callerEmployee);
}
