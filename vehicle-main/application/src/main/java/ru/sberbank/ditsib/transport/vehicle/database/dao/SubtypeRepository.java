package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype;

import java.util.Optional;
import java.util.UUID;

public interface SubtypeRepository extends JpaRepository<Subtype, UUID> {
    Optional<Subtype> findByTitle(String title);
    Page<Subtype> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
}