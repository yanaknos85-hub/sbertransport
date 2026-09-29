## Context

Sprint 1 (Foundation-волна) сервиса `etrn-service` — микросервис для управления жизненным циклом ЭТрН в экосистеме АС СберТранспорт.

**Исходные артефакты:**
- `knowledge/data-model.md` — core-модель (2 сущности + JSONB)
- `knowledge/architecture.md` — архитектура (слои, порты, API)
- `knowledge/assumptions.md` — 24 предположения (A-01..A-24)

**Решение:** Sprint 1 = **2 сущности** (`etrn` + `etrn_audit`) + **JSONB-проекция** будущих full-сущностей + **репликация справочников** через Kafka Consumer (external messages). Граница: когда JSONB становится узким местом → распилить в нормализованные таблицы.

**Переименование:** Убрано `Card` из всех имён — `EtrnCard*` → `Etrn*`. Таблица `etrn_cargo.etrn` (не `etrn_card`), аудит `etrn_cargo.etrn_audit` (не `etrn_card_audit`).

---

## Goals / Non-Goals

### Goals

- Реализовать **etrn-core** как работающий сервис с:
  - `etrn` + `etrn_audit` (PostgreSQL, схема `etrn_cargo`)
  - `title_chain`, `verifications`, `lock_info` как JSONB
  - 4 enum'а статусов: `EtrnCardStatus` (6), `EtrnTitleStatus` (9), `SigningOperationStatus` (8), `LockStatus` (5)
  - 4 статуса при старте: `IDENTIFIED`, `WAIT_CONDITIONS`, `READY_FOR_BANK_ACTION`, `PROCESS_COMPLETED`
  - REST API: POST (создание), POST (list), GET (детально), POST (force-transition), PUT/DELETE (lock/unlock)
  - Аудит: русский язык, `etrn_audit`
  - **Репликация справочников:** `employee`, `organization`, `department` (Kafka Consumer из external библиотеки)
  - Error-handling: `EtrnNotFoundException`, `BadRequestException`, `LockConflictException`, `InvalidTransitionException`, `UserNotFoundException`

### Non-Goals

- **Не** КОРУС-интеграция — вынесена в отдельную задачу (FeignClient, `KorusClient`, `FETCH_ETRN_STATE`)
- **Не** полная статусная машина (P2-P8 — 10+ статусов) — только `IDENTIFIED` + force-transition allowlist
- **Не** reconciliation (`OUTCOME_UNKNOWN` — заглушка, решается force-transition)
- **Не** партиционирование (audit — пока без партиций)
- **Не** нормализация `title_chain`/`verifications` (JSONB)
- **Не** PDF/XML
- **Не** cache (Caffeine не используется)
- **Не** ручка PATCH /status — удалена
- **Не** position репликация — top-level bean не реализован
- **Не** unit- и integration-тесты — вынесены в отдельный change

---

## Decisions

### Decision 1: Переименование — `EtrnCard*` → `Etrn*`

| Было | Стало |
|------|-------|
| `etrn_card` | `etrn_cargo.etrn` |
| `etrn_card_audit` | `etrn_cargo.etrn_audit` |
| `EtrnCardController` | `EtrnController` |
| `EtrnCardService` | `EtrnService` |
| `EtrnCardRepository` | `EtrnRepository` |
| `EtrnCardListDto` / `EtrnCardDetailDto` | `EtrnJournalDto` / `EtrnDetailDto` |

**Решение:** Глобально, во всех слоях. Причина — `Card` избыточен (одна ЭТрН = один `etrn`).

### Decision 2: `humanReadableId` — единственный бизнес-ключ

Три системы используют один идентификатор:

```
ЦС → etrn-service → КОРУС
    humanReadableId →  humanReadableId → humanReadableId
```

- `UNIQUE` на БД
- Поиск не по `id`, а по `humanReadableId`
- Дедупликация при повторном `POST /cs/title3`

### Decision 3: Lock — JSONB `lock_info` (на `etrn`) + OAuth2-аутентификация

```json
{
  "userId": "uuid-из-JWT",
  "lockUntil": "2026-07-22T12:30:00"
}
```

- **Не** отдельная сущность — over-engineering
- **Краткоживущий** (5 мин, конфигурируемо — A-05)
- **Аутентификация:** `userId` извлекается из OAuth2 JWT-токена через `ContextHelper.getUserId(Authentication)` — **не передается в теле запроса**
- PUT/DELETE `/lock` не принимает тело — пользователь определяется по токену

### Decision 4: Статусы — 4 inline-enum'а

```
EtrnCardStatus (6): IDENTIFIED, WAIT_KORUS_DATA, WAIT_CONDITIONS, READY_FOR_BANK_ACTION, WAIT_KORUS_CONFIRMATION, PROCESS_COMPLETED
EtrnTitleStatus (9): EXPECTED, AVAILABLE, WAIT_CONDITIONS, READY_TO_SIGN, SIGNING, SIGNED_LOCALLY, SENT_TO_OPERATOR, ACCEPTED_BY_OPERATOR, REJECTED
SigningOperationStatus (8): CREATED, SIGNING, SIGNED_LOCALLY, SENDING_TO_KORUS, ACCEPTED_BY_KORUS, REJECTED_BY_KORUS, OUTCOME_UNKNOWN, ERROR
LockStatus (5): ACQUIRING, ACTIVE, LOCKED_BY_OTHER, EXPIRED, RELEASED
```

