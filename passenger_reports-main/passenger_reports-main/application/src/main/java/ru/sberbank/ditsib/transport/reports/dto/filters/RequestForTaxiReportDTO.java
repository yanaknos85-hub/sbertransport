package ru.sberbank.ditsib.transport.reports.dto.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Schema(title = "Фильтры для поиска поездок на такси")
public class RequestForTaxiReportDTO extends RequestReportDTO {
    @Schema(description = "ID совместной поездки")
    private UUID sharedRideId;
    
    @Schema(description = "Флаг совместной поездки")
    private Boolean coopTrip;
    
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
    
    @Schema(description = "Инициатор совместной поездки")
    private String sharedRideOwnerFIO;
    
    @Schema(description = "Вид тарифа")
    private List<String> tripClass;
    
    @Schema(description = "Статус оплаты")
    private String registryFactPayment;
    
    @Override
    public boolean isEmpty() {
        return isEmpty(RequestForTaxiReportDTO.class.getDeclaredFields()) && super.isEmpty();
    }
    
}
