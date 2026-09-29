package ru.sber.transport.telemechanic.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.telemechanic.service.grpc.Departments;
import ru.sber.transport.telemechanic.service.grpc.Organizations;
import ru.sber.transport.telemechanic.service.grpc.Positions;
import ru.sber.transport.telemechanic.service.grpc.impl.DepartmentsImpl;
import ru.sber.transport.telemechanic.service.grpc.impl.OrganizationsImpl;
import ru.sber.transport.telemechanic.service.grpc.impl.PositionsImpl;

/**
 * Конфигурация gRPC клиента grpc-corporate
 */
@Slf4j
@Configuration
@GrpcClientBean(client = @GrpcClient("grpc-corporate"), clazz = OrganizationsGrpc.OrganizationsBlockingStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-corporate"), clazz = DepartmentsGrpc.DepartmentsBlockingStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-corporate"), clazz = PositionsGrpc.PositionsBlockingStub.class)
public class GrpcCorporateConfig {
    
    /**
     * Инициализация конфигурации gRPC
     */
    @PostConstruct
    public void init() {
        log.info("Configuring grpc-corporate stubs");
    }
    
    @Bean(name = "grpcOrganizations", bootstrap = Bean.Bootstrap.DEFAULT)
    public Organizations grpcOrganizations(OrganizationsGrpc.OrganizationsBlockingStub stub) {
        log.info("Creating organizations grpc provider");
        return new OrganizationsImpl(stub);
    }
    
    @Bean(name = "grpcDepartments", bootstrap = Bean.Bootstrap.DEFAULT)
    public Departments grpcDepartments(DepartmentsGrpc.DepartmentsBlockingStub stub) {
        log.info("Creating departments grpc provider");
        return new DepartmentsImpl(stub);
    }
    
    @Bean(name = "grpcPositions", bootstrap = Bean.Bootstrap.DEFAULT)
    public Positions grpcPositions(PositionsGrpc.PositionsBlockingStub stub) {
        log.info("Creating positions grpc provider");
        return new PositionsImpl(stub);
    }
}