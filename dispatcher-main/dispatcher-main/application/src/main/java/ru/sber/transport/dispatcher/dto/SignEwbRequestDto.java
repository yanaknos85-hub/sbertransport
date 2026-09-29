package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SignEwbRequestDto", description = "Запрос на подпись EWB документа")
public class SignEwbRequestDto {

    @Schema(description = "Идентификатор")
    @NotNull
    private UUID id;

    @Schema(description = "Имя файла")
    @NotNull
    private String fileName;

    @Schema(description = "Содержимое (строковое представление)")
    @NotNull
    private String content;

    @Schema(description = "Подпись")
    @NotNull
    private String signature;

    @Schema(description = "Время создания")
    @NotNull
    private LocalDateTime creationTime;

    @Schema(description = "Человекочитаемый идентификатор")
    @NotNull
    private String humanReadableId;

    @Schema(description = "Идентификатор ЭПЛ")
    @NotNull
    private UUID ewbUuid;

    @Schema(description = "Дата начала")
    private OffsetDateTime startDate;

    @Schema(description = "Дата окончания")
    private OffsetDateTime finishDate;

    @Schema(description = "Часовой пояс")
    @NotNull
    @NotEmpty
    private String timeZone;

    @Schema(description = "Тип перевозки")
    private String transportationType;

    @Schema(description = "Тип связи")
    private String communicationType;

    @Schema(description = "Идентификатор тарифного отдела")
    private UUID tariffDepartmentId;

    @Schema(description = "Идентификатор транспорта")
    private UUID transportId;

    @Schema(description = "Идентификатор водителя")
    private UUID driverId;

    @Schema(description = "Идентификатор пользователя")
    private UUID userId;
}
