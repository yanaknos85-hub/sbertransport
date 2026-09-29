package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Region;

import java.util.UUID;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {
}
