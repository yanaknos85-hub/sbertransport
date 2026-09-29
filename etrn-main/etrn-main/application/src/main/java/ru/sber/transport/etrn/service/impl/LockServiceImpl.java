package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.dao.EtrnAuditRepository;
import ru.sber.transport.etrn.database.dao.EtrnRepository;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.database.model.EtrnAudit;
import ru.sber.transport.etrn.exceptions.BadRequestException;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.etrn.exceptions.LockConflictException;
import ru.sber.transport.etrn.service.EmployeeService;
import ru.sber.transport.etrn.service.LockService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация {@link LockService} — управление блокировками (lease lock) для конкурентного доступа к ЭТрН.
 *
 * <p>Блокировка — краткоживущий механизм, ограничивает одновременный доступ к карточке.
 * Не меняет бизнес-статус карточки. Имеет TTL (настраивается через {@value lockTtlSecondsProperty}).
 *
 * <p>Примеры:
 * <ul>
 *   <li>Пользователь нажал «В работу» → блокировка на 5 минут</li>
 *   <li>Через 3 минуты тот же пользователь снова нажал → идемпотентный возврат</li>
 *   <li>Другой пользователь нажал → LockConflictException (409)</li>
 *   <li>Планировщик раз в N минут снимает истёкшие блокировки</li>
 * </ul>
 *
 * <p>Выделен из {@link EtrnServiceImpl} по SOLID SRP: управление блокировками —
 * самостоятельная бизнес-доменная концепция.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
class LockServiceImpl implements LockService {

    private static final String AUDIT_ACTION_LOCK = "Блокировка ЭТрН";
    private static final String AUDIT_ACTION_UNLOCK_MANUAL = "Снятие блокировки ЭТрН (ручное)";
    private static final String AUDIT_ACTION_UNLOCK_AUTO = "Снятие блокировки ЭТрН (авто)";

    private final EtrnRepository etrnRepository;
    private final EtrnAuditRepository auditRepository;
    private final EmployeeService employeeService;

    @Value("${lock.ttl-seconds}")
    private int lockTtlSeconds;

