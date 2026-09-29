package ru.sberbank.ditsib.transport.reports.scheduler;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.dao.InboxMessageRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;
import ru.sberbank.ditsib.transport.reports.enums.RequestTaxiStatus;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {"SCHEDULER_INBOX_MESSAGES_READ=0/5 * * * * *"})
@EmbeddedPostgres
class InboxMessageSchedulerTest {

    @Autowired
    private InboxMessageRepository inboxMessageRepository;
    @Autowired
    private RequestRepository requestRepository;

    @SneakyThrows
    @Test
    @Sql({"/scripts/basic_corp_structure.sql", "/scripts/inbox_message.sql"})
    void processNewMessages() {
        var requestId = UUID.fromString("9e3d7ca2-14c3-4d1a-b2fc-2db34743c06e");
        await().atMost(Duration.ofSeconds(10)).pollDelay(Duration.ofSeconds(1))
                .until(() -> inboxMessageRepository.findAll().stream()
                        .noneMatch(inboxMessage -> inboxMessage.getStatus().equals(InboxMessageStatusEnum.NEW.name())));
        var actualMessages = inboxMessageRepository.findAll();
        assertThat(actualMessages)
                .hasSize(2)
                .extracting(InboxMessage::getMessageId,
                        InboxMessage::getEntityId,
                        InboxMessage::getStatus,
                        InboxMessage::getErrorReason)
                .containsExactlyInAnyOrder(tuple(UUID.fromString("5b79f508-71a3-4a81-8eed-213a8b4773aa"),
                                requestId,
                                InboxMessageStatusEnum.ERROR.name(),
                                "Name is null"),
                        tuple(UUID.fromString("bb29872c-5127-4627-b416-fd114471ccf6"),
                                requestId,
                                InboxMessageStatusEnum.DONE.name(),
                                null));
        var actualRequests = requestRepository.findAll();
        assertThat(actualRequests)
                .hasSize(1)
                .extracting(Request::getId,
                        Request::getStatus)
                .containsExactly(tuple(requestId,
                        RequestTaxiStatus.TAXI_AWAITING_APPROVAL.name()));
    }
}