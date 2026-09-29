package ru.sber.transport.telemechanic.service.impl;

import ch.qos.logback.classic.Level;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.dao.PositionRepository;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.service.grpc.Positions;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с должностями")
class PositionServiceImplTest {
    
    @InjectMocks
    private PositionServiceImpl service;
    @Mock
    private PositionRepository repository;
    @Mock
    private Positions positions;
    @Captor
    private ArgumentCaptor<Position> captor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(PositionServiceImpl.class);
    
    @Test
    void get() {
        var entity = Instancio.create(Position.class);
        var notExistId = UUID.randomUUID();
        doReturn(Optional.of(entity)).when(repository).findById(entity.getId());
        doReturn(Optional.empty()).when(repository).findById(notExistId);
        assertThat(service.get(entity.getId())).isEqualTo(Optional.of(entity));
        assertThat(service.get(notExistId)).isNotPresent();
    }
    
    @Test
    void delete() {
        var entity = Instancio.create(Position.class);
        doReturn(entity).when(repository).save(captor.capture());
        service.delete(entity);
        var savedEntity = captor.getValue();
        assertThat(savedEntity)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(entity);
        assertThat(savedEntity.isActive()).isFalse();
        verify(repository).save(any(Position.class));
    }
    
    @Test
    void save() {
        var entity = Instancio.create(Position.class);
        doReturn(entity).when(repository).save(captor.capture());
        var actual = service.save(entity);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(entity);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(repository).save(any(Position.class));
    }
    
    @Test
    void saveGrpcEntity() {
        var message = UUID.randomUUID().toString();
        var id = UUID.randomUUID();
        var wrongId = UUID.randomUUID();
        var entity = Instancio.create(Position.class);
        doReturn(entity).when(positions).one(id);
        doReturn(null).when(positions).one(wrongId);
        doReturn(entity).when(repository).save(captor.capture());
        service.saveGrpcEntity(message, id);
        var actual = captor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(entity);
        assertThatExceptionOfType(AwaitingSynchronizationException.class)
                .isThrownBy(() -> service.saveGrpcEntity(message, wrongId))
                .withMessage("Awaiting an position synchronization");
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo(PositionServiceImpl.class.getName());
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo(message);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    }
}