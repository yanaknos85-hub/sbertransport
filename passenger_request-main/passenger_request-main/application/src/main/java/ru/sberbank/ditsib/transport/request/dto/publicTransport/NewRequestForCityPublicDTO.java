package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;

import java.util.UUID;

/**
 * DTO с данными по новой заявке на компенсацию за <b>городской</b> общественный транспорт
 **/
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(
        title = "DTO с данными по новой заявке на компенсацию за городской общественный транспорт",
        description = "Данные по новой заявке"
)
@Deprecated
public class NewRequestForCityPublicDTO extends NewRequestForPublicDTO {
    
    /**
     * Тип заявки на компенсацию за общественный транспорт
     */
    @Schema(description = "Тип заявки на компенсацию за общественный транспорт")
    @Builder.Default
    private PublicCompensationType compensationType = PublicCompensationType.CITY_TRIP_COMPENSATION;
    
    /**
     * Id of tariff used
     */
    @NotNull
    @Schema(description = "Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Данные о поездке
     */
    @Schema(description = "Данные о поездке")
    @NotNull @Valid
    private PublicTripDataDTO tripData;
    
    /**
     * Данные о тарифе
     */
    @Schema(description = "Данные о тарифе")
    @NotNull @Valid
    private PublicTariffDataDTO tariffData;
}
