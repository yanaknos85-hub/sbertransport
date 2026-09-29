package ru.sber.transport.notifications.mapper.settings;

import org.mapstruct.*;
import ru.sber.transport.notifications.dto.timing.TimingDto;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.List;

/**
 * Маппер настроек тайминга.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TimingSettingsMapper {
    
    default TimingSettings toModel(TimingDto dto) {
        if (dto == null) return null;
        var timingSettings = new TimingSettings();
        timingSettings.setTimeBefore(dto.getTimeBefore());
        timingSettings.setType(toModel(dto.getEventType()));
        timingSettings.setTimeFieldName(dto.getTimeFieldName());
        timingSettings.setDeadlineFieldName(dto.getDeadlineFieldName());
        return timingSettings;
    }
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TimingSettings> toModel(List<? extends TimingDto> dto);
    
    @ValueMapping(target = "AT_EVENT", source = "AT_EVENT")
    @ValueMapping(target = "BEFORE_DEADLINE", source = "DEADLINE")
    EventType toModel(ru.sber.transport.notifications.dto.timing.EventType type);

    @Mapping(target = "eventType", source = "type")
    TimingDto toDto(TimingSettings dto);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TimingDto> toDto(List<? extends TimingSettings> countingOf);
    
    @ValueMapping(source = "AT_EVENT", target = "AT_EVENT")
    @ValueMapping(source = "BEFORE_DEADLINE", target = "DEADLINE")
    ru.sber.transport.notifications.dto.timing.EventType toDto(EventType type);
}
