package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationGroupDto;
import ru.sberbank.ditsib.transport.reports.mappers.OrganizationMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.OrganizationListener;
import ru.sberbank.ditsib.transport.reports.service.OrganizationGroupService;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component("organizationsInput")
public class OrganizationListenerImpl implements OrganizationListener   {
    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;
    private final OrganizationGroupService organizationGroupService;
    
    @Override
    public void handleOrganization(UUID id, OrganizationMessage message) {
        var organizationId = Optional.ofNullable(id).orElse(message.getId());
        if (!message.isDeleted()) {
            if(message.getOrganizationGroup()!=null) {
                organizationGroupService.findById(message.getOrganizationGroup().id())
                                                                        .orElseGet(() -> organizationGroupService.save(
                                                                                new OrganizationGroupDto(message.getOrganizationGroup().id(),
                                                                                                         message.getOrganizationGroup().name(),
                                                                                                         message.getOrganizationGroup().internal())));
                saveOrganization(message, organizationId);
            } else {
                saveOrganization(message, organizationId);
            }
        }
    }
    
    private void saveOrganization(OrganizationMessage message, UUID organizationId){
        var newDepartment = organizationMapper.fromMessage(message);
        if(newDepartment.getId()==null){
            newDepartment.setId(organizationId);
        }
        var existingOrg = organizationService.findById(organizationId);
        if(existingOrg.isPresent()){
            if(existingOrg.get().getOrganizationGroup()!=null && newDepartment.getOrganizationGroup()==null) {
                var organizationGroupId = existingOrg.get().getOrganizationGroup().getId();
                organizationService.save(organizationMapper.update(newDepartment, existingOrg.get()));
                var organizationsInGroup = organizationService.findByOrganizationGroupId(organizationGroupId);
                if(organizationsInGroup.isEmpty()){
                    organizationGroupService.delete(organizationGroupId);
                }
            } else {
                organizationService.save(organizationMapper.update(newDepartment, existingOrg.get()));
            }
        } else {
            organizationService.save(newDepartment);
        }
    }
}
