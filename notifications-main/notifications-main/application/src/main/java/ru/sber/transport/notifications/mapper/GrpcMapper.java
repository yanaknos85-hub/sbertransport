package ru.sber.transport.notifications.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.sync.grpc.service.NotificationsOuterClass;

/**
 * Маппер для grpc
 */
@Mapper
public interface GrpcMapper {

    /**
     * Маппинг для Channel
     * @param channel - канал bp пкзс
     * @return - канал (если в grpc пришло значение UNRECOGNIZED, то возвращается null)
     */
    @ValueMapping(source = "UNRECOGNIZED", target = "<NULL>")
    ChannelType mapChannel(NotificationsOuterClass.Channel channel);
}
