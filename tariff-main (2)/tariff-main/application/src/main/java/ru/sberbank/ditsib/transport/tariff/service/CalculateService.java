package ru.sberbank.ditsib.transport.tariff.service;

import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Service for calculating costs.
 */
public interface CalculateService {

    /**
     * Calculate tariff data.
     *
     * @param tripData data of trip.
     * @param employee данные о сотруднике
     * @return calculated tariff data.
     */
    Collection<? extends CalculatedDto> calculate(TripDto tripData, Employee employee);

    /**
     * Calculate tariff data.
     *
     * @param tripData          данные о поездке
     * @param employee          данные о сотруднике
     * @param checkLimits       true - проверить лимиты
     * @param transportTypeList список типов транспорта
     * @return рассчитанные данные
     */
    Collection<? extends CalculatedDto> calculate(
            TripDto tripData, Employee employee, boolean checkLimits,
            List<TransportTypeEnum> transportTypeList
    );

    /**
     * Получить все тарифы.
     *
     * @param tripDto      Данные о поездке
     * @param employee     Данные о пассажире
     * @param regionBranch Данные о гео-зонах точки отправления начиная с самой маленькой/детальной, например, города до крупной, например, области
     * @param isNightTrip  true - удалить из результата поиска не ночные тарифы
     * @return Данные о найденных подходящих тарифах
     */
    List<? extends BaseTariff> findAllTariffs(
            @NotNull TripDto tripDto, Employee employee, List<RegionDto> regionBranch, Boolean isNightTrip
    );

    /**
     * Рассчитать стоимость поездки по тарифу.
     *
     * @param transportTypeEnum вид транспорта.
     * @param tripData          данные поездки.
     * @param employee          данные о сотруднике
     * @return рассчитанные данные.
     */
    Collection<? extends CalculatedDto> calculate(TransportTypeEnum transportTypeEnum, TripDto tripData, Employee employee);


    /**
     * Calculate tariff data.
     *
     * @param tariffId ID of tariff.
     * @param tripData data of trip.
     * @param employee данные о сотруднике
     * @return calculated tariff data.
     */
    CalculatedDto calculate(TransportTypeEnum tariffType, UUID tariffId, TripDto tripData, Employee employee);

    /**
     * Общий метод расчета тарифов
     *
     * @param tripData                 Данные о поездке
     * @param employee                 Данные о пассажире
     * @param regionBranch             Данные о гео-зонах точки отправления начиная с самой маленькой/детальной, например, города до крупной, например, области
     * @param deleteNonNightTariffTaxi true - удалить из результата поиска не ночные тарифы
     * @param checkLimits              true - проверить лимиты
     * @param transportTypeList        список типов транспорта
     * @return Данные о найденных подходящих тарифах
     */
    Collection<? extends CalculatedDto> calculateCommon(
            TripDto tripData, Employee employee, List<RegionDto> regionBranch, Boolean deleteNonNightTariffTaxi, boolean checkLimits,
            List<TransportTypeEnum> transportTypeList
    );

    /**
     * Подсчитать транспорты.
     *
     * @param tripData      данные поездки
     * @param search        строка поиска
     * @param availableOnly только доступные
     * @param employee      данные о сотруднике
     * @param token         токен
     * @return результат
     */
    TransportPageDTO calculateTransports(
            TripDto tripData, String search, Boolean availableOnly, Employee employee, String token
    );

    /**
     * Подсчитать транспорт.
     *
     * @param tripData    данные поездки
     * @param transportId ид транспорта
     * @param startDate   время начала поездки
     * @param endDate     время окончания поездки
     * @param employee    данные о сотруднике
     * @param token       токен
     * @return результат
     */
    TransportWithCalculateDTO calculateTransport(
            TripDto tripData, UUID transportId, long startDate, long endDate, Employee employee, String token
    );
}
