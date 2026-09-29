package ru.sber.transport.address.messaging.sender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.messages.addresses.avro.FavoriteAddressMessage;

@Mapper
public interface FavoriteAddressMessageMapper {

    @Mapping(target = "address", source = "source")
    @Mapping(target = "coordinates", source = "source")
    @Mapping(target = "employeeId", source = "source.owner")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "addressBuilder", ignore = true)
    @Mapping(target = "coordinatesBuilder", ignore = true)
    FavoriteAddressMessage toMessage(FavoriteAddress source, boolean deleted);

}
