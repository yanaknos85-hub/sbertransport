package ru.sber.transport.authsb.services.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.authsb.api.service.client.RestApiSberBuisness;
import ru.sber.transport.authsb.config.SSLConfiguration;
import ru.sber.transport.authsb.config.SberBusinessIdConfiguration;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.database.model.Role;
import ru.sber.transport.authsb.database.model.Session;
import ru.sber.transport.authsb.database.model.User;
import ru.sber.transport.authsb.dto.TokenResponseDto;
import ru.sber.transport.authsb.enums.TokenTypeEnum;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.exceptions.ResponseNotFoundException;
import ru.sber.transport.authsb.jwt.GostJwtDecoder;
import ru.sber.transport.authsb.services.OrganizationService;
import ru.sber.transport.authsb.services.ServiceController;
import ru.sber.transport.authsb.services.SessionService;
import ru.sber.transport.authsb.services.UserService;
import ru.sber.transport.authsb.utils.DateUtils;
import ru.sber.transport.authsb.utils.HashUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sber.transport.authsb.api.service.client.ParamRequest.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class ServiceControllerImpl implements ServiceController {

    public static final String INCORRECT_PARAMS_MSG = "Некорректный формат параметров";
    public static final String RESPONSE_MSG = "Получен ответ от сервиса: {}";

    private final SSLConfiguration sslConfiguration;

    private final SberBusinessIdConfiguration sberBusinessIdConfiguration;

    private final RestApiSberBuisness restApiSberBuisness;

    private final SessionService sessionService;

    private final GostJwtDecoder gostJwtDecoder;

    private final UserService userService;

    private final OrganizationService organizationService;

    private static final int TOKEN_LIFETIME = 25500300;

    /**
     * Сервис для получения ссылки для авторизации
     *
     * @param sessionId id сессии
     * @return ссылка для авторизации
     */
    @Override
    public String createUrl(String sessionId) {

        if (!StringUtils.hasText(sessionId)) {
            log.warn("Не указан параметр sessionId");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан параметр sessionId");
        }

        String state = HashUtils.hashSha256(sessionId);

        // Проверяем, существует ли уже сессия
        if (sessionService.findByState(state).isPresent()) {
            log.warn("Сессия уже существует: sessionId = {}", sessionId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сессия с таким идентификатором уже существует");
        }

        String nonce = HashUtils.hashSha256(UUID.randomUUID().toString());

        Session session = Session.builder()
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .nonce(nonce)
                .state(state)
                .active(true)
                .build();


        sessionService.save(session);
        log.debug("Сессия сохранена в БД: sessionId =  {}", sessionId);


        return sberBusinessIdConfiguration.getUrlSbid()
                + sberBusinessIdConfiguration.getGetAuthMetod()
                + "?client_id=" + sberBusinessIdConfiguration.getClientId()
                + "&response_type=" + sberBusinessIdConfiguration.getResponseType()
                + "&redirect_uri=" + sberBusinessIdConfiguration.getRedirectUri()
                + "&scope=" + replaceSpacesWithPlus(sberBusinessIdConfiguration.getScope())
                + "&state=" + state
                + "&nonce=" + nonce;
    }

    private String replaceSpacesWithPlus(String input) {
        return input != null ? input.replace(" ", "+") : "";
    }

    @Override
    @Transactional
    public TokenResponseDto getAuthToken(String code, String state) {
        validateState(state);

        // Проверяем, существует ли уже сессия
        Session session = findActiveSession(state);
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));

        // Проверяем, не истекла ли сессия
        validateSessionExpiration(session, now);

        // Получаем токены доступа из СберБизнес
        Map<String, Object> result = exchangeCode(code);

        // валидация ответа
        validateRequiredFields(result);

        // валидация id токена и декодирование
        Map<String, Object> idTokenClaims = decodeAndValidateIdToken(result, session);

        // обновляем сессию
        updateSessionWithTokens(session, result, idTokenClaims, code, now);

        // проверка ЮЗЕРА
        var user = findUser((String) result.get(ACCESS_TOKEN));

        sessionService.save(session);
        log.debug("Сессия сохранена в БД: sessionId = {}", session.getId());
        return buildTokenResponseWithGeneratedToken(result, user.getId(), user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()));
    }

    /**
     * Обновление токенов доступа
     *
     * @param refreshToken токен обновления
     * @return новые токены доступа
     */
    @Override
    public TokenResponseDto refreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadResponseException(INCORRECT_PARAMS_MSG);
        }
        Session session = sessionService.findByRefreshTokenAndActiveTrue(refreshToken);
        var result = restApiSberBuisness.sendForm(TokenTypeEnum.REFRESH_TOKEN.getName(), refreshToken);
        log.info(RESPONSE_MSG, result);
        session.setExpiresIn(Integer.valueOf(result.get(EXPIRES_IN).toString()));
        session.setRefreshToken((String) result.get(REFRESH_TOKEN));
        session.setAccessToken((String) result.get(ACCESS_TOKEN));
        var user = userService.findBySub(session.getSub()).orElseThrow();
        sessionService.save(session);
        return buildTokenResponseWithGeneratedToken(result, user.getId(),
                user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()));
    }

    private User findUser(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BadResponseException(INCORRECT_PARAMS_MSG);
        }
        return buildUser(accessToken);

    }

    private void validateState(String state) {
        if (!StringUtils.hasText(state)) {
            throw new BadResponseException(INCORRECT_PARAMS_MSG);
        }
    }

    private Session findActiveSession(String state) {
        Session session = sessionService.findByStateAndActiveTrue(state);
        log.info("Найдена активная сессия по state: {}", session.getState());
        return session;
    }

    private void validateSessionExpiration(Session session, LocalDateTime now) {
        if (!(session.getCreationTime().isBefore(now) && session.getCreationTime().plusDays(180).isAfter(now))) {
            log.warn("Сессия с таким state {} устарела, так как неактивна более 180 дней", session.getState());
            throw new BadResponseException("Ваша сессия устарела");
        }
    }

    /**
     * Стучимся в СберБизнес за токенами
     * @param code
     * @return
     */
    private Map<String, Object> exchangeCode(String code) {
        Map<String, Object> result;
        if (code != null) {
            result = restApiSberBuisness.sendForm(TokenTypeEnum.AUTHORIZATION_CODE.getName(), code);
        } else {
            throw new BadResponseException("Нет авторизации");
        }
        return result;
    }

    private void validateRequiredFields(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            throw new ResponseNotFoundException(sberBusinessIdConfiguration.getRestUrlSbid() + sberBusinessIdConfiguration.getPostAuthMetod());
        }

        var error = new StringBuilder();
        if (result.get(ACCESS_TOKEN) == null) {
            error.append(ACCESS_TOKEN).append("\n");
        }
        if (result.get(TOKEN_TYPE) == null) {
            error.append(TOKEN_TYPE).append("\n");
        }
        if (result.get(EXPIRES_IN) == null) {
            error.append(EXPIRES_IN).append("\n");
        }
        if (result.get(ID_TOKEN) == null) {
            error.append(ID_TOKEN).append("\n");
        }

        if (error.length() > 0) {
            log.error(String.format(NOT_FOUND_FIELDS_MSG_FORMAT,
                    sberBusinessIdConfiguration.getRestUrlSbid() + sberBusinessIdConfiguration.getPostAuthMetod(),
                    error.insert(0, "\n").toString()));
            throw new ResponseNotFoundException(
                    sberBusinessIdConfiguration.getRestUrlSbid() + sberBusinessIdConfiguration.getPostAuthMetod(),
                    error.insert(0, "\n").toString());
        }
    }

    private Map<String, Object> decodeAndValidateIdToken(Map<String, Object> result, Session session) {
        Map<String, Object> claims = gostJwtDecoder.decode((String) result.get(ID_TOKEN));
        if (claims == null) {
            throw new BadResponseException("Неверная подпись");
        }

        // Валидация nonce
        String nonce = (String) claims.get(NONCE);
        if (nonce == null || !session.getNonce().equals(nonce)) {
            throw new BadResponseException("Неверный nonce");
        }

        // Валидация aud
        String aud = (String) claims.get(AUD);
        if (!sberBusinessIdConfiguration.getClientId().equals(aud)) {
            throw new BadResponseException("Неверный клиент SB");
        }

        // Валидация iss
        String iss = "https://" + claims.get(ISS);
        if (!sberBusinessIdConfiguration.getUrlSbid().equals(iss)) {
            throw new BadResponseException("Неверный url");
        }
        return claims;
    }

    private void updateSessionWithTokens(Session session, Map<String, Object> result, Map<String, Object> claims, String code, LocalDateTime now) {
        session.setSub((String) claims.get(SUB));
        session.setAccessToken((String) result.get(ACCESS_TOKEN));
        session.setRefreshToken((String) result.get(REFRESH_TOKEN));
        session.setExpiresIn(Integer.valueOf(result.get(EXPIRES_IN).toString()));
        if (code != null) {
            session.setCode(code);
            session.setCreationTime(now);
        }
    }

    private TokenResponseDto buildTokenResponseWithGeneratedToken(Map<String, Object> result, UUID id, Set<String> roles) {
        return TokenResponseDto.builder()
                .refreshToken((String) result.get(REFRESH_TOKEN))
                .accessToken(generateToken(id.toString(), roles))
                .accessExpiration(Integer.valueOf(result.get(EXPIRES_IN).toString()))
                .refreshExpiration(TOKEN_LIFETIME)
                .build();
    }

    public User buildUser(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BadResponseException(INCORRECT_PARAMS_MSG);
        }

        try {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
            String resultToken = restApiSberBuisness.getUserInfo(token);
            var result = gostJwtDecoder.decode(resultToken);
            log.info(RESPONSE_MSG, result);
            log.debug("Поиск организации по огрн = {} и кпп = {}", result.get(ORG_OGRN), result.get(ORG_KPP));
            var org = organizationService.findByOgrnAndKpp(
                            (String) result.get(ORG_OGRN),
                            (String) result.get(ORG_KPP))
                    .orElseGet(() -> {
                        log.info("Организация не найдена, создаём новую по данным из SBID");
                        return getOrganizationByAttribute(result);
                    });
            log.info("Организация найдена или создана: {}", org.getId());
            log.info("Передаем валидные данные о пользователе в сервис юзеров");
            restApiSberBuisness.sendToUserService(result);
            return compliteUser(result, org);
        } catch (Exception e) {
            log.error("Ошибка при получении информации о пользователе", e);
            throw new BadResponseException("Не удалось получить информацию о пользователе");
        }
    }

    private User compliteUser(Map<String, Object> userAttribute, Organization org) {
        log.info("Сохранение нового пользователя sub = {}, name = {}", userAttribute.get(SUB), userAttribute.get(NAME));
        var user = userService.findBySub((String) userAttribute.get(SUB));
        if (user.isPresent()) {
            return user.get();
        }
        var userNew = User.builder()
                    .inn((String) userAttribute.get(INN))
                    .sub((String) userAttribute.get(SUB))
                    .fullName((String) userAttribute.get(NAME))
                    .phoneNumber((String) userAttribute.get(PHONE_NUMBER))
                    .organization(org)
                    .build();
        return userService.save(userNew);
    }

    Organization getOrganizationByAttribute(Map<String, Object> userAttribute) {
        var org = Organization.builder()
                    .inn((String) userAttribute.get(INN))
                    .email((String) userAttribute.get(EMAIL))
                    .orgLawForm((String) userAttribute.get(ORG_LAW_FORM))
                    .kpp((String) userAttribute.get(ORG_KPP))
                    .ogrn((String) userAttribute.get(ORG_OGRN))
                    .fullName((String) userAttribute.get(ORG_FULL_NAME))
                    .offerExpirationDate(DateUtils.fromIsoZonedDateTime((String) userAttribute.get(OFFER_EXPIRATION_DATE)))
                    .oktmo((String) userAttribute.get(ORG_OKTMO))
                    .individualExecutiveAgency((Integer) userAttribute.get(INDIVIDUAL_EXECUTIVE_AGENCY))
                    .build();
        return organizationService.save(org);
    }

    public String generateToken(String userId, Set<String> roles) {
        log.info("Генерация JWT-токена для пользователя: {}", userId);

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID пользователя не может быть пустым");
        }

        try {
            // Читаем ПРИВАТНЫЙ ключ для подписи токена
            PrivateKey privateKey = readPrivateKey();
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);
            claims.put("iss", "SberTransport");
            claims.put("aud", "cargo-microservices");
            claims.put("transport", "false");
            claims.put("data_master", "true");
            claims.put("random", "f55757a8-c31d-4301-b842-4991abcc9668");
            claims.put("factor", "BASIC");

            Instant now = Instant.now();
            Date issuedAt = Date.from(now);
            Date expiration = Date.from(now.plus(180, ChronoUnit.DAYS));
            claims.put("nbf", issuedAt);

            // Создание JWT
            String jwt = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(issuedAt)
                    .setHeaderParam("kid", "jwt")
                    .setHeaderParam("typ", "JWT")
                    .setHeaderParam("alg", "RS512")
                    .setHeaderParam("cty", "application/json")
                    .setId(userId)
                    .setExpiration(expiration)
                    .signWith(privateKey, SignatureAlgorithm.RS512)
                    .compact();

            log.debug("Сгенерирован JWT токен для: {}", userId);
            return jwt;

        } catch (Exception e) {
            log.error("Не удалось сгенерировать токен для пользователя: {}", userId, e);
            throw new BadResponseException("Не удалось сгенерировать токен");
        }
    }

    private PrivateKey readPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] keyBytes;
        log.info("Чтение приватного ключа из файла: {}", sslConfiguration.getPrivateKey());
        try (var resourceAsStream = getClass().getClassLoader()
                .getResourceAsStream(sslConfiguration.getPrivateKey())) {
            if (resourceAsStream == null) {
                try (var inputStream = Files.newInputStream(Paths.get(sslConfiguration.getPrivateKey()))) {
                    keyBytes = StreamUtils.copyToByteArray(inputStream);
                }
            } else {
                keyBytes = StreamUtils.copyToByteArray(resourceAsStream);
            }
        } catch (Exception e) {
            log.error("Не удалось прочитать приватный ключ", e);
            throw new BadResponseException("Не удалось прочитать приватный ключ " + e.getMessage());
        }

        String key = new String(keyBytes, StandardCharsets.UTF_8)
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        log.info("Приватный ключ: {}", key);
        byte[] decodedKey = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

}
