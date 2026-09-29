package ru.sber.transport.push.providers;

import com.google.firebase.messaging.FirebaseMessaging;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.business.providers.TokenProvider;
import ru.sber.transport.push.database.push.Tables;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;

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
@DisplayName("Проверка провайдера токенов")
@ActiveProfiles("test")
public class TokenProviderImplTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private DSLContext dslContext;

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var tokenData = Instancio.create(TokenData.class);
        var recipientId = Instancio.create(UUID.class);
        var id = Instancio.create(UUID.class);

        uploadRecord(id, recipientId, tokenData);
    }

    @Test
    @DisplayName("Проверка обновления")
    void test_update() {
        var tokenData = Instancio.create(TokenData.class);
        var recipientId = Instancio.create(UUID.class);
        var id = Instancio.create(UUID.class);

        uploadRecord(id, recipientId, tokenData);

        tokenData.setValue(Instancio.create(String.class));
        tokenData.setPlatformType(Instancio.create(PlatformType.class));

        uploadRecord(id, recipientId, tokenData);
    }

    @Test
    @DisplayName("Проверка получения")
    void test_get() {
        var record = new TokenRecord();
        record.setId(UUID.randomUUID());
        record.setRecipientId(UUID.randomUUID());
        record.setPlatformType(Instancio.create(PlatformType.class).name());
        record.setValue(Instancio.create(String.class));

        dslContext
                .insertInto(Tables.TOKEN)
                .set(record)
                .execute();

        var model = tokenProvider.get(record.getRecipientId()).get(0);

        assertEquals(record.getPlatformType(), model.getPlatformType().name());
        assertEquals(record.getValue(), model.getValue());
    }

    @Test
    @DisplayName("Проверка изменения владельца токена")
    void test_change_token_owner() {
        var record = new TokenRecord();
        record.setId(UUID.randomUUID());
        record.setRecipientId(UUID.randomUUID());
        record.setPlatformType(Instancio.create(PlatformType.class).name());
        record.setValue(Instancio.create(String.class));

        dslContext
                .insertInto(Tables.TOKEN)
                .set(record)
                .execute();

        var tokenData = new TokenData();
        tokenData.setId(record.getId());
        tokenData.setPlatformType(PlatformType.valueOf(record.getPlatformType()));
        tokenData.setValue(record.getValue());
        var newOwnerId = UUID.randomUUID();

       uploadRecord(record.getId(), newOwnerId, tokenData);
    }

    private void uploadRecord(UUID id, UUID recipientId, TokenData tokenData) {
        tokenProvider.save(id, recipientId, tokenData);

        var record = dslContext
                .selectFrom(Tables.TOKEN)
                .where(Tables.TOKEN.RECIPIENT_ID.eq(recipientId))
                .fetchOneInto(Tables.TOKEN);

        assertNotNull(record);
        assertEquals(recipientId, record.getRecipientId());
        assertEquals(tokenData.getPlatformType().name(), record.getPlatformType());
        assertEquals(tokenData.getValue(), record.getValue());
    }
}
