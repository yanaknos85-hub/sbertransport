package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Ответ на изменение статуса оплаты заявки", description = "Ответ на изменение статуса оплаты заявки")
public class PaymentStateResponseDTO {
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;
    @Schema(description = "Статус заявки")
    private String status;
    @Schema(description = "Сообщение об ошибке")
    private String responseMessage = null;
}
