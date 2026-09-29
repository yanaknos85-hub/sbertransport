package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * Object with data about action.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Статус резервирования", description = "Статус резервирования")
@Builder
public class LimitReservationResultDto {

    /**
     * Request Id
     */
    @Schema(description = "Идентификатор заявки на поездку")
    private UUID tripRequestId;

    /**
     * Limit Id
     */
    @Schema(description = "Идентификатор лимита")
    private UUID limitId;
    /**
     * Reservation status.
     */
    @Schema(description = "Статус резервирования")
    private LimitReservationStatus limitReservationStatus;

    /**
     * Message.
     */
    @Schema(description = "Сообщение")
    private String message;

}
