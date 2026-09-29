package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.mapper.FleetOwnerOrganizationMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;
import ru.sber.transport.telemechanic.provider.FleetOwnerOrganizationProvider;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;
import ru.sber.transport.telemechanic.service.OrganizationService;

@Transactional
@RequiredArgsConstructor
@Component
@Slf4j
public class FleetOwnerOrganizationProviderImpl implements FleetOwnerOrganizationProvider {
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    private final OrganizationService organizationService;
    private final FleetOwnerOrganizationMapper mapper;
    
    @Override
    public void save(FleetOwnerOrganizationMessage message) {
        saveFleetOwnerOrganization(message, true);
    }
    
    private void saveFleetOwnerOrganization(FleetOwnerOrganizationMessage message, boolean active) {
        var optionalOrganization = organizationService.get(message.organizationId());
        if (optionalOrganization.isPresent()) {
            fleetOwnerOrganizationService.save(mapper.fleetOwnerOrganizationMessageToFleetOwnerOrganization(message,
                                                                                                            optionalOrganization.get(),
                                                                                                            active));
        } else {
            log.info("Can't save fleet owner organization, id:{}, organizationId:{}, awaiting organization " +
                     "synchronization",
                     message.getId(),
                     message.organizationId());
            throw new AwaitingSynchronizationException("Awaiting an organization synchronization");
        }
    }
}
