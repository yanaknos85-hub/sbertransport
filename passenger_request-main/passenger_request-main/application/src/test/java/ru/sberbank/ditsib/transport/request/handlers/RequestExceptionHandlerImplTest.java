package ru.sberbank.ditsib.transport.request.handlers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Isolated
@Feature("app_passenger_request")
@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка обработчика исключений валидации корпоративных документов")
class RequestExceptionHandlerImplTest {

    @InjectMocks
    private RequestExceptionHandlerImpl handler;

    @Test
    @DisplayName("DocumentsValidationFailedException -> 409 CONFLICT с ExceptionBody")
    void handleDocumentsValidationFailed() {
        var ex = new DocumentsValidationFailedException(List.of("Документ А", "Документ Б"));
        var httpRequest = new MockHttpServletRequest("POST", "/api/requests");
        var webRequest = new ServletWebRequest(httpRequest);

        ResponseEntity<Object> response = handler.handleDocumentsValidationFailed(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .isInstanceOf(ExceptionBody.class);
        assertThat(((ExceptionBody) response.getBody()).getMessage())
                .isEqualTo("Ошибка валидации документов");
    }

    @Test
    @DisplayName("DocumentsValidationTimeoutException -> 408 REQUEST_TIMEOUT с сообщением о недоступности")
    void handleDocumentsValidationTimeout() {
        var ex = new DocumentsValidationTimeoutException("Сервис валидации документов временно недоступен");
        var httpRequest = new MockHttpServletRequest("POST", "/api/requests");
        var webRequest = new ServletWebRequest(httpRequest);

        ResponseEntity<Object> response = handler.handleDocumentsValidationTimeout(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        assertThat(response.getBody())
                .isInstanceOf(String.class);
        assertThat((String) response.getBody())
                .isEqualTo("Сервис валидации документов временно недоступен");
    }
}