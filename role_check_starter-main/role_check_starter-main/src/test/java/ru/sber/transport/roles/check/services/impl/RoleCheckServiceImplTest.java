package ru.sber.transport.roles.check.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.roles.check.services.RoleProvider;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@IsolatedTest
@UnitTest
@Feature("lib_role_check")
@DisplayName("Проверка ролей")
class RoleCheckServiceImplTest {

    private final RoleProvider provider = mock(RoleProvider.class);

    private final AuthorizationManager<RequestAuthorizationContext> service = new RoleCheckServiceImpl(provider);

    @Test
    @DisplayName("Проверка запроса OPTIONS")
    void test_check_options() {
        var authentication = (Supplier<Authentication>) () -> new UsernamePasswordAuthenticationToken("user", "password", List.of(new SimpleGrantedAuthority("role")));
        var request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.OPTIONS.name());
        var context = new RequestAuthorizationContext(request);

        assertThat(service.check(authentication, context).isGranted()).isTrue();
    }

    @Test
    @DisplayName("Проверка запроса")
    void test_check() {
        var authentication = (Supplier<Authentication>) () -> new UsernamePasswordAuthenticationToken("user", "password", List.of(new SimpleGrantedAuthority("role")));
        var request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setRequestURI("uri");
        var context = new RequestAuthorizationContext(request);

        when(provider.getAllowedRoles(HttpMethod.GET, "uri")).thenReturn(Set.of("role"));

        assertThat(service.check(authentication, context).isGranted()).isTrue();
        var request2 = new MockHttpServletRequest();
        request2.setMethod(HttpMethod.GET.name());
        request2.setRequestURI("uri2");
        var context2 = new RequestAuthorizationContext(request2);

        when(provider.getAllowedRoles(HttpMethod.GET, "uri2")).thenReturn(Set.of("role2"));

        assertThat(service.check(authentication, context2).isGranted()).isFalse();
    }

}