package ru.sberbank.transport.oto.cargo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.transport.oto.cargo.database.dao.EmployeeRepository;
import ru.sberbank.transport.oto.cargo.database.model.Department;
import ru.sberbank.transport.oto.cargo.database.model.Employee;

/**
 * Конфигурация пути получения организации пользователя для проверки принадлежности организации.
 */
@Configuration
public class CheckAccessConfiguration {
    
    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            EmployeeRepository employeeRepository
                                                        ) {
        return authenticated -> employeeRepository.findByUserId(authenticated)
                                                  .map(Employee::getDepartment)
                                                  .map(Department::getOrganizationId).orElse(null);
    }
    
}
