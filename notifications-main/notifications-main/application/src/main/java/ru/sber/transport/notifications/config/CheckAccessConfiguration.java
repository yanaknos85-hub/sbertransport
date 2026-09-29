package ru.sber.transport.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;

/**
 * Конфигурация пути получения организации пользователя для проверки принадлежности организации.
 */
@Configuration
public class CheckAccessConfiguration {

    @Bean
    EmployeeOrganizationFunction checkAccessOrganization(
            EmployeeRepository employeeRepository, DepartmentRepository departmentRepository
    ) {
        return authenticated -> employeeRepository.findByUserId(authenticated)
                .map(Employee::getDepartmentId)
                .flatMap(departmentRepository::findById)
                .map(Department::getOrganizationId).orElse(null);
    }

}
