package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelTypeRepository extends JpaRepository<FuelType, UUID> {

    @Query("SELECT ft FROM FuelType ft LEFT JOIN FETCH ft.fuelTypeNames LEFT JOIN FETCH ft.engineType")
    Page<FuelType> findAllWithNamesAndEngineTypes(Pageable pageable);

    @Query("SELECT ft FROM FuelType ft LEFT JOIN FETCH ft.fuelTypeNames LEFT JOIN FETCH ft.engineType")
    List<FuelType> findAllWithNamesAndEngineTypes(Sort sort);

    Optional<FuelType> findByTitleAndEngineType(String title, EngineType engineType);
}