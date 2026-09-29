package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.AttorneyRepository;
import ru.sber.transport.telemechanic.database.dao.DispatcherRepository;
import ru.sber.transport.telemechanic.exception.dispatcher.DispatcherException;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static ru.sber.transport.telemechanic.exception.dispatcher.DispatcherException.NOT_FOUND_BY_ATTORNEY_NUMBER_MSG;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class DispatcherListenerTest extends KafkaTest {
    
    @Autowired
    private DispatcherRepository dispatcherRepository;
    @Autowired
    private AttorneyRepository attorneyRepository;
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql" , "/scripts/dispatcher_integration_test.sql"})
    @SneakyThrows
    void listen_ShouldSaveDispatcher_WhenEwbCreationPossibilityIsTrue() {
        var dispatcherId = UUID.randomUUID();
        var employeeId = UUID.fromString("1cd45c19-fd39-413c-99a0-30f35bd642a8");
        
        var message = Instancio.of(DispatcherMessage.class)
                                             .set(field(DispatcherMessage::id), dispatcherId)
                                             .set(field(DispatcherMessage::oauthId), employeeId)
                                             .set(field(DispatcherMessage::attorneyNumber), UUID.randomUUID())
                                             .set(field(DispatcherMessage::autoparkId), UUID.randomUUID())
                                             .set(field(DispatcherMessage::contractorId), UUID.randomUUID())
                                             .set(field(DispatcherMessage::active), true)
                                             .set(field(DispatcherMessage::issueDate), LocalDate.now().minusDays(1))
                                             .set(field(DispatcherMessage::expiryDate), LocalDate.now().plusDays(10))
                                             .set(field(DispatcherMessage::ewbCreationPossibility), true)
                                             .set(field(DispatcherMessage::creationSystem), "test-creation-system")
                                             .create();
        
        produceMessage("service.dispatcher", message);
        
        var actual = dispatcherRepository.findByEmployeeIdAndActiveIsTrue(employeeId).orElseThrow();
        var attorney = attorneyRepository.findById(actual.getAttorney().getId()).orElseThrow();
        assertThat(attorney.getNumber()).isEqualTo(message.attorneyNumber());
        assertThat(attorney.getIssueDate()).isEqualTo(message.issueDate());
        assertThat(attorney.getExpiryDate()).isEqualTo(message.expiryDate());
        assertThat(attorney.getCreationSystem()).isEqualTo(message.creationSystem());
        assertThat(actual.isActive()).isEqualTo(message.active());
        assertThat(actual.getAutoparkId()).isEqualTo(message.autoparkId());
        assertThat(actual.getContractorId()).isEqualTo(message.contractorId());
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql" })
    @SneakyThrows
    void listen_ShouldNotSaveDispatcher_WhenEwbCreationPossibilityIsFalse() {
        var dispatcherId = UUID.randomUUID();
        var employeeId = UUID.fromString("2cd45c19-fd39-413c-99a0-30f35bd642a8");
        
        var message = Instancio.of(DispatcherMessage.class)
                                             .set(field(DispatcherMessage::id), dispatcherId)
                                             .set(field(DispatcherMessage::oauthId), employeeId)
                                             .set(field(DispatcherMessage::ewbCreationPossibility), false)
                                             .create();
        
        produceMessage("service.dispatcher", message);
        
        var actual = dispatcherRepository.findByEmployeeIdAndActiveIsTrue(employeeId).orElse(null);
        assertThat(actual).isNull();
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/dispatcher_integration_test.sql" })
    @SneakyThrows
    void listen_ShouldUpdateDispatcher_WhenAlreadyExists() {
        var dispatcherId = "e8c386ae-0fba-4a26-a425-0e9073ae71f1";
        var employeeId = "3cd45c19-fd39-413c-99a0-30f35bd642a8";
        
        var message = Instancio.of(DispatcherMessage.class)
                                             .set(field(DispatcherMessage::id), UUID.fromString(dispatcherId))
                                             .set(field(DispatcherMessage::oauthId), UUID.fromString(employeeId))
                                             .set(field(DispatcherMessage::attorneyNumber), UUID.randomUUID())
                                             .set(field(DispatcherMessage::autoparkId), UUID.randomUUID())
                                             .set(field(DispatcherMessage::contractorId), UUID.randomUUID())
                                             .set(field(DispatcherMessage::active), true)
                                             .set(field(DispatcherMessage::issueDate), LocalDate.now().minusDays(1))
                                             .set(field(DispatcherMessage::expiryDate), LocalDate.now().plusDays(10))
                                             .set(field(DispatcherMessage::ewbCreationPossibility), true)
                                             .set(field(DispatcherMessage::creationSystem), "WEB")
                                             .create();
        
        produceMessage("service.dispatcher", message);
        
        var actual = dispatcherRepository.findByEmployeeIdAndActiveIsTrue(UUID.fromString(employeeId)).orElseThrow();
        assertThat(actual).isNotNull();
        var updatedAttorney = attorneyRepository.findById(actual.getAttorney().getId()).orElseThrow();
        assertThat(updatedAttorney.getNumber()).isEqualTo(message.attorneyNumber());
        assertThat(updatedAttorney.getIssueDate()).isEqualTo(message.issueDate());
        assertThat(updatedAttorney.getExpiryDate()).isEqualTo(message.expiryDate());
        assertThat(updatedAttorney.getCreationSystem()).isEqualTo(message.creationSystem());
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/dispatcher_integration_test.sql" })
    @SneakyThrows
    void listen_ShouldDeleteDispatcher() {
        var dispatcherId = "e8c386ae-0fba-4a26-a425-0e9073ae71f1";
        var attorneyNumber = "76a3d37e-636c-4a66-8782-6199fe002f26";
        
        var message = Instancio.of(DispatcherMessage.class)
                               .set(field(DispatcherMessage::id), UUID.fromString(dispatcherId))
                               .set(field(DispatcherMessage::attorneyNumber), UUID.fromString(attorneyNumber))
                               .set(field(DispatcherMessage::active), false)
                               .set(field(DispatcherMessage::ewbCreationPossibility), true)
                               .create();
        
        produceMessage("service.dispatcher", message);
        
        var list = dispatcherRepository.findAll();
        assertThat(list).isNotNull();
        var actual = list.getFirst();
        assertThat(actual.isActive()).isFalse();
    }
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/dispatcher_integration_test.sql" })
    @SneakyThrows
    void listen_ShouldDeleteDispatcher_NotFoundByAttorneyNumber() {
        var dispatcherId = "e8c386ae-0fba-4a26-a425-0e9073ae71f1";
        var randomAttorneyNumber = UUID.randomUUID();
        var message = Instancio.of(DispatcherMessage.class)
                               .set(field(DispatcherMessage::id), UUID.fromString(dispatcherId))
                               .set(field(DispatcherMessage::attorneyNumber), randomAttorneyNumber)
                               .set(field(DispatcherMessage::active), false)
                               .set(field(DispatcherMessage::ewbCreationPossibility), true)
                               .create();
        
        try {
            produceMessage("service.dispatcher", message);
        } catch (DispatcherException e) {
            assertThat(e.getMessage()).isEqualTo(String.format(NOT_FOUND_BY_ATTORNEY_NUMBER_MSG, randomAttorneyNumber));
        }
        
        var list = dispatcherRepository.findAll();
        assertThat(list).isNotNull();
        var actual = list.getFirst();
        assertThat(actual.isActive()).isTrue();
    }
}