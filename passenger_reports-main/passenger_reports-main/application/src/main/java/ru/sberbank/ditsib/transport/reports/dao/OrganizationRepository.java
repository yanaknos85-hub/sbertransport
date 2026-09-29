package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Organization;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    /**
     * Поиск организаций по ID группы организаций
     *
     * @param id ID группы организаций
     *
     * @return список организаций
     */
    List<Organization> findAllByOrganizationGroupId(UUID id);
    
    /**
     * Поиск всех организаций по параметру частичного совпадения наименования
     *
     * @param officialNamePart параметр для поиска организации по частичному совпадению с ее наименованием
     *
     * @return список организаций удовлетворяющий критерию поиска по параметру
     */
    List<Organization> findAllByOfficialNameContainingIgnoreCase(String officialNamePart);
}
