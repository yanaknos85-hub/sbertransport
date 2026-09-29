package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;

/**
 * Сервис для отправки документов в сканер чеков.
 */
public interface ReceiptScannerService {

    /**
     * Отправляет документ в сканер чеков.
     *
     * @param request сообщение с данными документа для сканирования
     */
    void sendToReceiptScanner(ReceiptScannerDocumentMessage request);
}
