package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Request", description = "Заявка на перевозку груза")
public class RequestShortDto {

    @Schema(description = "Уникальный идентификатор заявки")
    private UUID id;

    @Schema(description = "Человекочитаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001")
    @JsonProperty("humanreadableId")
    private String humanReadableId;

    @Schema(description = "Организация-поставщик")
    private UUID organizationId;

    @Schema(description = "Номер заказа в системе грузовладельца")
    private String internalId;

    @Schema(description = "Идентификатор пользователя — владельца заявки")
    private UUID ownerId;

    @Schema(description = "Статус заявки: cargo_draft, cargo_published, cargo_archived")
    private String status;

    @Schema(description = "Дата и время создания записи")
    private LocalDateTime createdAt;

    @Schema(description = "Дата и время последнего обновления")
    private LocalDateTime updatedAt;

    @Schema(description = "Дата автоматического удаления черновика")
    private LocalDateTime expiresAt;

    @Schema(description = "Дата публикации заявки")
    private LocalDateTime publishedAt;

    @Schema(description = "Дата завершения заявки")
    private LocalDateTime completedAt;

}


