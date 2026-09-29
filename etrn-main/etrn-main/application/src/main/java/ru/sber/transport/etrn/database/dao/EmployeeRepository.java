package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.etrn.database.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {


    /**
     * Find employee with department by ID.
     *
     * @param userId ID of user.
     * @return employee.
     */
    @EntityGraph("employeeWithDepartment")
    @Query("select e from Employee e where e.userId = :userId")
    Optional<Employee> findByUserIdWithDepartment(UUID userId);

    /**
     * Получить список сотрудников по флагу активности
     *
     * @param isActive флаг активности
     * @return список сотрудников
     */
    List<Employee> findAllByActive(boolean isActive);
}