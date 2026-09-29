package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.UUID;

@Schema(title = "Заявка на линии", description = "Данные заявки на линии")
public record RequestOnTheLineDto(
        @Schema(description = "Тип клиентского пути", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean ewbPath,
        @Schema(description = "Идентификатор ЭПЛ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID ewbId,
        @Schema(description = "Идентификатор заявки телемеханика", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID requestId,
        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        RequestStatus requestStatus,
        @Positive
        @Schema(description = "Пробег при выезде", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer odometerOut,
        @Schema(description = "Остаток топлива при выезде", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer fuelLitreageOut,
        @Schema(description = "Наличие QR-кода", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean qrCode,
        @Schema(description = "Транспортное средство", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        TransportOnTheLineDto transport
) {
    @Schema(name = "TransportOnTheLineDto", title = "Транспорт на линии", description = "Транспорт на линии")
    public record TransportOnTheLineDto(
            @NotNull(message = "ID транспортного средства не задан")
            @Schema(description = "Идентификатор транспортного средства", requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            UUID id,
            @NotBlank(message = "Государственный номер транспортного средства должен быть задан")
            @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                     message = "Регистрационный знак не прошел проверку")
            @Schema(description = "Номер транспортного средства", requiredMode = Schema.RequiredMode.REQUIRED,
                    pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                    example = "А777АА777")
            String stateNumber,
            @Size(max = 255, message = "Марка транспортного средства не должна превышать 255 символов")
            @NotBlank(message = "Марка транспортного средства не задана")
            @Schema(description = "Марка транспортного средства", requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "Mazda")
            String brand,
            @Size(max = 255, message = "Модель транспортного средства не должна превышать 255 символов")
            @NotBlank(message = "Модель транспортного средства не задана")
            @Schema(description = "Модель транспортного средства", requiredMode = Schema.RequiredMode.REQUIRED,
                    example = "cx-5")
            String model,
            @NotNull
            @Schema(description = "Объем топливного бака", requiredMode = Schema.RequiredMode.REQUIRED)
            int fuelTankVolume
    ) {
    }
}
