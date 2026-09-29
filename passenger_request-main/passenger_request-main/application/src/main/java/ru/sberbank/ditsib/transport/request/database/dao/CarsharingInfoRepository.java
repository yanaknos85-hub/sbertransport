package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingInfo;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.Optional;
import java.util.UUID;

public interface CarsharingInfoRepository extends JpaRepository<CarsharingInfo, UUID> {
    
    Optional<CarsharingInfo> getByEmployee(Employee employee);
    
}