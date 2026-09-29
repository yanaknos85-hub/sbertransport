package ru.sber.transport.telemechanic.dto.ewb_report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "EwbRegistryInfo", title = "Данные для реестра по ЭПЛ", description = "Данные по ЭПЛ для ответа формирования реестар ЭПЛ")
public record EwbRegistryInfo(
        @NotBlank(message = "ID путевого листа не должен быть пустым")
        @Size(max = 20, message = "Размер ID путевого листа должен быть не больше 36 символов")
        @Schema(description = "ID путевого листа",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "PL-0001-11111111",
                maximum = "20")
        String humanReadableId,
        
        @Size(max = 36, message = "Размер уникального идентификатора документа путевого листа должен быть не больше 36 символов")
        @Schema(description = "Уникальный идентификатор документа путевого листа",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "643602ce-a454-4dc6-a11e-011b4052a05a",
                maximum = "36",
                nullable = true)
        String ewbUuid,
        
        @NotNull(message = "Статус путевого листа не должен быть пустым")
        @Schema(description = "Статус путевого листа",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "EWB_CREATED")
        EwbStatus status,
        
        @NotNull(message = "Дата создания путевого листа не должна быть пустой")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @Schema(description = "Дата и время создания путевого листа",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer",
                format = "int64",
                example = "1696616506000")
        LocalDateTime creationTime,
        
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата заезда",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "2017-02-25",
                nullable = true)
        LocalDate startDate,
        
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @Schema(description = "Дата заезда",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                type = "integer",
                format = "int64",
                example = "1696616506000",
                nullable = true)
        LocalDate finishDate,
        
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @Schema(description = "Дата и время выезда",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "2017-02-25",
                nullable = true)
        LocalDateTime telemechDecisionOutTime,
        
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @Schema(description = "Дата и время заезда",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                type = "integer",
                format = "int64",
                example = "1696616506000",
                nullable = true)
        LocalDateTime telemechDecisionInTime,
        
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @Schema(description = "Дата и время прохождения медицинского осмотра",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                type = "integer",
                format = "int64",
                example = "1696616506000",
                nullable = true)
        LocalDateTime medicDecisionTime,
        
        @PositiveOrZero(message = "Показания одометра на выезде не должны быть отрицательными")
        @Max(value = 999_999_999, message = "Показания одометра на выезде не должны быть больше 999 999 999")
        @Schema(description = "Показания одометра на выезде",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "1234",
                nullable = true)
        Integer telemechOutMileage,
        
        @Max(value = 999_999_999, message = "Показания одометра на заезде не должны быть больше 999 999 999")
        @PositiveOrZero(message = "Показания одометра на заезде не должны быть отрицательными")
        @Schema(description = "Показания одометра на заезде",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "1234",
                nullable = true)
        Integer telemechInMileage,
        
        @Schema(description = "Успешное прохождеие медицинского осмотра",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "true",
                nullable = true)
        boolean medicSuccess,
        
        @Schema(description = "Успешное прохождеие технического контроля",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "true",
                nullable = true)
        boolean telemechSuccess
) {
}
