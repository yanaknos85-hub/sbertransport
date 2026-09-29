package ru.sber.transport.cargo.exchange.request.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.sber.transport.cargo.exchange.request.enums.Role.CARRIER;
import static ru.sber.transport.cargo.exchange.request.enums.Role.SHIPPER;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer,
                                                   Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer,
                                                   Customizer<CsrfConfigurer<HttpSecurity>> csrfCustomizer,
                                                   AuthorizationManager<RequestAuthorizationContext> authorizationManager) throws Exception {
        return http.securityMatchers(AbstractRequestMatcherRegistry::anyRequest)
                .sessionManagement(sessionManagementCustomizer)
                .cors(corsCustomizer)
                .csrf(csrfCustomizer)
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .access(authorizationManager))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer(CorsConfigurationSource corsConfigurationSource) {
        log.info("Create cors configurer");
        return httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource);
    }

    @Bean
    public Customizer<CsrfConfigurer<HttpSecurity>> csrfCustomizer() {
        log.info("Create csrf configurer");
        return AbstractHttpConfigurer::disable;
    }

    @Bean
    public Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer() {
        log.info("Create session management configurer");
        return it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Primary
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed.origin:*}") String origin,
                                                           @Value("${cors.allowed.header:*}") String header) {
        log.info("Create cors configuration source");
        var config = new CorsConfiguration();
        config.addAllowedOrigin(origin);
        config.addAllowedHeader(header);
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");

        var corsConfigurations = Map.of("/**", config);

        var source = new UrlBasedCorsConfigurationSource();
        source.setCorsConfigurations(corsConfigurations);
        return source;
    }

    @Bean
    public AuthorizationManager<RequestAuthorizationContext> authorizationManager() {
        return (authentication, object) -> {
            var granted = List.of(CARRIER.name(), SHIPPER.name());
            var roles = Optional.ofNullable(authentication.get())
                    .filter(JwtAuthenticationToken.class::isInstance)
                    .map(JwtAuthenticationToken.class::cast)
                    .map(JwtAuthenticationToken::getToken)
                    .map(jwt -> jwt.getClaimAsStringList("roles"))
                    .orElse(Collections.emptyList());

            return new AuthorizationDecision(roles.stream().anyMatch(granted::contains));
        };
    }
}
