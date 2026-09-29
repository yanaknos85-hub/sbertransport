package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.OrganizationContact;
import ru.sber.transport.telemechanic.database.model.OrganizationContactKey;

import java.util.UUID;

/**
 * Репозиторий организаций владельцев автопарков
 */
@Repository
public interface OrganizationContactRepository extends JpaRepository<OrganizationContact, OrganizationContactKey> {
    void deleteAllByOrganizationContactKeyOrganizationId(UUID organizationId);
}
