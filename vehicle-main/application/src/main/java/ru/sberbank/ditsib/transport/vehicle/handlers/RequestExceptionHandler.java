package ru.sberbank.ditsib.transport.vehicle.handlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sberbank.ditsib.transport.vehicle.dto.ErrorResponseDto;
import ru.sberbank.ditsib.transport.vehicle.exception.ConflictException;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.exception.FuelEngineTypesRelationValidationException;
import ru.sberbank.ditsib.transport.vehicle.exception.NotFoundException;

import java.util.HashMap;
import java.util.UUID;

/**
 * Обработка rest исключений
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String EXCEPTION_OCCURRED_MSG = "Exception occurred ";

    public static final String ERROR_OCCURRED_MSG = "Error occurred (errorId=%s) ";

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

    @ExceptionHandler
    public ResponseEntity<Object> handleTaskRejectedException(TaskRejectedException exception, WebRequest request) {
        log.info("Попробуйте снова через несколько минут");
        log.debug(exception.getMessage(), exception);
        return handleExceptionInternal(exception,
                "Попробуйте снова через несколько минут",
                new HttpHeaders(),
                HttpStatus.TOO_EARLY,
                request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(EXCEPTION_OCCURRED_MSG, exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Удаление невозможно в связи с наличием связных записей")).build();
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleFuelEngineTypesRelationException(FuelEngineTypesRelationValidationException exception) {
        log.error(EXCEPTION_OCCURRED_MSG, exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage())).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleException(EntityAlreadyExistsException exception, HttpServletRequest request) {
        var errorId = UUID.randomUUID();
        log.error(ERROR_OCCURRED_MSG.formatted(errorId.toString()), exception);
        return ErrorResponseDto.of(errorId, HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleConflictException(ConflictException exception, HttpServletRequest request) {
        var errorId = UUID.randomUUID();
        log.error(ERROR_OCCURRED_MSG.formatted(errorId.toString()), exception);
        return ErrorResponseDto.of(errorId, HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleBusinessException(NotFoundException exception) {
        log.error(EXCEPTION_OCCURRED_MSG, exception);
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage())).build();
    }
}