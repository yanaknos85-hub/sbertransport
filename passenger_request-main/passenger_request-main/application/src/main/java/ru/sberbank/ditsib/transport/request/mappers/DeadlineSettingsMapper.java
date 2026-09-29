package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.request.messaging.message.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.deadline.CarsharingJoinDeadlineSettingsItem;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.database.model.deadline.LimitDeadlineSettingsItem;
import ru.sberbank.ditsib.transport.request.database.model.deadline.RequestDeadlineSettingsItem;

@Mapper
public interface DeadlineSettingsMapper {
    
    RequestDeadlineSettingsItem messageToRequestItem(
            DeadlineSettingsMessage.RequestDeadlineSettingsItem messageRequestItem);
    
    LimitDeadlineSettingsItem messageToLimitItem(
            DeadlineSettingsMessage.LimitDeadlineSettingsItem messageRequestItem);
    
    CarsharingJoinDeadlineSettingsItem messageToCarsharingJoinItem(
            DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem messageRequestItem);
    
    DeadlineSettings fromMessage(DeadlineSettingsMessage message);
}
