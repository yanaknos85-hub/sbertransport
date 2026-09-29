package ru.sber.transport.notifications.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.request.Vehicle;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
}
