package ru.sberbank.ditsib.transport.vehicle.providers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.mapper.DepartmentMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.DepartmentProvider;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.util.Objects;

/**
 * Реализация провайдера подразделений.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class DepartmentProviderImpl implements DepartmentProvider {
    
    private final DepartmentService service;
    
    private final DepartmentMapper mapper;
    
    private final OrganizationService organizationService;
    
    @Override
    public void delete(DepartmentMessage message) {
        var entity = mapper.departmentMessageToDepartment(message);
        service.delete(entity);
    }
    
    @Override
    public void save(DepartmentMessage message) {
        var entity = mapper.departmentMessageToDepartment(message);
        if (Objects.isNull(entity.getOrganization())) {
            log.info("Can't save department id:{}, departmentName:{}, organizationId:{}, organization is not present",
                     message.getId(),
                     message.getDepartmentName(),
                     message.getOrganizationId());
        } else if (organizationService.get(entity.getOrganization().getId()).isEmpty()) {
            log.info("Can't save department id:{}, departmentName:{}, organizationId:{}, awaiting organization " +
                            "synchronization",
                    message.getId(),
                    message.getDepartmentName(),
                    message.getOrganizationId());
            throw new EntityNotFoundException(Organization.class, entity.getOrganization());
        } else {
            service.save(entity);
        }
    }
}
