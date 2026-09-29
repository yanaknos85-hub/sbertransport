package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.contractor.Dispatcher;

import java.util.Optional;
import java.util.UUID;

public interface DispatcherService {
    
    /**
     * Получение диспетчера.
     *
     * @param id идентификатор диспетчера.
     * @return диспетчер.
     */
    Optional<Dispatcher> get(UUID id);
    
    /**
     * Сохранение диспетчера.
     *
     * @param dispatcher водитель.
     * @return сохраненный диспетчер.
     */
    Dispatcher save(Dispatcher dispatcher);

    /**
     * Удаление диспетчера
     * @param dispatcherId id диспетчера
     */
    void deleteById(UUID dispatcherId);
}
