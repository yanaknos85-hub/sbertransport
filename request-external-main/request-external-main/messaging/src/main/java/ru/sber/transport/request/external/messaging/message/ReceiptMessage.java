package ru.sber.transport.request.external.messaging.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

/**
 * Данные чека
 *
 * @param requestId     идентификатор заявки
 * @param transportType тип транспорта
 * @param desiredDate          дата поездки
 * @param cost          расчётная стоимость в копейках
 * @param files         список документов
 */

@Builder
public record ReceiptMessage(
        UUID requestId,
        String transportType,
        @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime desiredDate,
        Long cost,
        List<FileData> files
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return requestId;
    }

    /**
     * Данные чека
     *
     * @param ticketCost стоимость поездки в копейках
     * @param folderId   идентификатор папки, в данном случае идентификатор заявки
     * @param fileName   наименование файла
     */
    @Builder
    public record FileData(
            Long ticketCost,
            UUID folderId,
            String fileName
    ) {
    }
}
