package ru.sberbank.ditsib.transport.request.dto.fraud;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record FraudCommentDTO(
        @Schema(
                description = "Текст сообщения о нарушениях",
                example = "Подозрение на дробление поездок"
        )
        String text,

        @Schema(
                description = "Идентификатор связанной заявки",
                format = "uuid",
                example = "e6d206cd-515c-4a22-b04c-136299f1e279"
        )
        UUID id,

        @Schema(
                description = "Человекочитаемый идентификатор связанной заявки",
                example = "TR-12345"
        )
        String humanReadableId
) {
}