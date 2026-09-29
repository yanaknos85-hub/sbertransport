package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CargoDetails", description = "Информация о грузе")
@JsonIgnoreProperties(ignoreUnknown=true)
public class CargoDetailsDto {

    @Schema(description = "Уникальный идентификатор записи о грузе")
    private UUID id;

    @Schema(description = "Вес груза в килограммах. Должен быть больше 0. Ограничения: min=1, max=20000 кг.")
    private BigDecimal weightKg;

    @Schema(description = "Объём груза в кубометрах. Должен быть больше 0. Ограничения: min=1, max=90 м³.")
    private BigDecimal volumeM3;

    @Schema(description = "Объявленная стоимость груза в рублях. Максимальное значение: 1 000 000 000 руб.")
    private BigDecimal declaredValue;

    @Schema(description = "Длина груза в метрах. Ограничения: min=2, max=13 м.")
    private BigDecimal length;

    @Schema(description = "Ширина груза в метрах.")
    private BigDecimal width;

    @Schema(description = "Высота груза в метрах.")
    private BigDecimal height;

    @Schema(description = "Тип груза (ссылка на справочник cargo_type.id). Значение выбирается из справочника.")
    private List<String> cargoType;

    @Schema(description = "Тип упаковки (ссылка на справочник packages_type.id). Значение выбирается из справочника.")
    private List<String> cargoPackage;

    @Schema(description = "Метод определения массы груза")
    private List<String> methodDeterminingMass;

    @Builder.Default
    @Schema(description = "Количество грузовых мест. Должно быть больше 0. По умолчанию — 1.")
    private Integer occupiedPlacesCount = 1;

    @Builder.Default
    @Schema(description = "Вид тары (например, коробка, паллета, контейнер). Значение выбирается из справочника. По умолчанию — [\"00\"].")
    private List<String> typeOfContainer = List.of("00");
}

