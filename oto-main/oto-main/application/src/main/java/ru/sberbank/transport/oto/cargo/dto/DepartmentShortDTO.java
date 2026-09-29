package ru.sberbank.transport.oto.cargo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data transfer object with data about existing department.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткая информация о подразделении", description = "Данные подразделения")
public class DepartmentShortDTO {
    

    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    /**
     * Name of department
     */
    @Schema(description = "Название")
    private String departmentName;

}
