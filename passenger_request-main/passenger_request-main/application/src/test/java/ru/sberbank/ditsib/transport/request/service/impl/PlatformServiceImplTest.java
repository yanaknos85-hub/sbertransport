package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.messaging.resolvers.PlatformResolver;
import ru.sberbank.ditsib.transport.request.service.PlatformService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка функционала интеграции с корпклиентом")
class PlatformServiceImplTest {

    private final PlatformResolver resolver = mock(PlatformResolver.class);

    private final PlatformService service = new PlatformServiceImpl(resolver);

    @Test
    @DisplayName("Проверка получения группы исполнителей")
    void test_getExecutorGroup() {
        final var employeeId = UUID.randomUUID();
        final var regions = Instancio.createList(UUID.class);
        final var token = Instancio.create(String.class);
        final var data = Instancio.create(ExecutorGroupDTO.class);

        when(resolver.getExecutorGroupByEmployeeId(employeeId, regions, token)).thenReturn(data);

        final var group = service.getExecutorGroup(employeeId, regions, token);

        assertThat(group).isEqualTo(data);
    }

}