package ru.sberbank.transport.oto.cargo.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.exception.BadRequestException;
import ru.sberbank.transport.oto.cargo.exception.VisibilityScopeException;
import ru.sberbank.transport.oto.cargo.service.ValidationService;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ActiveProfiles("test")
@DisplayName("Тест ValidationServiceImpl")
class ValidationServiceImplTest {

    private final ValidationService validationService = new ValidationServiceImpl();

    @Test
    @DisplayName("Валидация успешная - указан organizationId")
    void testValidateRequestScopeVisibilityWithOrganizationId() {
        CargoRequestDto dto = Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::organizationId), UUID.randomUUID())
                .create();

        validationService.validateRequestScopeVisibility(dto);
    }

    @Test
    @DisplayName("Валидация успешная - указаны executorGroupIds")
    void testValidateRequestScopeVisibilityWithExecutorGroupIds() {
        CargoRequestDto dto = Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::executorGroupIds), Set.of(UUID.randomUUID(), UUID.randomUUID()))
                .create();

        validationService.validateRequestScopeVisibility(dto);
    }

    @Test
    @DisplayName("Валидация успешная - DTO с пустыми group ids (без group)")
    void testValidateRequestScopeVisibilityWithEmptyExecutorGroups() {
        CargoRequestDto dto = Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::organizationId), UUID.randomUUID())
                .set(Select.field(CargoRequestDto::isEmptyExecutorGroup), true)
                .create();

        validationService.validateRequestScopeVisibility(dto);
    }

    @Test
    @DisplayName("Ошибка: указаны одновременно organizationId и executorGroupIds")
    void testValidateRequestScopeVisibilityWithBothFilters() {
        CargoRequestDto dto = Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::organizationId), UUID.randomUUID())
                .set(Select.field(CargoRequestDto::executorGroupIds), Set.of(UUID.randomUUID()))
                .create();

        var exception = assertThrows(
                VisibilityScopeException.class,
                () -> validationService.validateRequestScopeVisibility(dto)
        );

        assertThat(exception.getMessage()).isEqualTo(VisibilityScopeException.ERROR_MESSAGE_BOTH_SCOPE);
    }

    @Test
    @DisplayName("Ошибка: null DTO")
    void testValidateRequestScopeVisibilityWithNullDto() {
        var exception = assertThrows(
                VisibilityScopeException.class,
                () -> validationService.validateRequestScopeVisibility(null)
        );

        assertThat(exception.getMessage()).isEqualTo(VisibilityScopeException.ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Ошибка: указаны одновременно isEmptyExecutorGroup и executorGroupIds")
    void testValidateEmptyExecutorGroupsWithBothFilters() {
        CargoRequestDto dto = Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::executorGroupIds), Set.of(UUID.randomUUID()))
                .set(Select.field(CargoRequestDto::isEmptyExecutorGroup), true)
                .create();

        var exception = assertThrows(
                BadRequestException.class,
                () -> validationService.validateEmptyExecutorGroups(dto)
        );

        assertThat(exception.getMessage()).isEqualTo(ValidationService.EXECUTOR_GROUP_EXCEPTION_MESSAGE);
    }
}
