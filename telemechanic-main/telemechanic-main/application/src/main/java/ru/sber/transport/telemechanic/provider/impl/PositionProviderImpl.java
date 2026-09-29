package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.mapper.PositionMapper;
import ru.sber.transport.telemechanic.provider.PositionProvider;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.PositionService;
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
    
    public static final String NO_ORGANIZATION_MESSAGE = "Can't save position id:{}, name:{}, organizationId:{}, organization isn't present";
    public static final String AWAITING_ORGANIZATION_MESSAGE = "Can't save position id:%s, entityName:%s, organizationId:%s, awaiting organization " +
                                                               "synchronization";
    private final PositionService service;
    private final PositionMapper mapper;
    private final OrganizationService organizationService;
    
    @Override
    public void delete(PositionMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }
    
    @SneakyThrows
    @Override
    public void save(PositionMessage message) {
        if (Objects.isNull(message.getOrganizationId())) {
            log.info(NO_ORGANIZATION_MESSAGE,
                     message.getId(),
                     message.getPositionName(),
                     message.getOrganizationId());
        } else {
            if (organizationService.get(message.getOrganizationId()).isEmpty()) {
                organizationService.saveGrpcEntity(AWAITING_ORGANIZATION_MESSAGE.formatted(message.getId(),
                                                                                           message.getPositionName(),
                                                                                           message.getOrganizationId()),
                                                   message.getOrganizationId());
            }
            var entity = mapper.positionMessageToPosition(message);
            service.save(entity);
        }
    }
}