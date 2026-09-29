package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.reports.model.RequestStatusOverdue;

import java.util.UUID;

public interface RequestStatusOverdueRepository extends JpaRepository<RequestStatusOverdue, UUID> {
}
