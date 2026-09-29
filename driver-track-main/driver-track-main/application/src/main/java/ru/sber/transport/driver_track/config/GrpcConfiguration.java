package ru.sber.transport.driver_track.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;

@Configuration
@GrpcClientBean(client = @GrpcClient("geo"), clazz = GeoServiceGrpc.GeoServiceBlockingStub.class)
public class GrpcConfiguration {
}
