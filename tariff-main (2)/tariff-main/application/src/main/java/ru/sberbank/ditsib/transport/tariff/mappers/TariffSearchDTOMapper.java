package ru.sberbank.ditsib.transport.tariff.mappers;

import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TransportClass;

import java.util.Map;
import java.util.UUID;

public interface TariffSearchDTOMapper {
    TariffSearchDTO mapFromParameters(Map<String, ?> parameters);
    
    default TransportTypeEnum mapToTransportTypeEnum(Object value) {
        if (value == null) {
            return null;
        }
        return TransportTypeEnum.valueOf((String) value);
    }
    
    default TransportServiceType mapToTransportServiceType(Object value) {
        if (value == null) {
            return null;
        }
        return TransportServiceType.valueOf((String) value);
    }
    
    default UUID mapToUUID(Object value) {
        if (value == null) {
            return null;
        }
        return UUID.fromString((String) value);
    }
    
    default String mapToString(Object value) {
        if (value == null) {
            return null;
        }
        return (String) value;
    }
    
    default TransportClass mapToTransportClass(Object value) {
        if (value == null) {
            return null;
        }
        return TransportClass.valueOf((String) value);
    }
    
    default Boolean mapToBoolean(Object value) {
        if (value == null) {
            return null;
        }
        return Boolean.valueOf((String) value);
    }
}
