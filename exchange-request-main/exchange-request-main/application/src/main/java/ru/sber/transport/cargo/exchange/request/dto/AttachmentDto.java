package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для вложения (прикреплённого файла).
 * Соответствует таблице attachments.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AttachmentDto", description = "Прикреплённый файл к заявке")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachmentDto {

    @Schema(
        description = "Уникальный идентификатор файла",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
        description = "Идентификатор заявки, к которой прикреплён файл",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID requestId;

    @Schema(
        description = "Тип файла: 'cargo_photo' или 'document'",
        allowableValues = { "cargo_photo", "document" },
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fileType;

    @Schema(
        description = "Оригинальное имя файла при загрузке",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String originalName;

    @Schema(
        description = "Путь к файлу в хранилище",
        maxLength = 1000,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String storagePath;

    @Schema(
        description = "Размер файла в байтах. Должен быть больше 0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long fileSize;

    @Schema(
        description = "MIME-тип файла",
        allowableValues = { "image/jpeg", "image/png", "application/pdf" },
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mimeType;

    @Schema(
        description = "Дата и время загрузки файла",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime uploadAt;
}




