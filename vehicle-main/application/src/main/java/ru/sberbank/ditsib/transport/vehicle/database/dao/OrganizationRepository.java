package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationNameWithDepartmentInfo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    @Query("SELECT o FROM Organization o WHERE o.active = true ORDER BY o.officialName")
    List<Organization> findAllByActiveIsTrueOrderByOfficialName();
    
    @Query(value =
                   """
                   select
                        o.id                as organizationId,
                        o.official_name     as officialName,
                        d.id                as departmentId,
                        d.department_name   as departmentName,
                        d.parent_id         as parentId
                   from vehicle.department d
                   join vehicle.organization o on d.organization_id = o.id
                   where d.organization_id in (?1)
                   and d.active is true;
                   """,
           nativeQuery = true)
    List<OrganizationNameWithDepartmentInfo> findByIdsWithActiveDepartments(Set<UUID> ids);
}
