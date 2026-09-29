package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.reports.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;
import ru.sberbank.ditsib.transport.reports.model.Department;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения данных департамента")
@MockitoBean(types = JwtDecoder.class)
public class DepartmentListenerTest extends SharedTest {
    
    @Autowired
    private Consumer<Message<DepartmentMessage>> departmentInput;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
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
    
        assertThat(departmentRepository.count()).isEqualTo(0);
        
        departmentInput.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id).orElse(null)).isNotNull();
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
        
        assertThat(departmentRepository.count()).isEqualTo(0);
        
        departmentInput.accept(MessageBuilder.withPayload(addMessage).build());
    
        DepartmentMessage editMessage = DepartmentMessage.builder().
            id(id).
            deleted(false).
            departmentName("Новый департамент").
            organizationId(UUID.randomUUID()).
            build();
        
        departmentInput.accept(MessageBuilder.withPayload(editMessage).build());
        
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id).orElse(null)).isNotNull();
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
        
        assertThat(departmentRepository.count()).isEqualTo(0);
        
        departmentInput.accept(MessageBuilder.withPayload(addMessage).build());
        
        DepartmentMessage deleteMessage = DepartmentMessage.builder().
                id(id).
                deleted(true).
                build();
        
        departmentInput.accept(MessageBuilder.withPayload(deleteMessage).build());
        
        assertThat(departmentRepository.count()).isEqualTo(1);
        Department actual = departmentRepository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(departmentRepository.findById(id).orElse(null)).isNotNull();
        assertThat(actual.getDepartmentName()).isEqualTo(addMessage.getDepartmentName());
        assertThat(actual.getOrganizationId()).isEqualTo(addMessage.getOrganizationId());
    }

}
