package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
    Optional<Employee> findByUserId(UUID userId);
    
    /**
     * Получить список сотрудников по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список сотрудников
     */
    List<Employee> findAllByActive(boolean isActive);
    
    /**
     * @param mobilePhone
     *
     * @return
     */
    @Query("select e from Employee e where e.active = :active and " +
           "e.mobilePhone like concat('%', :mobilePhone, '%') and " +
           "lower(concat(e.lastName,' ',e.firstName,' '," +
           "case when e.patronymic is not null then e.patronymic " +
           "else '' end)) like lower(concat('%', :fio, '%'))")
    List<Employee> findByActiveAndPhoneAndFio(
            @Param("active") boolean active
            , String mobilePhone, String fio
                                             );
    
    @Query("select e from Employee e left join fetch e.approveDepartments where e.id = :userId")
    Optional<Employee> findByUserIdWithApproveDepartments(UUID userId);
    
    Stream<Employee> findAllByIdIn(Collection<UUID> uuidSet);
    
    Optional<Employee> findFirstByMobilePhone(String phone);
}
