package ru.sber.transport.etrn.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.sber.transport.etrn.dto.ErrorResponseDto;
import ru.sber.transport.etrn.dto.LockConflictResponse;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Глобальный обработчик ошибок для REST контроллеров.
 */
@Slf4j
@RestControllerAdvice
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // ========== AttorneyCheckException ==========

    /**
     * Обработка исключений проверки доверенности.
     *
     * @param exception исключение AttorneyCheckException
     * @return ResponseEntity с HTTP-статусом из исключения
     */
    @ExceptionHandler(AttorneyCheckException.class)
    public ResponseEntity<ErrorResponseDto> handleAttorneyCheckException(AttorneyCheckException exception) {
        log.error("AttorneyCheckException: {}", exception.getMessage(), exception);
        return ResponseEntity
                .status(exception.getStatus())
                .body(new ErrorResponseDto(
                        "ATTORNEY_CHECK_ERROR",
                        exception.getMessage()
                ));
    }

    // ========== Business exceptions ==========

    @ExceptionHandler(EtrnNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EtrnNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LockConflictException.class)
    public ResponseEntity<LockConflictResponse> handleLockConflict(LockConflictException ex) {
        LockConflictResponse body = new LockConflictResponse(
                ex.getMessage(),
                ex.getLockedBy(),
                ex.getLockUntil()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Некорректный запрос");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // ========== General exception ==========

    /**
     * Обработка всех неиспорченных исключений.
     *
     * @param ex любое исключение
     * @return ResponseEntity с 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Необработанная ошибка", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}
