package ru.sber.transport.request.external.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.corporate.grpc.service.DelegatesGrpc;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.request_checks.grpc.OverrunRequestCheckServiceGrpc;
import ru.sber.transport.request_checks.grpc.RequestCheckServiceGrpc;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;

/**
 * Конфигурация gRPC клиентов
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = true)
@GrpcClientBean(clazz = DownloadServiceGrpc.DownloadServiceStub.class, client = @GrpcClient("files"))
@GrpcClientBean(clazz = UploadServiceGrpc.UploadServiceStub.class, client = @GrpcClient("files"))
@GrpcClientBean(clazz = DeleteServiceGrpc.DeleteServiceStub.class, client = @GrpcClient("files"))
@GrpcClientBean(clazz = OrganizationsGrpc.OrganizationsBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = DepartmentsGrpc.DepartmentsBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = EmployeesGrpc.EmployeesBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = DelegatesGrpc.DelegatesStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = PriceDataServiceGrpc.PriceDataServiceBlockingStub.class, client = @GrpcClient("tariff"))
@GrpcClientBean(clazz = LimitServiceGrpc.LimitServiceStub.class, client = @GrpcClient("limits"))
@GrpcClientBean(clazz = GeoZonesServiceGrpc.GeoZonesServiceBlockingStub.class, client = @GrpcClient("geo-zones"))
@GrpcClientBean(clazz = RequestCheckServiceGrpc.RequestCheckServiceBlockingStub.class, client = @GrpcClient("request-checks"))
@GrpcClientBean(clazz = OverrunRequestCheckServiceGrpc.OverrunRequestCheckServiceBlockingStub.class, client = @GrpcClient("request-checks"))
public class Stubs {

    /**
     * Инициализация конфигурации gRPC
     */
    @PostConstruct
    public void init() {
        log.info("Configuring gRPC stubs");
    }

}
