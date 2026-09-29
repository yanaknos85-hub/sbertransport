package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с делегатами
 */
public interface DelegateService {
    
    /**
     * Поиск делегата по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return делегат.
     */
    Optional<Delegate> get(UUID id);
    
    /**
     * Удаление делегата.
     *
     * @param delegate Делегат на удаление.
     */
    void delete(Delegate delegate);
    
    /**
     * Сохранение делегата.
     *
     * @param delegate делегат.
     */
    void save(Delegate delegate);
    
    /**
     * Получить, который делегировал полномочия на указанного.
     *
     * @param employee сотрудник.
     *
     * @return делегат.
     */
    List<Delegate> getEmployeeDelegateRecords(Employee employee);
    
    /**
     * Получить сотрудников, кому делегировал полномочия указанный..
     *
     * @param employee сотрудник.
     *
     * @return делегат.
     */
    List<Delegate> getDelegatesBySupervisor(Employee employee);
}
