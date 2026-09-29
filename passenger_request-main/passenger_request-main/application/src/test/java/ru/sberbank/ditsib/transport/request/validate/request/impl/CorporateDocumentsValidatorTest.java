package ru.sberbank.ditsib.transport.request.validate.request.impl;

import io.qameta.allure.Feature;
import org.assertj.core.api.Condition;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException;
import ru.sberbank.ditsib.transport.request.service.grpc.CorporateDocumentValidationGrpcClient;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка валидации корпоративных документов")
class CorporateDocumentsValidatorTest {

    private final CorporateDocumentValidationGrpcClient validationClient = mock(CorporateDocumentValidationGrpcClient.class);

    private final CorporateDocumentsValidator validator = new CorporateDocumentsValidator(validationClient);

    @Test
    @DisplayName("Успешная валидация — вызов gRPC-клиента и возврат без ошибок")
    void test_validate_success() {
        validator.setCorporateDocumentValidationForPersonalEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        assertDoesNotThrow(() -> validator.validate(request, employee));

        verify(validationClient).validateDocuments(
                (request.getPassenger().id()),
                (request.getPersonalCarId()),
                (request.getDesiredDate()),
                (null)
        );
    }

    @Test
    @DisplayName("Ошибка валидации — gRPC-клиент вернул DocumentsValidationFailedException")
    void test_validate_failedDocuments() {
        validator.setCorporateDocumentValidationForPersonalEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        doThrow(new DocumentsValidationFailedException(List.of("Документ просрочен")))
                .when(validationClient)
                .validateDocuments(any(), any(), any(), isNull());

        try {
            validator.validate(request, employee);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationFailedException e) {
            assertThat(e.getErrors()).isEqualTo(List.of("Документ просрочен"));
        }
    }

    @Test
    @DisplayName("Таймаут gRPC — ошибка DocumentsValidationTimeoutException")
    void test_validate_timeout() {
        validator.setCorporateDocumentValidationForPersonalEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        doThrow(new DocumentsValidationTimeoutException("Сервис CorporateService не отвечает"))
                .when(validationClient)
                .validateDocuments(any(), any(), any(), any());

        try {
            validator.validate(request, employee);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Проброс неизвестного исключения от gRPC-клиента")
    void test_validate_unknownException() {
        validator.setCorporateDocumentValidationForPersonalEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        doThrow(new RuntimeException("Неизвестная ошибка"))
                .when(validationClient)
                .validateDocuments(any(), any(), any(), any());

        try {
            validator.validate(request, employee);
            fail("DocumentsValidationTimeoutException expected");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Неизвестная ошибка");
        }
    }

    @Test
    @DisplayName("Успешная валидация для каршеринга — вызов gRPC-клиента и возврат без ошибок")
    void test_validate_carsharing_success() {
        validator.setCorporateDocumentValidationForCarsharingEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.CARSHARING)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        assertDoesNotThrow(() -> validator.validate(request, employee));

        verify(validationClient).validateDocumentsForCarSharingTrip(
                request.getPassenger().id(),
                request.getDesiredDate()
        );

        verify(validationClient, never())
                .validateDocuments(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Валидация для каршеринга не выполняется при сorporateDocumentValidationEnabled = false")
    void test_validate_carsharing_falseFlag() {
        validator.setCorporateDocumentValidationForCarsharingEnabled(false);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.CARSHARING)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        assertDoesNotThrow(() -> validator.validate(request, employee));

        verify(validationClient, never())
                .validateDocumentsForCarSharingTrip(any(), any());
    }

    @Test
    @DisplayName("Валидация для личного транспорта не выполняется при сorporateDocumentValidationForPersonalEnabled = false")
    void test_validate_personal_falseFlag() {
        validator.setCorporateDocumentValidationForPersonalEnabled(false);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PERSONAL)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        assertDoesNotThrow(() -> validator.validate(request, employee));

        verify(validationClient, never())
                .validateDocuments(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Ошибка валидации каршеринга — gRPC-клиент вернул DocumentsValidationFailedException")
    void test_validate_carsharing_failedDocuments() {
        validator.setCorporateDocumentValidationForCarsharingEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.CARSHARING)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        doThrow(new DocumentsValidationFailedException("Нет разрешения на каршеринг"))
                .when(validationClient)
                .validateDocumentsForCarSharingTrip(any(), any());

        try {
            validator.validate(request, employee);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationFailedException e) {
            assertThat(e.getErrors()).isEqualTo(List.of("Нет разрешения на каршеринг"));
        }
    }

    @Test
    @DisplayName("Таймаут gRPC каршеринга — ошибка DocumentsValidationTimeoutException")
    void test_validate_carsharing_timeout() {
        validator.setCorporateDocumentValidationForCarsharingEnabled(true);
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.CARSHARING)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        doThrow(new DocumentsValidationTimeoutException("Сервис CorporateService не отвечает"))
                .when(validationClient)
                .validateDocumentsForCarSharingTrip(any(), any());

        try {
            validator.validate(request, employee);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Неподдерживаемый тип транспорта — вызовов gRPC-клиента нет")
    void test_validate_unsupportedTransportType() {
        final var request = Instancio.of(NewRequestDTO.class)
                .set(Select.field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class).create())
                .set(Select.field(NewRequestDTO::getTransportType), TransportTypeEnum.PUBLIC)
                .set(Select.field(NewRequestDTO::getPersonalCarId), UUID.randomUUID())
                .set(Select.field(NewRequestDTO::getDesiredDate), LocalDateTime.now())
                .create();
        final var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), request.getPassenger().id())
                .create();

        assertDoesNotThrow(() -> validator.validate(request, employee));

        verify(validationClient, never())
                .validateDocuments(any(), any(), any(), any());
        verify(validationClient, never())
                .validateDocumentsForCarSharingTrip(any(), any());
    }

    @Test
    @DisplayName("validationEnabledFor возвращает PERSONAL и CARSHARING")
    void test_validationEnabledFor() {
        var types = validator.validationEnabledFor();

        assertThat(types).has(
                new Condition<>(transportTypeEnums -> {
                    boolean personal = transportTypeEnums.contains(TransportTypeEnum.PERSONAL);
                    boolean carsharing = transportTypeEnums.contains(TransportTypeEnum.CARSHARING);
                    return personal && carsharing;
                }, "PERSONAL && CARSHARING"));
        assertThat(types.size()).isEqualTo(2);
    }
}