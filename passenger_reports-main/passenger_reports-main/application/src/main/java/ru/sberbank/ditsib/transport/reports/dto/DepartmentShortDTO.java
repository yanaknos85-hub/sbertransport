package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткая информация о подразделении", description = "Данные подразделения")
public class DepartmentShortDTO {
    
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    @NotNull
    @Schema(description = "Код подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
    
    @Schema(description = "Название")
    private String departmentName;
    
    @Schema(description = "Родительское подразделение")
    private DepartmentShortDTO parent;
    
    public DepartmentShortDTO(UUID id, String code, String departmentName)  {
        this.id = id;
        this.code = code;
        this.departmentName = departmentName;
    }
}
