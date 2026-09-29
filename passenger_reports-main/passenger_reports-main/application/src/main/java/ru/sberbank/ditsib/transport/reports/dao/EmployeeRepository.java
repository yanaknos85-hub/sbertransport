package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Employee_;
import ru.sberbank.ditsib.transport.reports.model.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query(value="select e from Employee e where" +
            "(:firstName is null or e.firstName like :firstName) and " +
            "(:lastName is null or e.lastName like :lastName) and  " +
            "(:patronymic is null or e.patronymic like :patronymic) and " +
            "(coalesce(:departments, null) is null or e.department in (:departments)) and" +
            "(coalesce(:positions, null) is null or e.position in (:positions))")
    List<Employee> findAllByAllParams(String firstName,
                                                    String lastName,
                                                    String patronymic,
                                                    List<Department> departments,
                                                    List<Position> positions);
    
    @Query(value="FROM Employee e WHERE e.userId = :userId AND e.department.organizationId = :organizationId ")
    Optional<Employee> findByUserIdAndOrganization(UUID userId, UUID organizationId);
    
    @EntityGraph(attributePaths = { Employee_.DEPARTMENT })
    Optional<Employee> findByUserId(UUID userId);
    
    @Query("select distinct dep.organizationId as organizationId " +
           "from Employee emp inner join Department dep on emp.department.id = dep.id " +
           "where emp.userId = :userId")
    Optional<UUID> findOrganizationIdByUserId(UUID userId);
}
