package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.Lead;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с заявками.
 * Предоставляет базовые операции CRUD для сущности Lead.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findAllByMainLeadId(UUID id);
}
