package ru.sber.transport.javers.service;

import org.springframework.security.core.Authentication;

/**
 * Extract user data.
 */
public interface UserExtractor {

    /**
     * Extract user data from authentication.
     *
     * @param element authentication element.
     * @return user name.
     */
    String extract(Authentication element);

    /**
     * Check support.
     *
     * @param clazz authentication class to check.
     * @return supporting flag.
     */
    boolean support(Class<?> clazz);

}
