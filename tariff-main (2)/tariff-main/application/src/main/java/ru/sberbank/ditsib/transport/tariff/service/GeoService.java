package ru.sberbank.ditsib.transport.tariff.service;

import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.model.WaypointDTO;

import java.util.List;

public interface GeoService {
    
    TariffDescriptor.CalculateResponse getRoute(List<WaypointDTO> waypoints);
    
}
