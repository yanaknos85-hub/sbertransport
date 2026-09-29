package ru.sberbank.transport.oto.cargo.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.transport.oto.cargo.database.model.RequestStatusOverdue;

import java.util.UUID;

public interface RequestStatusOverdueRepository extends JpaRepository<RequestStatusOverdue, UUID> {
}
