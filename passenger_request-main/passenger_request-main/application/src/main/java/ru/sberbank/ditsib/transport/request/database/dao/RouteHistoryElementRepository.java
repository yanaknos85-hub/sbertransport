package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.RouteHistoryElement;

import java.util.UUID;

public interface RouteHistoryElementRepository extends JpaRepository<RouteHistoryElement, UUID> {
}
