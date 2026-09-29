package ru.sber.transport.notifications.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.notifications.controller.NotificationTimingController;
import ru.sber.transport.notifications.dto.timing.TimingDto;
import ru.sber.transport.notifications.mapper.settings.TimingSettingsMapper;
import ru.sber.transport.notifications.services.NotificationTimingService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация контроллера работы с уведомлениями.
 */
@RestController
@RequiredArgsConstructor
class NotificationTimingControllerImpl implements NotificationTimingController {
    
    private final NotificationTimingService service;
    
    private final TimingSettingsMapper timingSettingsMapper;
    
    @CheckOrganizationAccess
    @Override
    public List<TimingDto> get(
            @Organization UUID organizationId, UUID id
                                        ) {
        return timingSettingsMapper.toDto(service.getTimingOf(organizationId, id));
    }
    
    @CheckOrganizationAccess
    @Override
    public void set(
            @Organization UUID organizationId, UUID id, List<TimingDto> data
                   ) {
        service.setTimingOf(organizationId, id, timingSettingsMapper.toModel(data));
    }
}
