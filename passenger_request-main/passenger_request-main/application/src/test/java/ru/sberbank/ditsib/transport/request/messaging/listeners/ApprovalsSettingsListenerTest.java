package ru.sberbank.ditsib.transport.request.messaging.listeners;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.OtherTrTypesApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.PublicTrApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.TaxiApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.request.shared.TestSharedData;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.transport.request.shared.TestSharedData.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка слушателя настроек согласований")
@MockitoBean(types = JwtDecoder.class)
class ApprovalsSettingsListenerTest extends KafkaTest {
    
    @Autowired
    @Qualifier("approvalsSettingsInput")
    private Consumer<Message<Map<String, Object>>> approvalsSettingsInput;
    @Autowired
    private TaxiApprovalsSettingsRepository taxiSettingsRepository;
    @Autowired
    private PublicTrApprovalsSettingsRepository publicSettingsRepository;
    @Autowired
    private OtherTrTypesApprovalsSettingsRepository otherTrSettingsRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private TestSharedData sharedData = new TestSharedData();
    
    @AfterEach
    void clear() {
        taxiSettingsRepository.deleteAll();
        publicSettingsRepository.deleteAll();
        otherTrSettingsRepository.deleteAll();
    }
    
    @Test
    void handleTaxiMessage() {
        var item1 = sharedData.createMessageItem(REGION1_ID, 1500, PURPOSE1_ID);
        var item2 = sharedData.createMessageItem(REGION2_ID, 1000, PURPOSE2_ID);
        var message = sharedData.createTaxiMessage(true, 700, ORG1_ID, List.of(item1, item2));
        approvalsSettingsInput.accept(MessageBuilder.withPayload(objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {})).build());
        
        assertThat(taxiSettingsRepository.count()).isEqualTo(1);
        assertThat(publicSettingsRepository.count()).isEqualTo(0);
        assertThat(otherTrSettingsRepository.count()).isEqualTo(0);
        TaxiApprovalsSettings settings = taxiSettingsRepository.findAll().get(0);
        sharedData.commonCheck(message, settings);
    }
    
    @Test
    void handlePublicMessage() {
        var item1 = sharedData.createMessageItem(REGION1_ID, 1500, PURPOSE1_ID);
        var item2 = sharedData.createMessageItem(REGION2_ID, 1000, PURPOSE2_ID);
        var message = sharedData.createPublicMessage(
                true, 700, ORG1_ID, true, true,
                false, true, List.of(item1, item2));
        approvalsSettingsInput.accept(MessageBuilder.withPayload(objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {})).build());
        
        assertThat(taxiSettingsRepository.count()).isEqualTo(0);
        assertThat(publicSettingsRepository.count()).isEqualTo(1);
        assertThat(otherTrSettingsRepository.count()).isEqualTo(0);
        PublicTrApprovalsSettings settings = publicSettingsRepository.findAll().get(0);
        sharedData.publicCheck(message, settings);
    }
    
    @Test
    void handleOtherTrMessage() {
        var item1 = sharedData.createMessageItem(REGION1_ID, 1500, PURPOSE1_ID);
        var item2 = sharedData.createMessageItem(REGION2_ID, 1000, PURPOSE2_ID);
        var message = sharedData.createOtherTrMessage(
                true, 700, ORG1_ID, TransportTypeEnum.CARSHARING, false, List.of(item1, item2));
        approvalsSettingsInput.accept(MessageBuilder.withPayload(objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {})).build());
        
        assertThat(taxiSettingsRepository.count()).isEqualTo(0);
        assertThat(publicSettingsRepository.count()).isEqualTo(0);
        assertThat(otherTrSettingsRepository.count()).isEqualTo(1);
        OtherTrTypesApprovalsSettings settings = otherTrSettingsRepository.findAll().get(0);
        sharedData.otherTrCheck(message, settings);
    }
}