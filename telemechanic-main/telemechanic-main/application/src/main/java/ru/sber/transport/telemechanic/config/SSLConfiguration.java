package ru.sber.transport.telemechanic.config;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.telemechanic.config.properties.PredictProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;


@Configuration
@RequiredArgsConstructor
public class SSLConfiguration {
    
    private final PredictProperties properties;
    
    @Bean
    public RestTemplate restTemplate() throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {
        var clientCertificate = properties.getStore();
        var clientCertPassword = properties.getPassword();
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        
        File file;
        var optionalFile = Optional.ofNullable(getClass().getClassLoader().getResource(clientCertificate));
        if (optionalFile.isPresent()) {
            file = ResourceUtils.getFile(optionalFile.get());
        } else {
            file = Paths.get(clientCertificate).toFile();
        }

        if(!file.exists()) {
            throw new IOException("File not found:" + clientCertificate);
        }
        
        var sslContextBuilder = SSLContextBuilder.create();
        sslContextBuilder.loadKeyMaterial(file,
                                          clientCertPassword.toCharArray(),
                                          clientCertPassword.toCharArray());
        sslContextBuilder.loadTrustMaterial(null, acceptingTrustStrategy);
        var sslContext = sslContextBuilder.build();

        var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).build();
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(sslSocketFactory).build();
        HttpClient httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.custom().setConnectionManager(cm).evictExpiredConnections().build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}