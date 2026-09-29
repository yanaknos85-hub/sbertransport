package ru.sberbank.ditsib.transport.request.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.handlers.RequestExceptionHandler;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportRequestSplitCheckErrDTO;
import ru.sberbank.ditsib.transport.request.exceptions.*;
import ru.sberbank.ditsib.transport.request.exceptions.personal.PersonalTransportRequestSplitException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Component
@ControllerAdvice
@RequiredArgsConstructor
public class RequestExceptionHandlerImpl extends RequestExceptionHandler {

    @Value("${file.max-size}")
    private int maxFileSize;

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {

        var body = ExceptionBody.builder()
                .message(String.format("Maximum upload file size (%d bytes) exceeded", maxFileSize))
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    @ExceptionHandler(CarsharingException.class)
    ResponseEntity<ExceptionBody> handleCarsharingException(CarsharingException e, WebRequest request) {

        var body = ExceptionBody.builder()
                .message(e.getMessage())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(
            BusinessException exception,
            WebRequest request
    ) {
        log.info(exception.getMessage());
        if (log.isDebugEnabled()) {
            log.debug(exception.getMessage(), exception);
        }

        var responseStatus = Optional.ofNullable(exception.getClass().getAnnotation(ResponseStatus.class))
                .orElseThrow()
                .value();

        return handleExceptionInternal(
                exception,
                exception.getMessage(),
                new HttpHeaders(),
                responseStatus,
                request
        );
    }

    @ExceptionHandler({PersonalTransportRequestSplitException.class})
    public ResponseEntity<Object> handlePersonalTransportRequestSplitException(PersonalTransportRequestSplitException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        var body = new PersonalTransportRequestSplitCheckErrDTO(ex.getId(), ex.getHumanReadableId(), ex.getStatus());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DocumentsValidationFailedException.class)
    ResponseEntity<Object> handleDocumentsValidationFailed(
            DocumentsValidationFailedException ex,
            WebRequest request
    ) {
        log.warn("Document validation failed: {}", ex.getMessage());

        ExceptionBody.ExceptionBodyBuilder bodyBuilder = ExceptionBody.builder()
                .message("Ошибка валидации документов")
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(bodyBuilder.build());
    }

    @ExceptionHandler(DocumentsValidationTimeoutException.class)
    ResponseEntity<Object> handleDocumentsValidationTimeout(
            DocumentsValidationTimeoutException ex,
            WebRequest request
    ) {
        log.error("Document validation timeout: {}", ex.getMessage());

        return handleExceptionInternal(
                ex,
                "Сервис валидации документов временно недоступен",
                new HttpHeaders(),
                HttpStatus.REQUEST_TIMEOUT,
                request
        );
    }
}
