package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO с данными по публикуемому тарифу пешком(!sic)
 */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу пешком(!sic)", description = "Данные по тарифу")
public class NewWalkTariffDTO extends NewBaseTariffDto {
}
