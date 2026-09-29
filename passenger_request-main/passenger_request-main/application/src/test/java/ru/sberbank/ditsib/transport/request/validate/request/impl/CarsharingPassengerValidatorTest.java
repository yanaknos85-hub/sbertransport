package ru.sberbank.ditsib.transport.request.validate.request.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка валидации каршеринговых заявок")
class CarsharingPassengerValidatorTest {

    private final NewRequestValidator validator = new CarsharingPassengerValidator();

    public static Stream<Arguments> requestValidationSource() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), UUID.randomUUID(), new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заказ каршеринга коллеге невозможен.")),
                Arguments.of(UUID.fromString("00000000-0000-0000-0000-000000000000"), UUID.fromString("00000000-0000-0000-0000-000000000000"), null)
        );
    }

    @ParameterizedTest
    @MethodSource("requestValidationSource")
    @DisplayName("Проверка валидации")
    void test(UUID passengerId, UUID enteredEmployeeId, Exception expected) {
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).set(Select.field(EmployeeDTO::id), passengerId).create())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), enteredEmployeeId)
                .create();

        if (expected == null) {
            validator.validate(request, employee);
        } else {
            try {
                validator.validate(request, employee);
                fail("Expected exception");
            } catch (Exception e) {
                assertEquals(expected.getClass(), e.getClass());
                assertEquals(expected.getMessage(), e.getMessage());
            }
        }
    }

}