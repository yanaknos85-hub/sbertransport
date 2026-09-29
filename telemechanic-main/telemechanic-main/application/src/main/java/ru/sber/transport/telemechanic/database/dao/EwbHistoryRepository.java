package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.database.model.EwbHistory;

import java.util.UUID;

public interface EwbHistoryRepository extends JpaRepository<EwbHistory, UUID> {
}
