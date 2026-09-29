package ru.sberbank.ditsib.transport.vehicle.providers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.mapper.PositionMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.PositionProvider;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.PositionService;

import java.util.Objects;

/**
 * Реализация провайдера должностей.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class PositionProviderImpl implements PositionProvider {
    
    private final PositionService service;
    
    private final PositionMapper mapper;
    
    private final OrganizationService organizationService;
    
    @Override
    public void delete(PositionMessage message) {
        var entity = mapper.positionMessageToPosition(message);
        service.delete(entity);
    }
    
    @Override
    public void save(PositionMessage message) {
        var entity = mapper.positionMessageToPosition(message);
    
        if (Objects.isNull(entity.getOrganization())) {
            log.info("Can't save position id:{}, positionName:{}, organizationId:{}, organization is not present",
                     message.getId(),
                     message.getPositionName(),
                     message.getOrganizationId());
        } else if (organizationService.get(entity.getOrganization().getId()).isEmpty()) {
            log.info("Can't save position id:{}, positionName:{}, organizationId:{}, awaiting organization " +
                            "synchronization",
                    message.getId(),
                    message.getPositionName(),
                    message.getOrganizationId());
            throw new EntityNotFoundException(Organization.class, entity.getOrganization());
        } else {
        service.save(entity);
        }
    }
}
