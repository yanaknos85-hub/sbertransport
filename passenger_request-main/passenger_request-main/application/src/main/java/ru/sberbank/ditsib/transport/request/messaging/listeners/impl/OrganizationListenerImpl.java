package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.messaging.message.OrganizationMessage;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implementation of organizations listener.
 */
@RequiredArgsConstructor
public class OrganizationListenerImpl implements Consumer<Message<OrganizationMessage>> {
    
    private final OrganizationService organizationService;
    
    public void accept(Message<OrganizationMessage> message) {
        handleOrganization(message.getPayload().getId(), message.getPayload());
    }
    
    private void handleOrganization(UUID id, OrganizationMessage message) {
        var organizationId = Optional.ofNullable(id).orElse(message.getId());
        if (message.isDeleted()) {
            organizationService.delete(organizationService.get(organizationId).orElse(null));
        } else {
            organizationService.save(Organization.builder()
                    .id(organizationId)
                    .officialName(message.getOfficialName()).digitId(message.getDigitId())
                    .build());
        }
    }
}
