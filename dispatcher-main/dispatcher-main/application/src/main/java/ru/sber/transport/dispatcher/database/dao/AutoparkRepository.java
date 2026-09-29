package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.sber.transport.dispatcher.database.model.Autopark;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for working with autoparks.
 */
public interface AutoparkRepository extends JpaRepository<Autopark, UUID>, JpaSpecificationExecutor<Autopark> {
    
    /**
     * Check autopark existence by id.
     *
     * @param contractorId name of contractorId.
     *
     * @return <code>true</code> if autopark with the id is exists.
     */
    boolean existsByIdAndContractorIdAndActiveIsTrue(UUID id, UUID contractorId);

    /**
     * Получение автопарков по контрагенту.
     *
     * @param contractorId идентификатор контрагента.
     * @return автопарки
     */
    List<Autopark> findAllByContractorIdAndActiveIsTrue(UUID contractorId);

    List<Autopark> findAllByContractorIdAndNameContainingAndActiveIsTrue(UUID contractorId, String name);
    
    Optional<Autopark> findByContractorIdAndNameAndActiveIsTrue(UUID contractorId, String name);

    Optional<Autopark> findByContractorIdAndIdAndActiveIsTrue(UUID contractorId, UUID autoparkId);

    Optional<Autopark> findByIdAndActiveTrue(UUID id);

    Optional<Autopark> findByName(String name);
}
