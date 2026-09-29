package ru.sberbank.ditsib.transport.tariff.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class ContractorServiceImplTest extends KafkaTest {
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @MockitoBean
    private ContractService contractService;
    
    private ContractorService contractorService;
    
    @BeforeEach
    void setup() {
        contractorService = new ContractorServiceImpl(contractorRepository, contractService);
    }
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        var contractor = new Contractor();
        
        contractor.setId(UUID.randomUUID());
        contractor.setName("name");
        contractor.setRegionIds(new ArrayList<>(List.of(UUID.randomUUID())));
        
        contractorService.save(contractor);
        
        var actual = contractorRepository.findById(contractor.getId());
        
        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(contractor.getId());
        assertThat(actual.get().getName()).isEqualTo(contractor.getName());
        assertThat(actual.get().getRegionIds()).hasSameSizeAs(contractor.getRegionIds());
        assertThat(actual.get().getRegionIds().getFirst()).isEqualTo(contractor.getRegionIds().getFirst());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var contractor = new Contractor();
    
        contractor.setId(UUID.randomUUID());
        contractor.setName("name");
        contractor.setRegionIds(new ArrayList<>(List.of(UUID.randomUUID())));
    
        contractorRepository.save(contractor);
        
        var actual = contractorService.get(contractor.getId());
    
        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(contractor.getId());
        assertThat(actual.get().getName()).isEqualTo(contractor.getName());
        assertThat(actual.get().getRegionIds()).hasSameSizeAs(contractor.getRegionIds());
        assertThat(actual.get().getRegionIds().getFirst()).isEqualTo(contractor.getRegionIds().getFirst());
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_unexists() {
        var actual = contractorService.get(UUID.randomUUID());
        
        assertThat(actual).isEmpty();
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var contractor = new Contractor();
    
        contractor.setId(UUID.randomUUID());
        contractor.setIntegrationType(TaxiExternalIntegrationType.JSON_API_1_0.name());
        contractor.setName("name");
        contractor.setRegionIds(new ArrayList<>(List.of(UUID.randomUUID())));
    
        contractor = contractorRepository.save(contractor);
        
        when(contractService.getByContractor(contractor.getId())).thenReturn(List.of(new Contract()));
        
        assertThat(contractorRepository.count()).isEqualTo(1);
        
        contractorService.delete(contractor);
    
        assertThat(contractorRepository.count()).isZero();
        
        var contractCaptor = ArgumentCaptor.forClass(Contract.class);
        
        verify(contractService).delete(contractCaptor.capture());
        
        assertThat(contractCaptor.getValue()).isNotNull();
    }
}