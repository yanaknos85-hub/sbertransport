package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(title = "Информация об организации и подразделениях", description = "Данные организации и подразделений")
public record OrganizationWithDepartmentDto (
        
        @Schema(description = "Наименование организации")
        String officialName,
        
        @Schema(description = "Список департаментов")
        List<DepartmentDto> departmentDtoList
) {

}

