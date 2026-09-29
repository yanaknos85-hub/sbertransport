package ru.sber.transport.address.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.provider.EmployeeProvider;
import ru.sber.transport.address.business.use_cases.Employees;

import java.util.Optional;

import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка работы бизнес-кейсов")
class EmployeesImplTest {

    private final EmployeeProvider provider = mock(EmployeeProvider.class);

    private final Employees employees = new EmployeesImpl(provider);

    @Test
    @DisplayName("Проверка сохранения нового")
    void test_save_new() {
        var source = Instancio.create(Employee.class);

        employees.update(source);

        verify(provider).save(source);
    }

    @Test
    @DisplayName("Проверка сохранения измененного")
    void test_save_edited() {
        var source = Instancio.create(Employee.class);

        when(provider.get(source.id())).thenReturn(Optional.of(source));

        employees.update(source);

        verify(provider, never()).save(source);
    }

}