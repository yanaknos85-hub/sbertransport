package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sber.transport.dispatcher.database.model.ShiftConflict;

/**
 * Репозиторий для работы с таблицей конфликтов смен (shift_conflict)
 */
@Repository
public interface ShiftConflictRepository extends JpaRepository<ShiftConflict, String>, JpaSpecificationExecutor<ShiftConflict> {}