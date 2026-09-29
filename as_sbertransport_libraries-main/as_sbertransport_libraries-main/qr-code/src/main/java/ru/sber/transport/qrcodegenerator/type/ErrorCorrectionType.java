package ru.sber.transport.qrcodegenerator.type;

/**
 * Тип уровня коррекции ошибок
 */
public enum ErrorCorrectionType {
    /**
     * ~7% correction
     */
    L,
    
    /**
     * ~15% correction
     */
    M,
    
    /**
     * ~25% correction
     */
    Q,
    
    /**
     * ~30% correction
     */
    H
}
