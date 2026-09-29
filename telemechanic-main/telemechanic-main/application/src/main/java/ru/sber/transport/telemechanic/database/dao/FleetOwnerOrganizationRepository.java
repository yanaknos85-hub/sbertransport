package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий организаций владельцев автопарков
 */
@Repository
public interface FleetOwnerOrganizationRepository extends JpaRepository<FleetOwnerOrganization, UUID> {
    
    Optional<FleetOwnerOrganization> findByOrganizationIdAndActiveTrue(UUID organizationId);
    
    boolean existsByOrganizationIdAndActiveTrue(UUID organizationId);
    
    @Query(value = """
                    select foo.organization_id, o.official_name
                    from telemechanic.fleet_owner_organization foo
                    join telemechanic.organization o on foo.organization_id = o.id
                    where o.active is true
                    order by o.official_name
                   """,
           nativeQuery = true)
    List<GetAllActiveOrganizationNamesDto> findAllActive();
}
