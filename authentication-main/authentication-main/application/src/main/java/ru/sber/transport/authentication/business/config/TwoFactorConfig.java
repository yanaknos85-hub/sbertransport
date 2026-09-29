package ru.sber.transport.authentication.business.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Configuration
public class TwoFactorConfig {

    @Bean
    KeyPair twoFactorKeyPair() throws NoSuchAlgorithmException {
        var generator = KeyPairGenerator.getInstance("RSA");
        return generator.generateKeyPair();
    }

    @Bean
    Key twoFactorJwtPrivate(@Qualifier("twoFactorKeyPair") KeyPair twoFactorKeyPair) {
        return twoFactorKeyPair.getPrivate();
    }

    @Bean
    Key twoFactorPublic(@Qualifier("twoFactorKeyPair") KeyPair twoFactorKeyPair) {
        return twoFactorKeyPair.getPublic();
    }

}
