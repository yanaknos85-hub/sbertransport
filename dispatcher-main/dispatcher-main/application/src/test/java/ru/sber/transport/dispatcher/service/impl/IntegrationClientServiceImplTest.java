package ru.sber.transport.dispatcher.service.impl;

import feign.FeignException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.IntegrationClientRepository;
import ru.sber.transport.dispatcher.database.model.IntegrationClient;
import ru.sber.transport.dispatcher.dto.feign.RegistrationResponseDto;
import ru.sber.transport.dispatcher.exceptions.FeignClientException;
import ru.sber.transport.dispatcher.feign.RegistrationClient;
import ru.sber.transport.dispatcher.messaging.senders.IntegrationClientSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка сервиса интеграции")
@ExtendWith(MockitoExtension.class)
public class IntegrationClientServiceImplTest {
    @InjectMocks
    private IntegrationClientServiceImpl service;

    @Mock
    private RegistrationClient registrationClient;

    @Mock
    private IntegrationClientRepository repository;

    @Mock
    private IntegrationClientSender sender;

    private static final UUID CONTRACTOR_ID = UUID.randomUUID();
    private static final String EMAIL = "example@example.com";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "test_password";
    private static final UUID USER_ID = UUID.randomUUID();


    @DisplayName("Успешная регистрация ТУЗ")
    @Test
    void shouldRegisterIntegrationClientSuccessfully() throws Exception {
        when(registrationClient.register(any())).thenReturn(new RegistrationResponseDto(USER_ID));

        var integrationClient = IntegrationClient.builder()
                .id(USER_ID)
                .contractorId(CONTRACTOR_ID)
                .build();

        when(repository.save(any())).thenReturn(integrationClient);

        service.add(CONTRACTOR_ID, EMAIL, LOGIN, PASSWORD);

        verify(registrationClient).register(any());
        verify(repository).save(any());
        verify(sender).send(eq(integrationClient));
    }

    @DisplayName("Ошибка регистрации при серверной ошибке")
    @Test
    void shouldFailOnServerErrorDuringRegistration() {
        doThrow(FeignException.FeignServerException.class)
                .when(registrationClient).register(any());

        assertThrows(RuntimeException.class,
                () -> service.add(CONTRACTOR_ID, EMAIL, LOGIN, PASSWORD));
    }

    @DisplayName("Ошибка регистрации при клиентской ошибке")
    @Test
    void shouldFailOnClientErrorDuringRegistration() {
        doThrow(FeignException.FeignClientException.class)
                .when(registrationClient).register(any());

        assertThrows(FeignClientException.class,
                () -> service.add(CONTRACTOR_ID, EMAIL, LOGIN, PASSWORD));
    }

    @DisplayName("Непредвиденная ошибка при регистрации")
    @Test
    void shouldFailOnUnexpectedErrorDuringRegistration() {
        doThrow(RuntimeException.class)
                .when(registrationClient).register(any());

        assertThrows(RuntimeException.class,
                () -> service.add(CONTRACTOR_ID, EMAIL, LOGIN, PASSWORD));
    }
}
