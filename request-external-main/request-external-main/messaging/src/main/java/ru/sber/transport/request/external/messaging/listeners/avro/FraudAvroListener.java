package ru.sber.transport.request.external.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.messages.ai_receipt_scanner.avro.FraudMessage;
import ru.sber.transport.request.external.model.Fraud;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class FraudAvroListener implements Consumer<Message<FraudMessage>> {

    public static final String FRAUD_TYPE_RECEIPT = "RECEIPT";
    private final TripOrdersProvider tripOrdersProvider;
    private final FraudsProvider fraudsProvider;

    @Override
    public void accept(Message<FraudMessage> raw) {
        final var isRequestExists = tripOrdersProvider.exists(raw.getPayload().getId());
        if (isRequestExists) {
            final var fraud = createFraud(raw.getPayload());
            fraudsProvider.save(fraud);
            log.debug("Successfully saved fraud with id: {}", fraud.getId());
        } else {
            log.debug("Trip order with id: {} not found", raw.getPayload().getId());
        }
    }

    private Fraud createFraud(FraudMessage fraudMessage) {
        return new Fraud() {

            @Override
            public UUID getId() {
                return fraudMessage.getId();
            }

            @Override
            public String getComment() {
                return fraudMessage.getComment();
            }

            @Override
            public String getType() {
                return FRAUD_TYPE_RECEIPT;
            }
        };
    }
}
