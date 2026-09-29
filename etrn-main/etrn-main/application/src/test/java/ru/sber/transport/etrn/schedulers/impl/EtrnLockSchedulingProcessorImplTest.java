package ru.sber.transport.etrn.schedulers.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.service.LockService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtrnLockSchedulingProcessorImplTest {

    @Mock
    private LockService lockService;

    @InjectMocks
    private EtrnLockSchedulingProcessorImpl processor;

    @Test
    @DisplayName("releaseEtrnLocks — вызывает lockService.scheduleAutoUnlock")
    void releaseEtrnLocks_callsScheduleAutoUnlock() {
        processor.releaseEtrnLocks();
        verify(lockService, times(1)).scheduleAutoUnlock();
    }

    @Test
    @DisplayName("releaseEtrnLocks — выполнение завершается без исключений")
    void releaseEtrnLocks_completesWithoutException() {
        assertThatCode(processor::releaseEtrnLocks).doesNotThrowAnyException();
    }
}
