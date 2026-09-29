package ru.sber.transport.contractor.database.dao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.sber.transport.contractor.database.model.Contractor;

import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.ServiceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with contractors.
 */
@Repository
public interface ContractorRepository extends JpaRepository<Contractor, UUID>, JpaSpecificationExecutor<Contractor> {
    
    /**
     * Check if contractor exists.
     *
     * @param name name to check.
     * @param tin taxpayer identification number to check.
     * @return <code>true</code> if there is contractor with at least one hit in values.
     */
    @Query("SELECT contractor.id from Contractor contractor INNER join contractor.organizations organization WHERE contractor.name = :name and contractor" +
           ".tin = :tin and organization = :organizationId and contractor.active = true")
    Optional<UUID> checkContractorExistenceByNameAndTin(String name, String tin, UUID organizationId);

    /**
     * Check if contractor exists.
     *
     * @param name name to check.
     * @param tin taxpayer identification number to check.
     * @param excludeContractor contractor to exclude from results.
     * @return <code>true</code> if there is contractor with at least one hit in values.
     */
    @Query("SELECT contractor.id from Contractor contractor INNER join contractor.organizations organization WHERE (contractor.name = :name and contractor" +
           ".tin = :tin) and organization = :organizationId and contractor <> :excludeContractor and contractor.active = true")
    Optional<UUID> checkContractorExistenceByNameAndTinButContractor(String name, String tin, UUID organizationId,
                                                              Contractor excludeContractor);
    
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "truncate table contractors.contractor CASCADE")
    void clearAll();

    /**
     * Получение контрагента с признаком активности.
     *
     * @param id идентификатор.
     * @param active признак активности.
     * @return контрагент.
     */
    Optional<Contractor> findByIdAndActive(UUID id, boolean active);

    /**
     * Поиск контрагента по имени, если будет найдено несколько контрагентов - выбросится исключение
     *
     * @param name имя контрагента.
     * @param tin ИНН.
     *
     * @return единственный контрагент с таким именем
     */
    Optional<Contractor> findByNameAndTinAndActiveIsTrue(String name, String tin);

    @EntityGraph(attributePaths = {"organizations"}, type = EntityGraph.EntityGraphType.FETCH)
    List<Contractor> findAll(@Nullable Specification<Contractor> spec, Sort sort);

    /**
     * Get Contractor By ServiceType And Organization.
     *
     * @param serviceType service type.
     * @param organizationId organization to check.
     * @return contractor id.
     */
    @Query("SELECT contractor from Contractor contractor INNER join contractor.organizations organization WHERE " +
            "contractor.serviceType = :serviceType and organization = :organizationId and contractor.active = true")
    Optional<Contractor> getContractorByServiceTypeAndOrganization(ServiceType serviceType, UUID organizationId);

    /**
     * Get Contractor By ServiceType And Organization.
     *
     * @param serviceType service type.
     * @param organizationId organization to check.
     * @param excludeContractor contractor to exclude from results.
     * @return contractor id.
     */
    @Query("SELECT contractor from Contractor contractor INNER join contractor.organizations organization WHERE " +
            "contractor.serviceType = :serviceType and organization = :organizationId and contractor <> :excludeContractor and contractor.active = true")
    Optional<Contractor> getContractorByServiceTypeAndOrganization(ServiceType serviceType, UUID organizationId, Contractor excludeContractor);

    /**
     * Поиск активного контрагента по телефону.
     *
     * @param phone телефон.
     * @return контрагент.
     */
    Optional<Contractor> findFirstByContactPersonPhoneAndActive(String phone, boolean active);

    /**
     * Поиск активного контрагента по email.
     *
     * @param email email.
     * @return контрагент.
     */
    Optional<Contractor> findFirstByContactPersonEmailAndActive(String email, boolean active);
}
