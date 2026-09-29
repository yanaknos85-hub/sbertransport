package ru.sberbank.ditsib.transport.request.service.grpc.impl;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.corporate.grpc.service.DocumentValidationServiceGrpc;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsForCarSharingRequest;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsForCarSharingResponse;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsRequest;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsResponse;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка gRPC-клиента валидации корпоративных документов")
class CorporateDocumentValidationGrpcClientImplTest {

    @Mock
    private DocumentValidationServiceGrpc.DocumentValidationServiceBlockingStub stub;

    @Mock
    private DocumentValidationServiceGrpc.DocumentValidationServiceBlockingStub stubWithDeadline;

    @InjectMocks
    private CorporateDocumentValidationGrpcClientImpl client;

    @Test
    @DisplayName("Успешная валидация — response.getValid() == true")
    void test_validate_success() {
        var response = ValidateDocumentsResponse.newBuilder()
                .setValid(true)
                .build();
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doReturn(response).when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        assertDoesNotThrow(() -> client.validateDocuments(
                UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null));
    }

    @Test
    @DisplayName("Ошибка валидации — response.getValid() == false -> DocumentsValidationFailedException")
    void test_validate_failed() {
        var response = ValidateDocumentsResponse.newBuilder()
                .setValid(false)
                .addErrors("Документ просрочен")
                .addErrors("Отсутствует доверенность")
                .build();
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doReturn(response).when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        try {
            client.validateDocuments(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
            fail("DocumentsValidationFailedException expected");
        } catch (DocumentsValidationFailedException e) {
            assertThat(e.getErrors()).isEqualTo(List.of("Документ просрочен", "Отсутствует доверенность"));
        }
    }

    @Test
    @DisplayName("Таймаут gRPC — DEADLINE_EXCEEDED -> DocumentsValidationTimeoutException")
    void test_validate_deadlineExceeded() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.DEADLINE_EXCEEDED))
                .when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        try {
            client.validateDocuments(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Сервис недоступен — UNAVAILABLE -> DocumentsValidationTimeoutException")
    void test_validate_unavailable() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        try {
            client.validateDocuments(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Любая другая gRPC-ошибка -> DocumentsValidationTimeoutException с общим сообщением")
    void test_validate_otherGrpcError() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT))
                .when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        try {
            client.validateDocuments(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null);
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис валидации документов временно недоступен");
        }
    }

    @Test
    @DisplayName("colleagueEmployeeId передаётся в запрос при не null")
    void test_validate_withColleagueId() {
        var response = ValidateDocumentsResponse.newBuilder()
                .setValid(true)
                .build();
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doReturn(response).when(stubWithDeadline).validateDocuments(any(ValidateDocumentsRequest.class));

        assertDoesNotThrow(() -> client.validateDocuments(
                UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Успешная валидация каршеринга — response.getValid() == true")
    void test_validate_carsharing_success() {
        var response = ValidateDocumentsForCarSharingResponse.newBuilder()
                .setValid(true)
                .build();
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doReturn(response).when(stubWithDeadline).validateDocumentsForCarSharing(any(ValidateDocumentsForCarSharingRequest.class));

        assertThatCode(() -> client.validateDocumentsForCarSharingTrip(UUID.randomUUID(), LocalDateTime.now())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ошибка валидации каршеринга — response.getValid() == false -> DocumentsValidationFailedException")
    void test_validate_carsharing_failed() {
        var response = ValidateDocumentsForCarSharingResponse.newBuilder()
                .setValid(false)
                .setError("Нет разрешения на каршеринг")
                .build();
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doReturn(response).when(stubWithDeadline).validateDocumentsForCarSharing(any(ValidateDocumentsForCarSharingRequest.class));

        try {
            client.validateDocumentsForCarSharingTrip(UUID.randomUUID(), LocalDateTime.now());
            fail("DocumentsValidationFailedException expected");
        } catch (DocumentsValidationFailedException e) {
            assertThat(e.getErrors()).isEqualTo(List.of("Нет разрешения на каршеринг"));
        }
    }

    @Test
    @DisplayName("Таймаут gRPC каршеринга — DEADLINE_EXCEEDED -> DocumentsValidationTimeoutException")
    void test_validate_carsharing_deadlineExceeded() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.DEADLINE_EXCEEDED))
                .when(stubWithDeadline).validateDocumentsForCarSharing(any(ValidateDocumentsForCarSharingRequest.class));

        try {
            client.validateDocumentsForCarSharingTrip(UUID.randomUUID(), LocalDateTime.now());
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Сервис каршеринга недоступен — UNAVAILABLE -> DocumentsValidationTimeoutException")
    void test_validate_carsharing_unavailable() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(stubWithDeadline).validateDocumentsForCarSharing(any(ValidateDocumentsForCarSharingRequest.class));

        try {
            client.validateDocumentsForCarSharingTrip(UUID.randomUUID(), LocalDateTime.now());
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис CorporateService не отвечает");
        }
    }

    @Test
    @DisplayName("Любая другая gRPC-ошибка каршеринга -> DocumentsValidationTimeoutException с общим сообщением")
    void test_validate_carsharing_otherGrpcError() {
        doReturn(stubWithDeadline).when(stub).withDeadlineAfter(anyLong(), any());
        doThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT))
                .when(stubWithDeadline).validateDocumentsForCarSharing(any(ValidateDocumentsForCarSharingRequest.class));

        try {
            client.validateDocumentsForCarSharingTrip(UUID.randomUUID(), LocalDateTime.now());
            fail("DocumentsValidationTimeoutException expected");
        } catch (DocumentsValidationTimeoutException e) {
            assertThat(e.getMessage()).isEqualTo("Сервис валидации документов временно недоступен");
        }
    }
}