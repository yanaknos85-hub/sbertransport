package ru.sberbank.ditsib.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sberbank.ditsib.exception.BusinessException;

import java.util.HashMap;
import java.util.Optional;

/**
 * Обработка rest исключений
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode statusCode,
                                                                  WebRequest request) {
        log.debug(exception.getMessage(), exception);
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.info("Не пройдены проверки по полям:" + errors);
        return handleExceptionInternal(exception, errors, headers, statusCode, request);
    }

    @ExceptionHandler({BusinessException.class})
    protected ResponseEntity<Object> handleBusinessException(RuntimeException exception,
                                                             WebRequest request) {
        log.info(exception.getMessage());
        log.debug(exception.getMessage(), exception);
        var status = Optional.ofNullable(exception.getClass().getAnnotation(ResponseStatus.class))
                .orElseThrow()
                .value();
        return handleExceptionInternal(exception,
                exception.getMessage(),
                new HttpHeaders(),
                status,
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException exception,
                                                                        WebRequest request) {
        log.info("Удаление невозможно в связи с наличием связных записей:{}", exception.getMessage());
        log.debug(exception.getMessage(), exception);
        return handleExceptionInternal(exception,
                "Удаление невозможно в связи с наличием связных записей",
                new HttpHeaders(),
                HttpStatus.CONFLICT,
                request);
    }
}