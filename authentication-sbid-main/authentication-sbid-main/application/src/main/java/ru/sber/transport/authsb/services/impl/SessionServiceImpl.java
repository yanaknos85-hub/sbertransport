package ru.sber.transport.authsb.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.authsb.database.dao.SessionRepository;
import ru.sber.transport.authsb.database.model.Session;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.services.SessionService;

import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Override
    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Optional<Session> findByState(String state) {
        return sessionRepository.findByState(state);
    }

    @Override
    public Session findByStateAndActiveTrue(String state) {
        return sessionRepository.findByStateAndActiveTrue(state)
                .orElseThrow(() -> {
                    log.warn("Сессия с таким state не найдена или неактивна: state = {}", state);
                    return new BadResponseException("Недопустимый или устаревший state");
                });
    }

    @Override
    public Session findByRefreshTokenAndActiveTrue(String refreshToken) {
        return sessionRepository
                .findAllByRefreshTokenAndActiveTrue(refreshToken)
                .orElseThrow(() -> new BadResponseException("Недопустимый или устаревший refreshToken"));
    }
}
