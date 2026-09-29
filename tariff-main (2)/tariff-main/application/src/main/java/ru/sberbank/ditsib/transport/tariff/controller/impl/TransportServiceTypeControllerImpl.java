package ru.sberbank.ditsib.transport.tariff.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.tariff.dto.TransportServiceTypeDTO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер со списком контсант
 */
@RequestMapping({"transport-service-type","transport-service-type/"})
@Tag(name = "Виды транспортных услуг", description = "Виды транспортных услуг")
@RequiredArgsConstructor
@RestController
public class TransportServiceTypeControllerImpl {

    /**
     * Запрос списка допустимых статусов
     *
     * @return список допустимых статусов
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех видов транспортных услуг", description = "Получение всех видов транспортных услуг")
    public List<TransportServiceTypeDTO> getAll() {
        return Arrays.stream(TransportServiceType.values())
                     .map(elt -> TransportServiceTypeDTO.builder()
                                                     .name(elt.toString())
                                                     .rusName(elt.getDescription())
                                                     .build())
                     .collect(Collectors.toList());
    }
}