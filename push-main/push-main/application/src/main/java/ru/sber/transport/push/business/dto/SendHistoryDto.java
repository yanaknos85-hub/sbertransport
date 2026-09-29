package ru.sber.transport.push.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Исторические данные об отправленных уведомлениях")
public class SendHistoryDto {

    @Schema(description = "Идентификатор сообщения")
    private UUID messageId;

    @Schema(description = "Время отправки сообщения")
    private OffsetDateTime sendTime;

    @Schema(description = "Идентификатор получателя")
    private UUID recipientId;

    @Schema(description = "Идентификатор токена")
    private UUID tokenId;

    @Schema(description = "Текст сообщения")
    private String message;

    @Schema(description = "Статус отправки сообщения")
    private SendStatus status;

    @Schema(description = "Описание ошибки")
    private ErrorDescription errorDescription;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Описание ошибки отправки уведомления")
    public static class ErrorDescription {

        @Schema(description = "Тип ошибки")
        private String type;

        @Schema(description = "Детали ошибки")
        private String message;
    }

}
