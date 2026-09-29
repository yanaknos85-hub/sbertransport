package ru.sber.transport.push.providers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.business.providers.SendHistoryProvider;
import ru.sber.transport.push.business.providers.TokenProvider;
import ru.sber.transport.push.database.push.Tables;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;

import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@IsolatedTest
@Isolated
@SpringBootTest
@Transactional
@EmbeddedPostgres
@Feature("app_platform_push")
@DisplayName("Проверка провайдера истории уведомлений")
@ActiveProfiles("test")
public class SendHistoryProviderImplTest {

    @Autowired
    private SendHistoryProvider sendHistoryProvider;

    @Autowired
    private DSLContext dslContext;

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var sendHistoryDto = Instancio.create(SendHistoryDto.class);
        uploadRecord(sendHistoryDto);
    }

    private void uploadRecord(SendHistoryDto sendHistoryDto) {
        sendHistoryProvider.save(sendHistoryDto);

        var record = dslContext
                .selectFrom(Tables.SEND_HISTORY)
                .where(Tables.SEND_HISTORY.MESSAGE_ID.eq(sendHistoryDto.getMessageId()))
                .fetchOneInto(Tables.SEND_HISTORY);

        assertNotNull(record);
        assertEquals(sendHistoryDto.getRecipientId(), record.getRecipientId());
        assertEquals(sendHistoryDto.getSendTime().withNano(0), record.getSendTime().withNano(0).withOffsetSameInstant(ZoneOffset.UTC));
        assertEquals(sendHistoryDto.getMessage(), record.getMessage());
    }
}
