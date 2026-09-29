package ru.sber.transport.trips.cargo.providers.trips.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.Vehicle;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TripMapperTest {

    private TripMapper mapper = new TripMapperImpl(new WaypointsMapperImpl());

    @DisplayName("Маппинг полей в GetTripResponse.Order")
    @Test
    void toIntegrationResponse() {

        var trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setLoaders(10);
        trip.setLoadersWorkTime(60L);
        trip.setFactDistance(90.3);
        trip.setDriverWaitingTime(Duration.ofHours(3));
        trip.setFactCost(72000);
        trip.setFinishTime(OffsetDateTime.now());
        trip.setEndTime(OffsetDateTime.now());

        var driver = new Driver();
        var vehicle = new Vehicle();

        var result = mapper.toIntegrationResponse(trip, driver, vehicle);

        assertThat(result)
                .isNotNull();

//        Пробег, км
        assertThat(result.distance()).isEqualTo(trip.getFactDistance());
//        Время ожидания, мин
        assertThat(result.waitTime()).isEqualTo(trip.getDriverWaitingTime().toMillis());
//        Фактическая стоимость, руб
        assertThat(result.price()).isEqualTo(trip.getFactCost().longValue());
//        Время доставки
        assertThat(result.finishTime()).isEqualTo(trip.getEndTime());
//        + количество грузчиков, чел
        assertThat(result.factLoaders()).isEqualTo(trip.getLoaders());
////        Время работы грузчиков, мин
        assertThat(result.loadersWorkTime()).isEqualTo(trip.getLoadersWorkTime());

    }
}