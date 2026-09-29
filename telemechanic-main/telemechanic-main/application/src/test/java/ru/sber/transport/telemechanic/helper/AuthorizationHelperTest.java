package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHelperTest {
    @Test
    void nameShouldNotBeEmpty() {
        var userUUId = UUID.randomUUID();
        var userId = UserAuthorizationHelper.getUserId(new TestingAuthenticationToken(userUUId.toString(), ""));
        assertThat(userId)
                .isEqualTo(userUUId);
    }
}
