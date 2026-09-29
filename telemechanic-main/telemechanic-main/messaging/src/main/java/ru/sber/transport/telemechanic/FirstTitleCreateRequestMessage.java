package ru.sber.transport.telemechanic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Запись для получения сообщений о первом титуле из Kafka.
 * <p>Используется для обработки сообщений при формировании первого титула ЭПЛ.</p>
 *
 * @param id                    Идентификатор сообщения
 * @param content               Сформированный файл (содержимое титула)
 * @param fileName              Имя файла
 * @param signature             Данные подписи
 * @param creationTime          Дата и время формирования титула
 * @param humanReadableId       Человекочитаемый идентификатор ЭПЛ
 * @param startDate             Дата начала перевозки
 * @param finishDate            Дата окончания перевозки
 * @param transportationType    Вид перевозки (например, "СН")
 * @param communicationType     Вид сообщения (например, "Г")
 * @param tariffDepartmentId    Идентификатор подразделения водителя
 * @param transportId           Идентификатор транспортного средства
 * @param driverEmployeeId      Идентификатор сотрудника, который является водителем
 */
@Schema(description = """
        Запись для получения сообщений о первом титуле из Kafka.
        Используется при формировании первого титула электронного путевого листа (ЭПЛ).
        Содержит как метаданные документа, так и данные о перевозке.""",
        title = "FirstTitleCreateRequestMessage")
public record FirstTitleCreateRequestMessage(

        @NotNull(message = "Идентификатор сообщения должен быть задан")
        @Schema(description = "Уникальный идентификатор сообщения",
                example = "123e4567-e89b-12d3-a456-426614174002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "Сформированный файл (содержимое титула в формате PDF или ином)",
                example = "JVBERi0xLjQKJ...")
        String content,

        @Schema(description = "Имя файла",
                example = "first_title_20231215_143022.pdf")
        String fileName,
        
        @NotNull
        @Schema(description = "Уникальный идентификатор ЭПЛ",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID ewbUuid,
        
        @NotNull
        @Schema(description = "Идентификатор записи с таблицы corporate.user пользователя, инициировавшего запрос на создание ЭПЛ",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId,

        @NotBlank(message = "Данные подписи должны быть предоставлены")
        @Size(min = 100, message = "Слишком короткая строка подписи (минимум 100 символов)")
        @Schema(description = "Данные подписи (цифровая подпись)",
                example = "3082039F30820287A003020102020...",
                minLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String signature,

        @NotNull(message = "Дата и время формирования титула должны быть заданы")
        @Schema(description = "Дата и время формирования титула",
                example = "2023-12-15T14:30:22",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime creationTime,

        @NotBlank
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ",
                example = "ЭПЛ-2023-001234",
                requiredMode = Schema.RequiredMode.REQUIRED
                )
        String humanReadableId,

        @NotNull(message = "Дата начала перевозки должна быть задана")
        @Schema(description = "Дата начала перевозки",
                example = "2023-08-20",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,

        @NotNull(message = "Дата окончания перевозки должна быть задана")
        @Schema(description = "Дата окончания перевозки",
                example = "2023-08-25",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate finishDate,

        @NotBlank(message = "Вид перевозки должен быть указан")
        @Size(min = 1, max = 3, message = "Вид перевозки должен содержать от 1 до 3 символов")
        @Schema(description = "Вид перевозки",
                example = "СН",
                minLength = 1,
                maxLength = 3,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String transportationType,

        @NotBlank(message = "Вид сообщения должен быть указан")
        @Size(min = 1, max = 2, message = "Вид сообщения должен содержать от 1 до 2 символов")
        @Schema(description = "Вид сообщения",
                example = "Г",
                minLength = 1,
                maxLength = 2,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String communicationType,

        @NotNull(message = "Идентификатор подразделения водителя должен быть задан")
        @Schema(description = "Идентификатор подразделения водителя",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID tariffDepartmentId,

        @NotNull(message = "Идентификатор транспортного средства должен быть задан")
        @Schema(description = "Идентификатор транспортного средства",
                example = "123e4567-e89b-12d3-a456-426614174001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID transportId,

        @NotNull(message = "Идентификатор сотрудника, который является водителем, должен быть задан")
        @Schema(description = "Идентификатор сотрудника, который является водителем",
                example = "123e4567-e89b-12d3-a456-426614174002",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID driverEmployeeId
) implements Message<String> {
        @Override
        public String getId() {
                return id.toString();
        }
}