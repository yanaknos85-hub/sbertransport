package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import ru.sber.transport.dispatcher.serde.ByteSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "FirstTitleDto", description = "DTO для первого заголовка")
public class FirstTitleResponseDto {

    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;

    @Schema(description = "Имя файла")
    private String fileName;

    @Schema(description = "Содержимое файла (массив байт)")
    @JsonSerialize(using= ByteSerializer.class)
    private byte[] content;

    @Schema(description = "Время создания")
    private LocalDateTime creationTime;

    @Schema(description = "Текст ошибки")
    private String errorText;

    @Schema(description = "Идентификатор смены")
    private UUID shiftId;

    @Schema(description = "Идентификатор ЭПЛ")
    private UUID ewbId;

}
