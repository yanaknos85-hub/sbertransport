package ru.sber.transport.address.web.controller.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Базовая реализация контроллера адресов.
 */
abstract class BaseAddressControllerImpl {

    /**
     * Check user existence.
     *
     * @return user ID.
     */
    protected UUID getAuthenticated() {
        return UUID.fromString(((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getId());
    }
}
