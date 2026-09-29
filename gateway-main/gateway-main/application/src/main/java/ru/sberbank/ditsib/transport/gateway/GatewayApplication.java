package ru.sberbank.ditsib.transport.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Application.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class GatewayApplication {
    
    /**
     * Entry point to application.
     *
     * @param args arguments for starting application.
     */
    public static void main(String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
