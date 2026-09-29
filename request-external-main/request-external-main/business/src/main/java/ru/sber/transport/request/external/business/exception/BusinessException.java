package ru.sber.transport.request.external.business.exception;

/**
 * Ошибка бизнес слоя
 */
public class BusinessException extends RuntimeException{

    public BusinessException(String message) {
        super(message);
    }
}
