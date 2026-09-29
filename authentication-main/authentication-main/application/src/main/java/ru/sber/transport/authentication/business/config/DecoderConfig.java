package ru.sber.transport.authentication.business.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
class DecoderConfig {

    @Bean
    JwtDecoder decoder(
        @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String uri,
        @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location:}") String jwkPath,
        @Value("${spring.security.oauth2.resourceserver.jwt.jws-algorithms:RS256}") List<SignatureAlgorithm> algorithms,
        @Qualifier("twoFactorPublic") Key twoFactorPublic,
        KeyFactory factory,
        ObjectMapper objectMapper
    ) throws InvalidKeySpecException, IOException {
        JwtDecoder jwkBuilder = null;
        if (StringUtils.hasText(uri) && StringUtils.hasText(jwkPath)) {
            throw new IllegalStateException("Several keys defined but exactly one is required. Please set jwk-set-uri OR public-key-location");
        } else if(!StringUtils.hasText(uri) && !StringUtils.hasText(jwkPath)) {
            throw new IllegalStateException("No keys defined. Please set jwk-set-uri OR public-key-location");
        }
        var signatureAlgorithm = algorithms.get(0);
        if (StringUtils.hasText(uri)) {
            jwkBuilder = NimbusJwtDecoder.withJwkSetUri(uri).jwsAlgorithm(signatureAlgorithm).build();
        }
        if (StringUtils.hasText(jwkPath)) {
            var key = getClass().getClassLoader().getResource(jwkPath.replace("classpath:/", ""));
            if (key == null) {
                throw new IllegalStateException("Public key is defined but not found");
            }
            var jwk = Files.readString(Path.of(key.getFile()));
            jwk = jwk.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
            var specBytes = Base64.getMimeDecoder().decode(jwk);
            var keySpec = new X509EncodedKeySpec(specBytes);
            jwkBuilder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) factory.generatePublic(keySpec)).signatureAlgorithm(signatureAlgorithm).build();
        }
        var twoFactor = NimbusJwtDecoder.withPublicKey((RSAPublicKey) twoFactorPublic).signatureAlgorithm(signatureAlgorithm).build();
        return createDelegatedDecoder(jwkBuilder, twoFactor, objectMapper);
    }

    private JwtDecoder createDelegatedDecoder(JwtDecoder jwkBuilder, NimbusJwtDecoder twoFactor, ObjectMapper objectMapper) {
        return token -> {
            Map<String, String> headers;
            try {
                headers = objectMapper.readValue(new String(Base64.getDecoder().decode(token.split("\\.")[0])), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new JwtException("Header getting failed", e);
            }
            var key = headers.getOrDefault("kid", "jwt");
            if ("second".equals(key)) {
                return twoFactor.decode(token);
            }
            return jwkBuilder.decode(token);
        };
    }

}
