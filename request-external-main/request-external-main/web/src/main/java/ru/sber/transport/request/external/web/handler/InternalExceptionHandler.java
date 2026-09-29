package ru.sber.transport.request.external.web.handler;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.Entity;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.request.external.business.exception.BusinessException;
import ru.sber.transport.request.external.business.exception.CreatingException;
import ru.sber.transport.request.external.business.exception.DataConflictException;
import ru.sber.transport.request.external.business.exception.StatusSwitchForbiddenException;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;
import ru.sber.transport.request.external.providers.exceptions.ReserveException;
import ru.sber.transport.request.external.providers.exceptions.TariffProviderNotAvailableException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Класс, который отлавливает исключения, возникающие при работе с внешним API и отдает клиенту информацию об ошибке.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class InternalExceptionHandler {

    /**
     * Обработчик исключения, возникающего при неудачной попытке получить тариф.
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler({TariffProviderNotAvailableException.class})
    public ResponseEntity<ExceptionBody> handleException(TariffProviderNotAvailableException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ExceptionBody.builder()
                .message("Tariff provider not available")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now())
                .problem(Problem.builder().constraints(List.of(Constraint.builder().type("TARIFF_NOT_RESOLVABLE").build())).build())
                .build());
    }

    /**
     * Обработчик исключения, возникающего при конфликте данных.
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ExceptionBody> handleException(DataConflictException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionBody.builder()
                .message("Data conflict")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now())
                .problem(Problem.builder().field(e.getConflictedField()).value(e.getNewValue().toString()).constraints(List.of(Constraint.builder().type("DATA_CONFLICT").value(e.getOldValue()).build())).build())
                .build());
    }

    /**
     * Обработчик исключения, возникающего при попытке перевести заявку в запрещенный ей статус.
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(StatusSwitchForbiddenException.class)
    public ResponseEntity<ExceptionBody> handleException(StatusSwitchForbiddenException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionBody.builder()
                .message("Request status cannot be switched to status %s by %s".formatted(e.getNewState(), e.getAllowedRole()))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now())
                .problem(Problem.builder().field("status").value(e.getNewState().name()).constraints(List.of(Constraint.builder().type("SWITCH_STATUS_FORBID").build())).build())
                .build());
    }

    /**
     * Обработчик исключения, возникающего при попытке создать заявку.
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(CreatingException.class)
    public ResponseEntity<ExceptionBody> handleException(CreatingException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionBody.builder()
                .message("Failed to create order for current user")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now())
                .problem(Problem.builder().value(e.getId().toString()).constraints(List.of(Constraint.builder().type("REQUEST_CREATING_FORBIDDEN").build())).build())
                .build());
    }

    /**
     * Обработчик исключения, возникающего при ошибке gRPC
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ExceptionBody> handleException(StatusRuntimeException e, WebRequest request) {
        if (Status.NOT_FOUND.equals(e.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionBody.builder()
                    .message("File not found")
                    .path(((ServletWebRequest) request).getRequest().getRequestURI())
                    .timestamp(OffsetDateTime.now())
                    .build());
        } else {
            log.error("Unhandled error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionBody.builder()
                    .message(e.getMessage())
                    .path(((ServletWebRequest) request).getRequest().getRequestURI())
                    .timestamp(OffsetDateTime.now())
                    .build());
        }
    }

    @ExceptionHandler(ReserveException.class)
    public ResponseEntity<ExceptionBody> handleException(ReserveException e, WebRequest request) {
        final var requestURI = ((ServletWebRequest) request).getRequest().getRequestURI();
        final var now = OffsetDateTime.now();
        return switch (e.getType()) {
            case NOT_SUFFICIENT -> ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionBody.builder()
                    .message("Reserve sum too big")
                    .path(requestURI)
                    .timestamp(now)
                    .problem(Problem.builder().constraints(List.of(Constraint.builder().type(e.getType().name()).build())).build())
                    .build());
            case TYPE_NOT_AVAILABLE -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionBody.builder()
                    .message("Requested type is not available")
                    .path(requestURI)
                    .entity(Entity.builder().name("TRANSPORT_TYPE").id("TAXI").build())
                    .timestamp(now)
                    .build());
            case SERVICE_NOT_AVAILABLE -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionBody.builder()
                    .message("Requested service is not available")
                    .path(requestURI)
                    .entity(Entity.builder().name("SERVICE_TYPE").id("PASSENGER").build())
                    .timestamp(now)
                    .build());
            case DATA_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionBody.builder()
                    .message("Reserve data not found")
                    .path(requestURI)
                    .timestamp(now)
                    .entity(Entity.builder().name("Reserve").build())
                    .build());
        };
    }

    /**
     * Обработчик исключения, возникающего при превышении лимита длительности поездок.
     *
     * @param e       исключение, которое было сгенерировано.
     * @param request текущий запрос.
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(DurationLimitExceededException.class)
    public ResponseEntity<ExceptionBody> handleException(DurationLimitExceededException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionBody.builder()
                .message(e.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now())
                .problem(Problem.builder().constraints(List.of(Constraint.builder().type("DURATION_LIMIT_EXCEEDED").build())).build())
                .build());
    }

    /**
     * Обработчик исключений, возникающих в бизнес слое
     *
     * @param ex      исключение
     * @param request текущий запрос
     * @return HTTP-ответ с информацией об ошибке.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionBody> handleException(BusinessException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionBody.builder()
                        .message(ex.getMessage())
                        .path(((ServletWebRequest) request).getRequest().getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
                );
    }
}
