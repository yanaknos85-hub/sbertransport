package ru.sber.transport.request.external.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.web.api.TemporaryApi;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка делегата разрешений")
class AllowedDelegateImplTest {

    private final AvailableClasses classes = mock(AvailableClasses.class);

    private final TemporaryApi temporaryApiDelegate = new AllowedDelegateImpl(classes);

    @Test
    @DisplayName("Тест разрешения доступа")
    void test_allow() throws ExecutionException, InterruptedException {
        final var user = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").jti(user.toString()).header("algo", "none").build()));

        when(classes.allow(user)).thenReturn(true);

        final var actual = temporaryApiDelegate.allowOrder().get();

        assertThat(actual.getStatusCode().value()).isEqualTo(200);
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().isOrder()).isTrue();
    }

}