package ru.sber.transport.notifications.mapper.settings;

import org.mapstruct.*;
import ru.sber.transport.notifications.dto.counting.CountingDto;
import ru.sber.transport.notifications.dto.notification.*;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;
import ru.sber.transport.notifications.dto.timing.TimingDto;
import ru.sber.transport.notifications.mapper.RoleMapper;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Маппер настроек уведомлений.
 */
@Mapper(uses = {ChannelSettingsMapper.class, CountSettingsMapper.class, RestrictionSettingsMapper.class,
        TimingSettingsMapper.class, RoleMapper.class})
public interface NotificationSettingsMapper {

    NotificationClass toNotificationClass(NotificationClassDto dto);

    NotificationType toNotificationType(NotificationTypeDto dto);

    RestrictionSettings toRestrictionSettings(RestrictionDataDto dto);

    RestrictionDataDto toRestrictionDataDto(RestrictionSettings model);

    default ChannelSettings toModel(ChannelSettingsDto dto) {
        if (dto == null) return null;
        var channelSettings = new ChannelSettings();
        if (dto.getChannel() != null)
            channelSettings.setChannel(dto.getChannel().getModel());
        channelSettings.setText(dto.getText());
        channelSettings.setActive(dto.isEnabled());
        return channelSettings;
    }

    default TimingSettings toModel(TimingDto dto) {
        if (dto == null) return null;
        var timingSettings = new TimingSettings();
        if (dto.getEventType() != null)
            timingSettings.setType(dto.getEventType().getModel());
        timingSettings.setTimeBefore(dto.getTimeBefore());
        timingSettings.setDeadlineFieldName(dto.getDeadlineFieldName());
        timingSettings.setTimeFieldName(dto.getTimeFieldName());
        return timingSettings;
    }

    default CountingSettings toModel(CountingDto dto) {
        if (dto == null) return null;
        var countingSettings = new CountingSettings();
        countingSettings.setCount(dto.getValue());
        if (dto.getType() != null)
            countingSettings.setType(dto.getType().getModel());
        countingSettings.setPropertyName(dto.getProperty());
        return countingSettings;
    }

    default List<ChannelSettings> toSettingList(List<ChannelSettingsDto> dtos) {
        List<ChannelSettings> resultList = new ArrayList<>();
        dtos.forEach(dto -> resultList.add(toModel(dto)));
        return resultList;
    }

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<ChannelSettingsDto> toChannelSettingsDto(List<ChannelSettings> dto);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CountingDto> toCountingsDto(List<CountingSettings> dto);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TimingDto> toTimingsDtoList(List<TimingSettings> dto);

    default List<TimingSettings> toTimingsList(List<TimingDto> dtos) {
        List<TimingSettings> resultList = new ArrayList<>();
        dtos.forEach(dto -> resultList.add(toModel(dto)));
        return resultList;
    }

    default List<CountingSettings> toCountings(List<CountingDto> dtos) {
        List<CountingSettings> resultList = new ArrayList<>();
        dtos.forEach(dto -> resultList.add(toModel(dto)));
        return resultList;
    }

    default NotificationSettings toModel(NotificationSettingsDto dto) {
        if (dto != null) {
            NotificationSettings ns = toModel((NewNotificationSettingsDto) dto);
            if (ns != null) {
                ns.setId(dto.getId());
            }
            return ns;
        }
        return null;
    }

    default NotificationSettings toModel(NewNotificationSettingsDto dto) {
        if (dto == null) return null;
        var notificationSettings =
                NotificationSettings.builder()
                        .notificationClass(toNotificationClass(dto.getNotificationClass()))
                        .type(toNotificationType(dto.getNotificationType()))
                        .restrictions(toRestrictionSettings(dto.getRestriction()))
                        .build();
        notificationSettings.setName(dto.getName());
        notificationSettings.setDescription(dto.getDescription());
        notificationSettings.getChannels().addAll(toSettingList(dto.getChannels()));
        notificationSettings.getTimings().addAll(toTimingsList(dto.getTimings()));
        notificationSettings.getCountings().addAll(toCountings(dto.getCountings()));
        notificationSettings.setParentType(dto.getNotificationClass().getParentType());
        return notificationSettings;
    }

    default NotificationSettingsDto toDto(NotificationSettings dto) {
        if (dto == null) return null;
        var notificationSettingsDto = new NotificationSettingsDto();
        if (dto.getNotificationClass() != null)
            notificationSettingsDto.setNotificationClass(NotificationClassDto.valueOf(dto.getNotificationClass().name()));
        if (dto.getType() != null)
            notificationSettingsDto.setNotificationType(NotificationTypeDto.valueOf(dto.getType().name()));
        notificationSettingsDto.setRestriction(toRestrictionDataDto(dto.getRestrictions()));
        notificationSettingsDto.setId(dto.getId());
        notificationSettingsDto.setDescription(dto.getDescription());
        notificationSettingsDto.setChannels(toChannelSettingsDto(dto.getChannels()));
        notificationSettingsDto.setCountings(toCountingsDto(dto.getCountings()));
        notificationSettingsDto.setTimings(toTimingsDtoList(dto.getTimings()));
        notificationSettingsDto.setName(dto.getName());
        return notificationSettingsDto;
    }

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<NotificationSettingsDto> toDto(List<NotificationSettings> dto);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget NotificationSettings target, NotificationSettings source);
}
