package ru.sberbank.ditsib.transport.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;

@Validated
@RequestMapping("employee")
@Tag(name = "Сотрудник", description = "Набор операций для работы с сотрудниками")
public interface EmployeeController {

    @GetMapping
    @Operation(summary = "Получение сотрудника по Табельному номеру", description = "Получение сотрудника по Табельному номеру")
    EmployeeDto getEmployeeByPersonnelNumber(@RequestParam
                                             @Valid
                                             @NotBlank
                                             @Parameter(description = "Табельный номер сотрудника")
                                             String personnelNumber,

                                             @Parameter(hidden = true) Authentication authentication);
}
