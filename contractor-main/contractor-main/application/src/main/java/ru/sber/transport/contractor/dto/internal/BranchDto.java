package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchDto {

    @Schema(description = "Наименование")
    private String name;

    @Schema(description = "Идентификатор поздразделения для маршрутизации заявок")
    private UUID routingId;

    @Schema(description = "Нормативное количество автомобилей в филиале")
    private Integer vehicleCountNorm;

}
