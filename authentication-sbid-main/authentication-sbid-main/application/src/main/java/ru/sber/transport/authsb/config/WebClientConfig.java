package ru.sber.transport.authsb.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;

@Configuration
@Slf4j
@AllArgsConstructor
public class WebClientConfig {

    private final SSLConfiguration sslConfiguration;

    @Bean("sbidWebClient")
    public WebClient webClient() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(createTrustStore());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(createKeyStore(), sslConfiguration.getKeystorePassword().toCharArray());

            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .keyManager(kmf)
                    .trustManager(tmf)
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(
                                    ApplicationProtocolConfig.Protocol.ALPN,
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    ApplicationProtocolNames.HTTP_1_1
                            ))
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext))
                    .responseTimeout(java.time.Duration.ofSeconds(30));

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (Exception e) {
            log.error("Не удалось создать WebClient для SBID. Проверьте настройки SSL: keystore/truststore, пароли, пути к файлам.", e);
            throw new IllegalStateException("Ошибка инициализации WebClient", e);
        }
    }

    private KeyStore createKeyStore() {
        try {
            var keyStore = KeyStore.getInstance("PKCS12");
            try (var resourceAsStream = getClass().getClassLoader()
                    .getResourceAsStream(sslConfiguration.getKeystore())) {
                if (resourceAsStream == null) {
                    keyStore.load(Files.newInputStream(Paths.get(sslConfiguration.getKeystore())),
                            sslConfiguration.getKeystorePassword().toCharArray());
                } else {
                    keyStore.load(resourceAsStream, sslConfiguration.getKeystorePassword().toCharArray());
                }
            }
            return keyStore;
        } catch (Exception e) {
            log.error("Не удалось загрузить keystore из пути: {}. Убедитесь, что файл существует и пароль верный.", sslConfiguration.getKeystore(), e);
            throw new IllegalStateException("Ошибка загрузки keystore", e);
        }
    }


    private KeyStore createTrustStore() {
        try {
            var keyStore = KeyStore.getInstance("JKS");
            try (var resourceAsStream = getClass().getClassLoader()
                    .getResourceAsStream(sslConfiguration.getTruststore())) {
                if (resourceAsStream == null) {
                    keyStore.load(Files.newInputStream(Paths.get(sslConfiguration.getTruststore())),
                            sslConfiguration.getTruststorePassword().toCharArray());
                } else {
                    keyStore.load(resourceAsStream, sslConfiguration.getTruststorePassword().toCharArray());
                }
            }
            return keyStore;
        } catch (Exception e) {
            log.error("Не удалось загрузить truststore из пути: {}. Убедитесь, что файл существует и пароль верный.", sslConfiguration.getTruststore(), e);
            throw new IllegalStateException("Ошибка загрузки truststore", e);
        }
    }
}