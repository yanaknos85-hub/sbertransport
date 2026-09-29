package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.FleetOwnerOrganizationRepository;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerOrganizationException;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class FleetOwnerOrganizationServiceImpl implements FleetOwnerOrganizationService {
    
    private final FleetOwnerOrganizationRepository repository;
    
    @Override
    public FleetOwnerOrganization save(FleetOwnerOrganization entity) {
        return repository.save(entity);
    }
    
    @Override
    public FleetOwnerOrganization get(UUID id) {
        return repository.findByOrganizationIdAndActiveTrue(id)
                .orElse(null);
    }
    
    @Override
    public void validateFleetOwnerOrganization(UUID organizationId) {
        if (!repository.existsByOrganizationIdAndActiveTrue(organizationId)) {
            throw new FleetOwnerOrganizationException(FleetOwnerOrganizationException.NOT_FOUND_MSG.formatted(organizationId));
        }
    }
    
    @Override
    public List<GetAllActiveOrganizationNamesDto> getAllActive() {
        return repository.findAllActive();
    }
}
