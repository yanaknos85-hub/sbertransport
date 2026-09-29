package ru.sber.transport.business.providers;

import ru.sber.transport.request.external.model.Fraud;

/**
 * Провайдер данных о фроде.
 */
public interface FraudsProvider {

    /**
     * Сохраняет данные о фроде
     *
     * @param source данные о фроде
     */
    void save(Fraud source);
}
