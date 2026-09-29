package ru.sber.transport.etrn.service;

import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Сервис управления блокировками (lease lock) для конкурентного доступа к ЭТрН.
 * Реализует паттерн "оптимистичная блокировка с таймаутом".
 *
 * <p>Выделен в отдельный сервис по SOLID SRP: управление блокировками —
 * самостоятельная бизнес-доменная концепция, не относящаяся напрямую к жизненному циклу карточки.
 */
public interface LockService {

    /**
     * Устанавливает блокировку на карточку для указанного пользователя.
     * Блокировка временно ограничивает одновременный доступ — другой пользователь
     * не сможет заблокировать ту же карточку до истечения TTL.
     *
     * <p>Идемпотентна: повторный вызов тем же пользователем не выбрасывает исключение.
     *
     * @param id UUID карточки ЭТрН
     * @param authentication текущий аутентифицированный пользователь
     * @throws ru.sber.transport.etrn.exceptions.EtrnNotFoundException если карточка не найдена
     * @throws ru.sber.transport.etrn.exceptions.LockConflictException если карточка уже заблокирована другим пользователем
     */
    void lock(UUID id, Authentication authentication);

    /**
     * Снимает блокировку с карточки.
     *
     * <p>Тот же пользователь, который установил блокировку, может снять её досрочно.
     * Если блокировка истекла по TTL — unlock принудительно снимает её.
     *
     * @param id UUID карточки ЭТрН
     * @param authentication текущий аутентифицированный пользователь
     * @throws ru.sber.transport.etrn.exceptions.EtrnNotFoundException если карточка не найдена
     * @throws ru.sber.transport.etrn.exceptions.BadRequestException если блокировка не установлена или пользователь не владеет блокировкой
     */
    void unlock(UUID id, Authentication authentication);

    /**
     * Автоматически снимает все истёкшие по TTL блокировки.
     *
     * <p>Вызывается планировщиком (cron job) для поддержания блокировок в актуальном состоянии.
     * Используется ShedLock для распределённой синхронизации в кластере.
     */
    void scheduleAutoUnlock();
}
