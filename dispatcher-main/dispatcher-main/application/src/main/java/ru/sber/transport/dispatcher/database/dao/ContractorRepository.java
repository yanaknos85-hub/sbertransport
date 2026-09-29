package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.dispatcher.database.model.Contractor;

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
     * @param tin  tin to check
     * @return <code>true</code> if there is contractor with at least one hit in values.
     */
    Optional<Contractor> findByNameAndTinAndActiveIsTrue(String name, String tin);

    /**
     * Получение контрагента с признаком активности.
     *
     * @param id     идентификатор.
     * @param active признак активности.
     * @return контрагент.
     */
    Optional<Contractor> findByIdAndActive(UUID id, boolean active);

    Optional<Contractor> findFirstByTinAndMsrnAndTechnicalAccountOwnerEmailAndActiveIsTrue(String tin, String msrn, String email);

    /**
     * Получение уникальных TIN активных контрагентов с включённой настройкой запроса штрафов.
     *
     * @return список уникальных ИНН
     */
    @Query(value = "SELECT DISTINCT c.tin FROM dispatcher.contractor c WHERE c.active = true AND c.is_fine_fetch_required = true", nativeQuery = true)
    List<String> findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();
}
