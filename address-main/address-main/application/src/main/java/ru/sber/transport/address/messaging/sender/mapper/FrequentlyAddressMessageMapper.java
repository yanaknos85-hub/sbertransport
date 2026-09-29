package ru.sber.transport.address.messaging.sender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.messages.addresses.avro.FrequentlyAddressMessage;

@Mapper
public interface FrequentlyAddressMessageMapper {

    @Mapping(target = "employeeId", source = "owner")
    @Mapping(target = "address", source = "source")
    @Mapping(target = "coordinates", source = "source")
    @Mapping(target = "addressBuilder", ignore = true)
    @Mapping(target = "coordinatesBuilder", ignore = true)
    FrequentlyAddressMessage toMessage(FrequentlyAddress source);

}
