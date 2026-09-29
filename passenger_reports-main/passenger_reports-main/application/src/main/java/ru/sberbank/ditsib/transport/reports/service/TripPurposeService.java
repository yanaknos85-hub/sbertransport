package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.TripPurpose;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripPurposeService {
    TripPurpose findOrCreateByPurposeId(UUID purposeId);
    Optional<TripPurpose> findById(UUID id);
    TripPurpose save(TripPurpose purpose);
    
    void setInactiveByPurposeId(UUID purposeId);
}
