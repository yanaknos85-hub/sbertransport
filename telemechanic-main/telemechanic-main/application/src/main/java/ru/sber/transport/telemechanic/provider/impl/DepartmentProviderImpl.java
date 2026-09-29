package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.telemechanic.mapper.DepartmentMapper;
import ru.sber.transport.telemechanic.provider.DepartmentProvider;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.OrganizationService;
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
    
    private static final String NO_ORGANIZATION_MESSAGE = "Can't save department id:{}, name:{}, organizationId:{}, organization isn't present";
    private static final String AWAITING_ORGANIZATION_MESSAGE = "Can't save department id:%s, entityName:%s, organizationId:%s, awaiting " +
                                                                "organization synchronization";
    private final DepartmentService service;
    private final DepartmentMapper mapper;
    private final OrganizationService organizationService;
    
    @Override
    public void delete(DepartmentMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @Override
    public void save(DepartmentMessage message) {
        if (Objects.isNull(message.getOrganizationId())) {
            log.info(NO_ORGANIZATION_MESSAGE,
                     message.getId(),
                     message.getDepartmentName(),
                     message.getOrganizationId());
        } else {
            if (organizationService.get(message.getOrganizationId()).isEmpty()) {
                organizationService.saveGrpcEntity(AWAITING_ORGANIZATION_MESSAGE.formatted(message.getId(),
                                                                                           message.getDepartmentName(),
                                                                                           message.getOrganizationId()),
                                                   message.getOrganizationId());
            }
            if (message.getParentId() != null && service.get(message.getParentId()).isEmpty()) {
                service.saveGrpcParentEntities(message.getParentId());
            }
            var entity = mapper.departmentMessageToDepartment(message);
            service.save(entity);
        }
    }
    
    @Override
    public void addAutopark(AutoparkMessage message) {
        if(message.routingId() != null) {
            var departmentOpt = service.get(message.routingId());
            departmentOpt.ifPresent(department -> {
                department.setAutoparkId(message.id());
                department.setAutoparkName(message.name());
                service.save(department);
            });
        }
    }
    
    @Override
    public void deleteAutopark(AutoparkMessage message) {
        var departmentOpt = service.getByAutoparkId(message.id());
        departmentOpt.ifPresent(department -> {
            department.setAutoparkId(null);
            department.setAutoparkName(null);
            service.save(department);
        });
    }
}