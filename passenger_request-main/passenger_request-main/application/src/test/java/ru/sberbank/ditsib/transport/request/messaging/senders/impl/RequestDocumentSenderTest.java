package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestDocumentMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestDocumentSender;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats.getByFileFormat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка отправки информации о создании/удалении документов заявок в Kafka.")
class RequestDocumentSenderTest extends KafkaTest {

    @Autowired
    private RequestDocumentSender sender;

    @MockitoBean("requestDocumentOutput")
    private OutputBridge requestDocumentOutput;

    @Test
    @DisplayName("Отправка")
    void test() {
        var expectedData = new CompensationDocument();
        var id = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var folderId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var expectedFileFormat = getByFileFormat("jpeg").get();
        var expectedFileName = "fileName";
        var expectedFileSize = 1000;
        var expectedCreationTime = LocalDateTime.now().plusDays(1);

        expectedData.setId(id);
        expectedData.setCreationTime(expectedCreationTime);
        expectedData.setFileFormat(expectedFileFormat);
        expectedData.setFileName(expectedFileName);
        expectedData.setFileSize(expectedFileSize);
        expectedData.setFolder(folderId);

        sender.send(expectedData, requestId, employeeId);

        final var messageCaptor = ArgumentCaptor.forClass(RequestDocumentMessage.class);
        verify(requestDocumentOutput).send(messageCaptor.capture());
        final var actual = messageCaptor.getValue();

        assertThat(actual.getDocumentId()).isEqualTo(id);
        assertThat(actual.getRequestId()).isEqualTo(requestId);
        assertThat(actual.getFolderId()).isEqualTo(folderId);
        assertThat(actual.getEmployeeId()).isEqualTo(employeeId);
        assertThat(actual.getFileName()).isEqualTo(expectedFileName);
        assertThat(actual.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Отправка удаления")
    void test_deleted() {
        var expectedData = new CompensationDocument();
        var id = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var folderId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var expectedFileFormat = getByFileFormat("jpeg").get();
        var expectedFileName = "fileName";
        var expectedFileSize = 1000;
        var expectedCreationTime = LocalDateTime.now().plusDays(1);

        expectedData.setId(id);
        expectedData.setCreationTime(expectedCreationTime);
        expectedData.setFileFormat(expectedFileFormat);
        expectedData.setFileName(expectedFileName);
        expectedData.setFileSize(expectedFileSize);
        expectedData.setFolder(folderId);

        sender.sendDeleted(expectedData, requestId, employeeId);
        final var messageCaptor = ArgumentCaptor.forClass(RequestDocumentMessage.class);
        verify(requestDocumentOutput).send(messageCaptor.capture());
        final var actual = messageCaptor.getValue();

        assertThat(actual.getDocumentId()).isEqualTo(id);
        assertThat(actual.getRequestId()).isEqualTo(requestId);
        assertThat(actual.getEmployeeId()).isEqualTo(employeeId);
        assertThat(actual.isDeleted()).isTrue();
    }
}