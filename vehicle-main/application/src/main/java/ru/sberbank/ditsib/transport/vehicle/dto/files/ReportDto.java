package ru.sberbank.ditsib.transport.vehicle.dto.files;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(title = "Показания (excel)")
public record ReportDto(
        @Schema(description = "Организация", example = "Байкальский банк")
        String organizationName,
        @Schema(description = "Орг. Единица", example = "10110907")
        String easupId,
        @Schema(description = "Государственный номер", example = "А001АА199")
        String stateNumber,
        @Schema(description = "Марка", example = "Ford")
        String brand,
        @Schema(description = "Модель", example = "Focus")
        String model,
        @Schema(description = "VIN", example = "X2FXXXESGXLG47646")
        String vin,
        @Schema(description = "Год выпуска", example = "2022")
        int year,
        @Schema(description = "Дата начала эксплуатации", example = "2022.10.03")
        LocalDate exploitationStart,
        @Schema(description = "Вид ТС", example = "Служебный транспорт")
        String type,
        @Schema(description = "Подвид ТС", example = "ППКО")
        String subtype,
        @Schema(description = "Отчетный год", example = "2024")
        int reportYear,
        @Schema(description = "Вид отчета", example = "Одометр")
        String reportType,
        @Schema(description = "Январь", example = "100")
        Integer valueJanuary,
        @Schema(description = "Февраль", example = "200")
        Integer valueFebruary,
        @Schema(description = "Март", example = "300")
        Integer valueMarch,
        @Schema(description = "Апрель", example = "400")
        Integer valueApril,
        @Schema(description = "Май", example = "500")
        Integer valueMay,
        @Schema(description = "Июнь", example = "600")
        Integer valueJune,
        @Schema(description = "Июль", example = "700")
        Integer valueJuly,
        @Schema(description = "Август", example = "800")
        Integer valueAugust,
        @Schema(description = "Сентябрь", example = "900")
        Integer valueSeptember,
        @Schema(description = "Октябрь", example = "1000")
        Integer valueOctober,
        @Schema(description = "Ноябрь", example = "1100")
        Integer valueNovember,
        @Schema(description = "Декабрь", example = "1200")
        Integer valueDecember) {
}
