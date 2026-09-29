package ru.sberbank.ditsib.geo.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.handlers.RequestExceptionHandler;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Перехватчик ошибок запросов.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ControllerExceptionHandler extends RequestExceptionHandler {
    
    /**
     * Исключение гео-провайдера.
     *
     * @param e исключение.
     * @param request запрос.
     * @return ответ.
     */
    @ExceptionHandler(GeoApiException.class)
    public ResponseEntity<ExceptionBody> handleServiceException(GeoApiException e, WebRequest request) {
        if (Objects.equals(e.getCode(), 404)) {
            log.info(String.join("; ", e.getMessages()));
        } else {
            log.error(e.getLocalizedMessage(), e);
        }
        var exceptionBody = ExceptionBody.builder()
                                         .path(((ServletWebRequest) request).getRequest().getRequestURI())
                                         .timestamp(OffsetDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                                         .message(e.getMessages().stream().map(String::valueOf).collect(Collectors.joining("; ")))
                .build();
        
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(exceptionBody);
    }
    
}
