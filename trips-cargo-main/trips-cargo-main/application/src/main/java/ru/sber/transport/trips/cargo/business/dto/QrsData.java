package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные qr-кодов")
public class QrsData {

    @Schema(description = "Идентификатор путевой точки")
    private UUID waypointId;

    @Schema(description = "Человекочитаемый идентификатор заявки")
    private String requestHumanReadableId;

    @Schema(description = "Список QR-кодов")
    private List<String> qrs;

}
