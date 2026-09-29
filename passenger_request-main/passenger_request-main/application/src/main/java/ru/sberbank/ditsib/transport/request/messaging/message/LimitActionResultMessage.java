package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LimitActionResultMessage {

    /**
     * Request Id
     */
    private UUID tripRequestId;

    /**
     * Limit Id
     */
    private UUID limitId;
    
    /**
     * Reservation status.
     */
    private String limitReservationStatus;

    /**
     * Message.
     */
    private String message;
    
    /**
     * Идентификатор лимита (человекочитаемый)
     */
    private String humanReadableId;
    
}
