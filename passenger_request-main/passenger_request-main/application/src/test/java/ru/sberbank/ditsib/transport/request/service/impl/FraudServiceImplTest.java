package ru.sberbank.ditsib.transport.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.FraudRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TypedRequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudServiceImplTest {
    @Mock
    private FraudRepository fraudRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestSender<Request> requestSender;

    @Mock
    private TypedRequestRepository<Request> typedRequestRepository;

    private FraudServiceImpl fraudService;

    private final UUID requestId = UUID.randomUUID();
    private Request request;
    private FraudData fraudData;

    @BeforeEach
    void setUp() {
        var senders = Map.of(
                TransportTypeEnum.PERSONAL, requestSender
        );
        var repositories = Map.of(
                TransportTypeEnum.PERSONAL, typedRequestRepository
        );

        fraudService = new FraudServiceImpl(fraudRepository, requestRepository, repositories, senders);

        request = mock(Request.class);
        when(request.getId()).thenReturn(requestId);

        fraudData = new FraudData();
        fraudData.setRequest(request);
        fraudData.setType(FraudType.RADIUS);
        fraudData.setComment("Test comment");
    }

    @Test
    @DisplayName("Должен сохранить фрод, если он ещё не существует")
    void shouldSaveFraud_WhenNotExists() {
        when(fraudRepository.existsByRequestIdAndType(requestId, FraudType.RADIUS)).thenReturn(false);
        when(fraudRepository.saveAndFlush(fraudData)).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestRepository.findTransportType(requestId)).thenReturn(Optional.of(TransportTypeEnum.PERSONAL));
        when(typedRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        fraudService.saveAndSend(fraudData);

        verify(fraudRepository).saveAndFlush(fraudData);
        verify(requestSender).send(request);
    }

    @Test
    @DisplayName("Не должен сохранять фрод, если он уже существует")
    void shouldNotSaveFraud_WhenAlreadyExists() {
        when(fraudRepository.existsByRequestIdAndType(requestId, FraudType.RADIUS)).thenReturn(true);

        fraudService.saveAndSend(fraudData);

        verify(fraudRepository, never()).saveAndFlush(any());
        verify(requestSender, never()).send(any());
    }

    @Test
    @DisplayName("Должен вернуть сохранённый фрод, если он ещё не существует")
    void shouldReturnSavedFraud_WhenNotExists() {
        when(fraudRepository.existsByRequestIdAndType(requestId, FraudType.RADIUS)).thenReturn(false);
        when(fraudRepository.saveAndFlush(fraudData)).thenAnswer(invocation -> invocation.getArgument(0));

        var result = fraudService.save(fraudData);

        assertThat(result).isPresent();
        assertThat(result.orElse(null).getType()).isEqualTo(FraudType.RADIUS);
    }

    @Test
    @DisplayName("Должен вернуть пустой результат, если фрод уже существует")
    void shouldReturnEmpty_WhenAlreadyExists() {
        when(fraudRepository.existsByRequestIdAndType(requestId, FraudType.RADIUS)).thenReturn(true);

        var result = fraudService.save(fraudData);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("При вызове size() не должно быть NPE")
    void shouldNotThrowNpeWhenFraudDataIsNull() {

        when(fraudRepository.existsByRequestIdAndType(any(UUID.class), any(FraudType.class)))
                .thenReturn(false);
        when(fraudRepository.saveAndFlush(any(FraudData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(requestRepository.findTransportType(requestId)).thenReturn(Optional.of(TransportTypeEnum.PERSONAL));
        when(typedRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        fraudService.saveAndSend(fraudData);

        verify(requestSender).send(request);
    }
}