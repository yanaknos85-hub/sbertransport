## ADDED Requirements

### Requirement: Модель статусов карточки
Статус карточки ЭТрН (`EtrnCardStatus`) SHALL включать ровно 6 значений: `IDENTIFIED`, `WAIT_KORUS_DATA`, `WAIT_CONDITIONS`, `READY_FOR_BANK_ACTION`, `WAIT_KORUS_CONFIRMATION`, `PROCESS_COMPLETED`. Значения `ERROR` и `OUTCOME_UNKNOWN` на уровне карточки не допускаются — ошибки и неопределённый исход фиксируются только на уровне операции подписания (`SigningOperationStatus`).

#### Scenario: Полный набор статусов
- **WHEN** вызывается `EtrnCardStatus.values()`
- **THEN** возвращается ровно 6 значений: `IDENTIFIED`, `WAIT_KORUS_DATA`, `WAIT_CONDITIONS`, `READY_FOR_BANK_ACTION`, `WAIT_KORUS_CONFIRMATION`, `PROCESS_COMPLETED`

#### Scenario: Удалённые статусы отсутствуют
- **WHEN** вызывается `EtrnCardStatus.valueOf("ERROR")` или `EtrnCardStatus.valueOf("OUTCOME_UNKNOWN")`
- **THEN** выбрасывается `IllegalArgumentException`

#### Scenario: Ошибочный исход — уровень операции подписания
- **WHEN** требуется зафиксировать ошибку или неизвестный исход
- **THEN** используется `SigningOperationStatus` (`ERROR` / `OUTCOME_UNKNOWN`), статус карточки не затрагивается

## MODIFIED Requirements

### Requirement: Force-transition
Система SHALL принудительно переводить ЭТрН в целевой статус.

- **POST** `/api/etrn-cargo/{id}/force-transition` → `{"targetStatus": "...", "reason": "..."}`
- **Допустимые исходные статусы (allowlist):** `WAIT_CONDITIONS`, `WAIT_KORUS_CONFIRMATION`
- **Обязательно:** поле `reason` (запись в `etrn_audit`)
- **Аудит:** запись в `etrn_audit` с `action` = "Принудительный переход: ..."

#### Scenario: Успешный force-transition
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `WAIT_CONDITIONS` в `READY_FOR_BANK_ACTION`
- **THEN** 200 + `status` = `READY_FOR_BANK_ACTION` + запись в `etrn_audit`

#### Scenario: Недопустимый переход
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `IDENTIFIED` в `PROCESS_COMPLETED`
- **THEN** 400 + `InvalidTransitionException` с русским описанием

#### Scenario: Переход из удалённого статуса
- **WHEN** в `etrn` существует запись со статусом `OUTCOME_UNKNOWN` или `ERROR` и на неё выполняется POST /api/etrn-cargo/{id}/force-transition
- **THEN** 400 + `InvalidTransitionException` с русским описанием (исходный статус вне allowlist)

#### Scenario: Без причины
- **WHEN** POST /api/etrn-cargo/{id}/force-transition без `reason`
- **THEN** 400 + `BadRequestException` с русским описанием
