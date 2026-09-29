package ru.sber.transport.telemechanic.messaging.sender.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record OdometerHistoryValueMessage(
        @NotNull
        UUID transportId,

        @Positive
        int value,

        @NotNull
        UUID creatorUserId,

        @NotNull
        LocalDateTime creationTime,

        Map<String, Object> metaAttributes

)  implements Message<String> {
        @JsonIgnore
        @Override
        public String getId() {
                return transportId.toString();
        }
}
