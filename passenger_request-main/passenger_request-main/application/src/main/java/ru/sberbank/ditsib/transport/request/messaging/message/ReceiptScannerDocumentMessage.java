package ru.sberbank.ditsib.transport.request.messaging.message;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение для сканера чеков.
 * Отправляется в топик service.ai.receipt.scanner.listen.document для заявок на общественный транспорт.
 */
@Builder
@Schema(description = "Сообщение для сканера чеков")
public record ReceiptScannerDocumentMessage(

        @Schema(description = "Идентификатор заявки")
        UUID requestId,

        @Schema(description = "Тип транспорта (PUBLIC)")
        String transportType,

        @Schema(description = "Дата поездки")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime desiredDate,

        @Schema(description = "Расчётная стоимость в копейках")
        Integer cost,

        @Schema(description = "Список файлов")
        List<FileData> files

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return requestId;
    }

    @Builder
    @Schema(description = "Данные файла")
    public record FileData(

            @Schema(description = "Стоимость билета в копейках")
            Integer ticketCost,

            @Schema(description = "Идентификатор папки")
            UUID folderId,

            @Schema(description = "Имя файла")
            String fileName

    ) {
    }
}
