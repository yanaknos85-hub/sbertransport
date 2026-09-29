package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ReceiptScannerDocumentSenderImpl")
class ReceiptScannerDocumentSenderImplTest {

    @Mock
    private ObjectProvider<OutputBridge> receiptScannerDocumentOutput;

    @Mock
    private OutputBridge outputBridge;

    @InjectMocks
    private ReceiptScannerDocumentSenderImpl sender;

    @Test
    @DisplayName("Не должно быть вызовов при null-сообщении")
    void send_null_doesNothing() {
        sender.send(null);
        verifyNoInteractions(receiptScannerDocumentOutput);
    }

    @Test
    @DisplayName("Отправляет сообщение с одним файлом")
    void send_withOneFile() {
        var requestId = UUID.randomUUID();
        var folderId = UUID.randomUUID();
        var message = createMessage(requestId, List.of(createFileData(folderId)));

        setupSendSuccess();

        sender.send(message);

        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(receiptScannerDocumentOutput).ifAvailable(captor.capture());

        captor.getValue().accept(outputBridge);

        assertThat(message.requestId()).isEqualTo(requestId);
        assertThat(message.transportType()).isEqualTo("PUBLIC");
        assertThat(message.cost()).isEqualTo(50000);
        assertThat(message.files()).hasSize(1);
        assertThat(message.files().getFirst().fileName()).isEqualTo("check.png");
        assertThat(message.files().getFirst().ticketCost()).isEqualTo(38000);
    }

    @Test
    @DisplayName("Отправляет сообщение со всеми полями")
    void send_withAllFields() {
        var requestId = UUID.randomUUID();
        var message = ReceiptScannerDocumentMessage.builder()
                .requestId(requestId)
                .transportType("PUBLIC")
                .desiredDate(java.time.LocalDateTime.of(2025, 1, 15, 10, 30))
                .cost(83600)
                .files(List.of(createFileData(UUID.randomUUID())))
                .build();

        setupSendSuccess();

        sender.send(message);

        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(receiptScannerDocumentOutput).ifAvailable(captor.capture());

        captor.getValue().accept(outputBridge);

        assertThat(message.requestId()).isEqualTo(requestId);
        assertThat(message.desiredDate()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 10, 30));
        assertThat(message.cost()).isEqualTo(83600);
    }

    @Test
    @DisplayName("Не выбрасывает исключение при ошибке отправки")
    void send_exceptionIsCaught() {
        var message = createMessage();
        setupSendThrows();

        assertThatCode(() -> sender.send(message)).doesNotThrowAnyException();
    }

    private ReceiptScannerDocumentMessage createMessage() {
        return ReceiptScannerDocumentMessage.builder()
                .requestId(UUID.randomUUID())
                .transportType("PUBLIC")
                .cost(50000)
                .files(List.of(createFileData(UUID.randomUUID())))
                .build();
    }

    private ReceiptScannerDocumentMessage createMessage(UUID requestId, List<ReceiptScannerDocumentMessage.FileData> files) {
        return ReceiptScannerDocumentMessage.builder()
                .requestId(requestId)
                .transportType("PUBLIC")
                .cost(50000)
                .files(files)
                .build();
    }

    private ReceiptScannerDocumentMessage.FileData createFileData(UUID folderId) {
        return ReceiptScannerDocumentMessage.FileData.builder()
                .folderId(folderId)
                .fileName("check.png")
                .ticketCost(38000)
                .build();
    }

    private void setupSendSuccess() {
        doAnswer(invocation -> {
            var consumer = invocation.getArgument(0, Consumer.class);
            consumer.accept(outputBridge);
            return null;
        }).when(receiptScannerDocumentOutput).ifAvailable(any());
    }

    private void setupSendThrows() {
        doThrow(new RuntimeException("send error"))
                .when(receiptScannerDocumentOutput).ifAvailable(any());
    }
}
