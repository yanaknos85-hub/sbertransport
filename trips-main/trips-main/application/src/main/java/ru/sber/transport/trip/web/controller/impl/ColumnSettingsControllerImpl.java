package ru.sber.transport.trip.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.trip.web.controller.ColumnSettingsController;
import ru.sber.transport.trip.web.service.ColumnSettingsService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ColumnSettingsControllerImpl implements ColumnSettingsController {

    private final ColumnSettingsService columnSettingsService;

    @Override
    public Map<String, Object> createColumnSetting(JwtAuthenticationToken authentication, Map<String, Object> setting) {
        return columnSettingsService.createColumnSetting(UUID.fromString(authentication.getToken().getId()), setting);
    }

    @Override
    public void updateColumnSetting(JwtAuthenticationToken authentication, Map<String, Object> setting) {
        columnSettingsService.updateColumnSetting(UUID.fromString(authentication.getToken().getId()), setting);
    }

    @Override
    public void deleteColumnSetting(JwtAuthenticationToken authentication) {
        columnSettingsService.deleteColumnSetting(UUID.fromString(authentication.getToken().getId()));
    }

    @Override
    public Map<String, Object> getColumnSetting(JwtAuthenticationToken authentication) {
       return columnSettingsService.getColumnSetting(UUID.fromString(authentication.getToken().getId()));
    }
}
