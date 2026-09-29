# Spec: etrn-lock

## Purpose

Определение требований к кратковременной блокировке (lock/lease) карточек ЭТрН.

## Requirements

### Requirement: Краткоживущий lease
Система SHALL поддерживать блокировку (lock) ЭТрН при открытии окна подписания.

- **Поле:** `lock_info` — JSONB в `etrn`
  - `userId` — кто взял в работу (`UUID`, из OAuth2-токена)
  - `lockUntil` — когда перестанет действовать lease (`TIMESTAMP`, `now + TTL`)
- **Длительность:** `lock.ttl-seconds` (по умолчанию 300 сек = 5 минут, конфигурируется через переменную `TIME_TO_LOCK_SECONDS`)
- **Не более одной активной блокировки на карточку**
- **Аутентификация:** `userId` извлекается из OAuth2 JWT-токена через `ContextHelper.getUserId(Authentication)` — не передается в теле запроса

#### Scenario: Установка блокировки
- **WHEN** PUT /api/etrn-cargo/{id}/lock с аутентифицированным пользователем
- **THEN** 200 + `lock_info = { "userId": "...", "lockUntil": "now + TTL" }`
- **AND** запись в `etrn_audit`: `"Установлена блокировка (lock) пользователем <userId>"`

#### Scenario: Повторная блокировка (тот же пользователь)
- **WHEN** PUT /api/etrn-cargo/{id}/lock при активной блокировке, принадлежащей тому же userId
- **THEN** 200 + `lock_info` без изменений (идемпотентно)

#### Scenario: Повторная блокировка (другой пользователь)
- **WHEN** PUT /api/etrn-cargo/{id}/lock при активной блокировке, принадлежащей другому userId
- **THEN** 409 + `LockConflictResponse { message, lockedBy, lockUntil }`

#### Scenario: Истечение блокировки
- **WHEN** `lock_info.lockUntil < now`
- **THEN** При попытке lock — блокировка устанавливается заново (как если бы её не было)

---

### Requirement: Снятие блокировки
Система SHALL снимать блокировку при закрытии окна подписания.

- **Метод:** DELETE /api/etrn-cargo/{id}/lock
- **Аутентификация:** `userId` извлекается из OAuth2 JWT-токена — не передается в теле запроса
- **Валидация:** только пользователь, установивший блокировку, может её снять (если lock не истёк)

#### Scenario: Успешное снятие
- **WHEN** DELETE /api/etrn-cargo/{id}/lock с `userId`, совпадающим с `lock_info.userId`
- **THEN** 200 + `lock_info = null`
- **AND** запись в `etrn_audit`: `"Снята блокировка (unlock) пользователем <userId>"`

#### Scenario: Блокировку пытается снять не тот пользователь
- **WHEN** DELETE /api/etrn-cargo/{id}/lock с `userId`, не совпадающим с `lock_info.userId`
- **THEN** 400 + `"Блокировку может снять только пользователь, который её установил"`

#### Scenario: Блокировка не установлена
- **WHEN** DELETE /api/etrn-cargo/{id}/lock при `lock_info = null`
- **THEN** 400 + `"Блокировка не установлена"`

#### Scenario: Истёкшая блокировка
- **WHEN** DELETE /api/etrn-cargo/{id}/lock при `lockUntil < now`
- **THEN** 200 + `lock_info = null`
- **AND** запись в `etrn_audit`: `"Снята истёкшая блокировка (unlock) пользователем <userId>"`

---

### Requirement: Автоматическое снятие (scheduler)
Система SHALL снимать истёкшие блокировки методом `scheduleAutoUnlock()`.

- **Cron:** `0 0/5 * * * *` (каждые 5 минут, через `@Scheduled`)
- **Query:** `EtrnRepository.findAllWithExpiredLockingTime()` — native query на `lock_info ->> 'lockUntil' < now()`
- **Action:** сброс `lock_info = null` + `updatedAt` для каждой записи
- **AND** запись в `etrn_audit`: `"Автоматически снята истёкшая блокировка с ЭТрН <etrnId>"`

#### Scenario: Scheduler находит истёкшие блокировки
- **WHEN** метод `scheduleAutoUnlock()` вызван шедулером
- **THEN** `EtrnRepository.findAllWithExpiredLockingTime()` → `setLockInfo(null)` + saveAll + audit
