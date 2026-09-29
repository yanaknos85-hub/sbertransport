package ru.sberbank.ditsib.transport.reports.dto.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;

import jakarta.validation.Valid;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Schema(title = "Фильтры для поиска поездок на каршеринге")
@NoArgsConstructor
public class RequestForCarsharingReportDTO extends RequestReportDTO {
    
    @Schema(description = "Дата завершения поездки, диапазон")
    @Valid
    private DateRange finishedDate;
    
    @Schema(description = "Общий пробег (км), факт")
    private DoubleRange drivingLength;
    
}
