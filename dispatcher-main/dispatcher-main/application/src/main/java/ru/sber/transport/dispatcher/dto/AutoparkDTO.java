package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.*;

import java.util.*;

/**
 * Объект с данными об автопарке.
 *
 * @param id идентификатор.
 * @param name название.
 * @param contractor контрагент.
 * @param active признак активности.
 */
@Schema(title = "Информация об автопарке", description = "Данные автопарка")
public record AutoparkDTO (

    @Schema(description = "ID автопарка")
    UUID id,

    @Schema(description = "Название автопарка")
    String name,

    @Schema(description = "Информация о контрагенте")
    ContractorDTO contractor,

    @Schema(description = "Активность")
    boolean active,

    @Schema(description = "Идентификатор филиала во внешней системе для маршрутизации поездок")
    UUID routingId,

    @Schema(description = "Нормативное количество автомобилей в филиале")
    Integer vehicleCountNorm

) {}
