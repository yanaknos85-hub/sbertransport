package ru.sber.transport.telemechanic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Запись ответного сообщения о результате создания первого титула.
 * <p>Используется для отправки результата обработки запроса на создание первого титула
 * через Kafka. Содержит идентификатор сообщения и текст ошибки (если она возникла).</p>
 *
 * <p><b>Назначение полей:</b></p>
 * <ul>
 *   <li><b>id</b> - Уникальный идентификатор сообщения, по которому можно отследить обработку</li>
 *   <li><b>errorText</b> - Пустая строка означает успешную обработку, иначе содержит описание ошибки</li>
 * </ul>
 *
 * <p><b>Примеры использования:</b></p>
 * <pre>
 * // Успешная обработка
 * FirstTitleCreateResponseMessage success = new FirstTitleCreateResponseMessage(
 *     UUID.randomUUID(),
 *     ""
 * );
 *
 * // Ошибка обработки
 * FirstTitleCreateResponseMessage error = new FirstTitleCreateResponseMessage(
 *     messageId,
 *     "Не удалось найти водителя с указанным идентификатором"
 * );
 * </pre>
 *
 * @param id        Уникальный идентификатор сообщения (обязательно)
 * @param errorText Текст ошибки. Пустая строка - успех, иначе текст ошибки
 */
@Schema(description = """
        Запись ответного сообщения о результате создания первого титула.
        Используется для асинхронного получения результата обработки запроса
        на создание первого титула через Kafka.""",
        title = "FirstTitleCreateResponseMessage",
        example = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174002",
                  "errorText": ""
                }""")
public record FirstTitleCreateResponseMessage(

        @NotNull(message = "Идентификатор сообщения должен быть задан")
        @Schema(description = "Уникальный идентификатор сообщения",
                example = "123e4567-e89b-12d3-a456-426614174002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "Текст ошибки, возникшей при обработке запроса.",
                example = "У водителя с табельным номером: А333ХХ777 есть незакрытый ЭПЛ на дату 14.10.2024",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String errorText

) implements Message<String> {
    @Override
    public String getId() {
        return id.toString();
    }
}