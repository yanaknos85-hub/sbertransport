package ru.sber.transport.javers.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.javers.service.UserExtractor;

@Component
@ConditionalOnClass(JwtAuthenticationToken.class)
class JwtInfoExtractor implements UserExtractor {

    @Override
    public String extract(Authentication element) {
        return ((JwtAuthenticationToken) element).getToken().getId();
    }

    @Override
    public boolean support(Class<?> clazz) {
        return JwtAuthenticationToken.class.isAssignableFrom(clazz);
    }
}
