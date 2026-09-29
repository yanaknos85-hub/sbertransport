package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для созданной заявки на компенсацию за проездные документы / карты
 */
@JsonPropertyOrder({ "id" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Schema(title = "DTO для созданной заявки на компенсацию за проездные документы / карты", description = "Данные заявки")
@Deprecated
public class RequestForPublicTravelCardDTO extends NewRequestForPublicTravelCardDTO {
    
    /**
     * Идентификатор
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Идентификатор (человекочитаемый)
     */
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    /**
     * Согласующий
     */
    @Schema(description = "Согласующий")
    private EmployeeDTO approvedBy;
    
    /**
     * Статус согласования
     */
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
    
    /**
     * Статус заявки
     */
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    /**
     * Дата и время подтверждения поездки
     */
    @Schema(description = "Дата и время подтверждения поездки")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tripConfirmationDate;
    
    /**
     * Дата и время создания
     */
    @Schema(description = "Дата и время создания")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
}
