package ru.sber.transport.request.external.messaging.senders.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.MessagingException;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.exceptions.BrokerException;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage;
import ru.sber.transport.request.external.messaging.senders.ReceiptSender;

/**
 * Реализация отправителя данных чека в сканнер чеков
 */
@Slf4j
@RequiredArgsConstructor
public class ReceiptScannerSenderImpl implements ReceiptSender {

    private final ObjectProvider<OutputBridge> receiptScannerOutput;
    private final ObjectProvider<OutputBridge> receiptScannerOutputSsl;

    @Override
    public void send(@NonNull ReceiptMessage message) throws BrokerException {
        try {
            receiptScannerOutput.ifAvailable(bridge -> bridge.send(message));
            receiptScannerOutputSsl.ifAvailable(bridge -> bridge.send(message));
        } catch (RuntimeException ex) {
            log.warn("Чек заявки [{}] не был отправлен в брокер сообщений : ", message.requestId(), ex);
            throw new BrokerException(ex);
        }
    }
}
