package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.reports.dto.oto.AddressDto;
import ru.sberbank.ditsib.transport.reports.model.Address;

@Mapper
public interface AddressMapper {
    
    AddressDto toDto(Address sourceAddress);
    
}
