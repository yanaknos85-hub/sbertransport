package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnegative;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о новом автопарке", description = "Данные нового автопарка")
public class NewAutoparkDTO {

    /**
     * Название автопарка
     */
    @NotEmpty
    @Size(max = 255)
    @Schema(description = "Название автопарка")
    private String name;

    /**
     * Идентификатор филиала во внешней системе для маршрутизации поездок
     */
    @Schema(description = "Идентификатор филиала во внешней системе для маршрутизации поездок")
    private UUID routingId;

    /**
     * Нормативное количество автомобилей в филиале
     */
    @Nonnegative
    @Schema(description = "Нормативное количество автомобилей в филиале")
    private Integer vehicleCountNorm;
}
