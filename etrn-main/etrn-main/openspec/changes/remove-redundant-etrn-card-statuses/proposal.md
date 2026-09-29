## Why

В модели карточки ЭТрН присутствуют статусы `ERROR` и `OUTCOME_UNKNOWN`, которые бизнес-процессом не используются: ошибок уровня карточки нет, а ошибки/неопределённый исход относятся только к уровню операции подписания (`SigningOperationStatus`). Из-за этого фронтенд и API вынуждены поддерживать лишние значения, а allowlist force-transition раздут статусами, которые фактически никогда не возникают на уровне карточки.

## What Changes

- **BREAKING** из enum `EtrnCardStatus` исключаются значения `ERROR` и `OUTCOME_UNKNOWN`. Остаются ровно 6 статусов: `IDENTIFIED`, `WAIT_KORUS_DATA`, `WAIT_CONDITIONS`, `READY_FOR_BANK_ACTION`, `WAIT_KORUS_CONFIRMATION`, `PROCESS_COMPLETED`.
- **BREAKING** allowlist force-transition (`ALLOWED_TRANSITION_FROM`) сжимается до `WAIT_CONDITIONS` и `WAIT_KORUS_CONFIRMATION` — сценарий reconciliation из `OUTCOME_UNKNOWN`/`ERROR` на уровне карточки больше не поддерживается.
- Удаление тестов и констант, связанных с удалёнными статусами карточки.
- `SigningOperationStatus` (`ERROR`, `OUTCOME_UNKNOWN`) — **не изменяется**, это отдельный уровень статусов.
- Синхронизация SSOT-документации (`_documents/knowledge/`, `GIGACODE.md`) и устаревшей строки OUT-OF-SCOPE в спеке `etrn-korus`.

## Capabilities

### New Capabilities

(нет)

### Modified Capabilities

- `etrn-card`: меняется модель статусов карточки (6 статусов вместо 8) и требование Force-transition — допустимые исходные статусы сужаются до `WAIT_CONDITIONS` и `WAIT_KORUS_CONFIRMATION`.

## Impact

- **Код (main):**
  - `application/src/main/java/ru/sber/transport/etrn/enums/EtrnCardStatus.java` — удаление 2 значений.
  - `application/src/main/java/ru/sber/transport/etrn/service/impl/EtrnServiceImpl.java` — `ALLOWED_TRANSITION_FROM` без `ERROR`/`OUTCOME_UNKNOWN`.
- **Код (test):**
  - `EtrnServiceImplTest` — удаление тестов `forceTransition_errorStatus_allowed`, `forceTransition_outcomeUnknown_allowed`.
  - `TestCommon` — удаление констант `STATUS_ERROR`, `STATUS_OUTCOME_UNKNOWN`.
- **БД:** миграция не требуется — колонка `etrn_cargo.etrn.status` — `VARCHAR(64)` без CHECK-констрейнта, сущность `Etrn` хранит статус как `String`.
- **API:** `POST /api/etrn-cargo/{id}/force-transition` из `ERROR`/`OUTCOME_UNKNOWN` будет отклоняться `409 InvalidTransitionException`; DTO-контракт (String-статус) формально не меняется.
- **Спеки/доки:** `openspec/specs/etrn-card/spec.md` (через delta), `openspec/specs/etrn-korus/spec.md` (устаревшая строка OUT-OF-SCOPE), `_documents/knowledge/init/knowledge/data-model.md`, `_documents/knowledge/init/references/05-state-model.md` и смежные.
