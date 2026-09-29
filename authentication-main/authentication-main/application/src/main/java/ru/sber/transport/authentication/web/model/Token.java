package ru.sber.transport.authentication.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * Object with data about token.
 */
@Getter
@Setter
@Schema(title = "Информация о токене", description = "Токен доступа к ресурсам и повторного получения токена доступа")
public class Token {
    
    /**
     * Access token.
     */
    @JsonProperty("token")
    @Schema(description = "Токен доступа к ресурсам. Содержит информацию о вошедшем " +
                          "пользователе. Его необходимо отправлять заголовком " +
                          "`Authorization: Bearer {token}` с каждым запросом.")
    private String accessToken;
    
    /**
     * Re-login token.
     */
    @Schema(description = "Токен повторного входа. Отправлять с заголовком " +
                          "`Authorization: Token {refreshToken}` при истечении " +
                          "срока жизни токена доступа.")
    private String refreshToken;
    
    @Schema(description = "Транспортный пароль")
    boolean transferPassword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime accessExpiration;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime refreshExpiration;

}
