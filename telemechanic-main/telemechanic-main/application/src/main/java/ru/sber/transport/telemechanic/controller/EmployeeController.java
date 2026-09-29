package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sber.transport.telemechanic.dto.medic.GetMedicDto;

import java.util.List;

@Validated
@RequestMapping("employee")
@Tag(name = "Сотрудник", description = "Набор операций для работы с сотрудниками")
public interface EmployeeController {
    
    @GetMapping
    @Operation(summary = "Получение данных сотрудника по ФИО", description = "Получение данных сотрудника по ФИО")
    List<GetMedicDto> getMedicByFIO(@RequestParam
                                    @Valid
                                    @NotBlank
                                    @Parameter(description = "ФИО")
                                    String fio);
}
