package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.database.model.RestrictionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Контракт получение",
        description = "Контракт получение")
public class GetContractDTO {

    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @NotNull
    @Schema(description = "Контрагент", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractorId;

    @NotNull
    @Schema(description = "Наименование контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractorName;

    @NotNull
    @Schema(description = "Организации", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UUID> organizationIds;

    @NotNull
    @Schema(description = "Наименования организация", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> organizationNames;

    @Deprecated
    @NotNull
    @Schema(description = "Регион действия. Актуальная информация по региону переезжает в `regionId`. Это поле будет " +
            "удалено через 2 релиза (2021-05-28)", deprecated = true)
    private String region;

    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;

    @NotNull
    @Schema(description = "Сумма", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sum;

    @NotNull
    @Schema(description = "Пользователь", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;

    @NotNull
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    @Schema(description = "Время создания", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime creationTime;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Schema(description = "Время начала", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Schema(description = "Время окончания", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @NotNull
    @Schema(description = "Флаг активности", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;

    @Schema(description = "Присутствуют привязанные тарифы")
    private boolean tariffsExist;

    @NotNull
    @Schema(description = "Номер контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractNumber;

    @Schema(description = "Номер договора УВХД")
    private String uvhd;

    @NotNull
    @Schema(description = "Включить НДС")
    private boolean includeVat;

    @Schema(description = "Значение НДС")
    private Integer vatValue;

    @Schema(description = "Регионы заключения договора", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Set<UUID> regionIds = new HashSet<>();

    @Schema(description = "Тип договора")
    private ContractType contractType;

    @Schema(description = "Тип ограничения связи")
    private RestrictionType restrictionType;

    @Schema(description = "Идентификаторы ограничиваемых объектов")
    private List<UUID> restrictedIds;

    @Schema(description = "Человекочитаемые идентификаторы связанных по геозоне договоров")
    private List<String> connectedContracts;

    @NotNull
    @Schema(description = "Штраф за опоздание водителей ко времени подачи")
    private BigDecimal driverLatePickupPenalty;

    @NotNull
    @Schema(description = "Штраф за ненадлежащее качество услуг")
    private BigDecimal poorServiceQualityPenalty;

    @NotNull
    @Schema(description = "Штраф за отмену заявок водителями")
    private BigDecimal driverOrderCancellationPenalty;

    @NotNull
    @Schema(description = "Id ответственного за договор")
    private UUID responsibleEmployeeId;

    @NotNull
    @Schema(description = "ФИО ответственного за договор")
    private String responsibleEmployeeName;

    @Schema(description = "ID организации, которая будет производить оплату")
    private UUID paymentOrganizationId;
}
