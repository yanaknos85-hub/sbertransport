package ru.sberbank.ditsib.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.database.model.MainLead;
import ru.sberbank.ditsib.enumerate.MainLeadStatus;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с главными заявками.
 * Предоставляет базовые операции CRUD для сущности MainLead.
 */
@Repository
public interface MainLeadRepository extends JpaRepository<MainLead, UUID> {

    List<MainLead> findAllByStatusIn(List<MainLeadStatus> statusList);
}
