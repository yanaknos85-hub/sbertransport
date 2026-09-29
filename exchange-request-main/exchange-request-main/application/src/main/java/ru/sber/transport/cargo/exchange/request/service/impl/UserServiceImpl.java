package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.database.dao.UserRepository;
import ru.sber.transport.cargo.exchange.request.exception.UserNotFoundException;
import ru.sber.transport.cargo.exchange.request.service.UserService;
import ru.sber.transport.cargo.exchange.request.util.ContextHelper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findUserByToken(JwtAuthenticationToken authentication) {
        var userId = ContextHelper.getUserId(authentication);
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден: %s".formatted(userId)));
    }

    @Override
    public UUID findUserIdByToken(JwtAuthenticationToken authentication) {
        return findUserByToken(authentication).getId();
    }

    @Override
    public UUID findOrganizationIdByToken(JwtAuthenticationToken authentication) {
        var userId = ContextHelper.getUserId(authentication);
        return Optional.ofNullable(findUserByToken(authentication).getOrganization())
                .map(Organization::getId)
                .orElseThrow(()-> new UserNotFoundException("У пользователя не установлена организация: %s".formatted(userId)));
    }
}
