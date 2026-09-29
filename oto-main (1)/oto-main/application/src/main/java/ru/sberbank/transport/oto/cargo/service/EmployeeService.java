package ru.sberbank.transport.oto.cargo.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.transport.oto.cargo.database.model.Department;
import ru.sberbank.transport.oto.cargo.database.model.Employee;
import ru.sberbank.transport.oto.cargo.database.model.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    Optional<Employee> findEmployeeById(UUID id);

    List<Employee> findAllEmployeesByAllParams(String fullname, List<Department> department,
                                               List<Position> positions );
    Employee findOrCreateEmployeeById(UUID id);
    Employee save(Employee employee);
    void deleteById(UUID id);
    
    boolean existsById(UUID employeeId);
    
    /**
     * Получить Сотрудника из Authentication
     * @param authentication authentication
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication);
}
