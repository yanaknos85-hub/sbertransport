package ru.sber.transport.journal.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO абстрактная заявка (чтение)
 */
@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Абстрактная заявка", description = "Данные абстрактной заявки")
public class AbstractGetRequestDto {
    @Schema(description = "Идентификатор записи о заявке", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID id;
    @Schema(description = "Человекочитаемый идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100)
    private String humanReadableId;
    @Schema(description = "Создатель заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    private EmployeeDto author;
    @Schema(description = "Коллега", nullable = true)
    @Valid
    private EmployeeDto colleague;
    @Schema(description = "Комментарий", nullable = true, maxLength = 500)
    @Size(max = 500)
    private String comment;
    @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
    @NotBlank
    @Size(max = 255)
    private String status;
    @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64",
            example = "1696616506000")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    @NotNull
    private LocalDateTime creationTime;
    @Schema(description = "Планируемая дата и время контрольного срока", type = "integer", format = "int64", example = "1696616506000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    @NotNull
    private LocalDateTime deadlineTime;
    @Schema(description = "Марка по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
    @NotBlank
    @Size(max = 150)
    private String brandByPassport;
    @Schema(description = "Модель по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
    @NotBlank
    @Size(max = 150)
    private String modelByPassport;
    @Schema(description = "Оценка выполненного обращения заявителем", nullable = true)
    private GetEvaluationDto evaluation;
    @Schema(description = "Список возвратов на доработку", nullable = true)
    private List<RevisionDto> revisions;
    @Schema(description = "Есть ли возможность вернуть заявку на доработку", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean revisable;
}