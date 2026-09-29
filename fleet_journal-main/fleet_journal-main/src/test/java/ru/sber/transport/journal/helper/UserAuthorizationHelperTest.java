package ru.sber.transport.journal.helper;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAuthorizationHelperTest {
    
    @Test
    void getUserIdFromJwtAuth() {
        var userId = UUID.randomUUID();
        var authentication = new JwtAuthenticationToken(Jwt
                                                                .withTokenValue("some token value")
                                                                .jti(userId.toString())
                                                                .header("some header", "")
                                                                .build());
        var extractedUserId = UserAuthorizationHelper.getUserId(authentication);
        assertThat(extractedUserId).isEqualTo(userId);
    }
    
    @Test
    void getUserIdFromNonJwtAuth() {
        var userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "somePass");
        var extractedUserId = UserAuthorizationHelper.getUserId(authentication);
        assertThat(extractedUserId).isEqualTo(userId);
    }
}