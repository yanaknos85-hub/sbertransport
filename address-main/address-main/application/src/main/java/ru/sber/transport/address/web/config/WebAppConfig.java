package ru.sber.transport.address.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.provider.EmployeeProvider;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;

@Configuration
class WebAppConfig {

    @Bean
    EmployeeOrganizationFunction employeeOrganizationFunction(EmployeeProvider provider) {
        return id -> provider.get(id).map(Employee::organizationId).orElse(null);
    }

}
