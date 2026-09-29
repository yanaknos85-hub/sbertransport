package ru.sber.transport.telemechanic.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;

@Configuration
@GrpcClientBean(client = @GrpcClient("grpc-files"), clazz = DeleteServiceGrpc.DeleteServiceStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-files"), clazz = DownloadServiceGrpc.DownloadServiceStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-files"), clazz = UploadServiceGrpc.UploadServiceStub.class)
public class FilesGrpcConfig {
}
