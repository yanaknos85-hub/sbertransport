package ru.sber.transport.authsb.api.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.sber.transport.authsb.config.SberBusinessIdConfiguration;
import ru.sber.transport.authsb.config.UserServiceConfiguration;
import ru.sber.transport.authsb.exceptions.BadResponseException;

import java.util.Map;

import static ru.sber.transport.authsb.api.service.client.ParamRequest.*;
import static ru.sber.transport.authsb.enums.TokenTypeEnum.REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestApiSberBuisness {

    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    private final SberBusinessIdConfiguration sberBusinessIdConfiguration;

    private final UserServiceConfiguration userServiceConfiguration;

    private final static String HTTP_MSG = "HTTP ошибка при вызове SBID: {}";

    /**
     * Отправляет запрос в СберБизнес
     * @param grantType
     * @param param
     * @return
     */
    public Map<String, Object> sendForm(String grantType, String param) {
        log.info("Отправка формы с параметрами: grantType = {}, param = {}", grantType, param);
        String code = null;
        String refreshToken = null;

        if (REFRESH_TOKEN.getName().equals(grantType)) {
            refreshToken = param;
        } else {
            code = param;
        }

        MultiValueMap<String, String> formData = convertToFormData(grantType, code, sberBusinessIdConfiguration.getRedirectUri(), sberBusinessIdConfiguration.getClientId(), refreshToken);

        log.info("Отправка формы на URL: {}{}", sberBusinessIdConfiguration.getRestUrlSbid(), sberBusinessIdConfiguration.getPostAuthMetod());
        log.debug("Тело формы: {}", formData);

        try {
            return webClient
                    .post()
                    .uri(sberBusinessIdConfiguration.getRestUrlSbid() + sberBusinessIdConfiguration.getPostAuthMetod())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error(HTTP_MSG, e.getMessage(), e);
            String error = "unknown_error";
            String errorDescription = "No description";
            if (responseBody == null || responseBody.isBlank()) {
                log.warn("Пустое тело запроса от SBID");
                throw new BadResponseException("Пустой или невалидный ответ от SBID");
            }
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                error = (String) errorResponse.getOrDefault(ERROR, error);
                errorDescription = (String) errorResponse.getOrDefault("description", errorDescription);
            } catch (Exception parseEx) {
                log.error(HTTP_MSG, e.getMessage(), e);
            }
            var userMessage = String.format("Ошибка авторизации: %s — %s", error, errorDescription);
            throw new BadResponseException(userMessage);
        } catch (Exception e) {
            throw new BadResponseException("Ошибка при вызове SBID");
        }
    }

    /**
     * Получает информацию о пользователе из СберБизнес
     */
    public String getUserInfo(String authToken) {
        try {
            return webClient
                    .get()
                    .uri(sberBusinessIdConfiguration.getRestUrlSbid() + sberBusinessIdConfiguration.getUserInfoMetod())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            var responseBody = e.getResponseBodyAsString();
            log.error(HTTP_MSG, e.getMessage(), e);
            var error = "unknown_error";
            var errorDescription = "No description";
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                error = (String) errorResponse.getOrDefault(ERROR, error);
                errorDescription = (String) errorResponse.getOrDefault("error_description", errorDescription);
            } catch (Exception parseEx) {
                log.error(HTTP_MSG, e.getMessage(), e);
            }
            var userMessage = String.format("Ошибка авторизации: %s — %s", error, errorDescription);
            throw new BadResponseException(userMessage);
        } catch (Exception e) {
            log.error("Неизвестная ошибка при вызове SBID", e);
            throw new BadResponseException("Ошибка при вызове SBID");
        }
    }

    private MultiValueMap<String, String> convertUserToFormData(Map<String, Object> userAttributes) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        if (userAttributes != null) {
            userAttributes.forEach((key, value) -> {
                if (value != null) {
                    formData.add(key, value.toString());
                }
            });
        }
        return formData;
    }

    /**
     * Отправляет данные пользователя в пользовательский сервис
     * @param userAttributes данные пользователя
     * @return ответ от пользовательского сервиса
     */
    public String sendToUserService(Map<String, Object> userAttributes) {
        try {
            MultiValueMap<String, String> formData = convertUserToFormData(userAttributes);

            return webClient
                    .post()
                    .uri(userServiceConfiguration.getRestUrl() + userServiceConfiguration.getPostAuthMetod())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("HTTP ошибка при отправке данных в пользовательский сервис: {}, тело ответа: {}", e.getMessage(), e.getResponseBodyAsString());
            throw new BadResponseException("Ошибка при взаимодействии с пользовательским сервисом");
        } catch (Exception e) {
            log.error("Неизвестная ошибка при отправке данных в пользовательский сервис", e);
            throw new BadResponseException("Ошибка при вызове пользовательского сервиса");
        }
    }

    private MultiValueMap<String, String> convertToFormData(String grantType, String code, String redirectUri, String clientId, String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(GRANT_TYPE, grantType);
        if (code != null) {
            formData.add(CODE, code);
            formData.add(REDIRECT_URI, redirectUri);
        }
        if (refreshToken != null) {
            formData.add(ParamRequest.REFRESH_TOKEN, refreshToken);
        }
        formData.add(CLIENT_ID, clientId);
        formData.add(CLIENT_SECRET, sberBusinessIdConfiguration.getClientSecret());
        return formData;
    }
}
