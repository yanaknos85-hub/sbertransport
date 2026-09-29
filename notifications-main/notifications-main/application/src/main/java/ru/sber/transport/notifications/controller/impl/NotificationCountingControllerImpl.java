package ru.sber.transport.notifications.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.notifications.controller.NotificationCountingController;
import ru.sber.transport.notifications.dto.counting.CountingDto;
import ru.sber.transport.notifications.mapper.settings.CountSettingsMapper;
import ru.sber.transport.notifications.services.NotificationCountingService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера работы с уведомлениями.
 */
@RestController
@RequiredArgsConstructor
class NotificationCountingControllerImpl implements NotificationCountingController {
    
    private final NotificationCountingService service;
    
    private final CountSettingsMapper countSettingsMapper;
    
    @CheckOrganizationAccess
    @Override
    public List<CountingDto> get(
            @Organization UUID organizationId, UUID id
                                          ) {
        return countSettingsMapper.toDto(service.getCountingOf(organizationId, id));
    }
    
    @CheckOrganizationAccess
    @Override
    public void set(
            @Organization UUID organizationId, UUID id, List<CountingDto> data
                   ) {
        service.setCountingOf(organizationId, id, countSettingsMapper.toModel(data));
    }
}
