package ru.sberbank.ditsib.transport.request.messaging.message;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record FraudMessage(

        @Schema(description = "Идентификатор заявки")
        UUID id,
        @Schema(description = "Комментарий агента о подозрении на фрод")
        String comment

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
