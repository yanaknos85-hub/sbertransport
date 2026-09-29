package ru.sberbank.ditsib.transport.tariff.service;

import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {
    
    /**
     * Получение департамента по id
     * @param id
     * @return сущность департамента
     */
    Optional<Department> get(UUID id);
    
    /**
     * Сохранение департамента
     * @param department сущность департамента для сохранения
     * @return сохраненная сущность
     */
    Department save(Department department);
    
    /**
     * Удаление департамента
     * @param department Департамент
     */
    void delete(Department department);
}
