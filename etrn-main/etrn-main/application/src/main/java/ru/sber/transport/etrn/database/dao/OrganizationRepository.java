package ru.sber.transport.etrn.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.etrn.database.model.Organization;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    /**
     * Получить список организаций по флагу активности
     *
     * @param isActive флаг активности
     * @return список организаций
     */
    List<Organization> findAllByActive(boolean isActive);
}