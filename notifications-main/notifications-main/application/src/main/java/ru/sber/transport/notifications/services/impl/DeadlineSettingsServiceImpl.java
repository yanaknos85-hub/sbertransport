package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.deadline.DeadlineSettingsRepository;
import ru.sber.transport.notifications.database.model.deadline.DeadlineSettings;
import ru.sber.transport.notifications.services.DeadlineSettingsService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineSettingsServiceImpl implements DeadlineSettingsService {
    
    private final DeadlineSettingsRepository repository;
    
    @Override
    public DeadlineSettings getById(UUID settingsId) {
        return getOptional(settingsId).orElseThrow(
                () -> new EntityNotFoundException(DeadlineSettings.class, settingsId));
    }
    
    @Override
    public Optional<DeadlineSettings> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId);
    }
    
    @Override
    public Optional<DeadlineSettings> getOptional(UUID settingsId) {
        return repository.findById(settingsId);
    }
    
    @Override
    public boolean existsById(UUID settingsId) {
        return repository.existsById(settingsId);
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
