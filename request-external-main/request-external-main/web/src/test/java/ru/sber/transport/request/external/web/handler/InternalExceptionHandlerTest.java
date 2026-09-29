package ru.sber.transport.request.external.web.handler;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.request.external.business.exception.BusinessException;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;
import ru.sber.transport.request.external.providers.exceptions.ReserveException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.request.external.providers.exceptions.ReserveException.Type.DATA_NOT_FOUND;
import static ru.sber.transport.request.external.providers.exceptions.ReserveException.Type.NOT_SUFFICIENT;
import static ru.sber.transport.request.external.providers.exceptions.ReserveException.Type.SERVICE_NOT_AVAILABLE;
import static ru.sber.transport.request.external.providers.exceptions.ReserveException.Type.TYPE_NOT_AVAILABLE;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка хандлера ошибок")
class InternalExceptionHandlerTest {

    private final InternalExceptionHandler handler = new InternalExceptionHandler();

    public static Stream<Arguments> exceptionsSource() {
        // HttpStatus status, ReserveException.Type type, String message, String entityName, String entityId, String constraintType
        return Stream.of(
                Arguments.of(HttpStatus.CONFLICT, NOT_SUFFICIENT, "Reserve sum too big", null, null, "NOT_SUFFICIENT"),
                Arguments.of(HttpStatus.NOT_FOUND, TYPE_NOT_AVAILABLE, "Requested type is not available", "TRANSPORT_TYPE", "TAXI", null),
                Arguments.of(HttpStatus.NOT_FOUND, SERVICE_NOT_AVAILABLE, "Requested service is not available", "SERVICE_TYPE", "PASSENGER", null),
                Arguments.of(HttpStatus.NOT_FOUND, DATA_NOT_FOUND, "Reserve data not found", "Reserve", null, null)
        );
    }

    @Test
    @DisplayName("Проверка исключения файла - не найден")
    void test_handleFileNotFound() {
        final var e = new StatusRuntimeException(io.grpc.Status.NOT_FOUND);
        final var request = new ServletWebRequest(new MockHttpServletRequest("GET", "/uri"));

        final var response = handler.handleException(e, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("File not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/uri");
    }

    @Test
    @DisplayName("Проверка исключения файла - другая причина")
    void test_handleFileFailed() {
        final var e = new StatusRuntimeException(Status.RESOURCE_EXHAUSTED);
        final var request = new ServletWebRequest(new MockHttpServletRequest("GET", "/uri"));

        final var response = handler.handleException(e, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("RESOURCE_EXHAUSTED");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/uri");
    }

    @MethodSource("exceptionsSource")
    @ParameterizedTest
    @DisplayName("Проверка ошибок лимита")
    void test_handleGrpcLimitException(HttpStatus status, ReserveException.Type type, String message, String entityName, String entityId, String constraintType) {
        final var e = new ReserveException(type);
        final var request = new ServletWebRequest(new MockHttpServletRequest("GET", "/uri"));

        final var body = handler.handleException(e, request);

        assertThat(body.getStatusCode()).isEqualTo(status);
        assertThat(body.getBody()).isNotNull();
        final var error = body.getBody();
        assertThat(error.getMessage()).isEqualTo(message);
        if (entityName != null) {
            assertThat(error.getEntity().getName()).isEqualTo(entityName);
        }
        if (entityId != null) {
            assertThat(error.getEntity().getId()).isEqualTo(entityId);
        }
        if (constraintType != null) {
            assertThat(error.getProblems().iterator().next().getConstraints().getFirst().getType()).isEqualTo(constraintType);
        }
    }

    @Test
    @DisplayName("Проверка исключения превышения лимита длительности поездок")
    void test_handleDurationLimitExceededException() {
        final var e = new DurationLimitExceededException();
        final var request = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/v1/trip-orders"));

        final var response = handler.handleException(e, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Превышен лимит длительности поездок");
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/trip-orders");
        assertThat(response.getBody().getProblems()).isNotEmpty();
        assertThat(response.getBody().getProblems().iterator().next().getConstraints().getFirst().getType())
                .isEqualTo("DURATION_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("Проверка исключения взаимодействия с брокером сообщений")
    void test_handleBrokerException() {
        final var message = UUID.randomUUID().toString();
        final var ex = new BusinessException(message);
        final var request = new ServletWebRequest(new MockHttpServletRequest("GET", "/uri"));

        final var response = handler.handleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}