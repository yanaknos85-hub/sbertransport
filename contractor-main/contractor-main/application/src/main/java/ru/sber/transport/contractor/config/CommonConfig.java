package ru.sber.transport.contractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.scripting.reactive.ScriptUtils;

@Configuration
public class CommonConfig {

    @Bean
    public ConsentFunction getEmployeeConsentFunction(EmployeeRepository employeeRepository) {
        return model -> employeeRepository.findByUserId(model.getId())
                .map(Employee::isConsent)
                .orElse(false);
    }

    @Bean
    public ScriptUtils scriptUtils() {
        return new ScriptUtils();
    }

}
