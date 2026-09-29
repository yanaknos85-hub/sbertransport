package ru.sber.transport.business.providers;

import java.util.UUID;
import ru.sber.transport.request.external.model.Employee;

/**
 * Провайдер данных о сотрудниках.
 */
public interface EmployeesProvider {

    /**
     * Сохраняет сотрудника
     *
     * @param source данные сотрудника
     * @return сохраненный сотрудник
     */
    Employee save(Employee source);

    /**
     * Получает сотрудника по идентификатору
     *
     * @param id идентификатор сотрудника
     * @return сотрудник
     */
    Employee get(UUID id);
}
