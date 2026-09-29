package ru.sber.transport.business.providers;

import java.util.UUID;

/**
 * Провайдер данных об оценках.
 */
public interface AssessmnentProvider {

    /**
     * Удаляет оценку поездки
     *
     * @param requestId             идентификатор заявки на поездку
     */
    void deleteForRequest(UUID requestId);

}
