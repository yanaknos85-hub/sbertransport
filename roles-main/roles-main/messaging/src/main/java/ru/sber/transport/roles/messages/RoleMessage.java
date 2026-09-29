package ru.sber.transport.roles.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.sber.transport.messaging.Message;

import java.util.List;

/**
 * Сообщение ролей.
 *
 * @param code код роли.
 * @param name наименование роли.
 * @param description описание роли.
 * @param deleted флаг удаления.
 * @param dataMaster флаг СМД.
 * @param exclusives список признаков, для которых роль применима.
 * @param scopes области действия роли по-умолчанию.
 */
public record RoleMessage(

    String code,
    String name,
    String description,
    List<String> scopes,
    List<String> exclusives,
    boolean dataMaster,
    boolean deleted
) implements Message<String> {

    @JsonIgnore
    @Override
    public String getId() {
        return code;
    }
}
