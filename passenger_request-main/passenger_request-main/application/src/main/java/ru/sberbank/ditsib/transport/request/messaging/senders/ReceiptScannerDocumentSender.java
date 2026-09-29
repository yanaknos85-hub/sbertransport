package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;

/**
 * Отправитель сообщений для сканера чеков.
 */
public interface ReceiptScannerDocumentSender {

    /**
     * Отправить сообщение для сканера чеков.
     *
     * @param message сообщение для сканера
     */
    void send(ReceiptScannerDocumentMessage message);
}
