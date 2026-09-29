package ru.sber.transport.notifications.database.dao.messages.trip_request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.TripPurpose;

import java.util.UUID;

/**
 * Репозиторий для работы с целями поездок.
 */
public interface TripPurposeRepository extends JpaRepository<TripPurpose, UUID> {
}
