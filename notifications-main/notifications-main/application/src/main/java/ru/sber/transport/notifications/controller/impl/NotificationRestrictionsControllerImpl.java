package ru.sber.transport.notifications.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.notifications.controller.NotificationRestrictionsController;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;
import ru.sber.transport.notifications.mapper.settings.RestrictionSettingsMapper;
import ru.sber.transport.notifications.services.NotificationRestrictionsService;

import java.util.UUID;

/**
 * Реализация контроллера работы с ограничениями уведомлений.
 */
@RestController
@RequiredArgsConstructor
class NotificationRestrictionsControllerImpl implements NotificationRestrictionsController {
    
    private final NotificationRestrictionsService service;
    
    private final RestrictionSettingsMapper restrictionSettingsMapper;
    
    @CheckOrganizationAccess
    @Override
    public RestrictionDataDto get(
            @Organization UUID organizationId, UUID id
                                 ) {
        return restrictionSettingsMapper.toDto(service.getRestrictionsOf(organizationId, id));
    }
    
    @CheckOrganizationAccess
    @Override
    public void set(
            @Organization UUID organizationId, UUID id, RestrictionDataDto data
                   ) {
        service.setRestrictionsOf(organizationId, id, restrictionSettingsMapper.toModel(data));
    }
}
