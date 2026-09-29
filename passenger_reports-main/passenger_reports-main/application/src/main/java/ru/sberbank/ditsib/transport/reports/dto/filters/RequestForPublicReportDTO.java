package ru.sberbank.ditsib.transport.reports.dto.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.reports.dto.PublicUIVisibilityDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Schema(title = "Фильтры для поиска поездок на общественном транспорте")
@NoArgsConstructor
public class RequestForPublicReportDTO extends RequestReportDTO {
    @Schema(description = "Поля для флага \"С учетом отображаемых полей\"")
    private PublicUIVisibilityDTO publicUIVisibilityDTO;
    
    @Schema(description = "Тип компенсации")
    private Set<PublicCompensationType> compensationType;
    
    @Schema(description = "Тип транспорта")
    private Set<PublicTransportType> publicTransportType;
    
    @Schema(description = "Дата формирование приказа на выплату")
    @Valid
    private DateRange orderPaymentFormationFinishingDate;
    
    @Schema(description = "Желаемая дата поездки, диапазон")
    @Valid
    private DateRange desiredDateRange;
    
    @Schema(description = "Дата утверждения поездки (Дата начала формирования приказа на выплату), диапазон")
    @Valid
    private DateRange orderPaymentFormationStartDate;
    
    @Schema(description = "Дата согласования поездки, диапазон")
    @Valid
    private DateRange approveDate;
    
    @Schema(description = "Период выплаты")
    @Min(value = 1, message = "Период выплаты от 1 до 4")
    @Max(value = 4, message = "Период выплаты от 1 до 4")
    private Integer paymentPeriod;
    
    @Schema(description = "Есть вложение")
    private Boolean publicCompensationDocumentExist;
    
    @Override
    public boolean isEmpty() {
        return isEmpty(RequestForPublicReportDTO.class.getDeclaredFields()) && super.isEmpty();
    }
    
}