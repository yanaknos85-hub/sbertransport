package ru.sber.transport.notifications.commands;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.DelegateRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@AutoConfigureMockMvc
@DisplayName("Проверка отправки уведомлений делегатов")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class DelegateCommandsTest extends SharedCommands {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DelegateRepository delegateRepository;
    
    @AfterEach
    void removeAllData() {
        employeeRepository.deleteAll();
        organizationRepository.deleteAll();
        departmentRepository.deleteAll();
        delegateRepository.deleteAll();
        notificationSettingsRepository.deleteAll();
    }
    
    @Order(1)
    @Test
    @DisplayName("Проверка отправки уведомления 'Назначение' (notice_1101)")
    void test_userDelegateAssingmentCommand() {
        organizationMessageInput.accept(MessageBuilder.withPayload(organizationMessage).build());
        departmentMessageInput.accept(MessageBuilder.withPayload(departmentMessage).build());

        var notificationMessage = "Вам делегированы полномочия на согласование обращений по {transportType} " +
                "на период с {startDate} по {endDate}";

        supervisorEmployeeMessageInput.accept(MessageBuilder.withPayload(supervisorEmployeeMessage).build());
        delegateEmployeeMessageInput.accept(MessageBuilder.withPayload(delegateEmployeeMessage).build());
        assertThat(employeeRepository.count()).isEqualTo(2);

        var tokens = delegateEmployeeMessage.getId();
    
        assertThat(delegateRepository.count()).isZero();
        delegateMessageInput.accept(MessageBuilder.withPayload(delegateMessage).build());
        delegateEmployeeMessageInput.accept(MessageBuilder.withPayload(delegateEmployeeMessage).build());
        assertThat(delegateRepository.count()).isEqualTo(1);

        verifyTokenAndMessage(notificationMessage, tokens);
    }
}
