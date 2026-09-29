package ru.sberbank.ditsib.transport.vehicle.exception;

public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}