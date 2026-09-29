package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("medical-license")
@Tag(name = "Работа со справочником медиков", description = "Контроллер для работы со справочником медиокв")
public interface OrganizationMedicalLicenseController {
    
    @GetMapping
    @Operation(summary = "Проверка данных о медике и лицензии", description = "Проверка данных о медике и лицензии")
    void checkMedicalLicense(@Parameter(hidden = true) Authentication authentication);
}
