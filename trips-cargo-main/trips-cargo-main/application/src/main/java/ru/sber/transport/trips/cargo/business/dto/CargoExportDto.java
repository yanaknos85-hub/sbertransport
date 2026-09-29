package ru.sber.transport.trips.cargo.business.dto;

import ru.sber.transport.spreadsheet.annotation.NullRender;

import java.time.LocalDateTime;

/**
 * DTO для экспорта данных в таблицу Excel
 * @param ordinal  № п/п
 * @param driverFullName ФИО водителя
 * @param vehicleNumber № автомобиля
 * @param routeHumanReadableId HRID Маршрутного листа
 * @param desiredDate Дата подачи ТС
 * @param startAddress Адрес подачи ТС
 * @param intermediateAddresses Промежуточные адреса - точки маршрута
 * @param finishAddress Адрес последнего пункта назначения
 * @param factStartTime Время начала работы (подачи) ТС
 * @param factFinishTime Время окончания работы ТС
 * @param factDistanceKm Протяженность маршрута, км
 * @param factWaitTimeMin Ожидание, мин.
 * @param intermediateWaitTimeHours Погрузочно-разгрузочные работы, ч
 * @param weight Вес (кг)
 * @param maxWeight Грузоподъемность ТС, тонн
 * @param factPriceRub Тариф, руб./км (без НДС)
 * @param factWaitCostRub Тариф за время ожидания, руб./м. (без НДС)
 * @param loadingUnloadingTariffCostRubHours Тариф на погрузо-разгрузочные работы, руб./ч. (без НДС)
 * @param loadingUnloadingTariffCostRubMin Тариф на погрузо-разгрузочные работы, руб./мин. (без НДС)
 * @param transportCostsWithoutNds Транспортные расходы без НДС, руб.
 * @param factSumWithoutNdsRub Итого без НДС, руб
 */
public record CargoExportDto(

        Integer ordinal,

        String driverFullName,

        String vehicleNumber,

        @NullRender
        String routeHumanReadableId,

        @NullRender
        LocalDateTime desiredDate,

        String startAddress,

        String intermediateAddresses,

        String finishAddress,

        LocalDateTime factStartTime,

        LocalDateTime factFinishTime,

        Double factDistanceKm,

        Long factWaitTimeMin,

        @NullRender
        Double intermediateWaitTimeHours,

        Double weight,

        Double maxWeight,

        @NullRender
        Double factPriceRub,

        @NullRender
        Double factWaitCostRub,

        @NullRender
        Double loadingUnloadingTariffCostRubHours,

        @NullRender
        Double loadingUnloadingTariffCostRubMin,

        @NullRender
        Double transportCostsWithoutNds,

        @NullRender
        Double factSumWithoutNdsRub
) {
}
