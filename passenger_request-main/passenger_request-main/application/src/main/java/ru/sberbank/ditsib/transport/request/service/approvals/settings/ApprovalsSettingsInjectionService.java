package ru.sberbank.ditsib.transport.request.service.approvals.settings;

import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

/**
 * Сервис для внедрения настроек согласований
 */
public interface ApprovalsSettingsInjectionService {
    
    /**
     * Получить флаг необходимости прикрепления документа на этапе создания и просмотра на этапе согласования
     *
     * @param employee сотрудник из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrCreationDocumentRequired(Employee employee);
    
    /**
     * Получить флаг необходимости прикрепления документа на этапе подтверждения и просмотра на этапе утверждения
     *
     * @param employee сотрудник из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrConfirmationDocumentRequired(Employee employee);
    
    /**
     * Получить флаг необходимости этапа подтверждения поездки
     *
     * @param employee сотрудник из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrTripConfirmationRequired(Employee employee);
    
    /**
     * Получить флаг необходимости этапа утверждения поездки
     *
     * @param employee сотрудник из заявки (для вычисления organizationId)
     *
     * @return значение настройки
     */
    boolean isPublicTrTripAwaitingAffirmative(Employee employee);
}
