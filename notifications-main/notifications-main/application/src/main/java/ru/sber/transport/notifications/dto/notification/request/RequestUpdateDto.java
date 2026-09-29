package ru.sber.transport.notifications.dto.notification.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Schema(title = "Уведомление об изменении в заявке", description = "Уведомление о каком либо изменении в заявке")
public class RequestUpdateDto {
    private UUID requestId;
}
