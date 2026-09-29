package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.Organization;

import java.util.UUID;

/**
 * Repository of organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
