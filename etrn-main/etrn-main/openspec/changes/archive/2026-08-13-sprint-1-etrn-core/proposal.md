## Why

**Задача:** реализовать первый спринт (Foundation-волну) сервиса `etrn-service` — микросервиса управления жизненным циклом ЭТрН в экосистеме АС СберТранспорт. Сценарий — **Банк-получатель, подписание Т3**.

**Контекст:** Проект находится на этапе проектирования (pre-development). Есть утверждённая core-модель данных (`knowledge/data-model.md`) и архитектура (`knowledge/architecture.md`).

**Решения, принятые в ходе проектирования:**
- Сущность именуется `etrn` (не `etrn_card`), аудит — `etrn_audit` (не `etrn_card_audit`)
- Префикс `Card` убирается из всех имён: `EtrnRepository`, `EtrnController`, `EtrnService` и т.д.
- REST-ручки базируются на `/api/etrn-cargo/...` (не `/api/v1/etrn/...`)
- Ручка PATCH /status удалена. Вместо неё — `POST /api/etrn-cargo/{id}/force-transition` (только из терминальных состояний)
- Ручка списка — `POST /api/etrn-cargo/list` с телом запроса (pageSetting + sortSetting)
- Цепочка титулов (`titleChain`) — без `role`, без `orgId`. Вместо них `initiatorId` (UUID сотрудника). Репликация сотрудников/департаментов/организаций/должностей через Kafka (4 топика)
- `verifications` — snapshot результатов проверок с `overallPassed` (производная)

---

## What Changes

### Новые возможности (Sprint 1)

- **Создание ЭТрН** (POST /api/etrn-cargo) — одна транзакция: `etrn` + `etrn_audit`
- **Статусная модель** — 4 статуса: `READY_TO_SIGN`, `IN_PROGRESS`, `SIGNED`, `REJECTED` (inline-строка, без FK на справочники)
- **Рабочая очередь** — POST /api/etrn-cargo/list (тело: pageSetting + sortSetting)
- **Детальный просмотр** — GET /api/etrn-cargo/{id}
- **Force-transition** — POST /api/etrn-cargo/{id}/force-transition (только из `OUTCOME_UNKNOWN`, `ERROR`, `WAIT_KORUS_CONFIRMATION`, `WAIT_CONDITIONS`; обязателен `reason`, аудит)
- **Аудит** — `etrn_audit` (история действий, русский язык в `action`)
- **Lock-механизм** — `lock_info` как JSONB-поле в `etrn` (краткоживущий lease)
- **Репликация справочников** — 4 Kafka-топика: `employee`, `organization`, `department`, `position`
- **titleChain** — JSONB без `role`/`orgId`, с `initiatorId` (UUID сотрудника)
- **verifications** — JSONB snapshot: `checks[]` + `overallPassed` + `verifiedAt`
- **Интеграция с КОРУС** — адресный вызов по `humanReadableId`
- **Error-handling** — кастомные исключения с русским описанием

### **BREAKING** — для future-спринтов

- **Разрыв с полной моделью** (28 сущностей): `title_chain`, `verifications`, `lock_info` — JSONB, не нормализованные таблицы
- **Статусная модель** — 4 статуса, не полная (P2-P8: `IDENTIFIED`, `LOCKED`, `SENT_TO_OPERATOR` — отложены)
- **Lock** — в составе `etrn`, не отдельная сущность `etrn_lock`

---

## Capabilities

### New Capabilities

- `etrn-card` — создание, обновление, список, детальный просмотр ЭТрН (название capability сохранено для обратной совместимости specs, внутри — без Card)
- `etrn-audit` — запись и чтение истории действий над ЭТрН
- `etrn-lock` — краткоживущий lease для T3 (lock + release)
- `etrn-queue` — рабочая очередь (POST-ручка с pageSetting/sortSetting)
- `etrn-korus` — интеграция с КОРУС (addressable fetch)
- `etrn-error` — кастомная обработка ошибок
- `etrn-org-replication` — репликация справочников сотрудников/организаций/департаментов/должностей из Kafka

### Modified Capabilities

- *Нет.* Sprint 1 — новый проект, без существующих spec-файлов.

---

## Impact

### Affected code

- `application/` — полный модуль (Controller → Service → Mapper → Repository → Entity)
- `pom.xml` — parent POM + module POM
- `db/changelog` — Liquibase миграции
- `application.yml` — конфигурация (порт 8227, БД `etrn`, OAuth2, HikariCP)
- `application-kafka.yml` — Kafka bindings для репликации

### Dependencies

- **New:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `lombok`, `mapstruct`, `liquibase`, `postgresql`, `spring-cloud-stream`, `spring-cloud-stream-binder-kafka`
- **Infrastructure:** Docker (jib), PostgreSQL, Kafka, OAuth2 RS512

### Integration points

- ЦС (inbound): `POST /cs/title3` → `humanReadableId`
- КОРУС (outbound): `FETCH_ETRN_STATE` → `humanReadableId`
- Kafka (inbound): `service.organization.employee`, `service.organization`, `service.organization.department`, `service.organization.position`