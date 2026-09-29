package ru.sberbank.ditsib.config;

import feign.Client;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Конфигурация для Feign клиента с JKS сертификатом для Сберовской среды
 */
@Slf4j
@Configuration
public class MainLeadFeignConfig {

    @Value("${main-lead-service.store}")
    private String store;
    @Value("${main-lead-service.password}")
    private String password;

    @Bean
    public Client feignClient() {
        try {
            var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(createStore(), password.toCharArray());
            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(createStore());
            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return new Client.Default(sslContext.getSocketFactory(), null);
        } catch (Exception e) {
            log.error("Ошибка настройки SSL для Feign клиента", e);
            throw new RuntimeException("Не удалось настроить SSL", e);
        }
    }

    @SneakyThrows
    private KeyStore createStore() {
        var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (var resourceAsStream = getClass().getClassLoader().getResourceAsStream(store)) {
            if (resourceAsStream == null) {
                keyStore.load(Files.newInputStream(Paths.get(store)), password.toCharArray());
            } else {
                keyStore.load(resourceAsStream, password.toCharArray());
            }

        }
        return keyStore;
    }
}