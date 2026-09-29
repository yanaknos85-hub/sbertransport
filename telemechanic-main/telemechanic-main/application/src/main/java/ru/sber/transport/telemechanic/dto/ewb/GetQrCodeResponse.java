package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetQrCodeResponse", title = "Ответ на запрос QR-кода")
public record GetQrCodeResponse(
        @Schema(description = "QR-код закодированный в Base64")
        String qr
) {
}
