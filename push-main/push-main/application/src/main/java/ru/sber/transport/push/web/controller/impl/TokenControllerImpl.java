package ru.sber.transport.push.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.business.providers.TokenProvider;
import ru.sber.transport.push.web.controller.TokenController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenControllerImpl implements TokenController {

    private final TokenProvider tokenProvider;

    @Override
    public ResponseEntity<?> saveToken(UUID recipientId, UUID tokenId, TokenData tokenData) {
        var id = tokenId == null ? UUID.randomUUID() : tokenId;
        var tokenRecord = tokenProvider.save(id, recipientId, tokenData);
        var body = Map.of("tokenId", tokenRecord.getId());
        return ResponseEntity.ok(body);
    }
}
