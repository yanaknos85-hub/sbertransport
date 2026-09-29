package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Position;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
}