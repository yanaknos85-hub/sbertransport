package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с корп.каршерингами
 */
public interface CorporateCarsharingService {
    
    /**
     * Получить все доступные каршеринги для корп.клиента в регионе
     * @param organizationId корп.клиент
     * @return список доступных каршерингов
     */
    List<CorporateCarsharing> getByOrganizationId(UUID organizationId);
    
    /**
     * Получить корп.каршеринг для корп.клиента и контракта
     * @param contractId ID контракта
     * @param organizationId ID корп.клиента
     * @return корп.каршеринг
     */
    CorporateCarsharing getByContractIdAndOrganizationId(UUID contractId, UUID organizationId);
    
    /**
     * Проверить, есть ли сотрудник (по ID) в списке сотрудников
     * @param employees список сотрудников
     * @param employeeId сотрудник для проверки
     * @return признак наличия в списке
     */
    boolean checkEmployeesSetContainsUuid(Set<Employee> employees, UUID employeeId);
}
