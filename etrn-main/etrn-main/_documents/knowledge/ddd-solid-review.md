# DDD/SOLID Code Review — Результаты и План Улучшений

> **Дата:** 31.07.2026
> **Объект:** `app_cargo_etrn/application/src/main/java/ru/sber/transport/etrn/`
> **Статус:** Часть исправлений применена, план развития сформирован

---

## 1. Что уже исправлено

### 1.0. Lock/Unlock возвращают void (Command — без side-effect reading)

`lock()` и `unlock()` — это **Command**, они не должны возвращать DTO.

```
PUT /api/v1/etrn/cards/{id}/lock  → 204 No Content (void)
DELETE /api/v1/etrn/cards/{id}/lock → 204 No Content (void)
```

Это следует принципу: команды изменяют состояние и не возвращают данные.

### 1.1. Логирование — `log.debug` во всех ключевых местах

| Файл | Что добавлено |
|------|---------------|
| `EtrnServiceImpl.java` | ~25 строк `log.debug`: входные параметры, validate, сохранение, аудит (7 методов) |
| `LockServiceImpl.java` | ~20 строк `log.debug`: lock/unlock/auto-unlock с userId, статусами, аудитами |
| `ContextHelper.java` | Извлечение userId из JWT и fallback, warn при ошибке |
| `EmployeeServiceImpl.java` | Поиск сотрудника по userId |
| `EtrnLockSchedulingProcessorImpl.java` | `log.debug` вместо `log.info` с "звёздочками" |

### 1.2. Исправлена ошибка с enum статусов

**Было:**
```java
private static final Set<String> ALLOWED_TRANSITION_FROM = Set.of(
    EtrnCardStatus.WAIT_CONDITIONS.name(),
    SigningOperationStatus.OUTCOME_UNKNOWN.name(), // ⚠️ неверный enum
    SigningOperationStatus.ERROR.name()            // ⚠️ неверный enum
);
```

**Стало:**
```java
private static final Set<String> ALLOWED_TRANSITION_FROM = Set.of(
    EtrnCardStatus.WAIT_CONDITIONS.name(),
    EtrnCardStatus.WAIT_KORUS_CONFIRMATION.name(),
    EtrnCardStatus.ERROR.name(),
    EtrnCardStatus.OUTCOME_UNKNOWN.name()
);
```

В `EtrnCardStatus` добавлены пропущенные значения: `ERROR`, `OUTCOME_UNKNOWN`.

### 1.2. Исправлена ошибка с enum статусов

**До (один сервис — 8 методов):**
```
EtrnService
├── create()                    ← команда
├── getById()                   ← чтение
├── list()                      ← чтение
├── forceTransition()           ← команда
├── lock()                      ← блокировки
├── unlock()                    ← блокировки
└── scheduleAutoUnlock()        ← планировщик
```

**После (3 сервиса — SRP соблюден):**
```
EtrnService (4 метода)                          LockService (3 метода)
├── create()                                    ├─ lock() → void (204 No Content)
├── getById()                                   ├─ unlock() → void (204 No Content)
├── list()                                      └─ scheduleAutoUnlock()
├── forceTransition()

EtrnControllerImpl:
├── lock → lockService.lock() → ResponseEntity<Void>
├── unlock → lockService.unlock() → ResponseEntity<Void>
└── schedule → lockService.scheduleAutoUnlock()
```

### 1.3. Lock/Unlock возвращают void (Command — без side-effect reading)

`lock()` и `unlock()` — это **Command**, они не должны возвращать DTO. После исправления:

```
PUT /api/v1/etrn/cards/{id}/lock  → 204 No Content (void)
DELETE /api/v1/etrn/cards/{id}/lock → 204 No Content (void)
```

Это следует принципу: команды изменяют состояние и не возвращают данные.

---

## 2. CQRS: Command vs Query

### 2.1. DDD-анализ

| № | Проблема | Приоритет | Описание |
|---|----------|-----------|----------|
| **DDD-01** | 🔴 Критическое | **Anemic Domain Model** — `Etrn` имеет `@Setter` на все 30+ полей. Бизнес-логика (смена статуса, lock/unlock) живёт в Service-слое, а не в агрегате. |
| **DDD-02** | 🟡 Среднее | **Нет Factory pattern** — создание ЭТрН идёт через `mapper.toEntity() + @Mapping(target = "status", constant = "IDENTIFIED")`. Инварианты создания размазаны по мапперу. |
| **DDD-03** | 🟡 Среднее | **Нет Domain Events** — при изменении статуса карточки нет `EtrnStatusChangedEvent`. Аудит в `etrn_audit` — persistence concern, не domain event. |
| **DDD-04** | 🟢 Низкое | **Native query ORM leak** — `findAllWithExpiredLockingTime()` использует нативный SQL с JSONB-фильтрацией. Для такой задачи JPQL/Criteria не сработают, но это осознанный компромисс. |
| **DDD-05** | 🟢 Низкое | **Value Object без валидации** — `LockInfo` (record) не проверяет инварианты (lockUntil > now). |

### 2.2. SOLID-анализ

