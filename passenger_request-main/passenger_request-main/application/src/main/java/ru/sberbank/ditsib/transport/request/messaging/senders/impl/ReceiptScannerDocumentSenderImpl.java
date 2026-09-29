package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.ReceiptScannerDocumentSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptScannerDocumentSenderImpl implements ReceiptScannerDocumentSender {

    @Qualifier("receiptScannerDocumentOutput")
    private final ObjectProvider<OutputBridge> receiptScannerDocumentOutput;

    @Override
    public void send(ReceiptScannerDocumentMessage message) {
        if (message != null) {
            try {
                receiptScannerDocumentOutput.ifAvailable(bridge -> bridge.send(message));
            } catch (Exception e) {
                log.error("Ошибка при отправке сообщения в топик service.ai.receipt.scanner.listen по заявке {}", message.requestId(), e);
            }
        }
    }
}
