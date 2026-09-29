package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.With;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.UUID;

@With
@Schema(name = "CreatedRequestDto", title = "Ответ на запрос создания пустой заявки")
public record CreatedRequestDto(
        @NotNull
        @Schema(description = "Идентификатор заявки")
        UUID id,
        @NotNull
        @Schema(description = "Статус заявки")
        RequestStatus requestStatus,
        @NotNull
        @Schema(description = "Информация о транспорте")
        TransportDto transport,
        @Schema(description = "Проверки")
        ChecksTreeDto.ChecksTree checks
) {
    
    @Schema(name = "CreatedRequestDto.TransportDto", title = "Транспорт")
    public record TransportDto(
            @NotNull
            @Schema(description = "Идентификатор транспорта")
            UUID id,
            @NotBlank
            @Schema(description = "Государственный номер транспорта")
            String stateNumber,
            @NotBlank
            @Schema(description = "Марка транспорта")
            String brand,
            @NotBlank
            @Schema(description = "Модель транспорта")
            String model
    ) {
    }
}
