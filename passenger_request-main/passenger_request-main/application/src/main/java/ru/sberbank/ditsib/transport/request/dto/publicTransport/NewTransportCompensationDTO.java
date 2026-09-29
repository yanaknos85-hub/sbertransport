package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.request.dto.PublicCompensationTypeDTO;
import ru.sberbank.ditsib.transport.request.dto.PublicTransportTypeDTO;

import java.time.LocalDate;
import java.util.UUID;

@Schema(title = "Новая заявка на компенсацию за общественный транспорт", description = "Данные заявки")
@JsonPropertyOrder({ "id" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class NewTransportCompensationDTO {
    
    /**
     * Тип компенсации
     */
    @NotNull(message = "Не указан вид компенсации")
    @Schema(description = "Вид компенсации", requiredMode = Schema.RequiredMode.REQUIRED)
    private PublicCompensationTypeDTO compensationType;
    
    /**
     * Тип транспорта
     */
    @NotNull(message = "Не указан вид транспорта")
    @Schema(description = "Вид транспорта")
    private PublicTransportTypeDTO transportType;
    
    /**
     * Стоимость билета
     */
    @NotNull(message = "Не указана стоимость билета")
    @Min(value = 0, message = "Минимальная стоимость должна быть больше 0")
    @Schema(description = "Стоимость билета для междугородних поездок", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ticketsCost;
    
    /**
     * Количество билетов
     */
    @Min(value = 1, message = "Количество билетов должно быть больше 1")
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
    @Future(message = "Дата должна быть в будущем")
    @Schema(description = "Дата окончания действия билета")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ticketsExpirationEnd;
    
    /**
     * ID приложенного документа
     */
    @Schema(description = "ID приложенного документа")
    private UUID attachedDocumentId;
    
}
