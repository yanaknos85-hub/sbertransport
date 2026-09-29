package ru.sber.transport.notifications.database.dao.messages.trip_request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.request.SharedRide;

import java.util.UUID;

public interface TripSharedRideRepository extends JpaRepository<SharedRide, UUID> {
}
