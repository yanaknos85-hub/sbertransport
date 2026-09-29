package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Employee_;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Request_;
import ru.sberbank.ditsib.transport.reports.model.Waypoint_;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI_;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide_;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff_;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract_;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {
    
    @Override
    @EntityGraph(attributePaths = { Request_.PASSENGER,
                                    Request_.AUTHOR, Request_.WAYPOINTS, Request_.WAYPOINTS + "." + Waypoint_.ADDRESS,
                                    Request_.SINGLE_TAXI_TRIP, Request_.TRANSPORT_COMPENSATION,
                                    Request_.APPROVED_BY, Request_.TARIFF, Request_.TARIFF + "." + BaseTariff_.CONTRACT,
                                    Request_.TARIFF + "." + BaseTariff_.CONTRACT + "." + Contract_.CONTRACTOR, Request_.PURPOSE,
                                    Request_.SHARED_RIDE, Request_.SHARED_RIDE + "." + SharedRide_.KPI,
                                    Request_.SHARED_RIDE + "." + SharedRide_.KPI + "." + SharedRideKPI_.ORDERS_KPI,
                                    Request_.SHARED_RIDE + "." + SharedRide_.COOP_TAXI_TRIP,
                                    Request_.CONTRACTOR, Request_.PERSONAL_CAR, Request_.LIMIT, Request_.AUTOPARK })
    Page<Request> findAll(@Nullable Specification<Request> spec, Pageable pageable);
    
    @EntityGraph(attributePaths = { Request_.PASSENGER, Request_.WAYPOINTS, Request_.SINGLE_TAXI_TRIP })
    List<Request> findByRideId(UUID rideId);
    
    @EntityGraph(attributePaths = { Request_.PASSENGER, Request_.PASSENGER + "." + Employee_.DEPARTMENT, Request_.SINGLE_TAXI_TRIP })
    @Query("SELECT request FROM Request request INNER JOIN FETCH request.passenger pass INNER JOIN FETCH pass.department dep")
    List<Request> findAllWithDepartments(Set<UUID> requestList);
    
    @EntityGraph(attributePaths = { Request_.PASSENGER, Request_.SINGLE_TAXI_TRIP })
    @Query("SELECT request FROM Request request WHERE request.rideId = :rideId")
    List<Request> findBySharedRideId(UUID rideId);
    
    @EntityGraph(attributePaths = { Request_.PASSENGER, Request_.PERSONAL_CAR, Request_.SINGLE_TAXI_TRIP })
    @Query("FROM Request request WHERE request.employeeDriverId = :driver " +
           "AND year(request.orderPaymentFormationStartDate) = :year " +
           "AND month(request.orderPaymentFormationStartDate) = :month ")
    List<Request> findByDriverAndMonth(UUID driver, int year, int month);
    
    List<Request> findAllByIdIn(List<UUID> ids);
}
