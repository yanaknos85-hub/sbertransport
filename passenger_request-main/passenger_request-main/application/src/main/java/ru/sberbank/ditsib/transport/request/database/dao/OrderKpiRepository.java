package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.magenta.OrderKpi;

import java.util.UUID;

/**
 * Репозиторий данных заказов KPI.
 */
@Repository
public interface OrderKpiRepository extends JpaRepository<OrderKpi, UUID> {
}
