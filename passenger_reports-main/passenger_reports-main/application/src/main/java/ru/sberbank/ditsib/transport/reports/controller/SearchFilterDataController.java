package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentDTO;
import ru.sberbank.ditsib.transport.reports.dto.RequestStatusCodeListDTO;
import ru.sberbank.ditsib.transport.reports.model.TransportTypeEnum;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequestMapping
@Validated
@Tag(name = "Заявки", description = "Поиск заявок организации")
public interface SearchFilterDataController {
    
    /**
     * Получение списка подразделений по идентификатору организации и возможным родительским подразделеням
     *
     * @return список заявок
     */
    @PostMapping(value = {"{organizationId}/department","{organizationId}/department/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Список подразделений", description = "Поиск поздразделений по идентификатору организации и названия родительских " +
                                                               "подразделений")
    List<String> getDepartmentList(
            @RequestBody @Valid DepartmentDTO departmentDTO,
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                  );
    
    /**
     * Получение списка причин отмены заявки, по типу транспорта.
     *
     * @return список причин отмены
     */
    @PostMapping(value = {"{transportType}/statusCode","{transportType}/statusCode/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Список причин отмены", description = "Поиск всех причин отмены заявки для конкретного типа транспорта")
    RequestStatusCodeListDTO getRequestStatusCodeList(
            @PathVariable("transportType") TransportTypeEnum transportType,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                     );
    
}
