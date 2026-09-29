package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.exception.TransportUnboundException;
import ru.sber.transport.telemechanic.service.TransportValidationService;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransportValidationServiceImpl implements TransportValidationService {
    @Override
    public void validateTransportOrgsContainsDispatcherOrg(Set<UUID> organizationIds, UUID dispatcherOrgId, UUID transportId) {
        if (!organizationIds.contains(dispatcherOrgId)) {
            throw new TransportUnboundException(transportId);
        }
    }
}
