package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize;

import java.util.Optional;
import java.util.UUID;

public interface WheelSizeRepository extends JpaRepository<WheelSize, UUID> {
    Optional<WheelSize> findByTitle(String title);

    Page<WheelSize> findAllByTitleContainingIgnoreCase(String title, PageRequest pageRequest);
}
