# СТ ETRN-00-BE-01. Системные требования (выжимка v3.1)

**Источник:** Confluence pageId=24386932383
**Версия:** v3.1 от 19.07.2026
**Jira:** TRANSPORT-44256 | **Story Points:** 13 SP
**Область:** Sprint 1 · Stage 1 · Т3

---

## 1. Назначение

Реализовать `etrn-service` — владельца агрегата ЭТрН, persistence/state/readiness/signing orchestration и внутренних API. В Sprint 1 активируется сценарий Этапа 1 с подписанием Т3. Модель агрегата и контракты должны поддерживать цепочку Т1–Т4.

## 2. Архитектурная граница

| Граница | Требование |
|---------|-----------|
| Владелец | etrn-service: domain + persistence + внутренние REST/application services |
| Входы | Команды/события от /cs/title3, KORUS facade, FE, signing adapter |
| Выходы | Read DTO, события outbox, команды KORUS, аудит/метрики |
| Запрещено | Прямой доступ других сервисов к таблицам; физическое удаление |

## 3. Компонентная модель

| Компонент | Ответственность |
|-----------|----------------|
| EtrnApplicationService | Оркестрация команд и запросов агрегата |
| IdentityResolver | Сопоставление strong/weak идентификаторов, конфликты |
| Stage1StateEngine | Вычисление разрешённых переходов card/title |
| T3ValidationService | FORMAT/READINESS/PRE_SIGN и соответствующие условия |
| TitleLockService | Атомарный захват, освобождение, истечение блокировки (lease) |
| SigningOrchestrator | Жизненный цикл операции, команды signing adapter и KORUS |
| EtrnRepository | Транзакционное сохранение; optimistic lock |
| OutboxPublisher | Надёжная доставка downstream-команд/событий |
| ReadModelProjector | DTO списка подписания и карты |

## 4. Контракты (ключевые)

| Контракт | Семантика |
|----------|-----------|
| resolveOrCreateCard(identifiers) | cardId; conflict — не merge |
| applyCsContext(cardId, routeContext, sourceRef) | Внешний контекст ЦС |
| applyKorusSnapshot(cardId, snapshot) | Версионирование domain data + validation |
| getSigningQueue(query) | Read DTO + счётчики |
| getCard(cardId) | Detail DTO с titles/T3/conditions/history |
| acquireT3Lock(cardId, user, session) | Lock token + expiresAt или LOCKED_BY_OTHER |
| releaseT3Lock(cardId, token) | Идемпотентное освобождение |
| startT3Signing(cardId, lockToken, requestKey) | PRE_SIGN + уникальная операция |
| registerSigningOutcome(operationId, result) | State transition + outbox/event |

## 5. Системные инварианты (I-01–I-09)

| ID | Инвариант |
|----|-----------|
| I-01 | Одна strong identity → один cardId без merge |
| I-02 | Card/title/signing — только по утверждённым переходам |
| I-03 | T3 sign — без READY_TO_SIGN, valid lock и PRE_SIGN не проходит |
| I-04 | ≤ 1 active signing operation на Т3 |
| I-05 | OUTCOME_UNKNOWN запрещает новую operation до reconciliation |
| I-06 | PROCESS_COMPLETED ≠ DELETE |
| I-07 | routeContext не создаёт внутренний route |
| I-08 | Frontend не подменяет backend validation |
| I-09 | Нет хардкода «единственный банковский титул = T3» |

## 6. Статусная модель

| Уровень | Последовательность Stage 1 |
|---------|---------------------------|
| Card | IDENTIFIED → WAIT_KORUS_DATA → WAIT_CONDITIONS / READY_FOR_BANK_ACTION → WAIT_KORUS_CONFIRMATION → PROCESS_COMPLETED |
| T3 | EXPECTED → AVAILABLE/WAIT_CONDITIONS → READY_TO_SIGN → SIGNING → SIGNED_LOCALLY → SENT_TO_OPERATOR → ACCEPTED_BY_OPERATOR |
| Signing op | CREATED → SIGNING → SIGNED_LOCALLY → SENDING_TO_KORUS → ACCEPTED_BY_KORUS; ERROR/REJECTED/OUTCOME_UNKNOWN |
| Lock | ACTIVE (технический, не card status) |

## 7. Ошибки и API mapping

| Сценарий | HTTP | Системное поведение |
|----------|------|---------------------|
| IDENTITY_CONFLICT | — | blocking issue, без слияния |
| NOT_READY | 409 | список условий |
| LOCKED_BY_OTHER | 409 | + expiry / владелец (display-safe) |
| LOCK_EXPIRED | 409 | reacquire |
| CHECKSUM_CHANGED | 409 | reload + revalidate |
| ACTIVE_OPERATION_EXISTS | 409 | вернуть операцию |
| OUTCOME_UNKNOWN | 423/409 | blocked до reconciliation |

## 8. Транзакции и идемпотентность

- Aggregate + event + outbox commit — атомарно
- Unique constraints: identifiers, active title, active lock, active operation
- Optimistic version — предотвращает last-write-wins
- Sign request key — при повторе возвращает существующую операцию

## 9. Безопасность и наблюдаемость

- RBAC: раздельные permissions на раздел/карточку/readiness/sign/tech details
- Secrets: OAuth/KORUS/signing — вне бизнес-таблиц и логов
- ПДн: минимально необходимое раскрытие
- Audit: append-only для lock/sign/send/result
- Метрики: inbox rate, cards by status, KORUS latency, validation blocks, locks, signing results, SLA breaches
- Алерты: auth failures, retry exhaustion, unknown outcome, stuck confirmation, identity conflicts

## 10. Тестовая модель (ключевые сценарии)

- Identity: new/existing/conflict/race
- Progressive enrichment: minimal → partial → full
- Все разрешённые и запрещённые переходы card/title/operation
- 20 readiness conditions
- Two-user lock, expiry, disconnect, stale token
- Local error / rejected / unknown / accepted — без дублирования
- Completion: данные в БД сохранены, deep link работает