package ru.sber.transport.dispatcher.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.dispatcher.exceptions.DataConflictException;
import ru.sber.transport.dispatcher.exceptions.FieldsException;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.handlers.RequestExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@RestControllerAdvice
class ContractorRequestExceptionHandler extends RequestExceptionHandler {

    @ExceptionHandler(FieldsException.class)
    ResponseEntity<ExceptionBody> handlerFieldsErrors(FieldsException fieldsException, WebRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        var bodyBuilder = ExceptionBody.builder().message(status.getReasonPhrase())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI());

        for (var violation : fieldsException.getExceptions()) {
            var constraint = Constraint.builder().type("TypeMismatch").value(violation.getRequiredType().getSimpleName()).build();

            var problem = Problem.builder()
                    .field(violation.getField())
                    .value(String.valueOf(violation.getValue()))
                    .constraints(Collections.singletonList(constraint)).build();

            bodyBuilder.problem(problem);
        }

        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    @ExceptionHandler(DataConflictException.class)
    ResponseEntity<ExceptionBody> handlerDataConflictError(DataConflictException conflictException, WebRequest request) {
        var status = HttpStatus.CONFLICT;
        var bodyBuilder = ExceptionBody.builder().message(status.getReasonPhrase())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI());

        var problem = Problem.builder()
                .field(String.valueOf(conflictException.getConflicted().toString()))
                .value(String.valueOf(conflictException.getConflictedField()))
                .constraints(Collections.singletonList(
                        Constraint.builder().type(conflictException.getType().name()).value(conflictException.getConflictEntitiesIds()).build()))
                .build();
        bodyBuilder.problem(problem);
        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

}
