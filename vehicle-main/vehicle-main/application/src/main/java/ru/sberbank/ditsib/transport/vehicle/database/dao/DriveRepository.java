package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive;

import java.util.Optional;
import java.util.UUID;

public interface DriveRepository extends JpaRepository<Drive, UUID> {
    Optional<Drive> findByTitle(String title);
}
