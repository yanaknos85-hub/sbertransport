package ru.sber.transport.request.external.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.business.providers.LimitsProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.corporate.grpc.service.DelegatesGrpc;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.providers.grpc.DelegatesGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.DepartmentsGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.EmployeesGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.GeoZonesGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.LimitsGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.OrganizationsProviderGrpcProvaderImpl;
import ru.sber.transport.request.external.providers.grpc.PricesGrpcProviderImpl;
import ru.sber.transport.request.external.providers.grpc.RequestGrpcProviderImpl;
import ru.sber.transport.tariff.external.PriceDataServiceGrpc;

/**
 * Конфигурация gRPC клиентов
 */
@Slf4j
@Configuration
public class Grpc {

    static final String GRPC_EMPLOYEES = "grpcEmployees";

    static final String GRPC_DEPARTMENTS = "grpcDepartments";

    static final String GRPC_DELEGATES = "grpcDelegates";

    static final String GRPC_ORGANIZATIONS = "grpcOrganizations";

    static final String GRPC_GEO_ZONES = "grpcGeoZones";

    /**
     * Инициализация конфигурации gRPC
     */
    @PostConstruct
    public void init() {
        log.info("Configuring gRPC application");
    }

    /**
     * GRPC-провайдер организаций
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер организаций
     */
    @Bean(name = GRPC_ORGANIZATIONS, bootstrap = Bean.Bootstrap.DEFAULT)
    public OrganizationsProvider grpcOrganizations(OrganizationsGrpc.OrganizationsBlockingStub stub) {
        log.info("Creating organizations grpc provider");
        return new OrganizationsProviderGrpcProvaderImpl(stub);
    }

    /**
     * GRPC-провайдер геозон
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер геозон
     */
    @Bean(name = GRPC_GEO_ZONES, bootstrap = Bean.Bootstrap.DEFAULT)
    public GeoZonesProvider grpcGeoZones(GeoZonesServiceGrpc.GeoZonesServiceBlockingStub stub) {
        log.info("Creating geo-zones grpc provider");
        return new GeoZonesGrpcProviderImpl(stub);
    }

    /**
     * gRPC-провайдер департаментов
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер департаментов
     */
    @Bean(name = GRPC_DEPARTMENTS, bootstrap = Bean.Bootstrap.DEFAULT)
    public DepartmentsProvider grpcDepartments(DepartmentsGrpc.DepartmentsBlockingStub stub) {
        log.info("Creating departments grpc provider");
        return new DepartmentsGrpcProviderImpl(stub);
    }

    /**
     * gRPC-провайдер департаментов
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер департаментов
     */
    @Bean(name = GRPC_DELEGATES, bootstrap = Bean.Bootstrap.DEFAULT)
    public DelegatesProvider grpcDelegates(DelegatesGrpc.DelegatesStub stub) {
        log.info("Creating delegates grpc provider");
        return new DelegatesGrpcProviderImpl(stub);
    }

    /**
     * gRPC-провайдер сотрудников
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер сотрудников
     */
    @Bean(name = GRPC_EMPLOYEES, bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeesProvider grpcEmployees(EmployeesGrpc.EmployeesBlockingStub stub) {
        log.info("Creating employees grpc provider");
        return new EmployeesGrpcProviderImpl(stub);
    }

    /**
     * Провайдер тарифов
     *
     * @param stub gRPC-клиент
     * @return Провайдер тарифов
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public PricesProvider prices(PriceDataServiceGrpc.PriceDataServiceBlockingStub stub) {
        log.info("Creating prices gRPC provider");
        return new PricesGrpcProviderImpl(stub);
    }

    /**
     * Провайдер лимитов
     *
     * @param stub gRPC-клиент
     * @return провайдер лимитов
     */
    @ConditionalOnProperty(value = "limits.enabled", havingValue = "true", matchIfMissing = true)
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public LimitsProvider limitsProvider(LimitServiceGrpc.LimitServiceStub stub,
        @Value("${limits.service:PASSENGER}") String service, @Value("${limits.type:TAXI}") String type) {
        log.info("Creating limits provider");
        return new LimitsGrpcProviderImpl(stub, service, type);
    }

    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public RequestGrpcProviderImpl requestGrpcProviderImpl(
        TripOrdersService service,
        TripOrdersProvider provider
    ) {
        return new RequestGrpcProviderImpl(
            service,
            provider
        );
    }

}
