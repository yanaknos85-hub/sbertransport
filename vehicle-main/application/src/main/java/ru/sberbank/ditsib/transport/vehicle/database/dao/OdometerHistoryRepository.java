package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerHistory;

import java.util.UUID;

@Repository
public interface OdometerHistoryRepository extends JpaRepository<OdometerHistory, UUID> {
}
