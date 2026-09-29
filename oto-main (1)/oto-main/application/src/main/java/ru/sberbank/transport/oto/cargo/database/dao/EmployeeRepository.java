package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Department;
import ru.sberbank.transport.oto.cargo.database.model.Position;
import ru.sberbank.transport.oto.cargo.database.model.Employee;

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
    
    Optional<Employee> findByUserId(UUID userId);
}
