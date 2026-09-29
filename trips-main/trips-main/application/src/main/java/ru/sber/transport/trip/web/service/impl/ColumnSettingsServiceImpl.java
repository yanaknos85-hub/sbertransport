package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.model.Dispatcher;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.providers.column_settings.ColumnSettingsProvider;
import ru.sber.transport.trip.web.service.ColumnSettingsService;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColumnSettingsServiceImpl implements ColumnSettingsService {

    private final ColumnSettingsProvider columnSettingsProvider;

    private final DispatcherProvider dispatcherProvider;

    @Override
    public Map<String, Object> createColumnSetting(UUID userId, Map<String, Object> setting) {
        return columnSettingsProvider.save(getDispatcherId(userId), setting);
    }

    @Override
    public void updateColumnSetting(UUID userId, Map<String, Object> setting) {
        columnSettingsProvider.update(getDispatcherId(userId), setting);
    }

    @Override
    public void deleteColumnSetting(UUID userId) {
        columnSettingsProvider.delete(getDispatcherId(userId));
    }

    @Override
    public Map<String, Object> getColumnSetting(UUID userId) {
        return columnSettingsProvider.get(getDispatcherId(userId));
    }

    private UUID getDispatcherId(UUID id) {
        return dispatcherProvider.get(id)
                .orElseGet(() -> dispatcherProvider.getByOauthId(id)
                        .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class, id))).getId();
    }
}
