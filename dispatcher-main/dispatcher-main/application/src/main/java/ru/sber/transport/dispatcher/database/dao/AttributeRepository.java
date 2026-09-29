package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.dispatcher.database.model.Attribute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий Признаков водителя
 */
public interface AttributeRepository extends JpaRepository<Attribute, UUID>, JpaSpecificationExecutor<Attribute> {
    
    /**
     * Поиск признака водителя по имени и контрагенту и с любым статусом активности
     * @param name наименование признака водителя
     * @param contractorId id контрагента
     * @return признак водителя
     */
    @Query("FROM Attribute attribute INNER JOIN attribute.contractor contractor " +
           "WHERE contractor.id = :contractorId AND attribute.name = :name ")
    Optional<Attribute> findByNameAndContractorIdAndAnyActive(String name, UUID contractorId);

    /**
     * Поиск признака водителя по имени и контрагенту
     * @param name наименование признака водителя
     * @param contractorId id контрагента
     * @return признак водителя
     */
    @Query("FROM Attribute attribute INNER JOIN attribute.contractor contractor " +
            "WHERE contractor.id = :contractorId AND attribute.name = :name AND attribute.status = 'ACTIVE' ")
    Optional<Attribute> findByNameAndContractorId(String name, UUID contractorId);

    /**
     * Поиск признака водителя по имени и контрагенту
     * @param name наименование признака водителя
     * @param contractorId id контрагента
     * @return признак водителя
     */
    @Query("FROM Attribute attribute INNER JOIN attribute.contractor contractor " +
            "WHERE contractor.id = :contractorId AND attribute.name = :name AND attribute.status = 'ACTIVE' AND attribute.id != :excludeAttributeId ")
    Optional<Attribute> findByNameAndContractorIdExclude(String name, UUID contractorId, UUID excludeAttributeId);

    /**
     * Поиск признака водителя по id и контрагенту
     * @param id id признака водителя
     * @param contractorId id контрагента
     * @return признак водителя
     */
    @Query("FROM Attribute attribute INNER JOIN attribute.contractor contractor " +
            "WHERE contractor.id = :contractorId AND attribute.id = :id AND attribute.status = 'ACTIVE' ")
    Optional<Attribute> findByIdAndContractorId(UUID id, UUID contractorId);

    /**
     * Получение всех признаков водителей  контрагента
     * @param contractorId id контрагента
     * @return список признаков
     */
    @Query("FROM Attribute attribute INNER JOIN attribute.contractor contractor " +
            "WHERE contractor.id = :contractorId AND attribute.status = 'ACTIVE' ")
    List<Attribute> findAllByContractorId(UUID contractorId);
}
