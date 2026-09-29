package ru.sberbank.ditsib.transport.tariff.database.model;

/**
 * Тип договора.
 */
public enum ContractType {
    
    /**
     * Доходный.
     */
    INCOME,
    
    /**
     * Расходный.
     */
    OUTCOME,
    
    /**
     * Транзитный.
     */
    TRANSITIONAL
}
