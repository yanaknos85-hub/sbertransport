package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.coprorate.Department;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с подразделениями.
 */
public interface DepartmentService {
    
    /**
     * Получение подразделения.
     *
     * @param departmentId идентификатор подразделения.
     * @return подразделение.
     */
    Optional<Department> get(UUID departmentId);
    
    /**
     * Сохранение подразделения.
     *
     * @param department подразделение.
     * @return сохраненное подразделение.
     */
    Department save(Department department);
    
    /**
     * Удаление подразделения.
     *
     * @param id идентификатор подразделения.
     */
    void delete(UUID id);
}
