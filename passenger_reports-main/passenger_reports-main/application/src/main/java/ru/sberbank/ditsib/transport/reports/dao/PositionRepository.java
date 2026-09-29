package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.Position;

import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
}
