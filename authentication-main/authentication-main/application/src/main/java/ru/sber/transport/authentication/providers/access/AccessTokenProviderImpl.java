package ru.sber.transport.authentication.providers.access;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;
import ru.sber.transport.authentication.business.dto.AccessTokenData;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.dto.Scope;
import ru.sber.transport.authentication.business.providers.AccessTokenProvider;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.authentication.messaging.senders.BlackListSender;
import ru.sber.transport.authentication.providers.config.TokenProperties;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Реализация провайдера токенов доступа.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class AccessTokenProviderImpl implements AccessTokenProvider {

    /**
     * Настройки JWT.
     */
    private final Map<AuthType, TokenProperties> tokenProperties;

    /**
     * Отправитель данных о токенах в черный список.
     */
    private final BlackListSender blackListSender;

    private final RoleRepository roleRepository;

    private final Tracer tracer;

    /**
     * Секретный ключ JWT.
     */
    @Qualifier("jwtPrivateKey")
    private final Key jwtKey;

    @Qualifier("twoFactorJwtPrivate")
    private final Key twoFactorPrivateKey;

    private final KeyFactory keyFactory;

    @SuppressWarnings("java:S3958")
    @Override
    public AccessTokenData generate(AccountDto accountDto, boolean enforceBasic) throws NoSuchAlgorithmException, InvalidKeySpecException {
        log.debug("Generating access token for {} started at {}", accountDto.getLogin(), LocalDateTime.now(ZoneOffset.UTC));

        var authType = enforceBasic ? AuthType.BASIC : accountDto.getAuthType();

        var properties = tokenProperties.get(authType);
        var expiration = properties.getExpire();

        var issuedAt = OffsetDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));

        var expireAt = issuedAt
                .plusSeconds(expiration.getSeconds())
                .plusMinutes(expiration.getMinutes())
                .plusHours(expiration.getHours())
                .plusDays(expiration.getDays())
                .plusMonths(expiration.getMonths())
                .plusYears(expiration.getYears());

        var roles = roleRepository.findAllByAccountId(accountDto.getId());

        var jwsHeaders = JwsHeader
            .with(SignatureAlgorithm.RS512)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .type("JWT")
            .keyId(getKid(authType))
            .build();
        var jwsClaims = JwtClaimsSet.builder()
            .claim(ROLES_CLAIM_NAME, roles.stream().map(RoleRecord::getCode).toList())
            .claim(SCOPE_CLAIM_NAME, accountDto.getScope() == null ? Scope.EMPLOYEE : accountDto.getScope())
            .claim(RANDOM_CLAIM_NAME, UUID.randomUUID())
            .claim(FACTOR_CLAIM_NAME, accountDto.getAuthType().name())
            .claim(REQUEST_ID_CLAIM_NAME, Optional.ofNullable(tracer.currentSpan()).map(Span::context).map(TraceContext::traceId)
                .orElse(String.valueOf(UUID.randomUUID())))
            .expiresAt(expireAt.toInstant())
            .issuer(properties.getIssuer())
            .issuedAt(issuedAt.toInstant())
            .notBefore(issuedAt.toInstant())
            .subject(accountDto.getLogin())
            .id(accountDto.getId().toString());
        if (!AuthType.TWO_FA.equals(authType))  {
            jwsClaims
                .claim(TRANSPORT_CLAIM_NAME, accountDto.isTransferPassword())
                .claim(DATAMASTER_CLAIM_NAME, roles.stream().anyMatch(RoleRecord::getDataMaster));
        }
        var parameters = JwtEncoderParameters.from(jwsHeaders, jwsClaims.build());

        var jwk = createJwk(authType);
        var jwksList = new JWKSet(jwk);
        var jwks = new ImmutableJWKSet<>(jwksList);
        var encoder = new NimbusJwtEncoder(jwks);
        var jwt = encoder.encode(parameters);

        return new AccessTokenData(jwt.getTokenValue(), OffsetDateTime.ofInstant(Objects.requireNonNull(jwt.getExpiresAt()), ZoneOffset.UTC));
    }

    private JWK createJwk(AuthType authType) throws InvalidKeySpecException {
        var privateKey = switch (authType) {
            case BASIC -> (PrivateKey) jwtKey;
            case TWO_FA -> (PrivateKey) twoFactorPrivateKey;
        };
        var publicKey = createPublic(privateKey);
        var use = KeyUse.SIGNATURE;
        var ops = Set.of(KeyOperation.SIGN);
        var alg = Algorithm.parse(SignatureAlgorithm.RS512.name());
        var kid = getKid(authType);
        return new RSAKey(
            publicKey,
            privateKey,
            use,
            ops,
            alg,
            kid,
            null, null, null, null, null, null, null, null);
    }

    private @NotNull String getKid(AuthType authType) {
        return switch (authType) {
            case BASIC -> "jwt";
            case TWO_FA -> "second";
        };
    }

    private RSAPublicKey createPublic(PrivateKey privateKey) throws InvalidKeySpecException {
        RSAPrivateCrtKey privk = (RSAPrivateCrtKey)privateKey;
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
        return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public void toBlackList(String token) {
        blackListSender.send(token);
    }
}
