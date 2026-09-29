package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;

import java.util.*;

/**
 * Репозиторий для работы с контрагентами.
 */
public interface ContractorRepository extends JpaRepository<Contractor, UUID> {
    
    /**
     * Получение контрагентов по имени.
     *
     * @param contractor имя контрагента.
     * @return контрагент.
     */
    Optional<Contractor> findByName(String contractor);
}
