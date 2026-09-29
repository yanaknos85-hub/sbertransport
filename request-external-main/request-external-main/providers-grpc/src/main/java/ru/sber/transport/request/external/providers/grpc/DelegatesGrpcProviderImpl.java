package ru.sber.transport.request.external.providers.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.corporate.grpc.service.DelegatesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.request.external.model.Delegate;
import ru.sber.transport.request.external.model.Employee;

@RequiredArgsConstructor
public class DelegatesGrpcProviderImpl implements DelegatesProvider {

    private final DelegatesGrpc.DelegatesStub stub;

    @Override
    public void save(@NonNull Delegate source) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }

    @Override
    public List<Employee> get(UUID headId) {
        throw new UnsupportedOperationException("gRPC does not support this method");
    }

    @SneakyThrows(InterruptedException.class)
    @Override
    public List<Delegate> getAll() {
        final var list = new ArrayList<Delegate>();
        final var semaphore = new Semaphore(0);
        final var error = new AtomicReference<RuntimeException>();
        stub.all(Empty.getDefaultInstance(), new StreamObserver<>() {

            @Override
            public void onNext(OrganizationsOuterClass.Delegate delegate) {
                if ("TAXI".equals(delegate.getType())) {
                    list.add(createDelegate(delegate));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(new RuntimeException(throwable));
                semaphore.release();
            }

            @Override
            public void onCompleted() {
                semaphore.release();
            }
        });
        semaphore.acquire();
        if (error.get() != null) {
            throw error.get();
        }
        return list;
    }

    private Delegate createDelegate(OrganizationsOuterClass.Delegate delegate) {
        return new Delegate() {

            @Override
            public UUID delegateId() {
                return UUID.fromString(delegate.getDelegateId());
            }

            @Override
            public UUID supervisorId() {
                return UUID.fromString(delegate.getSupervisorId());
            }

            @Override
            public LocalDate startDate() {
                return createDate(delegate.getStartDate());
            }

            @Override
            public LocalDate endDate() {
                return createDate(delegate.getEndDate());
            }

            @Override
            public boolean active() {
                return delegate.getActive();
            }
        };
    }

    private LocalDate createDate(OrganizationsOuterClass.NullableDate source) {
        return source.hasValue() ? createDate(source.getValue()) : null;
    }

    private LocalDate createDate(OrganizationsOuterClass.Date source) {
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDay());
    }
}
