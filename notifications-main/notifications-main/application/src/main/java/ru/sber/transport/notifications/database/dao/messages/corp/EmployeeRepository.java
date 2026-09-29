package ru.sber.transport.notifications.database.dao.messages.corp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.model.coprorate.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


/**
 * Employee repository
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    
    /**
     * Find employee by ID.
     *
     * @param userId ID of user.
     *
     * @return employee.
     */
    Optional<Employee> findByUserId(UUID userId);

    @Query("select em from Employee em inner join TripRequest req on em.id = req.passengerId " +
           "where req.id in (:requestIds)")
    List<Employee> findAllByRequestIds(@Param("requestIds") Set<UUID> requestIds);
}
