package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;

import java.util.UUID;

@RequestMapping({"geo","geo/"})
@Tag(name = "Местоположение", description = "Набор операций для работы с местоположением")
public interface GeoDriverController {
    
    /**
     * @param requestId         id заявки
     *
     * @return                  информация о местоположении водителя
     */
    @GetMapping(value = {"driver/{requestId}","driver/{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    GeoDriverDTO getGeoDriverByRequestId(
            @PathVariable("requestId")
            @NotNull UUID requestId);
}
