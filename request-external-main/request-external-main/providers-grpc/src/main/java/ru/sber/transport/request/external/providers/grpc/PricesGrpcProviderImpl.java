package ru.sber.transport.request.external.providers.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.request.external.model.PriceData;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;
import ru.sber.transport.request.external.providers.exceptions.TariffProviderNotAvailableException;
import ru.sber.transport.tariff.external.ExternalTariff;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;

@Slf4j
@RequiredArgsConstructor
public class PricesGrpcProviderImpl implements PricesProvider {

    private final PriceDataServiceGrpc.PriceDataServiceBlockingStub stub;

    @Override
    public PriceData get(List<WaypointDTO> waypoints, Tariff tariff) {
        final var startPoint = waypoints.get(0);
        final var endPoint = waypoints.get(1);
        final var request = ExternalTariff.PriceDataRequest.newBuilder()
                .setStart(ExternalTariff.Coordinates.newBuilder().setLatitude(startPoint.latitude().doubleValue()).setLongitude(startPoint.longitude().doubleValue()).build())
                .setEnd(ExternalTariff.Coordinates.newBuilder().setLatitude(endPoint.latitude().doubleValue()).setLongitude(endPoint.longitude().doubleValue()).build())
                .addTariff(ExternalTariff.Tariff.valueOf(tariff.name()))
                .build();
        try {
            final var response = stub.request(request);
            return new PriceData() {

                @Override
                public BigDecimal price() {
                    final var cost = response.getCost();
                    return new BigDecimal(cost.getIntegerPart() + "." + cost.getFractionPart());
                }

                @Override
                public Duration duration() {
                    return Duration.parse(response.getTime());
                }

                @Override
                public URI link() {
                    return URI.create(response.getLink().getValue());
                }

                @Override
                public long distance() {
                    return response.getDistance();
                }
            };
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.RESOURCE_EXHAUSTED) {
                throw new TariffProviderNotAvailableException();
            }
            throw e;
        }
    }

}
