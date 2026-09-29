package ru.sberbank.ditsib.transport.reports.exception.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.handlers.RequestExceptionHandler;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Component
@ControllerAdvice
@RequiredArgsConstructor
public class ReportsExceptionHandlerImpl extends RequestExceptionHandler {
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request
                                                                 ) {
        ExceptionBody.ExceptionBodyBuilder bodyBuilder =
                ExceptionBody.builder()
                             .message(e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(
                                     Collectors.joining("\n")))
                             .path(((ServletWebRequest) request).getRequest().getRequestURI())
                             .timestamp(OffsetDateTime.now())
                             .path(((ServletWebRequest) request).getRequest().getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(bodyBuilder.build());
    }
    
}
