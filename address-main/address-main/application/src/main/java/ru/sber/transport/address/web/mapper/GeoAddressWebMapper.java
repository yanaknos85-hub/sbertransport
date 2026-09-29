package ru.sber.transport.address.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.web.model.Address;
import ru.sber.transport.web.model.UserAddress;

/**
 * Маппер адресов.
 */
@Mapper
public interface GeoAddressWebMapper extends AddressMapperToWeb<GeoAddress> {

    @Mapping(target = "count", ignore = true)
    @Mapping(target = "label", ignore = true)
    Address toWeb(GeoAddress address);

    @Mapping(target = "first", ignore = true)
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "label", ignore = true)
    UserAddress toUserWeb(GeoAddress frequently);
}
