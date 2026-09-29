package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DriverSearchResponseDto", description = "Сущность для результата поиска водителя")
public record DriverSearchResponse(
        @Schema(description = "Водитель")
        DriverSearchDto driver,
        @Schema(description = "Водительские права")
        DrivingLicenseDto drivingLicense) {
}
