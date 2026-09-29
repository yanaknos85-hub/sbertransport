package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.cargo.RequestForCargo;

import java.util.UUID;

@Repository
public interface RequestForCargoRepository extends JpaRepository<RequestForCargo, UUID>, JpaSpecificationExecutor<RequestForCargo> {
}
