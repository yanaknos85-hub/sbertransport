package ru.sber.transport.contractor.database.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.database.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Employee repository
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /**
     * Получить список сотрудников по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список сотрудников
     */
    List<Employee> findByActive(boolean isActive);
    
    /**
     * Find employee by ID.
     *
     * @param userId ID of user.
     *
     * @return employee.
     */
    Optional<Employee> findByUserId(UUID userId);
}
