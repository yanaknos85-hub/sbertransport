package ru.sberbank.ditsib.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.mappers.DepartmentMapper;
import ru.sberbank.ditsib.provider.DepartmentProvider;
import ru.sberbank.ditsib.service.DepartmentService;
import ru.sberbank.ditsib.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

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
            log.info("Can't save department id:{}, departmentName:{}, organizationId:{}, organization isn't present",
                     message.getId(),
                     message.getDepartmentName(),
                     message.getOrganizationId());
        } else if (organizationService.get(entity.getOrganization().getId()).isEmpty()) {
            log.info("Can't save department id:{}, departmentName:{}, organizationId:{}, awaiting organization " +
                            "synchronization",
                    message.getId(),
                    message.getDepartmentName(),
                    message.getOrganizationId());
            throw new AwaitingSynchronizationException("Awaiting organization synchronization");
        } else {
            service.save(entity);
        }
    }
}
