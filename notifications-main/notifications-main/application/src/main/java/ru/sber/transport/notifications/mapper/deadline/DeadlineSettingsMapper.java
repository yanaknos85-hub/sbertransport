package ru.sber.transport.notifications.mapper.deadline;

import org.mapstruct.Mapper;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sber.transport.notifications.database.model.deadline.DeadlineSettings;

@Mapper
public interface DeadlineSettingsMapper {
    
    DeadlineSettings fromMessage(DeadlineSettingsMessage message);
}
