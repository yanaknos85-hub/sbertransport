package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.request.database.model.DepLimit;

import java.util.Optional;
import java.util.UUID;

public interface DepLimitRepository extends JpaRepository<DepLimit, UUID> {
    
    Optional<DepLimit> findByIdAndActive(UUID id, boolean active);
    
    Optional<DepLimit> findByDepartmentIdAndYearAndActive(UUID departmentId, Integer year, boolean active);
    
}
