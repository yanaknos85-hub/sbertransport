package ru.sber.transport.authsb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Valid
@ConfigurationProperties(prefix = "cargo.user")
public class UserServiceConfiguration {

    /**
     * ссылка на урл сервиса UserService
     */
    @NotBlank(message = "Параметр 'cargo.user.restUrl' должен быть установлен.")
    private String restUrl;

    /**
     * ссылка на пост метод UserService
     */
    @NotBlank(message = "Параметр 'cargo.user.postAuthMetod' должен быть установлен.")
    private String postAuthMetod;
}
