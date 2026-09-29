package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository of employees
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /**
     * Find employee by ID.
     *
     * @param userId ID of user.
     * @return employee.
     */
    @EntityGraph("with-organization")
    Optional<Employee> findByUserId(UUID userId);

    /**
     * Find employee by personnel number.
     *
     * @param personnelNumber personnel number of user.
     * @return employee.
     */
    @EntityGraph("with-organization")
    Optional<Employee> findByPersonnelNumber(String personnelNumber);
}
