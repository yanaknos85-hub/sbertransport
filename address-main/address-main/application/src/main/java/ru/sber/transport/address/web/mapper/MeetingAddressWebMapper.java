package ru.sber.transport.address.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.web.model.NewMeetingAddress;
import ru.sber.transport.web.model.UserAddress;

/**
 * Маппер адресов.
 */
@Mapper
public interface MeetingAddressWebMapper extends AddressMapperToModel<MeetingAddress, NewMeetingAddress> {

    @Mapping(target = "address", source = "source")
    ru.sber.transport.web.model.MeetingAddress toWeb(MeetingAddress source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "region", source = "address.region")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "house", source = "address.house")
    @Mapping(target = "building", source = "address.building")
    @Mapping(target = "structure", source = "address.structure")
    @Mapping(target = "latitude", source = "address.latitude")
    @Mapping(target = "longitude", source = "address.longitude")
    MeetingAddress toModel(NewMeetingAddress newMeetingAddress);

    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "first", ignore = true)
    UserAddress toUserWeb(MeetingAddress favorite);
}
