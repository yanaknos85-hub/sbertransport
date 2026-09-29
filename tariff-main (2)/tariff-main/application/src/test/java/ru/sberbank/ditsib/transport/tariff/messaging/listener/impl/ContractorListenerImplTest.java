package ru.sberbank.ditsib.transport.tariff.messaging.listener.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@MockitoBean(types = JwtDecoder.class)
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получателя контрагентов")
@ActiveProfiles({"test", "kafka"})
class ContractorListenerImplTest extends KafkaTest {
    
    @MockitoBean
    private ContractorService contractorService;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Test
    @DisplayName("Сообщение с новыми данными")
    void test_message_new() {
        var id = UUID.randomUUID();
        
        var message = createContractorMessage(id);
        
        when(contractorService.get(id)).thenReturn(Optional.empty());
        
        produceMessage("service.contractor", message);
        
        var contractorCaptor = ArgumentCaptor.forClass(Contractor.class);
        
        verify(contractorService).save(contractorCaptor.capture());
        
        var actual = contractorCaptor.getValue();
        
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getRegionIds()).hasSameSizeAs(message.regionIds());
        assertThat(actual.getRegionIds().getFirst()).isEqualTo(message.regionIds().getFirst());
        assertThat(actual.getName()).isEqualTo(message.name());
    }
    
    @Test
    @DisplayName("Сообщение с измененными данными")
    void test_message_edited() {
        var id = UUID.randomUUID();
    
        var message = createContractorMessage(id);
    
        var contractor = new Contractor();
        contractor.setId(id);
        contractor.setName("Name");
        contractor.setRegionIds(new ArrayList<>(List.of(UUID.randomUUID())));
        
        when(contractorService.get(id)).thenReturn(Optional.of(contractor));
        
        produceMessage("service.contractor", message);
    
        var contractorCaptor = ArgumentCaptor.forClass(Contractor.class);
    
        verify(contractorService).save(contractorCaptor.capture());
    
        var actual = contractorCaptor.getValue();
    
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getRegionIds()).hasSameSizeAs(message.regionIds());
        assertThat(actual.getRegionIds().getFirst()).isEqualTo(message.regionIds().getFirst());
        assertThat(actual.getName()).isEqualTo(message.name());
    }
    
    @Test
    @DisplayName("Сообщение с удаленными данными")
    void test_message_deleted() {
        var id = UUID.randomUUID();
    
        var message = createContractorMessage(id);
    
        var contractor = new Contractor();
        contractor.setId(id);
        contractor.setName("Name");
        contractor.setRegionIds(new ArrayList<>(List.of(UUID.randomUUID())));
    
        when(contractorService.get(id)).thenReturn(Optional.of(contractor));
        
        produceMessage("service.contractor", message);
    
        assertEquals(0,contractorRepository.findAll().size());
    }
    
    private ContractorMessage createContractorMessage(UUID id) {
        return new ContractorMessage(
                id,
                "Name",
                "MSRN",
                "TIN",
                10,
                "Contact",
                "Phone",
                List.of(UUID.randomUUID()),
                null, null, null, false, null,
                null,null, null, null, 1, true
        );
    }

}