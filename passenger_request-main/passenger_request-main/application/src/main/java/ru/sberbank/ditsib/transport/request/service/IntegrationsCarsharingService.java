package ru.sberbank.ditsib.transport.request.service;

import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.UUID;

/**
 * Сервис по работе с integrations-carsharing
 */
public interface IntegrationsCarsharingService {
    
    /**
     * Добавление сотрудника к корп тарифу
     *
     * @param employee сотрудник
     * @param organizationId ид организации
     */
    void joinToTariff(@NonNull Employee employee, @NonNull UUID organizationId);
    
}
