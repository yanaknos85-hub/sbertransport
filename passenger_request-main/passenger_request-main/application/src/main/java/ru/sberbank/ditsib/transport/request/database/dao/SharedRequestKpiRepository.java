package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.magenta.SharedRideKPI;

import java.util.UUID;

/**
 * Репозиторий данных KPI совместных поездок.
 */
@Repository
public interface SharedRequestKpiRepository extends JpaRepository<SharedRideKPI, UUID> {
}
