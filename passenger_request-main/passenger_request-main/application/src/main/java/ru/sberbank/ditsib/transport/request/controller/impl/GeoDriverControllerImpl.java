package ru.sberbank.ditsib.transport.request.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.request.controller.GeoDriverController;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;
import ru.sberbank.ditsib.transport.request.service.GeoService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@E2EController
public class GeoDriverControllerImpl implements GeoDriverController {
    
    private final GeoService geoService;
    
    /**
     * @param requestId      id заявки
     * @return информация о местоположении водителя
     */
    @Override
    public GeoDriverDTO getGeoDriverByRequestId(UUID requestId) {
        var userId = ControllerUtils.currentUser();
        return geoService.getGeoDriverByRequestId(requestId, userId);
    }
}
