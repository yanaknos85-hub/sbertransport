package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.reports.model.TripPurpose;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripPurposeRepository extends JpaRepository<TripPurpose, UUID> {
    List<TripPurpose> findByPurpose(String purpose);
}
