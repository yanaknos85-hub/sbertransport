package ru.sber.transport.request_checks.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.sber.transport.handlers.RequestExceptionHandler;
import ru.sber.transport.request_checks.dto.ErrorResponse;
import ru.sber.transport.request_checks.exception.DocumentsExpiredException;
import ru.sber.transport.request_checks.exception.DurationLimitExceededException;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.exception.RequestTimeoutException;

/**
 * Глобальный обработчик исключений для REST API
 */
@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class RequestCheckExceptionHandler extends RequestExceptionHandler {

    @ExceptionHandler(DurationLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleDurationLimitExceededException(
            DurationLimitExceededException ex) {
        log.error(ex.getMessage(), ex);

        val body = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MultipointRequestsLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleMultipointLimitExceededException(
            MultipointRequestsLimitExceededException ex) {
        log.error(ex.getMessage(), ex);

        val body = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DocumentsExpiredException.class)
    public ResponseEntity<ErrorResponse> handleDocumentsExpiredException(
            DocumentsExpiredException ex) {
        log.warn(ex.getMessage(), ex);

        val body = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(RequestTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleRequestTimeoutException(
            RequestTimeoutException ex) {
        log.warn(ex.getMessage(), ex);

        val body = new ErrorResponse(ex.getMessage());

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(body);
    }

}
