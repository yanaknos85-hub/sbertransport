package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.ContractorAndJoinStatus;

import java.util.UUID;

public interface ContractorAndJoinStatusRepository extends JpaRepository<ContractorAndJoinStatus, UUID> {
}
