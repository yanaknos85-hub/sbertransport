package ru.sberbank.ditsib.transport.vehicle.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Вид отчета")
@RequiredArgsConstructor
@Getter
public enum ReportType {
    @Schema(description = "Отчет по показателям")
    ODOMETER("Одометр"),
    FUEL("Топливо"),
    MILEAGE("Пробеги");
    
    private final String description;
}
