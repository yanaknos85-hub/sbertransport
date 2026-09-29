package ru.sber.transport.common.api.service.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфиг feign клиента.
 */
@Configuration
public class FeignConfig {


    /**
     * Конфигурация Feign Client отключение проверки валидности ssl совы.
     *
     * @return Http Client.
     */
    @Bean
    public Client feignClient() {
        return new ApacheHttpClient(getHttpClient());
    }

    private CloseableHttpClient getHttpClient() {
        var timeout = 10000;
        try {
            var sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustSelfSignedStrategy()).build();
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .build();
            return HttpClientBuilder
                    .create()
                    .useSystemProperties()
                    .setDefaultRequestConfig(config)
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
