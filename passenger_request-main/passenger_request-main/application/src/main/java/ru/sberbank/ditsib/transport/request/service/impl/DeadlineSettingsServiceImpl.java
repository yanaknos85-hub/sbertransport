package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.database.dao.deadline.DeadlineSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.service.DeadlineSettingsService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineSettingsServiceImpl implements DeadlineSettingsService {
    
    private final DeadlineSettingsRepository repository;

    @Override
    public Optional<DeadlineSettings> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId);
    }
    
    @Override
    public Optional<DeadlineSettings> getOptional(UUID settingsId) {
        return repository.findById(settingsId);
    }
    
    @Override
    public DeadlineSettings save(DeadlineSettings settings) {
        return repository.save(settings);
    }
    
    @Override
    public void hardDelete(DeadlineSettings settings) {
        repository.delete(settings);
    }
}