| № | Принцип | Проблема | Приоритет |
|---|---------|----------|-----------|
| **SOLID-01** | **SRP** | `EtrnServiceImpl` — 4 ответственности: CRUD, Query, State Machine. **Частично исправлено** — lock выделен в `LockService`. | 🟡 Среднее |
| **SOLID-02** | **GRP (Granularity RP)** | `Etrn` entity — 30+ полей в одной таблице. Смешаны идентификация, контракт, логистика, габариты, ПЭП/МЧД, JSONB. | 🟡 Среднее |
| **SOLID-03** | **OCP** | `forceTransition` с `ALLOWED_TRANSITION_FROM = Set.of(...)` — жёстко закодирован Set путей. При добавлении нового перехода нужен правка кода. | 🟡 Среднее |
| **SOLID-04** | **LSP** | **Исправлено** — `SigningOperationStatus` больше не используется в `ALLOWED_TRANSITION_FROM`. Все статусы теперь из `EtrnCardStatus`. | ✅ Закрыто |
| **SOLID-05** | **ISP** | `EmployeeService`, `DepartmentService`, `OrganizationService` — thin wrapper поверх repository с unnecessary methods (save/delete) в публичном интерфейсе. | 🟢 Низкое |
| **SOLID-06** | **DIP** | `EmployeeServiceImpl` зависит от `EmployeeRepository` (это `JpaRepository`). Для чистого DIP нужен интерфейс-порт. | 🟢 Низкое |

---

## 3. CQRS: Command vs Query

### Концепция

Разделение сервисов на **Command** (изменение) и **Query** (чтение) based на принципе Command Query Responsibility Segregation:

| Служба | Ответственность | HTTP-аналог | Методы в текущем `EtrnService` |
|--------|-----------------|-------------|-------------------------------|
| **CardCommandService** | **Изменяет** состояние (write) | `POST`, `PUT`, `DELETE` | `create()`, `forceTransition()` |
| **CardQueryService** | **Читает** данные (read) | `GET` | `getById()`, `list()` |

### Зачем разделение

1. **SRP**: команды и чтения — разная ответственность
2. **Безопасность**: query-сервис может работать `readOnly = true`
3. **Масштабируемость**: read и write можно масштабировать независимо
4. **Читаемость**: интерфейс `LockService` уже показывает принцип — "это про блокировки"

### Целевая структура

```
EtrnCommandService (2 метода)         EtrnQueryService (2 метода)        LockService (3 метода)
├── create()                          ├── getById()                    ├─ lock()
├── forceTransition()                 └── list()                       ├─ unlock()
                                                       └─ scheduleAutoUnlock()
```

---

## 4. План улучшений — приоритеты и оценки

### P0 — Критические (сделать в текущем спринте)

| # | Задача | Описание | Сложность |
|---|--------|----------|-----------|
| **1** | Добавить поведенческие методы на `Etrn` | `acquireLock()`, `releaseLock()`, `forceTransitionTo()` — перенести бизнес-логику из Service в агрегат | Низкая |
| **2** | Создать `EtrnFactory.create()` | Инкапсулировать инварианты создания (status=IDENTIFIED, active=true) в одном месте, а не в MapStruct | Низкая |

### P1 — Средние (следующий спринт)

| # | Задача | Описание | Сложность |
|---|--------|----------|-----------|
| **3** | Разделить `EtrnService` на CQRS | `CardCommandService` + `CardQueryService` | Средняя |
| **4** | Domain Events | `EtrnStatusChangedEvent`, `EtrnCreatedEvent` — publish при изменениях | Средняя |
| **5** | State Machine для переходов | Конфигурируемая StateMachine вместо `Set.of(WAIT_CONDITIONS, ...) | Средняя |

### P2 — Низкий приоритет (backlog)

| # | Задача | Описание | Сложность |
|---|--------|----------|-----------|
| **6** | `LockInfo` с валидацией | Value Object с проверкой инвариантов (lockUntil > now) | Очень низкая |
| **7** | ISP: thin-сервисы | Разделить `EmployeeService`/`DepartmentService`/`OrganizationService` на Command/Query | Низкая |
| **8** | DIP: RepositoryPort | Заменить `JpaRepository` на абстрактный интерфейс | Низкая |
| **9** | GRP: разделение `Etrn` | Вынести контракт/логистику/габариты в подмодели | Высокая |

---

## 5. Статистика изменений (сессия 31.07.2026)

| Файл | Строк добавлено | Строк удалено | Тип |
|------|-----------------|---------------|-----|
| `LockService.java` | ~45 | 0 | **Новый** |
| `LockServiceImpl.java` | ~200 | 0 | **Новый** |
| `EtrnService.java` | 0 | 3 | Удаление методов |
| `EtrnServiceImpl.java` | ~25 | ~100 | Логирование + удаление lock-методов |
| `EtrnControllerImpl.java` | 2 | 2 | Зависимость от `LockService` |
| `EtrnLockSchedulingProcessorImpl.java` | 2 | 2 | Зависимость от `LockService` |
| `EtrnCardStatus.java` | 2 | 0 | Добавлены ERROR, OUTCOME_UNKNOWN |
| `ContextHelper.java` | 7 | 0 | Логирование userId |
| `EmployeeServiceImpl.java` | 2 | 0 | Логирование поиска |
| **Итого** | **~283** | **~105** | **+178 строк чистого кода** |

---

## 6. Ссылки на артефакты

| Документ | Путь |
|----------|------|
| Архитектура | `knowledge/architecture.md` |
| Модель данных (SSOT) | `knowledge/data-model.md` |
| Анализ противоречий | `knowledge/inconsistencies.md` |
| Статусная модель | `references/05-state-model.md` |
| Бизнес-требования | `references/01-business-requirements.md` |
| Системные требования | `references/02-system-requirements.md` |
| GIGACODE контекст | `GIGACODE.md` |
