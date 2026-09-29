package ru.sberbank.ditsib.transport.request;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sberbank.ditsib.transport.Microservice;
import ru.sberbank.ditsib.transport.request.config.AllPointsMaxWaitTimeProperties;
import ru.sberbank.ditsib.transport.request.config.DispatcherProperties;
import ru.sberbank.ditsib.transport.request.config.publicTransport.FileStorageProperties;
import ru.sberbank.ditsib.transport.request.service.taxiprice.properties.TaxiProvidersProperties;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@EnableTransactionManagement
@OpenAPIDefinition(info = @Info(title = "Заявки",
                                description = "Операции по работе с заявками",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableFeignClients
@EnableConfigurationProperties({ AllPointsMaxWaitTimeProperties.class, FileStorageProperties.class,
                                 DispatcherProperties.class,
                                 TaxiProvidersProperties.class })
@EnableJpaRepositories(
        basePackages = { "ru.sberbank.ditsib.transport.request", "ru.sber.transport.humanreadableid" },
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class
)
@EntityScan(basePackages = { "ru.sberbank.ditsib.transport.request", "ru.sber.transport.humanreadableid" })
@ComponentScan(
        basePackages = {
                "ru.sberbank.ditsib.transport.request",
                "ru.sber.transport.humanreadableid",
                "ru.sberbank.ditsib.transport.logging",
                "ru.sberbank.ditsib.transport"})
@EnableAsync
@EnableScheduling
@GrpcClientBean(client = @GrpcClient("files"), clazz = DeleteServiceGrpc.DeleteServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = DownloadServiceGrpc.DownloadServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = UploadServiceGrpc.UploadServiceStub.class)
@FileExchange
@NoAuthorize("/monitoring")
public class RequestApplication {
    
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(RequestApplication.class, args);
    }
    
    
}
