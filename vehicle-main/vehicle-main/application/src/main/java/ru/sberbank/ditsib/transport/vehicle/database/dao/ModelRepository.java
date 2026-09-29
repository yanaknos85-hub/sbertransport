package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model;

import java.util.Optional;
import java.util.UUID;

public interface ModelRepository extends JpaRepository<Model, UUID> {
    Optional<Model> findByTitle(String title);
    
    Page<Model> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
}