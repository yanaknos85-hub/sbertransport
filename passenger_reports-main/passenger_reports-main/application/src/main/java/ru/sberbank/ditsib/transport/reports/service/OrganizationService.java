package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Organization;

import java.util.List;
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
    
    /**
     * Поиск организаций по ID группы организаций
     *
     * @param id ID группы организаций
     *
     * @return список организаций
     */
    List<Organization> findByOrganizationGroupId(UUID id);
    
    /**
     * Поиск всех организаций по параметру частичного совпадения наименования
     *
     * @param searchParameter параметр для поиска организации по частичному совпадению с ее наименованием
     *
     * @return список организаций удовлетворяющий критерию поиска по параметру
     */
    List<Organization> findOrganizationsBySearchParameter(String searchParameter);
    
    /**
     * Поиск всех организаций
     *
     * @return список организаций
     */
    List<Organization> findAll();
    
}
