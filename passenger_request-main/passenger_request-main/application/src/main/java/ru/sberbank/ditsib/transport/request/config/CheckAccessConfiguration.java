package ru.sberbank.ditsib.transport.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;

/**
 * Конфигурация пути получения организации пользователя для проверки принадлежности организации.
 */
@Configuration
public class CheckAccessConfiguration {
    
    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            EmployeeRepository employeeRepository
                                                        ) {
        return (authenticated) -> employeeRepository.findByUserId(authenticated)
                                                    .map(Employee::getDepartment)
                                                    .map(Department::getOrganization).map(Organization::getId).orElse(null);
    }
    
}
