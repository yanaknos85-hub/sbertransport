package ru.sber.transport.address.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.web.model.Address;
import ru.sber.transport.web.model.NewAddress;
import ru.sber.transport.web.model.NewFavoriteAddress;
import ru.sber.transport.web.model.UserAddress;

/**
 * Маппер адресов.
 */
@Mapper
public interface FavoriteAddressWebMapper extends AddressMapperToWeb<FavoriteAddress>, AddressMapperToModel<FavoriteAddress, NewAddress> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    FavoriteAddress toModel(NewFavoriteAddress address);

    @Mapping(target = "count", ignore = true)
    Address toWeb(FavoriteAddress address);

    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "first", ignore = true)
    UserAddress toUserWeb(FavoriteAddress favorite);
}
