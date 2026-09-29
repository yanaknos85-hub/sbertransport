package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.EmployeeType;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    
    Optional<Employee> findByUserId(UUID userId);
    
    @Query("select distinct emp.id as id, emp.userId as userId, emp.firstName as firstName, emp.lastName as lastName, emp" +
           ".patronymic as patronymic, emp.departmentId as departmentId, dep.organizationId as organizationId " +
           "from Employee emp " +
           "inner join Department dep " +
           "on emp.departmentId = dep.id where emp.userId = :userId")
    Optional<EmployeeType> findByUserIdWithOrganizationId(@Param("userId") UUID userId);
}
