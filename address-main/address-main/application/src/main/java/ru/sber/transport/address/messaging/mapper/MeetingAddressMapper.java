package ru.sber.transport.address.messaging.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.messages.addresses.avro.MeetingAddressMessage;

/**
 * Маппер объектов филиалов.
 */
@Mapper
public interface MeetingAddressMapper {

    @Mapping(target = "address", source = "source")
    @Mapping(target = "coordinates", source = "source")
    @Mapping(target = "addressBuilder", ignore = true)
    @Mapping(target = "coordinatesBuilder", ignore = true)
    MeetingAddressMessage toMessage(MeetingAddress source);
}
