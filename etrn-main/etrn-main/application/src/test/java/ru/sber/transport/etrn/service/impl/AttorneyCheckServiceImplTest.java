package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.etrn.client.DispatcherClient;
import ru.sber.transport.etrn.dto.AttorneyCheckResponseDto;
import ru.sber.transport.etrn.dto.DispatcherDto;
import ru.sber.transport.etrn.exceptions.AttorneyCheckException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Тесты для AttorneyCheckServiceImpl.
 */
@DisplayName("Тесты сервиса проверки доверенностей")
@ExtendWith(MockitoExtension.class)
class AttorneyCheckServiceImplTest {

    @Mock
    private DispatcherClient dispatcherClient;

    @InjectMocks
    private AttorneyCheckServiceImpl attorneyCheckService;

    private void withMockedLocalDate(LocalDate now, RunnableWithException test) throws Exception {
        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(LocalDate::now).thenReturn(now);
            test.run();
        }
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    @Test
    @DisplayName("Успешная проверка доверенности — возвращает 200 с данными")
    void checkAttorney_success_returnsDto() throws Exception {
        // Arrange — все LocalDate создаются до входа в mock
        LocalDate issueDate = LocalDate.of(2025, 1, 15);
        LocalDate expiryDate = LocalDate.of(2027, 1, 15);
        var dispatcherDto = new DispatcherDto("TRN-2025-001", issueDate, expiryDate);
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok(dispatcherDto));

        LocalDate now = LocalDate.of(2026, 8, 12);
        withMockedLocalDate(now, () -> {
            // Act
            ResponseEntity<AttorneyCheckResponseDto> response = attorneyCheckService.checkAttorney();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().attorneyNumber()).isEqualTo("TRN-2025-001");
            assertThat(response.getBody().issueDate()).isEqualTo(issueDate);
            assertThat(response.getBody().expiryDate()).isEqualTo(expiryDate);
        });
    }

    @Test
    @DisplayName("Пустой ответ от Dispatcher — выбрасывает исключение")
    void checkAttorney_nullDispatcherDto_throwsException() throws Exception {
        // Arrange
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok((DispatcherDto) null));

        withMockedLocalDate(LocalDate.of(2026, 8, 12), () -> {
            // Act & Assert
            assertThatThrownBy(() -> attorneyCheckService.checkAttorney())
                    .isInstanceOf(AttorneyCheckException.class)
                    .hasMessageContaining("пустой ответ");
        });
    }

    @Test
    @DisplayName("Исключение от Feign-клиента — пробрасывается дальше")
    void checkAttorney_feignException_throwsException() throws Exception {
        // Arrange
        when(dispatcherClient.getSelfProfile())
                .thenThrow(new RuntimeException("Network error"));

        withMockedLocalDate(LocalDate.of(2026, 8, 12), () -> {
            // Act & Assert
            assertThatThrownBy(() -> attorneyCheckService.checkAttorney())
                    .isInstanceOf(AttorneyCheckException.class)
                    .hasMessageContaining("Network error");
        });
    }

    @Test
    @DisplayName("Проверка доверенности с null полями — возвращает DTO с null")
    void checkAttorney_withNullFields_returnsDtoWithNulls() throws Exception {
        // Arrange
        var dispatcherDto = new DispatcherDto(null, null, null);
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok(dispatcherDto));

        withMockedLocalDate(LocalDate.of(2026, 8, 12), () -> {
            // Act
            ResponseEntity<AttorneyCheckResponseDto> response = attorneyCheckService.checkAttorney();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().attorneyNumber()).isNull();
            assertThat(response.getBody().issueDate()).isNull();
            assertThat(response.getBody().expiryDate()).isNull();
        });
    }

    @Test
    @DisplayName("Доверенность ещё не действует — ошибка, если today до issueDate")
    void checkAttorney_futureIssueDate_throwsException() throws Exception {
        // Arrange
        LocalDate now = LocalDate.of(2026, 8, 12);
        LocalDate futureIssueDate = LocalDate.of(2026, 9, 1);
        LocalDate expiryDate = LocalDate.of(2027, 9, 1);
        var dispatcherDto = new DispatcherDto("TRN-2025-001", futureIssueDate, expiryDate);
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok(dispatcherDto));

        withMockedLocalDate(now, () -> {
            // Act & Assert
            assertThatThrownBy(() -> attorneyCheckService.checkAttorney())
                    .isInstanceOf(AttorneyCheckException.class)
                    .hasMessageContaining("недействительна")
                    .hasMessageContaining("TRN-2025-001");
        });
    }

    @Test
    @DisplayName("Срок действия доверенности истёк — ошибка, если today после expiryDate")
    void checkAttorney_expiredExpiryDate_throwsException() throws Exception {
        // Arrange
        LocalDate now = LocalDate.of(2026, 8, 12);
        LocalDate pastExpiryDate = LocalDate.of(2026, 1, 15);
        LocalDate issueDate = LocalDate.of(2025, 1, 15);
        var dispatcherDto = new DispatcherDto("TRN-2025-001", issueDate, pastExpiryDate);
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok(dispatcherDto));

        withMockedLocalDate(now, () -> {
            // Act & Assert
            assertThatThrownBy(() -> attorneyCheckService.checkAttorney())
                    .isInstanceOf(AttorneyCheckException.class)
                    .hasMessageContaining("недействительна")
                    .hasMessageContaining("TRN-2025-001");
        });
    }

    @Test
    @DisplayName("Доверенность валидна — today входит в диапазон [issueDate, expiryDate]")
    void checkAttorney_validDateRange_returnsDto() throws Exception {
        // Arrange
        LocalDate now = LocalDate.of(2026, 8, 12);
        var dispatcherDto = new DispatcherDto("TRN-2025-001", LocalDate.of(2025, 1, 15), LocalDate.of(2027, 1, 15));
        when(dispatcherClient.getSelfProfile()).thenReturn(ResponseEntity.ok(dispatcherDto));

        withMockedLocalDate(now, () -> {
            // Act
            ResponseEntity<AttorneyCheckResponseDto> response = attorneyCheckService.checkAttorney();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().attorneyNumber()).isEqualTo("TRN-2025-001");
        });
    }
}
