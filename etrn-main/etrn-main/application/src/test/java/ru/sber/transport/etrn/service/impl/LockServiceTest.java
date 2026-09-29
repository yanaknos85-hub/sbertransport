package ru.sber.transport.etrn.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.etrn.database.dao.EtrnAuditRepository;
import ru.sber.transport.etrn.database.dao.EtrnRepository;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.database.model.Etrn.LockInfo;
import ru.sber.transport.etrn.database.model.EtrnAudit;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.exceptions.BadRequestException;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.etrn.exceptions.LockConflictException;
import ru.sber.transport.etrn.service.EmployeeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса LockService")
class LockServiceTest {

    @Mock
    private EtrnRepository etrnRepository;

    @Mock
    private EtrnAuditRepository auditRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private LockServiceImpl lockService;

    private final UUID etrnId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    // Блокировки в будущем (относительно LocalDateTime.now() во время теста)
    private LocalDateTime futureLockUntil = LocalDateTime.now().plusMinutes(5);

    // Блокировки в прошлом (истекшие)
    private LocalDateTime pastLockUntil = LocalDateTime.now().minusHours(1);

    private void setupLockTtl(int ttlSeconds) {
        ReflectionTestUtils.setField(lockService, "lockTtlSeconds", ttlSeconds);
    }

    private Employee createEmployee(UUID id) {
        var employee = new Employee();
        employee.setId(id);
        return employee;
    }

    private Etrn createEtrn(UUID id) {
        return Etrn.builder()
                .id(id)
                .humanReadableId("ETRN-0001-00000001")
                .build();
    }

    private void setupAuthentication(Employee employee) {
        var auth = mock(Authentication.class);
        when(employeeService.getAuthenticatedEmployee(any())).thenReturn(employee);
    }

    // =========================================================================
    // lock
    // =========================================================================

