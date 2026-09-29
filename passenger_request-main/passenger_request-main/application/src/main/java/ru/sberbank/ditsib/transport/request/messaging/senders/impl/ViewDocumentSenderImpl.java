package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.messaging.message.ViewDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.ViewDocumentSender;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
public class ViewDocumentSenderImpl implements ViewDocumentSender {

    @Qualifier("viewDocumentOutput")
    private final ObjectProvider<OutputBridge> viewDocumentOutput;

    @Override
    public void send(CompensationDocument document, UUID employeeId) {
        final var message = ViewDocumentMessage.builder()
                .documentId(document.getId())
                .dateTime(LocalDateTime.now())
                .employeeId(employeeId)
                .build();
        viewDocumentOutput.ifAvailable(outputBridge -> outputBridge.send(message));
    }
}
