package ru.sberbank.ditsib.transport.request.validate.request.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка валидации мобильного телефона")
class MobilePhoneValidatorTest {

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final NewRequestValidator validator = new MobilePhoneValidator(employeeService);

    @Test
    @DisplayName("Проверка валидации мобильного телефона")
    void test_wrong() {
        try {
            final var request = Instancio.create(NewRequestDTO.class);
            final var employee = Instancio.of(Employee.class)
                    .ignore(Select.field(Employee::getMobilePhone))
                    .create();
            validator.validate(request, employee);
            fail("ResponseStatusException expected");
        } catch (ResponseStatusException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getReason()).isEqualTo("Не найден номер мобильного телефона пассажира - заказ невозможен");
        }
    }

}