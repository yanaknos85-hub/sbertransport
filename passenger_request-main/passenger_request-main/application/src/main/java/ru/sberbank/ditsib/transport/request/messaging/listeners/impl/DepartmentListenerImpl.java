package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.DepartmentMessage;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.function.Consumer;

/**
 * Implementation of departments listener.
 */
@RequiredArgsConstructor
public class DepartmentListenerImpl implements Consumer<Message<DepartmentMessage>> {
    
    private final DepartmentService departmentService;
    
    private final OrganizationService organizationService;
    
    public void accept(Message<DepartmentMessage> message) {
        handleDepartments(message.getPayload());
    }
    
    private void handleDepartments(DepartmentMessage message) {
        
        var organization = organizationService.get(message.getOrganizationId())
                                              .orElseGet(() -> organizationService.save(Organization.builder()
                                                                                                    .id(message.getOrganizationId())
                                                                                                    .build()));
        
        if (message.isDeleted()) {
            departmentService.get(message.getId()).ifPresent(departmentService::delete);
        } else {
            departmentService.save(Department.builder()
                                             .id(message.getId())
                                             .humanReadableId(message.getHumanReadableId())
                                             .parent(message.getParentId())
                                             .organization(organization)
                                             .departmentName(message.getDepartmentName())
                                             .location(message.getLocation())
                                             .departmentHead(message.getDepartmentHeadId())
                                             .build());
        }
    }
    
}
