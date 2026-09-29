package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.*;

import jakarta.validation.Valid;

@RequestMapping
@Tag(name = "Пользовательские атрибуты", description = "Набор операций для работы с пользовательскими атрибутами")
public interface UserAttributesController {
    
    @GetMapping(value = {"attributes","attributes/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение ui атрибутов пользователя")
    UserAttributesDTO getUserAttributes(@Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    @PutMapping(value = {"attributes/taxi","attributes/taxi/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение", description = "Изменение taxi ui атрибутов пользователя")
    UserAttributesDTO editTaxiUserAttributes(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Valid @RequestBody TaxiUIVisibilityDTO newData
                                            );
    
    @PutMapping(value = {"attributes/personal","attributes/personal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение", description = "Изменение personal ui атрибутов пользователя")
    UserAttributesDTO editPersonalUserAttributes(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Valid @RequestBody PersonalUIVisibilityDTO newData
                                                );
    
    @PutMapping(value = {"attributes/public","attributes/public/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение", description = "Изменение public ui атрибутов пользователя")
    UserAttributesDTO editPublicUserAttributes(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Valid @RequestBody PublicUIVisibilityDTO newData
                                              );
    
    @PutMapping(value = {"attributes/carsharing","attributes/carsharing/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение", description = "Изменение carsharing ui атрибутов пользователя")
    UserAttributesDTO editCarsharingUserAttributes(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Valid @RequestBody CarsharingUIVisibilityDTO newData
                                                  );
    
    @GetMapping(value = {"default/attributes","default/attributes/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение атрибутов пользователя со значениями по умолчанию")
    UserAttributesDTO getDefaultUserAttributes();
    
}
