package ru.sberbank.ditsib.transport.request.validate.request.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка валидации типов транспорта")
class TransportTypeValidatorTest {

    private final NewRequestValidator validator = new TransportTypeValidator();

    @Test
    @DisplayName("Проверка валидации типов транспорта")
    void test() {
        final var request = Instancio.create(NewRequestDTO.class);
        try {
            validator.validate(request, Instancio.create(Employee.class));
            fail("UnsupportedTransportTypeException expected");
        } catch (UnsupportedTransportTypeException e) {
            assertThat(e.getMessage()).isEqualTo("Логика работы для типа транспорта с ID '%s', не определена".formatted(request.getTransportType().getId()));
        }
    }

}