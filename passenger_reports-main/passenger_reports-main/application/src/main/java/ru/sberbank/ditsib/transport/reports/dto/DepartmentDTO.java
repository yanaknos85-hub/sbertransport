package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Подразделения", description = "Информация о заполненных подразделенияx")
public class DepartmentDTO {
    
    @Schema(description = "Подразделения 1го уровня")
    private List<String> department1;
    
    @Schema(description = "Подразделения 2го уровня")
    private List<String> department2;
    
    @Schema(description = "Подразделения 3го уровня")
    private List<String> department3;
    
    @Schema(description = "Подразделения 4го уровня")
    private List<String> department4;
    
    @Schema(description = "Подразделения 5го уровня")
    private List<String> department5;
    
    @Schema(description = "Уровень запрашиваемого департамента", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    @Max(6)
    private int departmentLevel;
}
