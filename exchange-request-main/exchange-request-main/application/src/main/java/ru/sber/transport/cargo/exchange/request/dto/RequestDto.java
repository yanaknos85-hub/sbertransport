package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Request", description = "Заявка на перевозку груза")
@JsonInclude(JsonInclude.Include.NON_NULL) // игнорировать null значения при сериализации JSON объектов
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDto {

    @Schema(description = "Уникальный идентификатор заявки")
    private UUID id;


    @Schema(description = "Человекочитаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("humanreadableId")
    private String humanReadableId;

    @Schema(description = "Номер заказа в системе грузовладельца", requiredMode = Schema.RequiredMode.REQUIRED)
    private String internalId;

    @Schema(description = "Идентификатор пользователя — владельца заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID ownerId;

    @Schema(description = "Статус заявки: cargo_draft, cargo_published, cargo_archived", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "Флаг использования ЭТрН", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Boolean useEtrn = Boolean.TRUE;

    @Schema(description = "Вид заявки для перевозчиков: fixed (MVP)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String viewType;

    @Schema(description = "Форма оплаты: cash, non_cash", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentForm;

    @Schema(description = "Условия оплаты: prepayment, on_delivery, deferred_payment", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentTerms;

    @Schema(description = "Срок оплаты в днях (1–30), заполняется только при отсрочке")
    private Integer paymentDays;

    @Schema(description = "Дата создания заявки в системе заказчика", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate requestCreated;

    @Schema(description = "ФИО отправителя")
    private String senderFio;

    @Schema(description = "Телефон отправителя")
    private String senderPhone;

    @Schema(description = "ФИО получателя")
    private String recipientFio;

    @Schema(description = "Телефон получателя")
    private String recipientPhone;

    @Schema(description = "Дата и время создания записи", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления")
    private LocalDateTime updatedAt;

    @Schema(description = "Дата автоматического удаления черновика", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresAt;

    @Schema(description = "Дата публикации заявки")
    private LocalDateTime publishedAt;

    @Schema(description = "Дата завершения заявки")
    private LocalDateTime completedAt;

    /**
     * Детали груза — отношение 1:1
     */
    @Schema(description = "Информация о грузе")
    private CargoDetailsDto cargoDetails;

    /**
     * Специальные условия перевозки
     */
    @Schema(description = "Специальные условия, связанные с перевозкой (температура, хрупкость и т.д.)")
    private SpecialConditionsDto specialConditions;

    /**
     * Требования к транспортному средству
     */
    @Schema(description = "Требования к транспортному средству (тип, грузоподъёмность, рефрижератор и т.д.)")
    private VehicleRequirementsDto vehicleRequirements;

    @Schema(description = "Список контрольных точек маршрута, упорядоченных по индексу")
    @Builder.Default
    private List<WaypointDto> waypoints = List.of();

    @Builder.Default
    private List<AttachmentDto> attachments = List.of();

    @Schema(description = "Стоимость заявки", example = "500000.0")
    private Double costRequest;

    @Schema(description = "Флаг включения НДС")
    private Boolean vatInclude;

    @Schema(description = "Организация")
    private UUID organizationId;

    @Schema(description = "Признак владельца выбранного отклика")
    private Boolean selectedReplyOwner;

    @Schema(description = "Признак экспедитора у грузоотправителя")
    private boolean senderForwarder = false;

}


