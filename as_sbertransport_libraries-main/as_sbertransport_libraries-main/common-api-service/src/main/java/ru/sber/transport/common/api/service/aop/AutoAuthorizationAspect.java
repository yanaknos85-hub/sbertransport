package ru.sber.transport.common.api.service.aop;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.sber.transport.common.api.service.exception.AuthorizationException;
import ru.sber.transport.common.api.service.model.Credentials;
import ru.sber.transport.common.api.service.model.dto.CredentialClient;
import ru.sber.transport.common.api.service.service.AuthorizationService;

import java.time.Instant;
import java.util.stream.Stream;

@Aspect
@Component
@RequiredArgsConstructor
public class AutoAuthorizationAspect {

    private final AuthorizationService authService;
    private final Credentials credentials;

    @Before(value = "@annotation(AutoAuthorization)")
    public void autoAuth(final JoinPoint joinPoint) {
        var credentialClient = getCredentialClient(joinPoint.getArgs());

        if (credentialClient == null ||
                credentialClient.getUri() == null ||
                credentialClient.getLogin() == null ||
                credentialClient.getPassword() == null) {
            throw new AuthorizationException("Credential isn't specified");
        }

        var credential = credentials.get(credentialClient.getUri(), credentialClient.getLogin());
        var now = Instant.now();

        if (credential == null ||
                StringUtils.isEmpty(credential.getTokenWithoutPrefix()) ||
                now.isAfter(credential.getExpirationRefreshToken())
        ) {
            authService.auth(credentialClient);
            return;
        }

        if (now.isAfter(credential.getExpirationToken()) && now.isBefore(credential.getExpirationRefreshToken())) {
            authService.refresh(credentialClient);
        } else {
            authService.auth(credentialClient);
        }
    }

    private CredentialClient getCredentialClient(Object[] args) {
        return Stream.of(args)
                .filter(CredentialClient.class::isInstance)
                .map(CredentialClient.class::cast)
                .findFirst()
                .orElseThrow(() -> {throw new IllegalArgumentException("No value present");});
    }

}
