package ru.sber.transport.request.external.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.request.external.messaging.message.UpdateTripRequestStatusMessage;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.State;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных об обновлении статуса из кафки")
class UpdateStatusListenerTest {

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    @Test
    @DisplayName("Получение актуальных данных об обновлении статуса")
    void testUpdateStatusForExistingTripOrder() {
        final var status = Instancio.of(State.class).create().name();
        final var message = new UpdateTripRequestStatusMessage(UUID.randomUUID(), status, null, null);

        new UpdateStatusListener(tripOrdersProvider).accept(message);

        final var editCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        verify(tripOrdersProvider).edit(eq(null), eq(message.getId()), editCaptor.capture(), eq(Set.of("=status")));

        final var editData = editCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(editData.getStatus().name()).isEqualTo(status);
        });
    }

    @Test
    @DisplayName("Обновление с явным статусом CONFIRMED")
    void testUpdateWithExplicitStatus() {
        final var requestId = UUID.randomUUID();
        final var status = "CONFIRMED";
        final var message = new UpdateTripRequestStatusMessage(requestId, status, null, null);

        new UpdateStatusListener(tripOrdersProvider).accept(message);

        final var editCaptor = ArgumentCaptor.forClass(EditTripOrderData.class);
        verify(tripOrdersProvider).edit(eq(null), eq(requestId), editCaptor.capture(), eq(Set.of("=status")));

        final var editData = editCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(editData.getStatus()).isEqualTo(State.CONFIRMED);
        });
    }
}