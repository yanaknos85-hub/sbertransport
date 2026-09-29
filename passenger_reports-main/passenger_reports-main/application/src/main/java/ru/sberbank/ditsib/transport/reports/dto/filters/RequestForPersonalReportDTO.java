package ru.sberbank.ditsib.transport.reports.dto.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "Фильтры для поиска поездок на личном транспорте")
@SuperBuilder
@NoArgsConstructor
public class RequestForPersonalReportDTO extends RequestReportDTO {
    
    @Schema(description = "Поля для флага \"С учетом отображаемых полей\"")
    private PersonalUIVisibilityDTO personalUIVisibilityDTO;
    
    @Schema(description = "Дата формирование приказа на выплату")
    @Valid
    private DateRange orderPaymentFormationFinishingDate;
    
    @Schema(description = "Желаемая дата поездки, диапазон")
    @Valid
    private DateRange desiredDateRange;
    
    @Schema(description = "Дата утверждения поездки (Дата начала формирования приказа на выплату), диапазон")
    @Valid
    private DateRange orderPaymentFormationStartRange;
    
    @Schema(description = "Флаг совместной поездки")
    private Boolean coopTrip;
    
    @Schema(description = "Признак пассажира")
    private Boolean passenger;
    
    @Schema(description = "ID совместной поездки")
    private UUID sharedRideId;
    
    @Schema(description = "Дата согласования поездки, диапазон")
    private DateRange approveDate;
    
    @Schema(description = "Период выплаты")
    @Min(value = 1, message = "Период выплаты от 1 до 4")
    @Max(value = 4, message = "Период выплаты от 1 до 4")
    private Integer paymentPeriod;
    
    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return isEmpty(RequestForPersonalReportDTO.class.getDeclaredFields()) && super.isEmpty();
    }
    
}
