package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Статус оплаты заявки для обновления", description = "Статус оплаты заявки для обновления")
@Builder
public class PaymentStateRequestDTO {
    @NotNull
    @Schema(description = "Идентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID requestId;
    @NotNull
    @Schema(description = "Признак оплаты", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean payed;
}
