package ru.sber.transport.contractor.exceptions.business;

public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}
