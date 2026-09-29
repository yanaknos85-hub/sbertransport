package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistrySelfOrganizationRequest;

import java.util.Map;

@Validated
@RequestMapping("medic-request")
@Tag(name = "Контроллер медицинских осмотров", description = "Контроллер для работы с медицинскими осмотрами")
public interface MedicRequestController {
    
    @Operation(summary = "Реестр медицинских осмотров по всем организациям",
               description = "Получение списка реестра медицинских осмотров всех организаций")
    @PostMapping(value = "/report/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Page<Map<String, Object>> getMedicRequestRegistryForAllOrganizations(
            @RequestBody @Valid MedicRequestRegistryAllOrganizationsRequest request
                                                                        );
    
    @Operation(summary = "Реестр медицинских осмотров по своей организации",
               description = "Получение списка реестра медицинских осмотров своей организации")
    @PostMapping(value = "/report/self-organization", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Page<Map<String, Object>> getMedicRequestRegistryForSelfOrganization(
            @RequestBody @Valid MedicRequestRegistrySelfOrganizationRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                                     );
}
