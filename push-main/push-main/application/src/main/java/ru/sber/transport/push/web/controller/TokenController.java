package ru.sber.transport.push.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.push.business.dto.TokenData;

import java.util.UUID;

/**
 * Контроллер для работы с токенами устройств.
 */
@RequestMapping("/token/")
public interface TokenController {

    /**
     * Сохранение токена.
     *
     * @param recipientId идентификатор получателя.
     * @param tokenId     идентификатор предыдущего токена.
     * @param tokenData   токен.
     * @return идентификатор нового токена.
     */
    @PostMapping("/{recipientId}/")
    ResponseEntity<?> saveToken(@PathVariable UUID recipientId, @RequestParam(required = false) UUID tokenId, @RequestBody TokenData tokenData);
}
