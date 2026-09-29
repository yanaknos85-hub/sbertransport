package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.tariff.database.model.ConnectionRestriction;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий ограничений связей между исполнителем и заказчиком
 */
@Repository
public interface ConnectionRestrictionRepository extends CrudRepository<ConnectionRestriction, UUID> {
    
    /**
     * Поиск ограничения
     *
     * @param organizationId идентификатор организации
     *
     * @return ограничение связи
     */
    List<ConnectionRestriction> findByOrganizationId(UUID organizationId);
    
    /**
     * Поиск ограничения
     *
     * @param organizationIds идентификаторы организации
     *
     * @return ограничение связи
     */
    List<ConnectionRestriction> findByOrganizationIdIn(List<UUID> organizationIds);
    
    /**
     * Поиск ограничения
     *
     * @param contractorId идентификатор контрагента
     *
     * @return ограничение связи
     */
    List<ConnectionRestriction> findByAndContractorId(UUID contractorId);
    
    /**
     * Удаление ограничения
     *
     * @param organizationIds список идентификаторов организаций
     * @param contractorId идентификатор контрагента
     */
    @Transactional
    @Modifying
    @Query("delete from ConnectionRestriction cr where cr.organization.id in :organizationIds and cr.contractor.id = :contractorId")
    void deleteByOrganizationIdsAndContractorId(@Param("organizationIds") List<UUID> organizationIds, @Param("contractorId") UUID contractorId);

    /**
     * Удаление ограничения
     *
     * @param contractorIds идентификатор контрагента
     * @param organizationId список идентификаторов организаций
     */
    @Transactional
    @Modifying
    @Query("delete from ConnectionRestriction cr where cr.contractor.id in :contractorIds and cr.organization.id = :organizationId")
    void deleteByContractorIdsAndOrganizationId(@Param("contractorIds") List<UUID> contractorIds, @Param("organizationId") UUID organizationId);
    
}
