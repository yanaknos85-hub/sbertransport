package ru.sber.transport.authentication.business.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Объект бизнес-модели аудита.
 */
@Data
public class AuditDto {
    
    /**
     * Время операции.
     */
    private LocalDateTime timestamp;
    
    /**
     * Результат операции.
     */
    private Result result;
    
    /**
     * Операция.
     */
    private Action action;
    
    /**
     * Инициатор операции.
     */
    private String login;
    
}
