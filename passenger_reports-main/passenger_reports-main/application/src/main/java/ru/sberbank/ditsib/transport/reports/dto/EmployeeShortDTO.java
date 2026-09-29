package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;


/**
 * Data transfer object with data about existing employee.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткая информация о сотруднике", description = "Данные сотрудника")
public class EmployeeShortDTO {
    
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Identifier (human readable)
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * First name
     */
    @Schema(description = "Имя")
    private String firstName;
    
    /**
     * Lat name
     */
    @Schema(description = "Фамилия")
    private String lastName;

}
