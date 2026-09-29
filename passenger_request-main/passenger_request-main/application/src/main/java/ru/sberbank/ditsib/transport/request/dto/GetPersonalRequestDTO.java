package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки для личного транспорта")
@SuperBuilder
public class GetPersonalRequestDTO extends GetRequestDTO {
    /**
     * ID of personal car
     */
    @Schema(description = "Идентификатор личного транспорта", deprecated = true)
    private UUID personalCarId;
    
    /**
     * Personal car
     */
    @Schema(description = "Транспортное средство", requiredMode = Schema.RequiredMode.REQUIRED)
    private PersonalCarDTO personalCar;
    
    /**
     * Дата утверждения
     */
    @Schema(description = "Дата утверждения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime orderPaymentFormationStartDate;
    
    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    @Schema(description = "Количество присоединившихся пассажиров (заявок)")
    private Integer numberPassengersJoined;
    
    @Schema(description = "Идентификатор связной заявки")
    private List<UUID> payRequestIds;
    
    /**
     * Сумма доплаты за всех пассажиров в копейках
     */
    @Schema(description = "Сумма доплаты за всех пассажиров, копеек")
    private Long additionalSum;
}