package ru.sber.transport.notifications.database.model.settings.channel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.database.model.HasPhone;

/**
 * Доступные каналы отправки сообщений.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ChannelType {
    
    /**
     * PUSH.
     */
    PUSH(HasId.class),
    
    /**
     * SMS.
     */
    SMS(HasPhone.class),
    
    /**
     * EMail.
     */
    EMAIL(HasEmail.class);

    private final Class<? extends HasContactData> contactClass;

}
