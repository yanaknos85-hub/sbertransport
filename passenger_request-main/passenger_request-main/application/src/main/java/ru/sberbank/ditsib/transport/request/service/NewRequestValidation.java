package ru.sberbank.ditsib.transport.request.service;

import lombok.NonNull;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;

/**
 * Валидатор новой заявки
 */
public interface NewRequestValidation {

    /**
     * Метод валидации новой заявки
     *
     * @param request заявка
     * @param employee сотрудник
     */
    void validate(@NonNull NewRequestDTO request, @NonNull Employee employee);
}
