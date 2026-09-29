package ru.sberbank.ditsib.transport.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Basic auth security adapter.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfigurerAdapter {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
       return http.authorizeExchange()
                  .anyExchange().permitAll()
                  .and().cors()
                  .and().csrf().disable().formLogin().disable().logout().disable().build();
    }
    
}
