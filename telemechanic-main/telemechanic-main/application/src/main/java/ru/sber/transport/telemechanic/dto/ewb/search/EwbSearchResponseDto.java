package ru.sber.transport.telemechanic.dto.ewb.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "EwbSearchResponseDto", description = "Ответ на запрос поиска ЭПЛ")
public record EwbSearchResponseDto(
        
        @NotNull
        @Schema(description = "Идентификатор ЭПЛ", example = "5a27205b-ab64-48a9-92e5-b44e8a407db6")
        UUID id,
        
        @NotBlank
        @Size(max = 36)
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ", example = "PL-0001-04567808", maxLength = 36)
        String humanReadableId,
        
        @NotBlank
        @Schema(description = "Название организации", example = "Байкальский банк")
        String organizationName,
        
        @NotNull
        @Schema(description = "Дата путевого листа", example = "2025-05-05")
        LocalDate startDate,
        
        @Schema(description = "Дата окончания путевого листа", example = "2025-05-05")
        LocalDate finishDate,
        
        @NotNull
        @Schema(description = "Статус ЭПЛ", example = "ON_THE_LINE")
        EwbStatus status,
        
        @Schema(description = "Прохождение медика", example = "true")
        boolean medicSuccess,
        
        @Schema(description = "Прохождение телемеханика", example = "true")
        boolean telemechSuccess,
        
        @Schema(description = "Автомобиль")
        Transport transport,
        
        @NotBlank
        @Schema(description = "ФИО водителя", example = "Иванов Иван Иванович")
        String driverFullName
) {
    @Schema(name = "Transport", description = "Автомобиль")
    public record Transport(
            @Schema(description = "Марка", example = "Opel")
            String brand,
            @Schema(description = "Модель", example = "Vectra")
            String model,
            @Schema(description = "Государственный номер", example = "A123AA777")
            String stateNumber
    ) {
    }
}
