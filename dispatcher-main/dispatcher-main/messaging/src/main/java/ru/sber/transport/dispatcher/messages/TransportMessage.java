package ru.sber.transport.dispatcher.messages;


import ru.sber.transport.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Сообщение с информацией об автомобиле
 *
 * @param id                     уникальный идентификатор сообщения
 * @param stateNumber            государственный номер автомобиля
 * @param brand                  марка автомобиля
 * @param model                  модель автомобиля
 * @param transportType          тип транспортного средства
 * @param year                   год выпуска автомобиля
 * @param vin                    идентификационный номер транспортного средства (VIN)
 * @param currentMileage         текущий пробег автомобиля
 * @param type                   тип транспортного средства
 * @param subtype                подтип транспортного средства
 * @param organizationIds        набор уникальных идентификаторов организаций, которым принадлежит автомобиль
 * @param deleted                признак удаления записи
 * @param departmentIds          набор уникальных идентификаторов подразделений организации
 * @param vehicleId              уникальный идентификатор транспортного средства
 * @param exploitationStart      дата начала эксплуатации автомобиля
 * @param exploitationEnd        дата окончания эксплуатации автомобиля
 * @param status                 статус транспортного средства
 * @param modelId                уникальный идентификатор модели автомобиля
 * @param brandId                уникальный идентификатор марки автомобиля
 */
public record TransportMessage(
        UUID id,
        String stateNumber,
        String brand,
        String model,
        String transportType,
        int year,
        String vin,
        int currentMileage,
        String type,
        String subtype,
        Set<UUID> organizationIds,
        boolean deleted,
        Set<UUID> departmentIds,
        UUID vehicleId,
        LocalDate exploitationStart,
        LocalDate exploitationEnd,
        String status,
        UUID modelId,
        UUID brandId,
        Set<UUID> fuelTypeIds,
        UUID engineTypeId,
        Integer fuelTankVolume,
        String inventoryNumber,
        BigDecimal cityConsumptionRate,
        BigDecimal countryConsumptionRate,
        BigDecimal hybridConsumptionRate,
        UUID contractorId,
        UUID autoparkId,
        String locationAddress,
        String parkingAddress
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
