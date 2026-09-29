package ru.sber.transport.telemechanic.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;

/**
 * Контроллер по владельцам автопарков
 */
@RequestMapping("fleet-owner-organizations")
@Tag(name = "Организация владельцев автопарков", description = "Контроллер для работы с организациями владельцев автопарков")
public interface FleetOwnerOrganizationController {
    
    /**
     * Получение списка активных организаций владельцев автопарков
     *
     * @return {@link List <GetAllActiveOrganizationNamesDto>}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение", description = "Получение списка организаций владельцев автопарков")
    List<GetAllActiveOrganizationNamesDto> getFleetOwnerOrganizations();
}
