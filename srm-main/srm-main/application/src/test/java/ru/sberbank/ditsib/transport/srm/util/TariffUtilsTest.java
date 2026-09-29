package ru.sberbank.ditsib.transport.srm.util;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@DisplayName("Проверка утилит для работы с тарифами")
class TariffUtilsTest {

    @Test
    @DisplayName("Проверка расчета по тарифу на ЛТ")
    void test_calculateByPersonalTariff() {
        final var tariff = Instancio.create(PersonalTariff.class);
        final var startTime = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.systemDefault());
        final var metrics = Instancio.create(SrmMetrics.class);

        final var basicCost = getBasicCost(tariff, metrics);
        final var result = TariffUtils.calculateByPersonalTariff(tariff, startTime, metrics);

        assertThat(result).isEqualTo((long) (basicCost * TariffUtils.getSeasonalCoefficient(tariff, startTime.toLocalDateTime()) * TariffUtils.getTimeCoefficient(tariff.getTimedTariffParams(), startTime)));
    }

    private long getBasicCost(PersonalTariff tariff, SrmMetrics metrics) {
        final var distanceCost = tariff.getMinRideDistanceCost() + Math.max(0, (long) metrics.totalDistance - tariff.getDistanceIncluded().longValue()) * tariff.getRideCostPerKm();
        final var minRideTimeCost = tariff.getMinRideTimeCost() == null ? 0 : tariff.getMinRideTimeCost();
        final var timeCost = minRideTimeCost + Math.max(0, (metrics.totalSeconds / 60 - tariff.getTimeIncluded()) * tariff.getRideCostPerMin());
        final var waitingPrice = Math.max(metrics.primaryWaitingTime / 60, metrics.intermediateWaitingTime / 60) * tariff.getWaitCostPerMin();
        return distanceCost + timeCost + waitingPrice;
    }

}