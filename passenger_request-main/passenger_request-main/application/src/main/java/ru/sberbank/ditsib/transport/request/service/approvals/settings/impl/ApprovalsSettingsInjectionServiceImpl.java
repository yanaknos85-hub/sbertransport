package ru.sberbank.ditsib.transport.request.service.approvals.settings.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.ApprovalsSettingsInjectionService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalsSettingsInjectionServiceImpl implements ApprovalsSettingsInjectionService {
    
    private final PublicApprovalsSettingsServiceImpl publicApprovalsSettingsService;
    private final EmployeeService employeeService;
    
    @Override
    public boolean isPublicTrCreationDocumentRequired(@NonNull Employee employee) {
        return getPublicApprovalsSettingsFlag(employee, PublicApprovalsSettingsFlagType.APPROVAL_DOCUMENT_CHECK);
    }
    
    @Override
    public boolean isPublicTrConfirmationDocumentRequired(@NonNull Employee employee) {
        return getPublicApprovalsSettingsFlag(employee,
                                              PublicApprovalsSettingsFlagType.TRIP_CONFIRMATION_DOCUMENT_CHECK);
    }
    
    @Override
    public boolean isPublicTrTripConfirmationRequired(@NonNull Employee employee) {
        return getPublicApprovalsSettingsFlag(employee, PublicApprovalsSettingsFlagType.TRIP_CONFIRMATION_ACTIVE);
    }
    
    @Override
    public boolean isPublicTrTripAwaitingAffirmative(Employee employee) {
        return getPublicApprovalsSettingsFlag(employee, PublicApprovalsSettingsFlagType.AWAITING_AFFIRMATIVE);
    }
    
    /**
     * Выбрать из настроек для ОТ одну по ее типу
     *
     * @param employee сотрудник
     * @param flagType тип поля из настроек
     *
     * @return значение поля из настроек
     */
    private boolean getPublicApprovalsSettingsFlag(Employee employee, PublicApprovalsSettingsFlagType flagType) {
        try {
            // извлечь настройки
            Department department;
            if (employee.getDepartment() != null) {
                department = employee.getDepartment();
            } else {
                throw new EntityNotFoundException(Department.class, Map.of("employee", employee.getId()));
            }
            UUID organizationId = department.getOrganization().getId();
            PublicTrApprovalsSettings settings = publicApprovalsSettingsService.get(organizationId);
            return switch (flagType) {
                case APPROVAL_DOCUMENT_CHECK -> settings.isApprovalDocumentCheck();
                case TRIP_CONFIRMATION_DOCUMENT_CHECK -> settings.isTripConfirmationDocumentCheck();
                case TRIP_CONFIRMATION_ACTIVE -> settings.isTripConfirmationActive();
                case AWAITING_AFFIRMATIVE -> settings.isAffirmativeActive();
                default -> throw new IllegalArgumentException();
            };
        } catch (RuntimeException ex) {
            // если настройки не найдены, либо какие-то ошибки в данных, то вернуть без аппрува
            if (ex.getMessage() != null) {
                log.warn(ex.getMessage());
            }
            // вернуть true (т.к. все настройки отменяют этап по false)
            return true;
        }
    }
    
    /**
     * Тип флага из настроек согласований для ОТ
     */
    private enum PublicApprovalsSettingsFlagType {
        APPROVAL_DOCUMENT_CHECK,
        TRIP_CONFIRMATION_DOCUMENT_CHECK,
        TRIP_CONFIRMATION_ACTIVE,
        AWAITING_AFFIRMATIVE
    }
}
