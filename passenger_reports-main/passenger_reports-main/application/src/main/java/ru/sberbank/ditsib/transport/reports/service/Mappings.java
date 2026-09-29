package ru.sberbank.ditsib.transport.reports.service;

import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.IVisibilityDto;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.util.Map;

@Component
public interface Mappings<T extends IVisibilityDto> {
    
    default Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get() {
        return get(null);
    }
    
    Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get(T dto);
    
    TransportTypeEnum transportType();
}
