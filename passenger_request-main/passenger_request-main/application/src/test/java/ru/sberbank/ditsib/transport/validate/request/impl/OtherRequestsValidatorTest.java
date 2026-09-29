package ru.sberbank.ditsib.transport.validate.request.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.data.jpa.domain.Specification;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.CreateRequestConflictException;
import ru.sberbank.ditsib.transport.request.validate.request.impl.OtherRequestsValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Instancio.create;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка валидатора других заявок")
class OtherRequestsValidatorTest {

    private final RequestRepository requestRepository = mock(RequestRepository.class);

    private final OtherRequestsValidator otherRequestsValidator = new OtherRequestsValidator(requestRepository, Set.of(TransportTypeEnum.TAXI), Set.of());

    @Test
    @DisplayName("Проверка валидации")
    void validate_throwsException() {
        var request = of(NewRequestDTO.class)
                .set(field(NewRequestDTO::getTimeZone), "GMT+3")
                .set(field(NewRequestDTO::getDesiredDate), LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                .set(field(NewRequestDTO::getExpected), of(ExpectedDataDTO.class)
                        .set(field(ExpectedDataDTO::getTime), Duration.ofHours(10))
                        .create())
                .create();
        var employee = create(Employee.class);

        doReturn(true).when(requestRepository).exists(any(Specification.class));

        assertThatExceptionOfType(CreateRequestConflictException.class)
                .isThrownBy(() -> otherRequestsValidator.validate(request, employee))
                .withMessage("На выбранные дату и время уже есть активная заявка. Измените время или отмените действующую заявку.");
    }
}
