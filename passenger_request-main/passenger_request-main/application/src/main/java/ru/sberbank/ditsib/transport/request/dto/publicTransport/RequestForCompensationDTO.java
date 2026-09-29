package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.dto.fraud.FraudCommentDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO с данными по новой заявке на компенсацию за общественный транспорт
 **/
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Компенсация за проезд",description = "Заявка на компенсацию за общественный транспорт")
public class RequestForCompensationDTO extends NewRequestForPublicDTO {
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
     * Текстовое описание кода статуса заявки
     */
    @Schema(description = "Текстовое описание кода статуса заявки")
    private String statusCodeDescription;
    
    /**
     * Дата создания
     */
    @Schema(description = "Дата создания")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationTime;
    
    /**
     * Дата и время подтверждения поездки
     */
    @Schema(description = "Дата и время подтверждения поездки")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tripConfirmationDate;

    /**
     * Заявки на компенсацию
     */
    @NotNull
    @Schema(description = "Список транспортных затрат", requiredMode = Schema.RequiredMode.REQUIRED)
    List<TransportCompensationDTO> transportCompensation;

    /**
     * Список документов для подтверждения оплаты
     */
    @Schema(description = "Список документов для подтверждения оплаты")
    private List<CompensationDocumentDTO> compensationDocuments;

    @Schema(description = "Информация о фроде. Заполняется только если есть подозрение на фрод")
    private List<FraudCommentDTO> fraudComment;

}
