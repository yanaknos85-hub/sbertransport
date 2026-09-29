package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;

import java.util.UUID;

public interface GeoService {
    GeoDriverDTO getGeoDriverByRequestId(UUID requestId, UUID userId);
}
