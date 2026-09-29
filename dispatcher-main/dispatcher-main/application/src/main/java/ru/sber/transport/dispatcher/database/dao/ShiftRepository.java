package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.dispatcher.dto.ShiftListResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID>, JpaSpecificationExecutor<Shift> {

    @Query("select shift from Shift shift " +
            "where shift.driver.id = :driverId " +
            "and shift.deleted = :deleted " +
            "and ((shift.startDate >= :start and shift.endDate <= :end) or " +
            "(shift.startDate <= :start and shift.endDate >= :end) or" +
            "(shift.startDate between :start and :end) or " +
            "(shift.endDate between :start and :end))")
    List<Shift> findAllByDriverIdAndDateBetweenAndDeleted(UUID driverId, LocalDateTime start, LocalDateTime end, boolean deleted);

    @Query("select shift from Shift shift " +
            "where shift.vehicle.id = :vehicleId " +
            "and shift.deleted = :deleted " +
            "and ((shift.startDate >= :start and shift.endDate <= :end) or " +
            "(shift.startDate <= :start and shift.endDate >= :end) or" +
            "(shift.startDate between :start and :end) or " +
            "(shift.endDate between :start and :end))")
    List<Shift> findAllByVehicleIdAndDateBetweenAndDeleted(UUID vehicleId, LocalDateTime start, LocalDateTime end, boolean deleted);

    List<Shift> findAllByDriverAndStartDateBeforeAndEndDateAfterAndDeleted(Driver driverId, LocalDateTime dateTime, LocalDateTime dateTimeRepeat, boolean deleted);

    @EntityGraph(attributePaths = {"vehicle"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("select shift from Shift shift " +
            "where shift.vehicle.id in (:vehicleIds) " +
            "and shift.deleted = false " +
            "and ((shift.startDate >= :start and shift.endDate <= :end) or " +
                  "(shift.startDate <= :start and shift.endDate >= :end) or" +
                  "(shift.startDate between :start and :end) or " +
                  "(shift.endDate between :start and :end))")
    List<Shift> findAllByVehicleIdsAndDate(List<UUID> vehicleIds, LocalDateTime start, LocalDateTime end);

    List<Shift> findAllByContractorIdAndDeletedFalse(UUID contractorId);

    @Query("select shift from Shift shift " +
            "where shift.contractorId = :contractorId " +
            "and shift.deleted = false " +
            "and shift.vehicle.id in (:vehicleIds) " +
            "and shift.startDate <= :date " +
            "and shift.endDate >= :date")
    List<Shift> findAllByContractorIdAndVehicleIdsAndDateAndDeletedFalse(UUID contractorId, List<UUID> vehicleIds, LocalDateTime date);

    List<Shift> findAllByVehicleAndDeletedFalse(Vehicle vehicle);

    List<Shift> findAllByRowIdAndStartDateAfter(UUID rowId, LocalDateTime dateTime);

    /**
     * Поиск смены по идентификатору маршрута во внешней системе
     * @param routeId идентификатор маршрута во внешней системе
     * @return смена
     */
    Optional<Shift> findByRouteId(String routeId);

    /**
     * Получение списка смен по фильтрам
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param startDateFrom дата начала периода
     * @param startDateTo дата окончания периода
     * @return список смен
     */
    @Query(value = """
        select 
            s.id as id,
             concat(d.last_name, ' ', d.first_name, coalesce(' ' || d.patronymic, '')) as driverName,
            v.model_brand as vehicleBrand,
            v.model_name as vehicleModel,
            v.state_number as vehicleStateNumber,
            s.start_date as startDate,
            s.end_date as endDate
        from dispatcher.shift s
        join dispatcher.driver d on s.driver_id = d.id
        join dispatcher.vehicle v on s.vehicle_id = v.id
        where s.is_deleted = false
        and s.contractor_id = :contractorId
        and v.autopark_id = :autoparkId
        and s.start_date between :startDateFrom and :startDateTo
        and s.ewb_id is null
        order by s.start_date
        """, nativeQuery = true)
    List<ShiftListResponseDto> findAllByContractorIdAndAutoparkIdAndStartDate(UUID contractorId,
                                                                              UUID autoparkId,
                                                                              LocalDateTime startDateFrom,
                                                                              LocalDateTime startDateTo);

    /**
     * Получение списка смен по фильтрам
     * @param contractorId ID контрагента
     * @param startDateFrom дата начала периода
     * @param startDateTo дата окончания периода
     * @return список смен
     */
    @Query(value = """
        select 
            s.id as id,
             concat(d.last_name, ' ', d.first_name, coalesce(' ' || d.patronymic, '')) as driverName,
            v.model_brand as vehicleBrand,
            v.model_name as vehicleModel,
            v.state_number as vehicleStateNumber,
            s.start_date as startDate,
            s.end_date as endDate
        from dispatcher.shift s
        join dispatcher.driver d on s.driver_id = d.id
        join dispatcher.vehicle v on s.vehicle_id = v.id
        where s.is_deleted = false
        and s.contractor_id = :contractorId
        and s.start_date between :startDateFrom and :startDateTo
        and s.ewb_id is null
        order by s.start_date
        """, nativeQuery = true)
    List<ShiftListResponseDto> findAllByContractorIdAndStartDate(UUID contractorId,
                                                                 LocalDateTime startDateFrom,
                                                                 LocalDateTime startDateTo);

    /**
     * Получение списка смен по фильтрам
     * @param startDateFrom дата начала периода
     * @param startDateTo дата окончания периода
     * @return список смен
     */
    @Query(value = """
        select 
            s.id as id,
             concat(d.last_name, ' ', d.first_name, coalesce(' ' || d.patronymic, '')) as driverName,
            v.model_brand as vehicleBrand,
            v.model_name as vehicleModel,
            v.state_number as vehicleStateNumber,
            s.start_date as startDate,
            s.end_date as endDate
        from dispatcher.shift s
        join dispatcher.driver d on s.driver_id = d.id
        join dispatcher.vehicle v on s.vehicle_id = v.id
        where s.is_deleted = false
        and s.start_date between :startDateFrom and :startDateTo
        and s.ewb_id is null
        order by s.start_date
        """, nativeQuery = true)
    List<ShiftListResponseDto> findAllByStartDate(LocalDateTime startDateFrom, LocalDateTime startDateTo);

    /**
     * Проверяет наличие пересечения смены водителя на заданный период
     * @param driverId идентификатор водителя
     * @param startDate начало
     * @param endDate конец
     * @return true если есть пересечение смен
     */
    @Query(value = """
        select exists (
        select 1
        from dispatcher.shift s
        where s.driver_id = :driverId
        and s.is_deleted = false
        and (
            (s.start_date >= :startDate and s.end_date <= :endDate) or
            (s.start_date <= :startDate and s.end_date >= :endDate) or
            (s.start_date between :startDate and :endDate) or
            (s.end_date between :startDate and :endDate)
        ))
        """, nativeQuery = true)
    boolean existsDriverShiftConflict(
            UUID driverId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Проверяет наличие пересечения смены водителя на заданный период
     * @param driverId идентификатор водителя
     * @param startDate начало
     * @param endDate конец
     * @return true если есть пересечение смен
     */
    @Query(value = """
        select exists (
        select 1
        from dispatcher.shift s
        where s.driver_id = :driverId
        and s.is_deleted = false
        and (
            (s.start_date >= :startDate and s.end_date <= :endDate) or
            (s.start_date <= :startDate and s.end_date >= :endDate) or
            (s.start_date between :startDate and :endDate) or
            (s.end_date between :startDate and :endDate)
        )
        and s.route_id != :routeId
        )
        """, nativeQuery = true)
    boolean existsDriverShiftConflictForUpdate(
            UUID driverId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String routeId
    );

    @Query(value = """
        select exists (
        select 1
        from dispatcher.shift s
        where s.vehicle_id = :vehicleId
        and s.is_deleted = false
        and (
            (s.start_date >= :startDate and s.end_date <= :endDate) or
            (s.start_date <= :startDate and s.end_date >= :endDate) or
            (s.start_date between :startDate and :endDate) or
            (s.end_date between :startDate and :endDate)
        ))
        """, nativeQuery = true)
    boolean existsVehicleShiftConflict(
            UUID vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query(value = """
        select exists (
        select 1
        from dispatcher.shift s
        where s.vehicle_id = :vehicleId
        and s.is_deleted = false
        and (
            (s.start_date >= :startDate and s.end_date <= :endDate) or
            (s.start_date <= :startDate and s.end_date >= :endDate) or
            (s.start_date between :startDate and :endDate) or
            (s.end_date between :startDate and :endDate)
        )
        and s.route_id != :routeId
        )
        """, nativeQuery = true)
    boolean existsVehicleShiftConflictForUpdate(
            UUID vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String routeId
    );

    @Query(value = """
        select
        s.id as id,
        s.start_date as startDate,
        s.end_date as finishDate,
        a.routing_id as tariffDepartmentId,
        v.transport_id as transportId,
        d.oauth_id as driverId
        from dispatcher.shift s
        join dispatcher.driver d on s.driver_id = d.id
        join dispatcher.vehicle v on s.vehicle_id = v.id
        join dispatcher.autopark a on v.autopark_id = a.id
        where s.id in (:ids)
        and s.is_deleted = false
        and v.autopark_id is not null
        """, nativeQuery = true)
    List<ShiftForEwbDto> findAllById(List<UUID> ids);
}
