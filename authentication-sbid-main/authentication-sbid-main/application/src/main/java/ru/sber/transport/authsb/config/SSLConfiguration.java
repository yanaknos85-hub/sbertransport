package ru.sber.transport.authsb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Valid
@ConfigurationProperties(prefix = "app")
public class SSLConfiguration {

    @NotBlank(message = "Путь к truststore не может быть пустым")
    private String truststore;

    @NotBlank(message = "Пароль от truststore не может быть пустым")
    private String truststorePassword;

    @NotBlank(message = "Путь к keystore не может быть пустым")
    private String keystore;

    @NotBlank(message = "Пароль от keystore не может быть пустым")
    private String keystorePassword;

    @NotBlank(message = "Имя приватного ключа не может быть пустым")
    private String privateKey;
}
