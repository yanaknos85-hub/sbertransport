package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий медицинских лицензий организаций
 */
@Repository
public interface OrganizationMedicalLicenseRepository extends JpaRepository<OrganizationMedicalLicense, UUID> {
    
    @Query(value = """
                    select exists(
                                   select oml.*
                                   from telemechanic.organization_medical_license oml
                                   where oml.active is true
                                     and current_date between oml.issue_date and oml.expiry_date
                                     and oml.id = :id)
                   """,
           nativeQuery = true)
    boolean existsAndActive(UUID id);
    
    Optional<OrganizationMedicalLicense> findByIdAndActiveIsTrue(UUID id);
}
