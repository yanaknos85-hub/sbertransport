package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.database.model.MedicRequestHistory;

import java.util.UUID;

public interface MedicRequestHistoryRepository extends JpaRepository<MedicRequestHistory, UUID> {
}