**Решение:** `VARCHAR(64)` — **не** FK на справочники. При создании устанавливается `IDENTIFIED`.

### Decision 5: `titleChain` — минималистичный

```json
{"title": "T1", "signedAt": "2026-07-22T10:00:00", "signedBy": "Иванов И.И."}
```

Три поля: `title` (T1–T4), `signedAt` (время подписания), `signedBy` (наименование подписанта из КОРУС).

### Decision 6: `verifications` — snapshot

```json
{
  "checks": [
    {"name": "Формат ЭТрН (XML)", "passed": true},
    {"name": "КНД", "passed": true},
    {"name": "XSD версия", "passed": true}
  ],
  "overallPassed": false,
  "verifiedAt": "2026-07-22T10:15:00"
}
```

При ревалидации — новый snapshot. Старый не хранится (не лог).

### Decision 7: Репликация справочников (Kafka Consumer из external library)

**Источники:** `ru.sberbank.ditsib.transport.messaging.messages` — внешняя библиотека от_transport-стека.

**Реализованные Consumer-бены:**
- `departmentsInput` → `DepartmentService.save()` / `delete()`
- `employeesInput` → `EmployeeService.save()` / `delete()`
- `organizationsInput` → `OrganizationService.save()` / `delete()` + upsert `OrganizationGroup`

**Реплицированные таблицы** — схема `etrn_cargo`: `employee`, `organization`, `department`, `organization_group`.

**Не реализована:** `positionsInput` (Position репликация).

### Decision 8: Транзакция — `etrn` + `etrn_audit`

```java
@Transactional
POST /api/etrn-cargo:
  1. new Etrn(humanReadableId)
  2. new EtrnAudit("ЭТрН создана")
  3. etrnRepository.save(etrn)
  4. auditRepository.save(audit)
  5. return 201
```

### Decision 9: Error-handling

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
class EtrnNotFoundException extends RuntimeException {
    public EtrnNotFoundException(String uuid) {
        super("ЭТрН " + uuid + " не найдена");
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class LockConflictException extends RuntimeException {
    private final UUID lockedBy;
    private final LocalDateTime lockUntil;
}
```

### Decision 10: Force-transition — allowlist

```
POST /api/etrn-cargo/{id}/force-transition
Body: {"targetStatus": "PROCESS_COMPLETED", "reason": "КОРУС подтвердил устно"}
```

**Допустимые исходные статусы (allowlist):**
- `WAIT_CONDITIONS`
- `WAIT_KORUS_CONFIRMATION`
- `OUTCOME_UNKNOWN`
- `ERROR`

**Запрещён из всех остальных статусов** (включая `IDENTIFIED`, `READY_FOR_BANK_ACTION`)

**Обязательно:** поле `reason` (запись в `etrn_audit`)

### Decision 11: Ручки для фронта

1. **POST /api/etrn-cargo/list** — тело: `pageSetting` + `sortSetting` + фильтры
2. **GET /api/etrn-cargo/{id}** — детальная информация
3. **PUT /api/etrn-cargo/{id}/lock** — установка блокировки (OAuth2, без тела)
4. **DELETE /api/etrn-cargo/{id}/lock** — снятие блокировки (OAuth2, без тела)

```json
// POST /api/etrn-cargo/list
{
  "pageSetting": {"page": 0, "size": 100},
  "sortSetting": {"directionAsc": false, "property": "CREATION_DATE"},
  "humanReadableId": "ЭТрН-772812",
  "statusFilter": "IDENTIFIED",
  "lockedByMe": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Decision 12: Контроллер — интерфейс + реализация

```
EtrnController       (интерфейс, Swagger @Tag)
    └── EtrnControllerImpl (@RestController)
```

Принцип — разделение контракта и реализации для удобного мокирования в тестах.

### Decision 13: Утилиты

- `ContextHelper.getUserId(Authentication)` — извлечение `userId` (UUID) из OAuth2 JWT или Basic-токена
- `IsoLocalDateTimeSerializer` — сериализация `LocalDateTime` в `ISO_LOCAL_DATE_TIME` через Jackson

---

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| **JSONB → растёт** (`title_chain` 4→10+ элементов) | Распилить в `etrn_title` (Sprint 2) |
| **`lock_info`** — без валидации (parallel signing) | Добавить `version` (optimistic lock) — уже есть |
| **Статусы** — 4 недостаточно для P2-P8 | Добавить новые статусы (Sprint 2) |
| **Аудит** — без партиций (растёт) | Партиционирование по `created_at` (Sprint 2) |
| **Репликация** — лаг / мёртвые ссылки на сотрудников | `UserNotFoundException` → `force-transition` для recovery |
| **POST /list** — не RESTful | Принятое соглашение с фронтом; один формат везде |
| **`EtrnNotFoundException`** принимает UUID, но getMessage форматирует как humanReadableId | Следующий спринт — унификация |
