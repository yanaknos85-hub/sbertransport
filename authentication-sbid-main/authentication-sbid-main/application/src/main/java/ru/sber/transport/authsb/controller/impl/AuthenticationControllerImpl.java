package ru.sber.transport.authsb.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authsb.controller.AuthenticationController;
import ru.sber.transport.authsb.dto.ErrorResponseDto;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.services.ServiceController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationControllerImpl implements AuthenticationController {

    private final ServiceController controllerService;

    @Override
    public ResponseEntity<?> createUrl(String sessionId) {
        log.info("******* Получение ссылки для авторизации пользователя в СберБизнес ID = {}", sessionId);
        try {
            return ResponseEntity.ok(controllerService.createUrl(sessionId));
        } catch (Exception ex) {
            log.error("******* Ошибка при получении ссылки для авторизации пользователя в СберБизнес ID = {}", sessionId, ex);
            return ResponseEntity.status(500).body(new ErrorResponseDto(500, "Внутренняя ошибка сервера", ex.toString()));
        }
    }

    @Override
    public ResponseEntity<?> getAuthLogin(String code, String state) {
        log.info("******* Получение токена для авторизации пользователя в СберБизнес ID {}", code);
        try {
            return ResponseEntity.ok(controllerService.getAuthToken(code, state));
        } catch (BadResponseException ex) {
            log.warn("******* Ошибка валидации при получении токена: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(400, "Ошибка валидации", ex.getMessage()));
        } catch (Exception ex) {
            log.error("******* Неизвестная ошибка при получении токена", ex);
            return ResponseEntity.status(500)
                    .body(new ErrorResponseDto(500, "Внутренняя ошибка сервера", ex.toString()));
        }
    }

    @Override
    public ResponseEntity<?> refresh(String refreshToken) {
        log.info("******* Обновление токена для авторизации пользователя в СберБизнес ID");
        try {
            return ResponseEntity.ok(controllerService.refreshToken(refreshToken));
        } catch (BadResponseException ex) {
            log.warn("******* Ошибка валидации при обновлении токена: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(400, "Ошибка валидации", ex.getMessage()));
        } catch (Exception ex) {
            log.error("******* Ошибка при обновлении токена", ex);
            return ResponseEntity.status(500)
                    .body(new ErrorResponseDto(500, "Внутренняя ошибка сервера", ex.toString()));
        }
    }
}