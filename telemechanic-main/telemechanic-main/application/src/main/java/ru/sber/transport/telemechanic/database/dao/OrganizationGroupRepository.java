package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.OrganizationGroup;

import java.util.UUID;

/**
 * Репозиторий групп организаций
 */
@Repository
public interface OrganizationGroupRepository extends JpaRepository<OrganizationGroup, UUID> {

}
