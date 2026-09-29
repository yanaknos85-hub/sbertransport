package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.EmployeeRepository;
import ru.sberbank.transport.oto.cargo.database.model.Department;
import ru.sberbank.transport.oto.cargo.database.model.Employee;
import ru.sberbank.transport.oto.cargo.database.model.Position;
import ru.sberbank.transport.oto.cargo.exception.UserNotFoundException;
import ru.sberbank.transport.oto.cargo.service.EmployeeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Optional<Employee> findEmployeeById(UUID id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> findAllEmployeesByAllParams(String fullname, List<Department> departments, List<Position> positions) {
        var fio = fullname.trim().split("\\s");
        var secondName = fio.length>0 ? fio[0] : null;
        var firstName = fio.length>1 ? fio[1] : null;
        var patronymic= new StringBuilder();
        for (int i = 2; i < fio.length; i++) {
            patronymic.append(fio[i]).append(" ");
        }
        return employeeRepository.findAllByAllParams(firstName,
                secondName,
                patronymic.toString().trim(),
                departments,
                positions);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee findOrCreateEmployeeById(UUID id) {
        var employee = employeeRepository.findById(id);
        return employee.orElseGet(() -> employeeRepository.save(Employee.builder()
                                                                        .id(id)
                                                                        .build()));
    }

    @Override
    public void deleteById(UUID id) {
        employeeRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID employeeId) {
        return employeeRepository.existsById(employeeId);
    }
    
    @Override
    public Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeRepository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}