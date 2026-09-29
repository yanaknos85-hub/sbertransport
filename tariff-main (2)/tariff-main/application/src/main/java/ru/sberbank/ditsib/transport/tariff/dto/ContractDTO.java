package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.RestrictionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * DTO контракта
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Контракт внесение",
        description = "Контракт внесение")
public class ContractDTO {

    @Schema(description = "Контрагент", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID contractorId;

    @Schema(description = "Организация", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UUID> organizationIds;

    @Schema(description = "Регионы действия. Будет проставлено обязательным через 2 релиза (2021-05-28)")
    @Builder.Default
    private Set<UUID> regionIds = new HashSet<>();

    @Deprecated
    @Schema(description = "Регион действия. Устаревшее, будет удалено через 2 релиза (2021-05-28). " +
            "Установите `regionId`", deprecated = true)
    private String region;

    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;

    @NotNull
    @Schema(description = "Сумма", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sum;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Schema(description = "Время начала", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Schema(description = "Время окончания")
    private LocalDate endDate;

    @NotNull
    @Schema(description = "Номер контракта", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contractNumber;

    @Pattern(regexp = "^[0-9]{1,12}$", message = "Номер договора УВХД должен состоять из чисел и быть не более 12 символов.")
    @Schema(description = "Номер договора УВХД")
    private String uvhd;

    @Schema(description = "Включить НДС")
    private boolean includeVat;

    @Schema(description = "Значение НДС")
    private Integer vatValue;

    @Schema(description = "Тип ограничения связи")
    private RestrictionType restrictionType = RestrictionType.NONE;

    @Schema(description = "Идентификаторы ограничиваемых объектов")
    private List<UUID> restrictedIds = new LinkedList<>();

    @Builder.Default
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    @Schema(description = "Штраф за опоздание водителей ко времени подачи")
    private BigDecimal driverLatePickupPenalty = BigDecimal.valueOf(0.1);

    @Builder.Default
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    @Schema(description = "Штраф за ненадлежащее качество услуг")
    private BigDecimal poorServiceQualityPenalty = BigDecimal.valueOf(0.1);

    @Builder.Default
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    @Schema(description = "Штраф за отмену заявок водителями")
    private BigDecimal driverOrderCancellationPenalty = BigDecimal.valueOf(0.1);

    @Schema(description = "Id ответственного за договор")
    private UUID responsibleEmployeeId;

    @Schema(description = "ID организации, которая будет производить оплату")
    private UUID paymentOrganizationId;
}
