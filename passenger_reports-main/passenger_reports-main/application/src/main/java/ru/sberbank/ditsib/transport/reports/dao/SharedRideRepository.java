package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI_;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide_;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий совместных поездок
 */
@Repository
public interface SharedRideRepository extends JpaRepository<SharedRide, UUID>,
        JpaSpecificationExecutor<SharedRide> {


    @EntityGraph(attributePaths = {SharedRide_.KPI, SharedRide_.KPI + "." + SharedRideKPI_.ORDERS_KPI})
    @Query("select s from SharedRide s where s.id = :id")
    Optional<SharedRide> findByIdWithKpi(UUID id);
}