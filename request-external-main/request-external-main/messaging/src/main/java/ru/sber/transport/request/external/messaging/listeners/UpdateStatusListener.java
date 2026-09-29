package ru.sber.transport.request.external.messaging.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.request.external.messaging.message.UpdateTripRequestStatusMessage;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Слушатель обновлений статуса заявки из внешних систем (например, ai_payout_check).
 * Получает сообщение {@link UpdateTripRequestStatusMessage} и обновляет статус заявки через провайдер.
 */
@Slf4j
@RequiredArgsConstructor
public class UpdateStatusListener implements Consumer<UpdateTripRequestStatusMessage> {

    private final TripOrdersProvider tripOrdersProvider;

    @Override
    public void accept(UpdateTripRequestStatusMessage message) {
        UUID requestId = message.getId();
        String newStatus = message.getStatus();
        log.info("Updating status for request {} to {}", requestId, newStatus);

        tripOrdersProvider.edit(
                null,
                requestId,
                new UpdateStatusEditOrderData(newStatus),
                Set.of("=status")
        );
        log.debug("Successfully updated status for request {} to {}", requestId, newStatus);
    }

    /**
     * Внутренняя реализация {@link EditTripOrderData} для обновления статуса заявки.
     */
    private record UpdateStatusEditOrderData(String status) implements EditTripOrderData {
        @Override
        public OrderData getActual() {
            return null;
        }

        @Override
        public State getStatus() {
            return State.valueOf(status);
        }

        @Override
        public String getReason() {
            return null;
        }

        @Override
        public Assessments getAssessments() {
            return null;
        }

        @Override
        public String getReceiptLink() {
            return null;
        }
    }
}