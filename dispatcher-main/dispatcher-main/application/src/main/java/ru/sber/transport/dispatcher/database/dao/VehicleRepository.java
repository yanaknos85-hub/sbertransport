package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Repository for working with transport.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    /**
     * Check vehicle existence by passport or stateNumber or vin
     *
     * @param stateNumber state number.
     * @param vin         vin.
     * @param passport    passport.
     * @return list of vehicles.
     */
    @Query("SELECT vehicle FROM Vehicle vehicle " +
            "WHERE (vehicle.stateNumber = :stateNumber " +
            "OR vehicle.vin = :vin " +
            "OR vehicle.passport = :passport)" +
            "AND vehicle.inExploitation = true")
    Optional<Vehicle> findByStateNumberOrVinOrPassport(String stateNumber, String vin, String passport);

    /**
     * Check vehicle existence by passport or stateNumber or vin
     *
     * @param stateNumber state number.
     * @param vin         vin.
     * @param passport    passport.
     * @return list of vehicles.
     */
    @Query("SELECT vehicle FROM Vehicle vehicle " +
            "WHERE (vehicle.stateNumber = :stateNumber " +
            "OR vehicle.vin = :vin " +
            "OR vehicle.passport = :passport)" +
            "AND vehicle.inExploitation = true " +
            "AND vehicle.id <> :id")
    Optional<Vehicle> findByStateNumberOrVinOrPassportExclude(String stateNumber, String vin, String passport, UUID id);

    Optional<Vehicle> findByTransportId(UUID transportId);

    Optional<Vehicle> findByAutoparkContractorIdAndAutoparkIdAndIdAndActiveTrue(UUID contractorId, UUID autoparkId, UUID vehicleId);

    List<Vehicle> findAllByAutoparkId(UUID autoparkId);

    List<Vehicle> findAllByAutoparkIdAndActiveTrueAndInExploitationTrue(UUID autoparkId);

    /**
     * Поиск свободных автомобилей в заданном интервале времени
     * @param start время начала интервала поиска
     * @param end время конца интервала поиска
     * @param contractorId id контрагента
     * @param type  тип автомобиля
     * @param search строка поиска
     * @return список свободных автомобилей
     */
    @Query("select vehicle from Vehicle vehicle " +
        "join Autopark autopark on vehicle.autopark.id = autopark.id " +
        "join Contractor contractor on autopark.contractor.id = contractor.id " +
        "where vehicle.id not in " +
        "(select trip.vehicleId from Trip trip " +
        "where trip.startTime <= :end and trip.endTime >= :start and trip.vehicleId is not null) " +
        "and vehicle.vehicleType = :type " +
        "and contractor.id = :contractorId " +
        "and (vehicle.model.name ilike %:search% or vehicle.model.brand ilike %:search% or vehicle.stateNumber ilike %:search%)")
    List<Vehicle>findAllFreeTransport(OffsetDateTime start, OffsetDateTime end, UUID contractorId, VehicleType type, String search);

    /**
     * Поиск авто по гос номеру
     * @param stateNumber государственный регистрационый знак
     * @return автомобиль
     */
    Optional<Vehicle> findByStateNumberIgnoreCaseAndInExploitationTrue(String stateNumber);

}
