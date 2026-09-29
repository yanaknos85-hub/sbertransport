package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.OrganizationGroupRepository;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationGroupDto;
import ru.sberbank.ditsib.transport.reports.model.OrganizationGroup;
import ru.sberbank.ditsib.transport.reports.service.OrganizationGroupService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationGroupServiceImpl implements OrganizationGroupService {
    
    private final OrganizationGroupRepository organizationGroupRepository;
    
    @Override
    public OrganizationGroup save(OrganizationGroupDto organizationGroupDto) {
        return organizationGroupRepository.save(new OrganizationGroup(organizationGroupDto.id(),
                                                                      organizationGroupDto.name(),
                                                                      organizationGroupDto.internal()));
    }
    
    @Override
    public Optional<OrganizationGroup> findById(UUID id) {
        return organizationGroupRepository.findById(id);
    }
    
    @Override
    public void delete(UUID id) {
        organizationGroupRepository.deleteById(id);
    }
}
