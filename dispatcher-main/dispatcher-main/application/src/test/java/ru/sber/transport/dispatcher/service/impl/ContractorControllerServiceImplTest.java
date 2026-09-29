package ru.sber.transport.dispatcher.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.dto.EmailIntegrationParamsDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка контроллер-сервиса контрагентов")
class ContractorControllerServiceImplTest {
    
    @DisplayName("Проверка запрета на сохранение некорректного email ")
    @Test
    void checkEmail_wrong() {
        try (var factory = Validation.byDefaultProvider().configure().buildValidatorFactory()) {
            var validator = factory.getValidator();
            Set<ConstraintViolation<EmailIntegrationParamsDto>> violations;

            var email = new EmailIntegrationParamsDto("name", "name", "sbexchange kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", ".sbexchange_kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sb..exchange_kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov.@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@.taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru.");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@-taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru-");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());
        }
    }
    
    @DisplayName("Проверка отсутствия запрета на сохранение корректного email ")
    @Test
    void checkEmail_correct() throws JsonProcessingException {

        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EmailIntegrationParamsDto>> violations;

        var email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());

        email = new EmailIntegrationParamsDto("name", "name", "robot-sber-test@yandex-team.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());

        email = new EmailIntegrationParamsDto("name", "name", "dmitry.ivanov@yandex.team.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());
    }
}