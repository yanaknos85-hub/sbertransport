package ru.sber.transport.authsb.services;

import ru.sber.transport.authsb.database.model.Organization;
import java.util.Optional;

/**
 * Сервис для управления организациями.
 */
public interface OrganizationService {

    /**
     * Найти организацию по ОГРН и КПП
     */
    Optional<Organization> findByOgrnAndKpp(String ogrn, String kpp);

    /**
     * Сохранить новую или обновить существующую организацию.
     */
    Organization save(Organization organization);

}