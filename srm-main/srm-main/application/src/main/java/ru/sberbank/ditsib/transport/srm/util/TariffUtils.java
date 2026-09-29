package ru.sberbank.ditsib.transport.srm.util;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.TimedTariffParams;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Утилиты для работы с тарифами
 */
@UtilityClass
public class TariffUtils {

    private static final int CHECKING_YEAR = 2000;

    // taxi
    public Long calculateByTaxiTariff(TaxiTariff tariff, ZonedDateTime startTime, SrmMetrics distanceAndTime) {
        return (long) (calculateTaxiBasicCost(tariff, distanceAndTime) * calculateTotalTaxiCoefficient(tariff, startTime));
    }

    //Расчет базовой стоимости поездки на такси
    public long calculateTaxiBasicCost(@NotNull TaxiTariff tariff, SrmMetrics distanceAndTime) {
        long minRideDistanceCost = Optional.ofNullable(tariff.getMinRideDistanceCost()).orElse(0);
        long minRideTimeCost = Optional.ofNullable(tariff.getMinRideTimeCost()).orElse(0);
        long timeCost = (minRideTimeCost + Math.max(0, ((distanceAndTime.totalSeconds / 60) - tariff.getTimeIncluded()) * tariff.getRideCostPerMin()));
        long waitingPrice = (long) Math.max(0, (distanceAndTime.primaryWaitingTime / 60) - tariff.getFreeWaitingTime()) * tariff.getWaitCostPerMin() + tariff.getWaitCostPerMinIntermediate() * (distanceAndTime.intermediateWaitingTime / 60);
        long distanceCost = (long) (minRideDistanceCost + Math.max(0, distanceAndTime.totalDistance - tariff.getDistanceIncluded()) * tariff.getRideCostPerKm());
        return distanceCost + timeCost + waitingPrice;
    }

    //Получение общего коэффициента поездки на такси
    public double calculateTotalTaxiCoefficient(TaxiTariff taxiTariff, ZonedDateTime startTime) {
        return getTimeCoefficient(taxiTariff.getTimedTariffParams(), startTime);
    }

    /* personal */

    public long calculateByPersonalTariff(PersonalTariff tariff, ZonedDateTime startTime, SrmMetrics distanceAndTime) {
        return (long) (calculatePersonalBasicCost(tariff, distanceAndTime) * calculateTotalPersonalCoefficient(tariff, startTime));
    }

    //Расчет базовой стоимости поездки
    public long calculatePersonalBasicCost(@NotNull PersonalTariff tariff, SrmMetrics distanceAndTime) {
        final var minRideDistanceCost = Optional.ofNullable(tariff.getMinRideDistanceCost()).orElse(0);
        final var distanceCost = minRideDistanceCost + Math.max(0, (long) distanceAndTime.totalDistance - tariff.getDistanceIncluded().longValue()) * tariff.getRideCostPerKm();
        final var minRideTimeCost = Optional.ofNullable(tariff.getMinRideTimeCost()).orElse(0);
        final var timeCost = minRideTimeCost + Math.max(0, (distanceAndTime.totalSeconds / 60 - tariff.getTimeIncluded()) * tariff.getRideCostPerMin());
        final var waitingPrice = Math.max(distanceAndTime.primaryWaitingTime / 60, distanceAndTime.intermediateWaitingTime / 60) * tariff.getWaitCostPerMin();
        return distanceCost + timeCost + waitingPrice;
    }

    public double calculateTotalPersonalCoefficient(@NotNull PersonalTariff tariff, ZonedDateTime tripTime) {
        return getSeasonalCoefficient(tariff, tripTime.toLocalDateTime()) * getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
    }

    public double getSeasonalCoefficient(@NotNull PersonalTariff tariff, LocalDateTime tripTime) {
        if (tariff.getSeasonalCoefficient() == null || tariff.getSeasonStart() == null || tariff.getSeasonEnd() == null) {
            return 1D;
        }
        //используется високосный год, чтобы не было проблем с 29 февраля
        tariff = tariff.toBuilder().seasonStart(tariff.getSeasonStart().withYear(CHECKING_YEAR)).seasonEnd(tariff.getSeasonEnd().withYear(CHECKING_YEAR)).build();
        if (!tariff.getSeasonStart().isAfter(tariff.getSeasonEnd())) {
            if (!tripTime.toLocalDate().withYear(CHECKING_YEAR).isBefore(tariff.getSeasonStart()) && tripTime.toLocalDate().withYear(CHECKING_YEAR).isBefore(tariff.getSeasonEnd())) {
                return tariff.getSeasonalCoefficient();
            }
        } else if (!tripTime.toLocalDate().withYear(CHECKING_YEAR).isBefore(tariff.getSeasonStart()) || tripTime.toLocalDate().withYear(CHECKING_YEAR).isBefore(tariff.getSeasonEnd())) {
            return tariff.getSeasonalCoefficient();
        }
        return 1D;
    }

    /* common */

    public Double getTimeCoefficient(TimedTariffParams params, ZonedDateTime time) {
        if (time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return Optional.ofNullable(params.getCoefDayOff()).orElse(1D);
        }
        LocalTime localTime = time.toLocalTime();
        if (!localTime.isBefore(TimedTariffParams.MORNING_START) && localTime.isBefore(TimedTariffParams.NOON_START)) {
            return Optional.ofNullable(params.getCoefWorkDayMorning()).orElse(1D);
        }
        if (!localTime.isBefore(TimedTariffParams.NOON_START) && localTime.isBefore(TimedTariffParams.EVENING_START)) {
            return Optional.ofNullable(params.getCoefWorkDayNoon()).orElse(1D);
        }
        if (!localTime.isBefore(TimedTariffParams.EVENING_START) && localTime.isBefore(TimedTariffParams.NIGHT_START)) {
            return Optional.ofNullable(params.getCoefWorkDayEvening()).orElse(1D);
        }
        if (!localTime.isBefore(TimedTariffParams.NIGHT_START) || localTime.isBefore(TimedTariffParams.MORNING_START)) {
            return Optional.ofNullable(params.getCoefWorkDayNight()).orElse(1D);
        }
        return 1D;
    }
}
