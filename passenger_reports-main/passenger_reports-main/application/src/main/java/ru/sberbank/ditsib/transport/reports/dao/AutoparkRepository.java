package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.driversData.Autopark;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
public interface AutoparkRepository extends JpaRepository<Autopark, UUID> {
}
