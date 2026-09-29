package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера поездок на такси")
class TaxiTripMapperTest {
    private final TaxiTripMapper mapper = new TaxiTripMapperImpl(new VehicleMapperImpl());
    
    @Test
    @DisplayName("Модель SingleTaxiTrip в сообщение")
    void test_singleTaxiTripMapper() {
        var request = new RequestForTaxi();
        request.setId(UUID.randomUUID());
        var singleTaxiTrip1 = new SingleTaxiTrip();
        singleTaxiTrip1.setId(UUID.randomUUID());
        singleTaxiTrip1.setDateTimeRegistered(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        singleTaxiTrip1.setOrganizationId(UUID.randomUUID());
        singleTaxiTrip1.setStatus(InboundTaxiTripStatus.ORDER_FINISHED);
        singleTaxiTrip1.setTariffId(UUID.randomUUID());
        singleTaxiTrip1.setTaxiId(400 + "");
        singleTaxiTrip1.setTripFactDistance(400.0);
        singleTaxiTrip1.setTripFactDuration(Duration.ofHours(1));
        singleTaxiTrip1.setTripFactPrice(400);
        singleTaxiTrip1.setTripFactWaitTime(Duration.ofHours(1));
        singleTaxiTrip1.setTripFinishTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(5));
        singleTaxiTrip1.setTripStartTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        singleTaxiTrip1.setTripType(TripType.SINGLE);
        singleTaxiTrip1.setRequests(Collections.singletonList(request));
        
        var actual = mapper.toMessage(singleTaxiTrip1, false);
        
        assertThat(actual.getId()).isEqualTo(singleTaxiTrip1.getId());
        assertThat(actual.getDateTimeRegistered()).isEqualTo(singleTaxiTrip1.getDateTimeRegistered());
        assertThat(actual.getOrganizationId()).isEqualTo(singleTaxiTrip1.getOrganizationId());
        assertThat(actual.getRequestId()).isEqualTo(singleTaxiTrip1.getRequests().get(0).getId());
        assertThat(actual.getSharedRideId()).isNull();
        assertThat(actual.getStatus()).isEqualTo(singleTaxiTrip1.getStatus().name());
        assertThat(actual.getTariffId()).isEqualTo(singleTaxiTrip1.getTariffId());
        assertThat(actual.getTaxiId()).isEqualTo(singleTaxiTrip1.getTaxiId());
        assertThat(actual.getTripFactDistance()).isEqualTo(singleTaxiTrip1.getTripFactDistance());
        assertThat(actual.getTripFactDuration()).isEqualTo(singleTaxiTrip1.getTripFactDuration());
        assertThat(actual.getTripFactPrice()).isEqualTo(singleTaxiTrip1.getTripFactPrice());
        assertThat(actual.getTripFactWaitTime()).isEqualTo(singleTaxiTrip1.getTripFactWaitTime());
        assertThat(actual.getTripFinishTime()).isEqualTo(singleTaxiTrip1.getTripFinishTime());
        assertThat(actual.getTripStartTime()).isEqualTo(singleTaxiTrip1.getTripStartTime());
        assertThat(actual.getTripType()).isEqualTo(singleTaxiTrip1.getTripType().name());
        assertThat(actual.isDeleted()).isFalse();
    }
}
