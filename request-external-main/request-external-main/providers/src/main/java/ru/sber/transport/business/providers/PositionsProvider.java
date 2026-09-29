package ru.sber.transport.business.providers;

import ru.sber.transport.request.external.model.Position;

/**
 * Бизнес-логика системы должностей
 */
public interface PositionsProvider {

    /**
     * Сохранить данные о должности
     *
     * @param source источник данных о должности
     */
    void save(Position source);
}
