## REMOVED Requirements

### Requirement: Force-transition
Система SHALL принудительно переводить ЭТрН в целевой статус.

**Reason:** Функционал не описан в требованиях к системе и не используется бизнес-процессом. Изменение статуса
карточки происходит только по событиям жизненного цикла.

**Migration:** Механизм полностью удалён. Клиенты, использовавшие `POST /api/etrn-cargo/{id}/force-transition`,
получат 404 (hand отсутствует). Переходы по событиям (КОРУС, подписание) работают без изменений.

- **POST** `/api/etrn-cargo/{id}/force-transition` → удалено
- Допустимые исходные статусы (allowlist): `WAIT_CONDITIONS`, `WAIT_KORUS_CONFIRMATION` → удалено
- Обязательно: поле `reason` → удалено
- Аудит: запись в `etrn_audit` с `action` = "Принудительный переход: ..." → удалено

#### Scenario: Успешный force-transition
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `OUTCOME_UNKNOWN` в `PROCESS_COMPLETED`
- **THEN** 200 + `status` = `PROCESS_COMPLETED` + запись в `etrn_audit`
→ **REMOVED**

#### Scenario: Недопустимый переход
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `IDENTIFIED` в `PROCESS_COMPLETED`
- **THEN** 400 + `InvalidTransitionException` с русским описанием
→ **REMOVED**

#### Scenario: Без причины
- **WHEN** POST /api/etrn-cargo/{id}/force-transition без `reason`
- **THEN** 400 + `BadRequestException` с русским описанием
→ **REMOVED**
