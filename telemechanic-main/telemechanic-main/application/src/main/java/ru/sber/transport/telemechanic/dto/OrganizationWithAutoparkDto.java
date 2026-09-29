package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(title = "Информация об организации и автопарках", description = "Данные организации и автопарках")
public record OrganizationWithAutoparkDto(
        
        @Schema(description = "Наименование организации")
        String organizationName,
        
        @Schema(description = "Список департаментов с аввтопарками")
        List<DepartmentWithAutoparkDto> departmentWithAutoparkDtoList
) {

}

