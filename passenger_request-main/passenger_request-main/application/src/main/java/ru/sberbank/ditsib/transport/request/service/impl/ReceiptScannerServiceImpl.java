package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.ReceiptScannerDocumentSender;
import ru.sberbank.ditsib.transport.request.service.ReceiptScannerService;

@Service
@RequiredArgsConstructor
public class ReceiptScannerServiceImpl implements ReceiptScannerService {

    private final ReceiptScannerDocumentSender receiptScannerDocumentSender;

    @Override
    public void sendToReceiptScanner(ReceiptScannerDocumentMessage request) {
        receiptScannerDocumentSender.send(request);
    }
}
