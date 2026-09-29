package ru.sber.transport.business.providers;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import ru.sber.transport.request.external.model.Delegate;
import ru.sber.transport.request.external.model.Employee;

/**
 * Бизнес-логика делегирования
 */
public interface DelegatesProvider {

    /**
     * Сохранить делегата
     *
     * @param source источник данных
     */
    void save(@NonNull Delegate source);

    /**
     * Получить делегатов
     *
     * @param headId идентификатор руководителя
     * @return список делегатов
     */
    List<Employee> get(UUID headId);

    /**
     * Возвращает список всех делегатов
     *
     * @return список делегатов
     */
    List<Delegate> getAll();
}
