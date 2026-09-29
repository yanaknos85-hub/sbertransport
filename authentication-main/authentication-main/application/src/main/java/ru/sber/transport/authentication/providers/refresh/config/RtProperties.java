package ru.sber.transport.authentication.providers.refresh.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties of refresh token.
 */
@Getter
@ConfigurationProperties(prefix = "rt")
public class RtProperties {

    /**
     * Expiration data of token.
     */
    private RtExpiration expire = new RtExpiration();

    /**
     * Class of expiration data of token.
     */
    @Setter
    @Getter
    public static class RtExpiration {

        /**
         * Seconds to expire.
         */
        private int seconds;

        /**
         * Minutes to expire.
         */
        private int minutes;

        /**
         * Hours to expire.
         */
        private int hours;

        /**
         * Days to expire.
         */
        private int days;

        /**
         * Months to expire.
         */
        private int months;

        /**
         * Years to expire.
         */
        private int years;

    }

}
