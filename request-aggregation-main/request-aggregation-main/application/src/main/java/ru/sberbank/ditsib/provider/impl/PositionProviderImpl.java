package ru.sberbank.ditsib.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.sberbank.ditsib.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.mappers.PositionMapper;
import ru.sberbank.ditsib.provider.PositionProvider;
import ru.sberbank.ditsib.service.OrganizationService;
import ru.sberbank.ditsib.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

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
            log.info("Can't save position id:{}, positionName:{}, organizationId:{}, organization isn't present",
                     message.getId(),
                     message.getPositionName(),
                     message.getOrganizationId());
        } else if (organizationService.get(entity.getOrganization().getId()).isEmpty()) {
            log.info("Can't save position id:{}, positionName:{}, organizationId:{}, awaiting organization " +
                            "synchronization",
                    message.getId(),
                    message.getPositionName(),
                    message.getOrganizationId());
            throw new AwaitingSynchronizationException("Awaiting an organization synchronization");
        } else {
        service.save(entity);
        }
    }
}
