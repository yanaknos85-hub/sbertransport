package ru.sber.transport.token_generator.grpc.config;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Set;

@Configuration
class JwtEncodingConfig {

    @Bean
    KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    @Bean
    JwtEncoder encoder(
        @Value("${jwt.kid:jwt}") String kid,
        @Value("${jwt.algo:RS512}") SignatureAlgorithm algorithm,
        KeyLoader keyLoader,
        KeyFactory keyFactory
    ) throws InvalidKeySpecException {
        var privateKey = (RSAPrivateCrtKey) keyLoader.getPrivateKey();
        var spec = new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent());
        var publicKey = keyFactory.generatePublic(spec);
        var jwk = new RSAKey(
            (RSAPublicKey) publicKey,
            privateKey,
            KeyUse.SIGNATURE,
            Set.of(KeyOperation.SIGN),
            Algorithm.parse(algorithm.name()),
            kid,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        var jwkSet = new JWKSet(List.of(jwk));
        var jwkSource = new ImmutableJWKSet<>(jwkSet);
        return new NimbusJwtEncoder(jwkSource);
    }
}
