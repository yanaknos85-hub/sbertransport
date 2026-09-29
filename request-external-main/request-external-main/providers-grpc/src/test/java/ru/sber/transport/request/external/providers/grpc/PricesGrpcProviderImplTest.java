package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.math.BigDecimal;
import java.time.Duration;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;
import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера цен")
class PricesGrpcProviderImplTest {

    private final PriceDataServiceGrpc.PriceDataServiceBlockingStub stub = mock(PriceDataServiceGrpc.PriceDataServiceBlockingStub.class);

    private final PricesProvider pricesProvider = new PricesGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение цены и времени")
    void test_getPrice() {
        final var waypoints = Instancio.createList(WaypointDTO.class);
        final var tariff = Instancio.create(Tariff.class);
        final var integerPart = Instancio.create(Integer.class);
        final var fractionPart = Instancio.create(Integer.class);
        final var distance = Instancio.create(Integer.class);
        final var waitTime = Instancio.create(Duration.class);
        final var time = Instancio.create(Duration.class);

        final var response = ExternalTariff.PriceDataResponse.newBuilder()
                .setCost(ExternalTariff.Cost.newBuilder().setIntegerPart(integerPart).setFractionPart(fractionPart).build())
                .setDistance(distance)
                .setWaitTime(waitTime.toString())
                .setTime(time.toString())
                .build();

        when(stub.request(any())).thenReturn(response);

        final var actual = pricesProvider.get(waypoints, tariff);

        assertThat(actual.price()).isEqualTo(new BigDecimal(integerPart + "." + fractionPart));
        assertThat(actual.duration()).isEqualTo(time);
    }
}