package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestDocumentSender;

import java.util.UUID;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
public class RequestDocumentSenderImpl implements RequestDocumentSender {

    @Qualifier("requestDocumentOutput")
    private final ObjectProvider<OutputBridge> requestDocumentOutput;

    @Override
    public void send(CompensationDocument document, UUID requestId, UUID employeeId) {
        requestDocumentOutput.ifAvailable(it -> it.send(RequestDocumentMessage.builder()
                        .documentId(document.getId())
                        .folderId(document.getFolder())
                        .fileName(document.getFileName())
                        .requestId(requestId)
                        .employeeId(employeeId)
                        .build()));
    }

    @Override
    public void sendDeleted(CompensationDocument document, UUID requestId, UUID employeeId) {
        requestDocumentOutput.ifAvailable(it -> it.send(RequestDocumentMessage.builder()
                        .documentId(document.getId())
                        .requestId(requestId)
                        .employeeId(employeeId)
                        .deleted(true)
                        .build()));
    }
}
