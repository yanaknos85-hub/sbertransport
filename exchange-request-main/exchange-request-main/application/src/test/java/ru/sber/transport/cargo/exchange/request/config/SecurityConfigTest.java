package ru.sber.transport.cargo.exchange.request.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private JwtAuthenticationToken createJwtWithRoles(List<String> roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", roles)
                .claim("jti", "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380005")
                .build();
        return new JwtAuthenticationToken(jwt, roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList());
    }

    @Test
    void shouldAllowAccess_WhenUserHasShipperRole() throws Exception {
        var auth = createJwtWithRoles(List.of("SHIPPER"));

        mockMvc.perform(get("/api/requests/{requestId}", UUID.randomUUID())
                        .with(authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 — контроллер не найден, но доступ разрешён
    }

    @Test
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldAllowAccess_WhenUserHasCarrierRole() throws Exception {
        var auth = createJwtWithRoles(List.of("CARRIER"));

        mockMvc.perform(post("/list/published")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccess_WhenUserHasOtherRole() throws Exception {
        var auth = createJwtWithRoles(List.of("ANONYMOUS"));

        mockMvc.perform(get("/api/requests/{requestId}", UUID.randomUUID())
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAccess_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/requests/{requestId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowCorsFromAnyOrigin() throws Exception {
        mockMvc.perform(post("/api/list/published")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }

    @Test
    @Sql(scripts = "/sql/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clearAll.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldAllowPostWithoutCsrfToken() throws Exception {
        var auth = createJwtWithRoles(List.of("SHIPPER"));

        mockMvc.perform(post("/list/published")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}