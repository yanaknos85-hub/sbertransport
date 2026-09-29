package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.contractor.Contractor;

import java.util.Optional;
import java.util.UUID;

public interface ContractorService {
    
    /**
     * Получение контрагента.
     *
     * @param id идентификатор контрагента.
     * @return контрагент.
     */
    Optional<Contractor> get(UUID id);
    
    /**
     * Сохранение контрагента.
     *
     * @param contractor контрагент.
     * @return сохраненный контрагента.
     */
    Contractor save(Contractor contractor);

    /**
     * Удаление контрагента
     * @param id id контрагента
     */
    void deleteById(UUID id);
}
