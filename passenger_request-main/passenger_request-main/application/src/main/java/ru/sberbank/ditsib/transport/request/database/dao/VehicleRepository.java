package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Vehicle;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findFirstByBrandAndModelAndStateNumberAndColorAndActive(String brand, String model, String stateNumber, String color,
                                                                          boolean active);
}
