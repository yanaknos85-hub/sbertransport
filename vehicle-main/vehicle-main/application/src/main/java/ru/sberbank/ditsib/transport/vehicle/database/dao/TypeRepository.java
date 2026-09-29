package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;

import java.util.Optional;
import java.util.UUID;

public interface TypeRepository extends JpaRepository<Type, UUID> {
    Optional<Type> findByTitle(String title);
    Page<Type> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
}