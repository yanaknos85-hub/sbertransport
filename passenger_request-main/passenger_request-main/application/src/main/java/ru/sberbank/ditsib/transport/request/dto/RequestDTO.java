package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@JsonPropertyOrder({ "id" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title = "Заявка (Запись)", description = "Данные заявки")
@AllArgsConstructor
public class RequestDTO extends NewRequestDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * Human readable id
     */
    @NotNull
    @Schema(description = "Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableId;
    
    /**
     * Approvers identifier
     */
    @Schema(description = "Согласующий")
    private EmployeeDTO approvedBy;
    
    /**
     * Status of approval
     */
    @Schema(description = "Статус согласования")
    private ApprovalState approvalState;
    
    /**
     * Status of request
     */
    @Schema(description = "Статус заявки")
    private TripRequestStatus status;
    
    @Schema(description = "Дата создания")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    @Schema(description = "Используемый в заявке личный автомобиль")
    private PersonalCarDTO personalCar;
    
    @Schema(description = "Произвольное описание работ . Например: [#ЗАКАЗА], [АВТО МАРКА], [АВТО ЦВЕТ], [АВТО РЕГ " +
                          "НОМЕР], [ФИО ВОДИТЕЛЯ], [КОНТАКТЫЙ ТЕЛЕФОН], заполняется после назначения водителя на " +
                          "поездку")
    private String resolution;
}
