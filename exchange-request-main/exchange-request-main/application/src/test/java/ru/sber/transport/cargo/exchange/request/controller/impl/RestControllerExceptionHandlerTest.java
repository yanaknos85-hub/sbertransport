package ru.sber.transport.cargo.exchange.request.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.sber.transport.cargo.exchange.request.service.UserService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RestControllerExceptionHandlerTest {

    private RestControllerExceptionHandler exceptionHandler;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        exceptionHandler = new RestControllerExceptionHandler();
    }

    @Test
    void handleHandlerMethodValidationException_ShouldReturnErrors() {
        // given
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        HttpHeaders headers = new HttpHeaders();
        org.springframework.http.HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);

        // when
        ResponseEntity<Object> response = exceptionHandler.handleHandlerMethodValidationException(
                exception, headers, statusCode, request);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleBusinessException_ShouldReturn500_WhenNoResponseStatusAnnotation() {
        // given
        RuntimeException exception = new RuntimeException("Test error");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);

        // when
        ResponseEntity<Object> response = exceptionHandler.handleBusinessException(exception, request);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Test error", body.get("message"));
    }

    @Test
    void handleBusinessException_ShouldReturnCustomStatus_WhenResponseStatusAnnotationPresent() {
        // given
        RuntimeException exception = new TestRuntimeException("Custom error");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);

        // when
        ResponseEntity<Object> response = exceptionHandler.handleBusinessException(exception, request);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Custom error", body.get("message"));
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnConflict() {
        // given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Test constraint violation");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);

        // when
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception, request);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Удаление невозможно в связи с наличием связных записей", body.get("message"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class TestRuntimeException extends RuntimeException {
        public TestRuntimeException(String message) {
            super(message);
        }
    }
}
