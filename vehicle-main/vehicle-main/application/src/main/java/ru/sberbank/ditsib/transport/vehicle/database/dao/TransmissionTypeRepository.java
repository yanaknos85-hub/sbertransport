package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.TransmissionType;

import java.util.Optional;
import java.util.UUID;

public interface TransmissionTypeRepository extends JpaRepository<TransmissionType, UUID> {
    Page<TransmissionType> findAllByTitleContainingIgnoreCase(String title, PageRequest pageRequest);

    Optional<TransmissionType> findByTitle(String title);
}
