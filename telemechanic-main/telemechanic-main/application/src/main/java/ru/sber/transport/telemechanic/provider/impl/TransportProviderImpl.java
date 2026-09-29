package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.messaging.sender.message.TransportMessage;
import ru.sber.transport.telemechanic.provider.TransportProvider;
import ru.sber.transport.telemechanic.service.TransportService;

import java.util.HashSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransportProviderImpl implements TransportProvider {
    
    private final TransportService transportService;
    private final OrganizationRepository organizationRepository;
    
    @Override
    public void delete(TransportMessage message) {
        transportService.get(message.getId()).ifPresent(transportService::deactivate);
    }
    
    @Override
    public void save(TransportMessage message) {
        if (checkAffiliation(message)) {
            var organizations = new HashSet<Organization>();
            if(message.organizationIds() != null && !message.organizationIds().isEmpty()) {
                organizations = new HashSet<>(organizationRepository.findAllById(message.organizationIds()));
            }
            transportService.saveOrUpdate(new Transport(message.id(),
                                                        message.stateNumber(),
                                                        message.brand(),
                                                        message.model(),
                                                        message.currentMileage(),
                                                        TransportStatus.IN_USE,
                                                        message.type(),
                                                        message.subtype(),
                                                        message.fuelTankVolume(),
                                                        organizations,
                                                        null,
                                                        message.contractorId(),
                                                        message.autoparkId()));
        }
    }
    
    private boolean checkAffiliation(TransportMessage message) {
        if (message.organizationIds() == null || message.organizationIds().isEmpty()) {
            if (message.contractorId() == null && message.autoparkId() == null) {
                log.warn("Transport must be affiliated to organization or contractor, message with id {} is ignored", message.id());
                return false;
            }
        } else if (message.contractorId() != null && message.autoparkId() != null) {
            log.warn("Transport must not be affiliated to organization and contractor at the same time, message with id {} is ignored", message.id());
            return false;
        }
        return true;
    }
}
