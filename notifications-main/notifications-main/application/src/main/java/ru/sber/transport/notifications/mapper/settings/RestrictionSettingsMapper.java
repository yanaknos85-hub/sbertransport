package ru.sber.transport.notifications.mapper.settings;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;
import ru.sber.transport.notifications.mapper.RoleMapper;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionRoles;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;

import java.util.Optional;

/**
 * Маппер настроек уведомлений.
 */
@Mapper(uses = RoleMapper.class)
public interface RestrictionSettingsMapper {
    
    @Mapping(target = "notification", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restrictType", source = "type")
    @Mapping(target = "roles", source = "roles")
    RestrictionSettings toModel(RestrictionDataDto dto);
    
    @Mapping(target = "restrictionSettings", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", source = "exclude")
    RestrictionRoles toModel(String exclude);
    
    @Mapping(target = "type", source = "restrictType")
    RestrictionDataDto toDto(RestrictionSettings dto);

    default String toString(RestrictionRoles source) {
        return Optional.ofNullable(source).map(RestrictionRoles::getRole).map(Role::getCode).orElse(null);
    }
    
}
