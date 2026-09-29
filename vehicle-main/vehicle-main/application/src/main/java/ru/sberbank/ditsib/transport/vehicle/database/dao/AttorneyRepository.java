package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.sberbank.ditsib.transport.vehicle.database.model.Attorney;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttorneyRepository extends JpaRepository<Attorney, UUID>, JpaSpecificationExecutor<Attorney> {

    @Override
    @EntityGraph("attorney-full")
    Page<Attorney> findAll(Specification<Attorney> spec, Pageable pageable);

    Optional<Attorney> findByAttorneyId(UUID attorneyId);
    
    @EntityGraph("attorney-full")
    List<Attorney> getAllByTelemechanicId(UUID telemechanicId);
}
