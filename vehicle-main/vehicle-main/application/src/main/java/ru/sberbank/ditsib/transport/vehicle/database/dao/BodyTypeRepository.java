package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.BodyType;

import java.util.Optional;
import java.util.UUID;

public interface BodyTypeRepository extends JpaRepository<BodyType, UUID> {
    Optional<BodyType> findByTitle(String title);

    Page<BodyType> findAllByTitleContainingIgnoreCase(String title, PageRequest pageRequest);
}
