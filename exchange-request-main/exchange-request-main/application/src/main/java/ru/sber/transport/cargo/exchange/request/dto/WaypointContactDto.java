package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * DTO для контактов, связанных с точками маршрута.
 * Используется для передачи информации о контактных лицах на этапах маршрута.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WaypointContactDto {

    @Schema(description = "Уникальный идентификатор контакта")
    private UUID id;

    @Schema(description = "Идентификатор точки маршрута, к которой привязан контакт")
    private UUID waypointId;

    @Schema(description = "Идентификатор организации, представителем которой является контакт(для mvp это ИНН)")
    private String organizationInn;

    @Schema(description = "ФИО ответственного лица", maxLength = 200)
    private String contactPerson;

    @Schema(description = "Контактный телефон", maxLength = 20)
    private String contactPhone;

    @Schema(description = "Email ответственного лица", maxLength = 100)
    private String contactEmail;
}
