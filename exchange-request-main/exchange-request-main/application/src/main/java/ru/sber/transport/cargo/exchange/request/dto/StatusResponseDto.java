package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для ответа на изменение статуса заявки.
 * Содержит информацию о предыдущем и текущем статусе.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatusResponseDto {

    private boolean success;

    private RequestStatusChangeInfo request;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestStatusChangeInfo {
        private UUID requestId;

        private StatusInfo previousStatus;
        private StatusInfo currentStatus;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusInfo {
        private String code;
        private RequestStatus name;
    }
}