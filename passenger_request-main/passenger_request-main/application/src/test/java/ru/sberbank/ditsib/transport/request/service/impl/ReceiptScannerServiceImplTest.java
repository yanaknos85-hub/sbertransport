package ru.sberbank.ditsib.transport.request.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.ReceiptScannerDocumentSender;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ReceiptScannerServiceImpl")
class ReceiptScannerServiceImplTest {

    @Mock
    private ReceiptScannerDocumentSender receiptScannerDocumentSender;

    @InjectMocks
    private ReceiptScannerServiceImpl receiptScannerService;

    @Test
    @DisplayName("Отправляет сообщение в сканер")
    void sendToReceiptScanner_sendsMessage() {
        var message = createMessage();

        receiptScannerService.sendToReceiptScanner(message);

        verify(receiptScannerDocumentSender).send(message);
    }

    private ReceiptScannerDocumentMessage createMessage() {
        return ReceiptScannerDocumentMessage.builder()
                .requestId(UUID.randomUUID())
                .transportType("PUBLIC")
                .cost(50000)
                .files(List.of(createFileData(UUID.randomUUID())))
                .build();
    }

    private ReceiptScannerDocumentMessage.FileData createFileData(UUID folderId) {
        return ReceiptScannerDocumentMessage.FileData.builder()
                .folderId(folderId)
                .fileName("check.png")
                .ticketCost(38000)
                .build();
    }
}
