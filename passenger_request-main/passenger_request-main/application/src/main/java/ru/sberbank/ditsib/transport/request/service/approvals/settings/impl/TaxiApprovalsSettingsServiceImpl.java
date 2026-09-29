package ru.sberbank.ditsib.transport.request.service.approvals.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.TaxiApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.TaxiApprovalsSettingsService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxiApprovalsSettingsServiceImpl implements TaxiApprovalsSettingsService {
    
    private final TaxiApprovalsSettingsRepository taxiSettingsRepository;
    
    @Override
    public TaxiApprovalsSettings save(TaxiApprovalsSettings settings) {
        return taxiSettingsRepository.save(settings);
    }
    
    @Override
    public Optional<TaxiApprovalsSettings> getOptional(UUID organizationId) {
        return taxiSettingsRepository.findByOrganizationId(organizationId);
    }
    
    @Override
    public TaxiApprovalsSettings get(UUID organizationId) {
        return getOptional(organizationId).orElseThrow(
                () -> new EntityNotFoundException(TaxiApprovalsSettings.class, Map.of("organizationId", organizationId)));
    }
    
    @Override
    public void delete(UUID organizationId) {
        TaxiApprovalsSettings settings = get(organizationId);
        taxiSettingsRepository.delete(settings);
    }
}
