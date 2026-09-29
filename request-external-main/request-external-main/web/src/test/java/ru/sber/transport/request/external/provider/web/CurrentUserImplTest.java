package ru.sber.transport.request.external.provider.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Feature;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.CurrentUser;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения текущего пользователя")
class CurrentUserImplTest {

    private final CurrentUser currentUserData = new CurrentUserImpl();

    @DisplayName("Проверка получения текущего пользователя")
    @Test
    void test() {
        final var userId = UUID.randomUUID();
        final var jwtToken = new JwtAuthenticationToken(Jwt.withTokenValue("token value").header("algo", "none").jti(userId.toString()).build());
        SecurityContextHolder.getContext().setAuthentication(jwtToken);

        final var currentUser = currentUserData.get();

        assertThat(currentUser).isEqualTo(userId);
    }

}