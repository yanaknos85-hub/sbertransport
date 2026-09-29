package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.database.model.User;
import ru.sber.transport.cargo.exchange.request.database.dao.UserRepository;
import ru.sber.transport.cargo.exchange.request.exception.UserNotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final JwtAuthenticationToken VALID_TOKEN = new JwtAuthenticationToken(
            Jwt.withTokenValue("valid-jwt-token-123")
                    .header("typ", "JWT")
                    .jti(USER_ID.toString())
                    .build());

    @Test
    void findUserIdByToken_shouldReturnUserId_whenUserExists() {
        // Given
        var userId = UUID.fromString(VALID_TOKEN.getToken().getId());
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));

        // When
        var result = userService.findUserIdByToken(VALID_TOKEN);

        // Then
        assertThat(result).isEqualTo(userId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserByToken_shouldReturnUser_whenUserExists() {
        // Given
        var userId = UUID.fromString(VALID_TOKEN.getToken().getId());
        var user = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        var result = userService.findUserByToken(VALID_TOKEN);

        // Then
        assertThat(result).isEqualTo(user);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserIdByToken_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        var userId = UUID.fromString(VALID_TOKEN.getToken().getId());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserIdByToken(VALID_TOKEN))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь не найден: %s".formatted(userId));

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUserByToken_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Given
        var userId = UUID.fromString(VALID_TOKEN.getToken().getId());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByToken(VALID_TOKEN))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь не найден: %s".formatted(userId));

        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
