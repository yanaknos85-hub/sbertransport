package ru.sberbank.ditsib.transport.reports.dto.publicTransport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Schema(title = "Заявка на компенсацию за общественный транспорт", description = "Данные заявки")
@JsonPropertyOrder({ "id" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TransportCompensationDTO {
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    
    /**
     * Тип компенсации
     */
    @NotNull
    @Schema(description = "Тип компенсации", requiredMode = Schema.RequiredMode.REQUIRED)
    private PublicCompensationType compensationType;
    
    /**
     * Тип транспорта
     */
    @Schema(description = "Тип транспорта")
    private PublicTransportType transportType;
    
    /**
     * Стоимость билета
     */
    @NotNull
    @Min(0)
    @Schema(description = "Стоимость билета для междугородних поездок", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ticketsCost;
    
    /**
     * Количество билетов
     */
    @Min(1)
    @Schema(description = "Количество билетов")
    @Builder.Default
    private Integer ticketsCount = 1;
    
    /**
     * Дата начала действия билета
     */
    @Schema(description = "Дата начала действия билета")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ticketsExpirationStart;
    
    /**
     * Дата окончания действия билета
     */
    @Future
    @Schema(description = "Дата окончания действия билета")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ticketsExpirationEnd;
    
    @JsonIgnore
    @Schema(description = "Ид заявки")
    private UUID requestId;
    
    /**
     * ID приложенного документа
     */
    @Schema(description = "ID приложенного документа")
    private UUID attachedDocumentId;
    
    /**
     * Код вида оплаты
     */
    @Schema(description = "Код вида оплаты")
    @Builder.Default
    private PaymentTypeCode paymentTypeCode = PaymentTypeCode.CODE_4666;
    
    public TransportCompensationDTO(
            UUID id, String compensationType, String transportType, Integer ticketsCost, Integer ticketsCount,
            LocalDate ticketsExpirationStart, LocalDate ticketsExpirationEnd, UUID requestId, UUID attachedDocumentId
                                   ) {
        this.id = id;
        this.compensationType = PublicCompensationType.valueOf(compensationType);
        this.transportType = PublicTransportType.valueOf(transportType);
        this.ticketsCost = ticketsCost;
        this.ticketsCount = ticketsCount;
        this.ticketsExpirationStart = ticketsExpirationStart;
        this.ticketsExpirationEnd = ticketsExpirationEnd;
        this.requestId = requestId;
        this.attachedDocumentId = attachedDocumentId;
    }
}