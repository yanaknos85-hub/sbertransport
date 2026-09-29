package ru.sber.transport.authentication.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.dto.Scope;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Проверка сброса пароля")
class ResetPasswordTest extends AbstractContextedTest {

    @Autowired
    private AccountCases accountCases;

    @Autowired
    private AccountProvider accountProvider;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean(name = "emailOutput")
    private OutputBridge emailOutput;

    @DisplayName("Проверка отправки сообщения диспетчеру о смене пароля")
    @Test
    void test_reset_password_dispatcher() throws IOException {
        var role = new RoleRecord();
        role.setCode("ROLE_DISPATCHER_CONTRACTOR");
        role.setName("ДИСПЕТЧЕР");
        role.setDescription("Диспетчер");
        role.setDataMaster(false);
        role.setDefaultFor(JSON.valueOf("[\"DISPATCHER\"]"));
        roleRepository.save(role);

        var dispatcher = AccountDto.builder()
                .id(UUID.randomUUID())
                .login("DispatcherD-D666")
                .email("dispatcher666@mail.ru")
                .hash(BCrypt.hashpw("password", BCrypt.gensalt()))
                .transferPassword(false)
                .roles(Map.of("ROLE_DISPATCHER_CONTRACTOR", true))
                .scope(Scope.DISPATCHER)
                .active(true)
                .authType(AuthType.BASIC)
                .build();

        var bytes = Files.readAllBytes(Paths.get("src/main/resources/text/email/resetPasswordForDispatcher.html"));
        var text = new String(bytes, StandardCharsets.UTF_8);
        accountCases.save(List.of(dispatcher));
        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailOutput).send(messageCaptor.capture());
        var message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(text);
    }

    @DisplayName("Проверка отправки сообщения водителю о смене пароля")
    @Test
    void test_reset_password_driver() throws IOException {
        var role = new RoleRecord();
        role.setCode("ROLE_DRIVER_CONTRACTOR");
        role.setName("ВОДИТЕЛЬ");
        role.setDescription("Водитель");
        role.setDataMaster(false);
        role.setDefaultFor(JSON.valueOf("[\"DRIVER\"]"));
        roleRepository.save(role);

        var driver = AccountDto.builder()
                .id(UUID.randomUUID())
                .login("DriverD-D666")
                .email("driver666@mail.ru")
                .hash(BCrypt.hashpw("password", BCrypt.gensalt()))
                .transferPassword(false)
                .roles(Map.of("ROLE_DRIVER_CONTRACTOR", true))
                .scope(Scope.DRIVER)
                .active(true)
                .authType(AuthType.BASIC)
                .build();

        var bytes = Files.readAllBytes(Paths.get("src/main/resources/text/email/resetPasswordForDriver.html"));
        var text = new String(bytes, StandardCharsets.UTF_8);
        accountCases.save(List.of(driver));
        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailOutput).send(messageCaptor.capture());
        var message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(text);
    }

    @DisplayName("Проверка отправки сообщения диспетчеру о смене пароля (второй способ)")
    @Test
    void test_reset_password_dispatcher_2() throws IOException, AccountNotFoundException {
        var role = new RoleRecord();
        role.setCode("ROLE_DISPATCHER_CONTRACTOR");
        role.setName("ДИСПЕТЧЕР");
        role.setDescription("Диспетчер");
        role.setDataMaster(false);
        role.setDefaultFor(JSON.valueOf("[\"DISPATCHER\"]"));
        roleRepository.save(role);

        var dispatcher = AccountDto.builder()
                .id(UUID.randomUUID())
                .login("DispatcherD-D666")
                .email("dispatcher666@mail.ru")
                .hash(BCrypt.hashpw("password", BCrypt.gensalt()))
                .transferPassword(false)
                .roles(Map.of("ROLE_DISPATCHER_CONTRACTOR", true))
                .scope(Scope.DISPATCHER)
                .active(true)
                .authType(AuthType.BASIC)
                .build();

        accountProvider.save(dispatcher);

        var bytes = Files.readAllBytes(Paths.get("src/main/resources/text/email/resetPasswordForDispatcher.html"));
        var text = new String(bytes, StandardCharsets.UTF_8);
        accountCases.resetPassword(dispatcher.getId());
        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailOutput).send(messageCaptor.capture());
        var message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(text);
    }

    @DisplayName("Проверка отправки сообщения водителю о смене пароля (второй способ)")
    @Test
    void test_reset_password_driver_2() throws IOException, AccountNotFoundException {
        var role = new RoleRecord();
        role.setCode("ROLE_DRIVER_CONTRACTOR");
        role.setName("ВОДИТЕЛЬ");
        role.setDescription("Водитель");
        role.setDataMaster(false);
        role.setDefaultFor(JSON.valueOf("[\"DRIVER\"]"));
        roleRepository.save(role);

        var driver = AccountDto.builder()
                .id(UUID.randomUUID())
                .login("DriverD-D666")
                .email("driver666@mail.ru")
                .hash(BCrypt.hashpw("password", BCrypt.gensalt()))
                .transferPassword(false)
                .roles(Map.of("ROLE_DRIVER_CONTRACTOR", true))
                .scope(Scope.DRIVER)
                .active(true)
                .authType(AuthType.BASIC)
                .build();

        accountProvider.save(driver);

        var bytes = Files.readAllBytes(Paths.get("src/main/resources/text/email/resetPasswordForDriver.html"));
        var text = new String(bytes, StandardCharsets.UTF_8);
        accountCases.resetPassword(driver.getId());
        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailOutput).send(messageCaptor.capture());
        var message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(text);
    }
}
