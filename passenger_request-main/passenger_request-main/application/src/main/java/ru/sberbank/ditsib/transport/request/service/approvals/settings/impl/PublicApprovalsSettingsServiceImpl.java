package ru.sberbank.ditsib.transport.request.service.approvals.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.PublicTrApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.PublicApprovalsSettingsService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicApprovalsSettingsServiceImpl implements PublicApprovalsSettingsService {
    
    private final PublicTrApprovalsSettingsRepository publicSettingsRepository;
    
    @Override
    public PublicTrApprovalsSettings save(PublicTrApprovalsSettings settings) {
        return publicSettingsRepository.save(settings);
    }

    @Override
    public Optional<PublicTrApprovalsSettings> getOptional(UUID organizationId) {
        return publicSettingsRepository.findByOrganizationId(organizationId);
    }

    @Override
    public PublicTrApprovalsSettings get(UUID organizationId) {
        return getOptional(organizationId).orElseThrow(
                () -> new EntityNotFoundException(PublicTrApprovalsSettings.class, Map.of("organizationId", organizationId)));
    }
    
    @Override
    public void delete(UUID organizationId) {
        PublicTrApprovalsSettings settings = get(organizationId);
        publicSettingsRepository.delete(settings);
    }
}
