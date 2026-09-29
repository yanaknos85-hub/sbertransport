package ru.sber.transport.cargo.exchange.request.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String MESSAGE_PARAMETER = "message";

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException exception,
                                                                           HttpHeaders headers,
                                                                           HttpStatusCode statusCode,
                                                                           WebRequest request) {
        log.debug(exception.getMessage(), exception);

        // Extracting error details
        List<Map<String, String>> errors = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            Map<String, String> errorMap = new HashMap<>();
                            errorMap.put("parameter", result.getMethodParameter().getParameterName());
                            errorMap.put(MESSAGE_PARAMETER, error.getDefaultMessage());
                            return errorMap;
                        }))
                .toList();

        log.warn("Не пройдены проверки по полям: {}", errors);
        return handleExceptionInternal(exception, errors, headers, statusCode, request);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleBusinessException(RuntimeException exception,
                                                             WebRequest request) {
        var status = Optional.ofNullable(exception.getClass().getAnnotation(ResponseStatus.class));

        if (status.isEmpty()) {
            log.error(exception.getMessage(), exception);

            return handleExceptionInternal(exception,
                    Map.of(MESSAGE_PARAMETER, exception.getMessage()),
                    new HttpHeaders(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request);
        }

        log.info(exception.getMessage());
        log.debug(exception.getMessage(), exception);

        return handleExceptionInternal(exception,
                Map.of(MESSAGE_PARAMETER, exception.getMessage()),
                new HttpHeaders(),
                status.get().value(),
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException exception,
                                                                        WebRequest request) {
        log.warn("Удаление невозможно в связи с наличием связных записей:{}", exception.getMessage());
        log.debug(exception.getMessage(), exception);
        return handleExceptionInternal(exception,
                Map.of(MESSAGE_PARAMETER, "Удаление невозможно в связи с наличием связных записей"),
                new HttpHeaders(),
                HttpStatus.CONFLICT,
                request);
    }
}
