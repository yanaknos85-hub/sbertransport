package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.telemechanic.database.model.RequestHistory;

import java.util.List;
import java.util.UUID;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, UUID> {
    
    List<RequestHistory> findByRequestIdOrderByChangeTime(UUID requestId);
}
