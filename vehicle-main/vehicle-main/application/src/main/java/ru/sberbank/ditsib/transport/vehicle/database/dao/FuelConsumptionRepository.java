package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelConsumption;

import java.util.UUID;

public interface FuelConsumptionRepository extends JpaRepository<FuelConsumption, UUID> {

}
