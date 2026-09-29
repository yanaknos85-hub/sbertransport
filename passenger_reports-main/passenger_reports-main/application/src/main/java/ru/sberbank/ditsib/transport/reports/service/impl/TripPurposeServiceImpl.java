package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.reports.model.TripPurpose;
import ru.sberbank.ditsib.transport.reports.service.TripPurposeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TripPurposeServiceImpl implements TripPurposeService {
    private final TripPurposeRepository tripPurposeRepository;

    @Override
    public TripPurpose findOrCreateByPurposeId(UUID purposeId) {
        return tripPurposeRepository.findById(purposeId).orElseGet(
                () -> tripPurposeRepository.save(new TripPurpose(purposeId))
        );
    }

    public Optional<TripPurpose> findById(UUID id){
        return tripPurposeRepository.findById(id);
    }

    @Override
    public TripPurpose save(TripPurpose purpose) {
       return tripPurposeRepository.save(purpose);
    }
    
    @Override
    public void setInactiveByPurposeId(UUID purposeId) {
        tripPurposeRepository.findById(purposeId)
                                     .ifPresent(p -> {
                                         p.setActive(false);
                                         tripPurposeRepository.save(p);
                                     });
    }
    
    
}
