package ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.LocationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.OrganizationRequestDto;

import java.util.List;
import java.util.UUID;

@Schema(description = "DTO для изменения Транспортного средства")
@With
public record TransportUpdateDto(
        @Schema(description = "Организации", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        List<OrganizationRequestDto> organizations,

        @Schema(description = "Расположение", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        LocationRequestDto location,

        @Schema(description = "Комментарий",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Комментарий",
                nullable = true,
                maximum = "255")
        @Size(max = 255, message = "Комментарий не должен превышать 255 символов")
        String comment,

        @Schema(description = "Должность для закрепления ТС", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID accessiblePositionId,

        @Schema(description = "Автомобиль", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        VehicleUpdateDto vehicle,

        @Schema(description = "Документы", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        DocumentsUpdateDto documents,

        @Schema(description = "Идентификатор контрагента (автопарка)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID contractorId,

        @Schema(description = "Идентификатор филиала автопарка", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID autoparkId,

        @Schema(description = "Балансовая единица",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "0001",
                maxLength = 4)
        @NotBlank(message = "Балансовая единица не может быть пустой")
        @Size(max = 4, message = "Балансовая единица не должна превышать 4 символов")
        String balanceUnitNumber,

        @Schema(description = "Завод",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "1234",
                maxLength = 4)
        @NotBlank(message = "Завод не может быть пустым")
        @Size(max = 4, message = "Завод не должен превышать 4 символов")
        String facility,

        @Schema(description = "Единица оборудования",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "EQ001234567890",
                maxLength = 30)
        @NotBlank(message = "Единица оборудования не может быть пустой")
        @Size(max = 30, message = "Единица оборудования не должна превышать 30 символов")
        String equipmentUnitSystemNumber
) {

}
