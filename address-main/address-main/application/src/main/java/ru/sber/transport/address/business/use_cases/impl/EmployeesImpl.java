package ru.sber.transport.address.business.use_cases.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.provider.EmployeeProvider;
import ru.sber.transport.address.business.use_cases.Employees;

@RequiredArgsConstructor
@Component
public class EmployeesImpl implements Employees {

    private final EmployeeProvider provider;

    @Override
    public void update(Employee source) {
        if (provider.get(source.id()).isEmpty()) {
            provider.save(source);
        }
    }
}
