package ru.sberbank.ditsib.transport.srm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.handlers.RequestExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class SrmRequestExceptionHandlerImpl extends RequestExceptionHandler {
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionBody> handleException(NullPointerException e, WebRequest request) {
        var body = ExceptionBody.builder()
                                .message(e.getMessage())
                                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                                .build();
        log.error("SrmRequestExceptionHandlerImpl", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
