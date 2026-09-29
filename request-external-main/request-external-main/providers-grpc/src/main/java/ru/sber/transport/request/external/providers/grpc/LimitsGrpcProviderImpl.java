package ru.sber.transport.request.external.providers.grpc;

import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.LimitsProvider;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.providers.exceptions.ReserveException;

@Slf4j
@RequiredArgsConstructor
public class LimitsGrpcProviderImpl implements LimitsProvider {

    private final LimitServiceGrpc.LimitServiceStub stub;

    private final String service;

    private final String type;

    @Override
    public void reserve(TripOrderData source, Function<TripOrderData, BigDecimal> sumFunc) {
        final var id = source.getId();
        final var cost = sumFunc.apply(source);
        final var date = source.getDate();
        final var responseObserver = new ResponseObserver();
        final var request = stub.reserve(responseObserver);
        final var costData = ru.sber.transport.limits.grpc.service.Limits.Cost.newBuilder()
                .setIntegerPart(cost.intValue())
                .setFractionPart(cost.toPlainString().contains(".") ? Integer.parseInt(cost.toPlainString().split("\\.")[1]) : 0)
                .build();
        final var data = ru.sber.transport.limits.grpc.service.Limits.ReserveRequest.newBuilder()
                .setId(id.toString())
                .setCost(costData)
                .setService(service)
                .setType(type)
                .setDate(ru.sber.transport.limits.grpc.service.Limits.DateTime.newBuilder()
                        .setYear(date.getYear())
                        .setMonth(date.getMonthValue())
                        .setDay(date.getDayOfMonth())
                        .setHour(date.getHour())
                        .setMinute(date.getMinute())
                        .setSecond(date.getSecond())
                        .setMillis(date.getNano())
                        .setZone(date.getOffset().getId())
                        .build())
                .setConsumer(source.getPassenger().getId().toString())
                .build();
        request.onNext(data);
        request.onCompleted();
        responseObserver.checkResponse();
    }

    @Override
    public void confirm(UUID id) {
        final var responseObserver = new ResponseObserver();
        final var request = stub.confirm(responseObserver);
        request.onNext(ru.sber.transport.limits.grpc.service.Limits.FixRequest.newBuilder().setId(id.toString()).build());
        request.onCompleted();
        responseObserver.checkResponse();
    }

    @Override
    public void cancel(UUID id) {
        final var responseObserver = new ResponseObserver();
        final var request = stub.cancel(responseObserver);
        request.onNext(ru.sber.transport.limits.grpc.service.Limits.FixRequest.newBuilder().setId(id.toString()).build());
        request.onCompleted();
        responseObserver.checkResponse();
    }

    private static final class ResponseObserver implements StreamObserver<ru.sber.transport.limits.grpc.service.Limits.Response> {

        private final AtomicReference<ru.sber.transport.limits.grpc.service.Limits.Response> result = new AtomicReference<>();
        private final AtomicReference<Throwable> error = new AtomicReference<>();
        private final Semaphore semaphore = new Semaphore(1);

        @SneakyThrows(InterruptedException.class)
        ResponseObserver() {
            semaphore.acquire();
        }

        @Override
        public void onNext(ru.sber.transport.limits.grpc.service.Limits.Response response) {
            result.set(response);
        }

        @Override
        public void onError(Throwable throwable) {
            error.set(throwable);
            semaphore.release();
        }

        @Override
        public void onCompleted() {
            semaphore.release();
        }

        @SneakyThrows(InterruptedException.class)
        void checkResponse() {
            semaphore.acquire();
            if (error.get() != null) {
                throw new IllegalStateException(error.get());
            }
            switch (result.get().getStatus()) {
                case DATA_NOT_FOUND -> throw new ReserveException(ReserveException.Type.DATA_NOT_FOUND);
                case NOT_SUFFICIENT -> throw new ReserveException(ReserveException.Type.NOT_SUFFICIENT);
                case SERVICE_NOT_AVAILABLE -> throw new ReserveException(ReserveException.Type.SERVICE_NOT_AVAILABLE);
                case TYPE_NOT_AVAILABLE -> throw new ReserveException(ReserveException.Type.TYPE_NOT_AVAILABLE);
                default -> log.debug("Reserve succeeded");
            }
        }
    }
}
