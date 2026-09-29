package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;

import java.util.*;

/**
 * Repository of organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    /**
     * Получить список организаций по флагу активности
     *
     * @param isActive флаг активности
     *
     * @return список организаций
     */
    List<Organization> findAllByActive(boolean isActive);
    
    /**
     * Получить организацию по названию.
     *
     * @param name название.
     * @return организация.
     */
    Optional<Organization> findByName(String name);
}
