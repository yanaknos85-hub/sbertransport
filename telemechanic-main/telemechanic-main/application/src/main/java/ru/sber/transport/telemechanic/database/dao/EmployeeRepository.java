package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Employee_;
import ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto;

import java.util.List;
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
     *
     * @return employee.
     */
    @EntityGraph(attributePaths = { Employee_.ORGANIZATION, Employee_.DEPARTMENT, Employee_.POSITION })
    Optional<Employee> findByUserId(UUID userId);
    
    List<Employee> findEmployeesByFullNameIndexContainingIgnoreCase(String fio);
    
    /**
     * Find employee info list by department ID and personnel number.
     *
     * @param departmentId department ID.
     * @param personnelNumberPattern personnel number pattern. Will be used in %% for LIKE query.
     *
     * @return {@link EmployeeInfoByDepartmentDto}
     */
    @Query(value = """
                    select new ru.sber.transport.telemechanic.dto.department.EmployeeInfoByDepartmentDto(
                    e.id,
                    e.personnelNumber,
                    (e.lastName || ' ' || e.firstName || coalesce(' ' || e.patronymic, '')) as fullName,
                    p.positionName,
                    t.tin
                    )
                    from Employee e
                    left join e.position as p
                    left join Tin t on t.employeeId = e.id
                    where e.department.id = :departmentId and e.personnelNumber like concat('%', :personnelNumberPattern, '%')
                   """)
    List<EmployeeInfoByDepartmentDto> searchByDepartmentAndPersonnelNumber(UUID departmentId, String personnelNumberPattern);
    
    /**
     * Получаем уникальный идентификатор (числовой) по идентификатору записи пользователя с таблицы corporate.user
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return Уникальный идентификатор (числовой)
     */
    @Query("select e.organization.digitId from Employee e where e.userId = :userId")
    Long findDigitIdByUserId(UUID userId);
}

