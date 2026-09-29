package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.RequestHistory;

import java.util.UUID;

/**
 * Репозиторий для работы с историей запросов
 */
@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, UUID>, JpaSpecificationExecutor<RequestHistory> {
}
