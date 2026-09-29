package ru.sberbank.transport.oto.cargo.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.transport.oto.cargo.database.dao.DepartmentRepository;
import ru.sberbank.transport.oto.cargo.database.model.Department;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения данных департамента")
@Disabled("Требует актуализации - падает в Jenkins")
@ActiveProfiles("test")
class DepartmentListenerTest extends KafkaTest {
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @AfterEach
    void dropRepository() {
        departmentRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Новый департамент")
    void addDepartmentTest() {
        UUID id = UUID.randomUUID();
        DepartmentMessage message = DepartmentMessage.builder().
            id(id).
            deleted(false).
            departmentName("Департамент").
            organizationId(UUID.randomUUID()).
            build();
    
        assertThat(departmentRepository.count()).isZero();
    
        produceMessage("service.organization.department", MessageBuilder.withPayload(message).build());
    
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id)).isPresent();
        assertThat(actual.getDepartmentName()).isEqualTo(message.getDepartmentName());
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
    }
    
    @Test
    @DisplayName("Редактирование департамента")
    void editDepartmentTest() {
        UUID id = UUID.randomUUID();
        DepartmentMessage addMessage = DepartmentMessage.builder().
            id(id).
            deleted(false).
            departmentName("Департамент").
            organizationId(UUID.randomUUID()).
            build();
        
        assertThat(departmentRepository.count()).isZero();
        
        produceMessage("service.organization.department", MessageBuilder.withPayload(addMessage).build());
    
        DepartmentMessage editMessage = DepartmentMessage.builder().
            id(id).
            deleted(false).
            departmentName("Новый департамент").
            organizationId(UUID.randomUUID()).
            build();
    
        produceMessage("service.organization.department", MessageBuilder.withPayload(editMessage).build());
        
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id)).isPresent();
        assertThat(actual.getDepartmentName()).isEqualTo(editMessage.getDepartmentName());
        assertThat(actual.getOrganizationId()).isEqualTo(editMessage.getOrganizationId());
    }
    
    @Test
    @DisplayName("Удаление департамента")
    void deleteDepartmentTest() {
        UUID id = UUID.randomUUID();
        DepartmentMessage addMessage = DepartmentMessage.builder().
                id(id).
                deleted(false).
                departmentName("Департамент").
                organizationId(UUID.randomUUID()).
                build();
        
        assertThat(departmentRepository.count()).isZero();
        
        produceMessage("service.organization.department", MessageBuilder.withPayload(addMessage).build());
        
        DepartmentMessage deleteMessage = DepartmentMessage.builder().
                id(id).
                deleted(true).
                build();
        
        produceMessage("service.organization.department", MessageBuilder.withPayload(deleteMessage).build());
        
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id)).isPresent();
        assertThat(actual.getDepartmentName()).isEqualTo(addMessage.getDepartmentName());
        assertThat(actual.getOrganizationId()).isEqualTo(addMessage.getOrganizationId());
    }

}
