package ru.sber.transport.javers.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.sber.transport.javers.service.UserExtractor;

@Component
@ConditionalOnClass(UsernamePasswordAuthenticationToken.class)
class UsernameAuthenticationExtractor implements UserExtractor {

    @Override
    public String extract(Authentication element) {
        return element.getName();
    }

    @Override
    public boolean support(Class<?> clazz) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(clazz);
    }
}
