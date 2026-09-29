package ru.sber.transport.authsb.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@Valid
@ConfigurationProperties(prefix = "cargo.sbid")
public class SberBusinessIdConfiguration {

    /**
     * ссылка на api SberSusinessID
     */
    @NotBlank(message = "Параметр 'cargo.sbid.urlSbid' должен быть установлен.")
    private String urlSbid;

    /**
     * ссылка на api SberSusinessID
     */
    @NotBlank(message = "Параметр 'cargo.sbid.restUrlSbid' должен быть установлен.")
    private String restUrlSbid;

    /**
     * метод для инициации OAuth-авторизации
     */
    @NotBlank(message = "Параметр 'cargo.sbid.getAuthMetod' должен быть установлен.")
    private String getAuthMetod;

    /**
     * метод для обмена authorization code на токены доступа
     */
    @NotBlank(message = "Параметр 'cargo.sbid.postAuthMetod' должен быть установлен.")
    private String postAuthMetod;

    /**
     * метод для получения информации о пользователе
     */
    @NotBlank(message = "Параметр 'cargo.sbid.getUserInfoMetod' должен быть установлен.")
    private String userInfoMetod;

    /**
     * Уникальный идентификатор Платформы (приложения), полученный при подключении к Sber API
     */
    @NotBlank(message = "Параметр 'cargo.sbid.clientId' должен быть установлен.")
    private String clientId;

    /**
     * Тип ответа. Авторизация по СберБизнес ID поддерживает только authorization code flow протокола OAuth 2.0.
     */
    @NotBlank(message = "Параметр 'cargo.sbid.responseType' должен быть установлен.")
    private String responseType;

    /**
     * Ссылка на ресурс (конечную точку) Платформы,
     * на которую будет передан код авторизации и перенаправлен браузер пользователя после успешной авторизации
     */
    @NotBlank(message = "Параметр 'cargo.sbid.redirectUri' должен быть установлен.")
    private String redirectUri;

    /**
     * Запрашиваемая область сведений. Набор атрибутов (claim) и операций,
     * которые будут доступны платформе после авторизации клиента
     */
    @NotBlank(message = "Параметр 'cargo.sbid.scope' должен быть установлен.")
    private String scope;

    /**
     * Секретный ключ приложения (не передается через браузер)
     */
    @NotBlank(message = "Параметр 'cargo.sbid.clientSecret' должен быть установлен.")
    private String clientSecret;

}
