package ru.sber.transport.authsb.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.authsb.dto.ErrorResponseDto;
import ru.sber.transport.authsb.dto.TokenResponseDto;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.services.ServiceController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerImplTest {

    @Mock
    private ServiceController serviceController; // ← Исправлено: использовать интерфейс, а не реализацию

    @InjectMocks
    private AuthenticationControllerImpl authenticationController;

    private final String VALID_SESSION_ID = "valid-session-id";
    private final String VALID_CODE = "auth-code-123";
    private final String VALID_STATE = "state-456";
    private final String VALID_REFRESH_TOKEN = "refresh-token-789";

    @Test
    @DisplayName("Проверка создания URL — успешный сценарий")
    void createUrl_Success() {
        String expectedUrl = "https://sbid.sber.ru/auth?client_id=abc";
        when(serviceController.createUrl(VALID_SESSION_ID)).thenReturn(expectedUrl);

        ResponseEntity<?> response = authenticationController.createUrl(VALID_SESSION_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedUrl);
        verify(serviceController).createUrl(VALID_SESSION_ID);
    }

    @Test
    @DisplayName("Проверка создания URL — ошибка на стороне сервиса")
    void createUrl_InternalError() {
        when(serviceController.createUrl(VALID_SESSION_ID)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = authenticationController.createUrl(VALID_SESSION_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto error = (ErrorResponseDto) response.getBody();
        assertThat(error.status()).isEqualTo(500);
        assertThat(error.message()).isEqualTo("Внутренняя ошибка сервера");
        verify(serviceController).createUrl(VALID_SESSION_ID);
    }

    @Test
    @DisplayName("Проверка получения токена — успешный сценарий")
    void getAuthLogin_Success() {
        TokenResponseDto mockResponse = TokenResponseDto.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .accessExpiration(3600)
                .build();

        when(serviceController.getAuthToken(VALID_CODE, VALID_STATE)).thenReturn(mockResponse);

        ResponseEntity<TokenResponseDto> response = (ResponseEntity<TokenResponseDto>) authenticationController.getAuthLogin(VALID_CODE, VALID_STATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(serviceController).getAuthToken(VALID_CODE, VALID_STATE);
    }

    @Test
    @DisplayName("Проверка получения токена — клиентская ошибка (BadResponseException)")
    void getAuthLogin_BadRequest() {
        when(serviceController.getAuthToken(VALID_CODE, VALID_STATE))
                .thenThrow(new BadResponseException("Некорректный state"));

        ResponseEntity<?> response = authenticationController.getAuthLogin(VALID_CODE, VALID_STATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto error = (ErrorResponseDto) response.getBody();
        assertThat(error.status()).isEqualTo(400);
        assertThat(error.message()).isEqualTo("Ошибка валидации");
        verify(serviceController).getAuthToken(VALID_CODE, VALID_STATE);
    }

    @Test
    @DisplayName("Проверка обновления токена — успешный сценарий")
    void refresh_Success() {
        TokenResponseDto mockResponse = TokenResponseDto.builder()
                .accessToken("refreshed-access-token")
                .refreshToken("refreshed-refresh-token")
                .accessExpiration(3600)
                .build();

        when(serviceController.refreshToken(VALID_REFRESH_TOKEN)).thenReturn(mockResponse);

        ResponseEntity<TokenResponseDto> response = (ResponseEntity<TokenResponseDto>) authenticationController.refresh(VALID_REFRESH_TOKEN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(serviceController).refreshToken(VALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Проверка обновления токена — пустой refreshToken")
    void refresh_EmptyToken() {
        doThrow(new BadResponseException("Некорректный формат параметров"))
                .when(serviceController).refreshToken("");
        doThrow(new BadResponseException("Некорректный формат параметров"))
                .when(serviceController).refreshToken(null);

        ResponseEntity<?> response1 = authenticationController.refresh("");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response1.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto error1 = (ErrorResponseDto) response1.getBody();
        assertThat(error1.status()).isEqualTo(400);
        assertThat(error1.message()).isEqualTo("Ошибка валидации");

        ResponseEntity<?> response2 = authenticationController.refresh(null);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response2.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto error2 = (ErrorResponseDto) response2.getBody();
        assertThat(error2.status()).isEqualTo(400);
        assertThat(error2.message()).isEqualTo("Ошибка валидации");

        verify(serviceController, times(1)).refreshToken("");
        verify(serviceController, times(1)).refreshToken(null);
    }

    @Test
    @DisplayName("Проверка обновления токена — внутренняя ошибка")
    void refresh_InternalError() {
        when(serviceController.refreshToken(VALID_REFRESH_TOKEN)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = authenticationController.refresh(VALID_REFRESH_TOKEN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto error = (ErrorResponseDto) response.getBody();
        assertThat(error.status()).isEqualTo(500);
        assertThat(error.message()).isEqualTo("Внутренняя ошибка сервера");
        verify(serviceController).refreshToken(VALID_REFRESH_TOKEN);
    }
}