package ru.sber.transport.push.providers.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.database.push.tables.records.TokenRecord;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_push")
@DisplayName("Проверка маппера токенов")
public class TokenMapperTest {

    private final TokenMapper tokenMapper = new TokenMapperImpl();

    @Test
    @DisplayName("Модель в рекорд")
    void test_toRecord() {
        var tokenData = Instancio.create(TokenData.class);
        var recipientId = Instancio.create(UUID.class);

        var record = tokenMapper.toRecord(recipientId, tokenData);

        assertEquals(recipientId, record.getRecipientId());
        assertEquals(tokenData.getPlatformType().name(), record.getPlatformType());
        assertEquals(tokenData.getValue(), record.getValue());
    }

    @Test
    @DisplayName("Рекорд в модель")
    void test_toModel() {
        var record = new TokenRecord();
        record.setRecipientId(UUID.randomUUID());
        record.setPlatformType(Instancio.create(PlatformType.class).name());
        record.setValue(Instancio.create(String.class));

        var model = tokenMapper.toModel(record);

        assertEquals(record.getPlatformType(), model.getPlatformType().name());
        assertEquals(record.getValue(), model.getValue());
    }
}
