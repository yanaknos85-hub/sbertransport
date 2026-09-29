package ru.sber.transport.request.external.messaging.message;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import ru.sber.transport.messaging.Message;

public record FraudMessagePlain(

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
