package ru.sber.transport.address.messaging.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.messages.addresses.avro.UsedAddressMessage;

@Mapper
public interface AddressMessageMapper {

    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "region", source = "address.region")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "house", source = "address.house")
    @Mapping(target = "building", source = "address.building")
    @Mapping(target = "structure", source = "address.structure")
    @Mapping(target = "latitude", source = "coordinates.latitude")
    @Mapping(target = "longitude", source = "coordinates.longitude")
    GeoAddress toBusiness(UsedAddressMessage source);

}
