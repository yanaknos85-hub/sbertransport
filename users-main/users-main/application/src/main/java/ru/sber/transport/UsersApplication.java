package ru.sber.transport;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sber.transport.utils.collections.MapUtils;

/**
 * Application.
 */
@SpringBootApplication(scanBasePackages = {"ru.sber.transport.web", "ru.sber.transport.users"})
@FileExchange
@GrpcClientBean(client = @GrpcClient("files"), clazz = DeleteServiceGrpc.DeleteServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = DownloadServiceGrpc.DownloadServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = UploadServiceGrpc.UploadServiceStub.class)
@Import(MapUtils.class)
@EnableDiscoveryClient
public class UsersApplication {
    
    /**
     * Entry point to application.
     *
     * @param args arguments for starting application.
     */
    public static void main(String... args) {
        SpringApplication.run(UsersApplication.class, args);
    }
    
}
