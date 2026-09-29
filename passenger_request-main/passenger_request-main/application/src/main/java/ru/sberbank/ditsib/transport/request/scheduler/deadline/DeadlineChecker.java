package ru.sberbank.ditsib.transport.request.scheduler.deadline;

/**
 * Базовый интерфейс для работы с контрольными сроками
 */
public interface DeadlineChecker {
    
    void execute();
    
    String info();
    
}
