package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;

public interface GeoDriverResolver {
    TaxiExternalIntegrationType getIntegrationType();
    GeoDriverDTO getGeoDriverByRequest(Request request);
}
