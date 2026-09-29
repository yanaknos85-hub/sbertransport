package ru.sber.transport.dispatcher.service;

import lombok.NonNull;
import ru.sber.transport.dispatcher.database.model.Contractor;

/**
 * Сервис для подсчета сотрудников контрагента.
 */
public interface ContractorCounter {

    /**
     * Изменить количество.
     *
     * @param source источник данных.
     * @param count инкремент.
     * @return сохраненный объект.
     */
    Contractor changeCount(Contractor source, int count);

    /**
     * Проверка возможности добавления сотрудников.
     *
     * @param target контрагент для проверки.
     * @return заключение о возможности добавления.
     */
    boolean canAddStaff(@NonNull Contractor target);
}
