package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sber.transport.telemechanic.database.dao.OrganizationGroupRepository;
import ru.sber.transport.telemechanic.database.model.OrganizationGroup;
import ru.sber.transport.telemechanic.service.OrganizationGroupService;

@RequiredArgsConstructor
@Service
public class OrganizationGroupServiceImpl implements OrganizationGroupService {
    
    private final OrganizationGroupRepository organizationGroupRepository;
    
    @Override
    @Transactional
    public void save(OrganizationMessage.OrganizationGroup organizationGroup) {
        organizationGroupRepository.save(new OrganizationGroup(organizationGroup.id(),
                                                               organizationGroup.name(),
                                                               organizationGroup.internal()));
    }
}
