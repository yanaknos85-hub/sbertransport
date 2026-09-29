package ru.sber.transport.notifications.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;

import java.util.Optional;

/**
 * Маппер ролей.
 */
@Mapper
public interface RoleMapper {

    /**
     * Обновить роль из сообщения.
     *
     * @param role роль для обновления.
     * @param message новые данные.
     */
    void update(@MappingTarget Role role, RoleMessage message);

    default Role toModel(String source) {
        return Optional.ofNullable(source).map(c -> {
            var role = new Role();
            role.setCode(c);
            return role;
        }).orElse(null);
    }

}
