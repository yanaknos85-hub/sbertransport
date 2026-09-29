package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.OrganizationAddress;
import ru.sber.transport.telemechanic.database.model.OrganizationAddress_;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationAddressRepository extends JpaRepository<OrganizationAddress, UUID> {
    
    @EntityGraph(attributePaths = { OrganizationAddress_.REGION })
    Optional<OrganizationAddress> findByOrganizationId(UUID organizationId);
}
