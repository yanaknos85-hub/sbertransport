package ru.sberbank.ditsib.transport.reports.dto.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Schema(title = "Фильтры для поиска поездок на каршеринге")
@NoArgsConstructor
public class RequestForGroupTransferReportDTO extends RequestReportDTO {
    
    @Schema(description = "Список классов группового трансфера")
    private List<GroupTransferClass> groupTransferClassList;
    
    @Schema(description = "Стоимость поездки, диапазон")
    private IntegerRange factCost;
    
    @Schema(description = "Стоимость поездки, диапазон")
    private DoubleRange factDistance;
    
    @Schema(description = "Фактическа дата поездки (диапазон)")
    @Valid
    private DateRange actualDepartureDate;
    
    @Schema(description = "Желаемая дата поездки, диапазон")
    @Valid
    private DateRange desiredDateRange;
    
    @Schema(description = "Общее время ожидания, диапазон")
    private DurationRange waypointWaitTime;
    
    @Schema(description = "Состояние нарушения контрольного срока")
    private Boolean deadlineState;
    
    @Schema(description = "Список регионов")
    private List<UUID> regions;
    
}
