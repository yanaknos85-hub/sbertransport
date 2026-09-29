package ru.sber.transport.authentication.providers.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.providers.access.config.JwtProperties;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@AutoConfigureAfter({JwtProperties.class, TokenProperties.class})
class TokenPropertiesConfig {

    @Bean
    Map<AuthType, TokenProperties> tokenPropertiesMap(List<TokenProperties> propertiesList) {
        return propertiesList.stream().collect(Collectors.toMap(TokenProperties::getAuthType, Function.identity()));
    }

}
