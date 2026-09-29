package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.magenta.OrderKpi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий данных заказов KPI.
 */
@Repository
public interface OrderKpiRepository extends JpaRepository<OrderKpi, UUID> {
    
    Optional<OrderKpi> findByRequestId(UUID id);
}
