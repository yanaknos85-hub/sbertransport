package ru.sberbank.ditsib.transport.reports.messaging.messages;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
public class UpdateTripRequestStatusMessage implements Message<UUID> {
    private UUID id;
    private String status;
    private LocalDateTime dateTime;
    private String userId;
}
