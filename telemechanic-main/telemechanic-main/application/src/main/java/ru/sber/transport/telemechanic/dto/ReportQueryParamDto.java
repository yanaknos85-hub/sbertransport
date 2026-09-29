package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReportQueryParamDto {
    
    @Schema(description = "Номер заявки")
    private String humanReadableId;
    
    @Schema(description = "Табельный номер")
    private String personnelNumber;
    
    @Schema(description = "Нижняя граница создания заявки")
    private LocalDateTime startCreationTime;
    
    @Schema(description = "Верхняя граница создания заявки")
    private LocalDateTime endCreationTime;
    
    @Schema(description = "Идентификатор организации")
    private String organizationId;
    
    @Schema(description = "Идентификаторы департаментов")
    private Set<UUID> departmentIds;
}
