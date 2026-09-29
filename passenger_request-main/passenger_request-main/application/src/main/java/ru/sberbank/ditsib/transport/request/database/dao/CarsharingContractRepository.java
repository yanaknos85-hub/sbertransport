package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingContract;

import java.util.UUID;

public interface CarsharingContractRepository extends JpaRepository<CarsharingContract, UUID> {
}
