package ru.sber.transport.address.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.web.model.Address;
import ru.sber.transport.web.model.UserAddress;

/**
 * Маппер адресов.
 */
@Mapper
public interface FrequentlyAddressWebMapper extends AddressMapperToWeb<FrequentlyAddress> {

    @Mapping(target = "label", ignore = true)
    Address toWeb(FrequentlyAddress address);

    @Mapping(target = "usages", source = "count")
    @Mapping(target = "label", ignore = true)
    UserAddress toUserWeb(FrequentlyAddress frequently);
}
