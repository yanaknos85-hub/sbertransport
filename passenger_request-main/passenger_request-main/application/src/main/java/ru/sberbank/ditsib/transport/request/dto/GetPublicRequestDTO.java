package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.TransportCompensationDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Заявка (Чтение)", description = "Данные заявки на публичный транспорт")
@SuperBuilder
public class GetPublicRequestDTO extends GetRequestDTO {
    
    /**
     * Заявки на компенсацию
     */
    @NotNull
    @Schema(description = "Список транспортных затрат", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@Valid TransportCompensationDTO> transportCompensation;
    
    /**
     * Дата утверждения
     */
    @Schema(description = "Дата утверждения")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime orderPaymentFormationStartDate;
    
    @Schema(description = "Идентификатор связной заявки")
    private List<UUID> payRequestIds;
}