    @Test
    @DisplayName("Установка блокировки — успешный сценарий")
    void lock_shouldSucceedWhenNoLock() {
        // Arrange
        setupLockTtl(300);
        setupAuthentication(createEmployee(userId));

        var etrn = createEtrn(etrnId);
        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));
        when(etrnRepository.save(any())).thenReturn(etrn);

        // Act
        var auth = mock(Authentication.class);
        lockService.lock(etrnId, auth);

        // Assert
        verify(etrnRepository).save(any());
        assertThat(etrn.getLockInfo()).isNotNull();
        assertThat(etrn.getLockInfo().userId()).isEqualTo(userId);
        assertThat(etrn.getLockInfo().lockUntil()).isAfter(LocalDateTime.now().minusSeconds(5));
        verify(auditRepository).save(any(EtrnAudit.class));
    }

    @Test
    @DisplayName("Установка блокировки — карточка не найдена")
    void lock_shouldThrowWhenEtrnNotFound() {
        // Arrange
        setupAuthentication(createEmployee(userId));
        when(etrnRepository.findById(etrnId)).thenReturn(Optional.empty());

        // Act & Assert
        var auth = mock(Authentication.class);
        assertThatExceptionOfType(EtrnNotFoundException.class)
                .isThrownBy(() -> lockService.lock(etrnId, auth))
                .withMessageContaining(etrnId.toString());
    }

    @Test
    @DisplayName("Установка блокировки — идемпотентность при повторном lock тем же пользователем")
    void lock_shouldBeIdempotentForSameUser() {
        // Arrange
        setupLockTtl(300);
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(userId, futureLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));

        // Act
        var auth = mock(Authentication.class);
        lockService.lock(etrnId, auth);

        // Assert
        verify(etrnRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Установка блокировки — конфликт с другим пользователем")
    void lock_shouldThrowConflictWhenLockedByOtherUser() {
        // Arrange
        setupLockTtl(300);
        var otherUserId = UUID.randomUUID();
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(otherUserId, futureLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));

        // Act & Assert
        var auth = mock(Authentication.class);
        assertThatExceptionOfType(LockConflictException.class)
                .isThrownBy(() -> lockService.lock(etrnId, auth));
    }

    @Test
    @DisplayName("Установка блокировки — истекшая блокировка перезаписывается")
    void lock_shouldOverwriteExpiredLock() {
        // Arrange
        setupLockTtl(300);
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(userId, pastLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));
        when(etrnRepository.save(any())).thenReturn(etrn);

        // Act
        var auth = mock(Authentication.class);
        lockService.lock(etrnId, auth);

        // Assert
        verify(etrnRepository).save(any());
        assertThat(etrn.getLockInfo().lockUntil()).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    // =========================================================================
    // unlock
    // =========================================================================

    @Test
    @DisplayName("Снятие блокировки — успешный сценарий")
    void unlock_shouldSucceedForOwner() {
        // Arrange
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(userId, futureLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));
        when(etrnRepository.save(any())).thenReturn(etrn);

        // Act
        var auth = mock(Authentication.class);
        lockService.unlock(etrnId, auth);

        // Assert
        assertThat(etrn.getLockInfo()).isNull();
        verify(etrnRepository).save(any());
        verify(auditRepository).save(any(EtrnAudit.class));
    }

    @Test
    @DisplayName("Снятие блокировки — блокировка не установлена")
    void unlock_shouldThrowWhenNoLock() {
        // Arrange
        setupAuthentication(createEmployee(userId));
        var etrn = createEtrn(etrnId);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));

        // Act & Assert
        var auth = mock(Authentication.class);
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> lockService.unlock(etrnId, auth))
                .withMessage("Блокировка не установлена");
    }

    @Test
    @DisplayName("Снятие блокировки — попытка разблокировки не владельцем")
    void unlock_shouldThrowWhenNotOwner() {
        // Arrange
        var otherUserId = UUID.randomUUID();
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(otherUserId, futureLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));

        // Act & Assert
        var auth = mock(Authentication.class);
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> lockService.unlock(etrnId, auth))
                .withMessageContaining("только пользователь");
    }

    @Test
    @DisplayName("Снятие блокировки — истекшая блокировка снимается принудительно")
    void unlock_shouldForceUnlockExpiredLock() {
        // Arrange
        setupAuthentication(createEmployee(userId));

        var lockInfo = new LockInfo(userId, pastLockUntil);
        var etrn = createEtrn(etrnId);
        etrn.setLockInfo(lockInfo);

        when(etrnRepository.findById(etrnId)).thenReturn(Optional.of(etrn));
        when(etrnRepository.save(any())).thenReturn(etrn);

        // Act
        var auth = mock(Authentication.class);
        lockService.unlock(etrnId, auth);

        // Assert
        assertThat(etrn.getLockInfo()).isNull();
        verify(etrnRepository).save(any());
    }

    // =========================================================================
    // scheduleAutoUnlock
    // =========================================================================

    @Test
    @DisplayName("Авто-разблокировка — снятие истёкших блокировок")
    void scheduleAutoUnlock_shouldRemoveExpiredLocks() {
        // Arrange
        var etrn1 = createEtrn(UUID.randomUUID());
        etrn1.setLockInfo(new LockInfo(userId, pastLockUntil));

        var etrn2 = createEtrn(UUID.randomUUID());
        etrn2.setLockInfo(new LockInfo(UUID.randomUUID(), pastLockUntil));

        var etrn3 = createEtrn(UUID.randomUUID());
        etrn3.setLockInfo(null);

        when(etrnRepository.findAllWithExpiredLockingTime()).thenReturn(List.of(etrn1, etrn2));
        when(etrnRepository.saveAll(any())).thenReturn(List.of(etrn1, etrn2));

        // Act
        lockService.scheduleAutoUnlock();

        // Assert
        assertThat(etrn1.getLockInfo()).isNull();
        assertThat(etrn2.getLockInfo()).isNull();
        verify(etrnRepository).findAllWithExpiredLockingTime();
        verify(etrnRepository).saveAll(any());
        verify(auditRepository, times(2)).save(any(EtrnAudit.class));
    }

    @Test
    @DisplayName("Авто-разблокировка — нет истёкших блокировок")
    void scheduleAutoUnlock_shouldDoNothingWhenNoExpiredLocks() {
        // Arrange
        when(etrnRepository.findAllWithExpiredLockingTime()).thenReturn(List.of());

        // Act
        lockService.scheduleAutoUnlock();

        // Assert
        verify(etrnRepository, never()).saveAll(any());
    }
}
