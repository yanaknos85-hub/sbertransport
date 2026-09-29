package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Лимит", description = "Лимит")
public class LimitDTO {

    @Schema(description = "идентификатор")
    private UUID id;
    
    @Schema(description = "идентификатор человекочитаемый")
    private String humanReadableId;
    
    @Schema(description = "идентификатор отдела")
    private UUID departmentId;
}