    @Override
    public void lock(UUID id, Authentication authentication) {
        log.debug("LockService.lock: запрос блокировки, etrnId={}", id);

        var activeUser = employeeService.getAuthenticatedEmployee(authentication);
        log.debug("LockService.lock: аутентифицированный пользователь, userId={}", activeUser.getId());

        Etrn entity = etrnRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("LockService.lock: ЭТрН не найдена, etrnId={}", id);
                    return new EtrnNotFoundException(id.toString());
                });

        // Проверка: блокировка уже активна?
        if (isLockActive(entity)) {
            UUID currentLockUserId = entity.getLockInfo().userId();
            boolean sameUser = currentLockUserId != null && currentLockUserId.equals(activeUser.getId());

            if (sameUser) {
                // Идемпотентность: тот же пользователь — возвращаем текущее состояние
                log.info("LockService.lock: повторный lock для того же пользователя, userId={}, etrnId={}",
                        activeUser.getId(), id);
                return;
            }

            // Конфликт: другой пользователь → 409
            log.warn("LockService.lock: конфликт блокировки, etrnId={}, lockedBy={}, requestedBy={}",
                    id, currentLockUserId, activeUser.getId());
            throw new LockConflictException(
                    entity.getLockInfo().userId(),
                    entity.getLockInfo().lockUntil()
            );
        }

        // Блокировки нет или она истекла — создаём новую
        LocalDateTime lockUntil = LocalDateTime.now().plusSeconds(lockTtlSeconds);
        Etrn.LockInfo lockInfo = new Etrn.LockInfo(activeUser.getId(), lockUntil);

        log.debug("LockService.lock: создание новой блокировки, userId={}, lockUntil={}",
                activeUser.getId(), lockUntil);

        entity.setLockInfo(lockInfo);

        etrnRepository.save(entity);
        log.debug("LockService.lock: блокировка установлена, etrnId={}", entity.getHumanReadableId());

        saveAudit(entity.getId(), entity.getHumanReadableId(), AUDIT_ACTION_LOCK,
                activeUser.getId(), activeUser.getId(), lockUntil);
    }

    @Override
    public void unlock(UUID id, Authentication authentication) {
        log.debug("LockService.unlock: запрос разблокировки, etrnId={}", id);

        var activeUser = employeeService.getAuthenticatedEmployee(authentication);
        log.debug("LockService.unlock: аутентифицированный пользователь, userId={}", activeUser.getId());

        Etrn entity = etrnRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("LockService.unlock: ЭТрН не найдена, etrnId={}", id);
                    return new EtrnNotFoundException(id.toString());
                });

        // Блокировки нет
        if (entity.getLockInfo() == null) {
            log.warn("LockService.unlock: блокировка не установлена, etrnId={}, userId={}",
                    id, activeUser.getId());
            throw new BadRequestException("Блокировка не установлена");
        }

        // Блокировка истекла по TTL — снимаем принудительно
        if (entity.getLockInfo().lockUntil() != null
                && entity.getLockInfo().lockUntil().isBefore(LocalDateTime.now())) {
            log.warn("LockService.unlock: попытка unlock с истёкшей блокировкой, etrnId={}, userId={}",
                    id, activeUser.getId());

            entity.setLockInfo(null);

            etrnRepository.save(entity);

            saveAudit(entity.getId(), entity.getHumanReadableId(), AUDIT_ACTION_UNLOCK_MANUAL,
                    activeUser.getId(), null, null);

            log.debug("LockService.unlock: истёкшая блокировка снята, etrnId={}", entity.getHumanReadableId());
            return;
        }

        // Проверка владения: только владелец может снять
        UUID lockedBy = entity.getLockInfo().userId();
        if (!lockedBy.equals(activeUser.getId())) {
            log.warn("LockService.unlock: попытка разблокировки не владельцем, etrnId={}, lockedBy={}, requestedBy={}",
                    id, lockedBy, activeUser.getId());
            throw new BadRequestException("Блокировку может снять только пользователь, который её установил");
        }

        // Разблокировка
        log.debug("LockService.unlock: снятие блокировки владельцем, etrnId={}, userId={}",
                entity.getHumanReadableId(), activeUser.getId());

        entity.setLockInfo(null);

        etrnRepository.save(entity);
        log.debug("LockService.unlock: блокировка снята, etrnId={}", entity.getHumanReadableId());

        saveAudit(entity.getId(), entity.getHumanReadableId(), AUDIT_ACTION_UNLOCK_MANUAL,
                activeUser.getId(), null, null);
    }

    @Override
    public void scheduleAutoUnlock() {
        log.debug("LockService.scheduleAutoUnlock: запуск авто-разблокировки");

        var expired = etrnRepository.findAllWithExpiredLockingTime();
        log.debug("LockService.scheduleAutoUnlock: найдено истёкших блокировок={}", expired.size());

        for (Etrn entity : expired) {
            log.debug("LockService.scheduleAutoUnlock: авто-разблокировка, etrnId={}, humanReadableId={}",
                    entity.getId(), entity.getHumanReadableId());

            entity.setLockInfo(null);

            saveAudit(entity.getId(), entity.getHumanReadableId(), AUDIT_ACTION_UNLOCK_AUTO,
                    null, null, null);
        }

        if (!expired.isEmpty()) {
            etrnRepository.saveAll(expired);
            log.info("LockService.scheduleAutoUnlock: автоматически снято блокировок={}", expired.size());
        } else {
            log.debug("LockService.scheduleAutoUnlock: истёкших блокировок не найдено");
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Проверяет, что блокировка на карточке активна (не истекла).
     */
    private boolean isLockActive(Etrn entity) {
        return entity.getLockInfo() != null
                && entity.getLockInfo().lockUntil() != null
                && entity.getLockInfo().lockUntil().isAfter(LocalDateTime.now());
    }

    /**
     * Сохраняет запись в аудит по блокировке/разблокировке.
     */
    private void saveAudit(UUID etrnId, String humanReadableId, String action,
                           UUID userId, UUID lockedBy, LocalDateTime lockUntil) {
        EtrnAudit audit = EtrnAudit.builder()
                .etrnId(etrnId)
                .action(action)
                .details(buildAuditDetails(humanReadableId, userId, lockedBy, lockUntil))
                .createdBy(userId)
                .build();
        auditRepository.save(audit);
    }

    /**
     * Формирует строку деталей для аудита блокировки.
     */
    private String buildAuditDetails(String humanReadableId, UUID userId,
                                      UUID lockedBy, LocalDateTime lockUntil) {
        StringBuilder sb = new StringBuilder("ЭТрН ").append(humanReadableId);
        if (userId != null) {
            sb.append(", userId=").append(userId);
        }
        if (lockedBy != null) {
            sb.append(", lockedBy=").append(lockedBy);
        }
        if (lockUntil != null) {
            sb.append(", lockUntil=").append(lockUntil);
        }
        return sb.toString();
    }
}
