package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    /**
     * Поиск организации по ID
     */
    Optional<Organization> findById(UUID uuid);

    /**
     * Сохранение организации
     */
    Organization save(Organization organization);

    /**
     * Поиск организации по ID или создание и сохранение новой организации(приатаченной)
     */
    Organization findOrCreateById(UUID uuid);

    /**
     * Удаление организации
     */
    void delete(Organization organization);

}
