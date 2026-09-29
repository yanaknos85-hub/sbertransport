package ru.sber.transport.telemechanic.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskRejectedException;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sber.transport.telemechanic.exception.BusinessException;
import ru.sber.transport.telemechanic.exception.PredictException;

import java.util.HashMap;
import java.util.Optional;

/**
 * Обработка rest исключений
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;


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

    @ExceptionHandler({BusinessException.class, PredictException.class})
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

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException exception,
                                                           WebRequest request) {
        log.info("Превышем максимальный размер для загружаемого файла:{}", maxFileSize);
        log.debug(exception.getMessage(), exception);
        return handleExceptionInternal(exception,
                "Превышем максимальный размер для загружаемого файла:" + maxFileSize,
                new HttpHeaders(),
                HttpStatus.PAYLOAD_TOO_LARGE,
                request);
    }
    
    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<Object> handleTaskRejectedException(TaskRejectedException exception, WebRequest request) {
        log.info("Попробуйте снова через несколько минут");
        log.debug(exception.getMessage(), exception);
        return handleExceptionInternal(exception, "Попробуйте снова через несколько минут", new HttpHeaders(), HttpStatus.TOO_EARLY, request);
    }
}