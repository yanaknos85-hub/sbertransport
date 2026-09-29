package ru.sber.transport.authsb.services;

import ru.sber.transport.authsb.database.model.Session;

import java.util.Optional;

public interface SessionService {

    Session save (Session session);

    Session findByStateAndActiveTrue(String state);

    Session findByRefreshTokenAndActiveTrue(String refreshToken);

    Optional<Session> findByState(String state);
}
