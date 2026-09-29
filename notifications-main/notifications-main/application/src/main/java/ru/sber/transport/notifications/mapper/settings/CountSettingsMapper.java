package ru.sber.transport.notifications.mapper.settings;

import org.mapstruct.*;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.dto.counting.CountType;
import ru.sber.transport.notifications.dto.counting.CountingDto;

import java.util.List;

/**
 * Маппер настроек количественного триггера.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CountSettingsMapper {
    
    default CountingSettings toModel(CountingDto dto) {
        if (dto == null) return null;
        var countingSettings = new CountingSettings();
        countingSettings.setCount(dto.getValue());
        if (dto.getType() != null)
            countingSettings.setType(dto.getType().getModel());
        countingSettings.setPropertyName(dto.getProperty());
        return countingSettings;
    }
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CountingSettings> toModel(List<? extends CountingDto> dto);
    
    default CountingDto toDto(CountingSettings model) {
        if (model == null) return null;
        var countingDto = new CountingDto();
        countingDto.setValue(model.getCount());
        if (model.getType() != null)
            countingDto.setType(CountType.valueOf(model.getType().name()));
        countingDto.setProperty(model.getPropertyName());
        return countingDto;
    }
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CountingDto> toDto(List<? extends CountingSettings> countingOf);
}
