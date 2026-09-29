package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.request.messaging.AddressMessage;
import ru.sberbank.ditsib.transport.request.database.model.Address;

/**
 * Маппер адресов.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AddressMapper {
    
    @Mapping(target = "region", source = "region")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "street", source = "street")
    @Mapping(target = "house", source = "house")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "structure", source = "structure")
    AddressMessage toMessage(Address address);
    
}
