package ru.sberbank.ditsib.transport.tariff.mappers.impl;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;

import java.util.Map;

@NoArgsConstructor
@Component
public class TariffSearchDTOMapperImpl implements TariffSearchDTOMapper {
    
    public TariffSearchDTO mapFromParameters(Map<String, ?> parameters) {
        TariffSearchDTO.TariffSearchDTOBuilder tariffSearchDTO = TariffSearchDTO.builder();
        if (parameters == null) {
            return tariffSearchDTO.build();
        }
        
        if (parameters.containsKey("transportType")) {
            tariffSearchDTO.transportType(mapToTransportTypeEnum(parameters.get("transportType")));
        }
        if (parameters.containsKey("serviceType")) {
            tariffSearchDTO.serviceType(mapToTransportServiceType(parameters.get("serviceType")));
        }
        if (parameters.containsKey("organizationId")) {
            tariffSearchDTO.organizationId(mapToUUID(parameters.get("organizationId")));
        }
        if (parameters.containsKey("contractId")) {
            tariffSearchDTO.contractId(mapToUUID(parameters.get("contractId")));
        }
        if (parameters.containsKey("contractNumber")) {
            tariffSearchDTO.contractNumber(mapToString(parameters.get("contractNumber")));
        }
        if (parameters.containsKey("contractorId")) {
            tariffSearchDTO.contractorId(mapToUUID(parameters.get("contractorId")));
        }
        if (parameters.containsKey("humanReadableId")) {
            tariffSearchDTO.humanReadableId(mapToString(parameters.get("humanReadableId")));
        }
        if (parameters.containsKey("regionId")) {
            tariffSearchDTO.regionId(mapToUUID(parameters.get("regionId")));
        }
        if (parameters.containsKey("transportClass")) {
            tariffSearchDTO.transportClass(mapToTransportClass(parameters.get("transportClass")));
        }
        if (parameters.containsKey("active")) {
            tariffSearchDTO.active(mapToBoolean(parameters.get("active")));
        }
        if (parameters.containsKey("isNightTariff")) {
            tariffSearchDTO.isNightTariff(mapToBoolean(parameters.get("isNightTariff")));
        }
        return tariffSearchDTO.build();
    }
    
}
