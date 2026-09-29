package ru.sberbank.ditsib.transport.request.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sber.transport.contractor.messages.ContractorUpdateTripMessage;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.GetDriverRequestDTO;

import java.util.Optional;
import java.util.UUID;

@Mapper(uses = UuidMapper.class)
public interface DriverDTOMapper {
    
    @Mapping(target = "driverId", source = "id")
    @Mapping(target = "driver", source = "source")
    GetDriverRequestDTO requestToGetDriverDTO(Driver source);
    
    Driver map(ContractorUpdateTripMessage.Driver driver);
    
    @Mapping(target = "phoneNumber", source = "contactPhone")
    RequestMessage.DriverData map(Driver driver);

    @Mapping(target = "id", source = "id", qualifiedByName = "parseId")
    @Mapping(target = "lastName", source = "secName")
    @Mapping(target = "firstName", source = "name")
    @Mapping(target = "contactPhone", source = "phone")
    Driver map(InContractorTaxiTripInProgressMessage.Driver driver);

    @Named("parseId")
    default UUID parseId(String source) {
        try {
            return Optional.ofNullable(source)
                    .map(UUID::fromString)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    default Integer rating(String source) {
        try {
            return Optional.ofNullable(source)
                    .map(Double::parseDouble)
                    .map(d -> d * 100)
                    .map(Double::intValue).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    void update(@MappingTarget Driver driver, ContractorUpdateTripMessage.Driver receivedDriver);
}
