package ru.sber.transport.authentication.providers.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Data about expiration.
 */
@Setter
@Getter
public class Expiration {
    
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
