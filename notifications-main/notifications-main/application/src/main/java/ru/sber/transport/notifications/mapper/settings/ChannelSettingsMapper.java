package ru.sber.transport.notifications.mapper.settings;

import org.mapstruct.Mapper;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.dto.notification.Channel;
import ru.sber.transport.notifications.dto.notification.ChannelSettingsDto;

/**
 * Маппер настроек уведомлений.
 */
@Mapper
public interface ChannelSettingsMapper {
    
    default ChannelSettings toModel(ChannelSettingsDto dto) {
        if (dto == null) return null;
        var channelSettings = new ChannelSettings();
        channelSettings.setActive(dto.isEnabled());
        if (dto.getChannel() != null) {
            channelSettings.setChannel(dto.getChannel().getModel());
        }
        channelSettings.setText(dto.getText());
        return channelSettings;
    }
    
    default ChannelSettingsDto toDto(ChannelSettings model) {
        if (model == null) return null;
        var channelSettingsDto = new ChannelSettingsDto();
        if (model.getChannel() != null) {
            channelSettingsDto.setChannel(Channel.valueOf(model.getChannel().name()));
        }
        channelSettingsDto.setText(model.getText());
        channelSettingsDto.setEnabled(model.isActive());
        return channelSettingsDto;
    }
    
}
