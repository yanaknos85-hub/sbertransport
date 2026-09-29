package ru.sber.transport.authentication.web.exceptions;

import io.grpc.StatusRuntimeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Фабрика для создании исключений для ответов от нотификаций по GRPC
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationsGrpcExceptionFactory {

    /**
     * Создать исключение по статусу
     * @param exception исключение от GRPC
     * @return исключение для ответа
     */
    public static RuntimeException create(StatusRuntimeException exception) {
        return switch (exception.getStatus().getCode()) {
            case FAILED_PRECONDITION -> new NotificationsGrpcPreconditionFailedException(exception.getMessage());
            case NOT_FOUND -> new NotificationsGrpcNotFoundException(exception.getMessage());
            case PERMISSION_DENIED -> new NotificationsGrpcForbiddenException(exception.getMessage());
            default -> exception;
        };
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotificationsGrpcNotFoundException extends RuntimeException {

        public NotificationsGrpcNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public static class NotificationsGrpcPreconditionFailedException extends RuntimeException {

        public NotificationsGrpcPreconditionFailedException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class NotificationsGrpcForbiddenException extends RuntimeException {

        public NotificationsGrpcForbiddenException(String message) {
            super(message);
        }
    }

}