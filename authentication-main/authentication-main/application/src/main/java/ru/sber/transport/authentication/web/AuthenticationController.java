package ru.sber.transport.authentication.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authentication.web.model.CodeData;
import ru.sber.transport.authentication.web.model.Role;
import ru.sber.transport.authentication.web.model.Token;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.authentication.business.dto.OwnershipProofDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;

import jakarta.validation.Valid;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Controller for working with authentication.
 */
@RequestMapping("/")
@Tag(name = "Аутентификация", description = "Набор операций для входа/выхода из системы")
public interface AuthenticationController {

    /**
     * Login.
     *
     * @param credentials credentials to login.
     *
     * @return token.
     */
    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @NoAuthorize
    @Operation(summary = "Вход", description = "Вход в систему", security = @SecurityRequirement(name = "login"))
    Token loginPost(
            @Parameter(hidden = true) @RequestHeader("User-Agent") String userAgent,
            @Parameter(hidden = true) @RequestHeader("Authorization") String credentials,
            @RequestHeader(value = "x-client-type", required = false) String clientType
    ) throws AccountNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Login.
     *
     * @param credentials credentials to login.
     *
     * @return token.
     */
    @GetMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @NoAuthorize
    @Operation(summary = "Вход", description = "Вход в систему", security = @SecurityRequirement(name = "login"))
    Token loginGet(
            @Parameter(hidden = true) @RequestHeader("User-Agent") String userAgent,
            @Parameter(hidden = true) @RequestHeader("Authorization") String credentials,
            @RequestHeader(value = "x-client-type", required = false) String clientType
    ) throws AccountNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException;

    /**
     * Вход с данными второго фактора аутентификации.
     *
     * @param code код второго фактора.
     * @return токен дальнейшего авторизации.
     */
    @PostMapping(value = "code", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Второй фактор входа", description = "Передача данных второго фактора для авторизации в системе")
    Token code(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestBody CodeData code
    ) throws NoSuchAlgorithmException, InvalidKeySpecException;
    
    /**
     * Logout.
     *
     * @param token access token.
     */
    @GetMapping(value = "logout")
    @Operation(summary = "Выход", description = "Выход из системы",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    @NoAuthorize
    @Parameter(name = "Authorization", hidden = true)
    void logout(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String token
               );
    
    /**
     * Смена транспортного пароля.
     *
     * @param credentials credentials to login.
     */
    @PostMapping(value = "changePassword", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Смена транспортного пароля",
               description = "Смена транспортного пароля")
    void changeTransferPassword(
            @Parameter(hidden = true) @RequestHeader("x-changePassword") String credentials
                                         );
    
    /**
     * Сброс пароля.
     *
     * @param userId userId.
     */
    @PutMapping(value = "resetPassword/{userId}")
    @Operation(summary = "Сброс пароля", description = "Сброс пароля",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    void resetPassword(@PathVariable("userId") UUID userId);

    /**
     * Отправка кода для подтверждения владения аккаунтом.
     *
     * @param ownershipProofDto данные для подтверждения владения аккаунтом.
     */
    @PostMapping(value = "ownership-code", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Отправка кода", description = "Отправка кода для подтверждения владения аккаунтом.")
    @NoAuthorize
    void sendCodeForOwnershipProof(@RequestBody @Valid OwnershipProofDto ownershipProofDto) throws AccountNotFoundException;

    /**
     * Подтверждение владения аккаунтом по коду и сброс пароля.
     *
     * @param code код
     */
    @PutMapping(value = "confirmation")
    @Operation(summary = "Подтверждение владения аккаунтом", description = "Подтверждение владения аккаунтом по коду и сброс пароля")
    @NoAuthorize
    void confirmationOfOwnershipAndResetPassword(@Parameter(hidden = true) @RequestHeader("x-code") String code,
                             @RequestBody @Valid OwnershipProofDto ownershipProofDto) throws AccountNotFoundException;

    /**
     * Получение списка ролей.
     *
     * @param userId userId.
     * @return list of roles.
     */
    @GetMapping(value = "roles/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение списка ролей", description = "Получение списка ролей",
               security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
    List<Role> getUserRoles(@PathVariable("userId") UUID userId);
}
