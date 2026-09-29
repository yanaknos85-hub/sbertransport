## Why

Механизм принудительного изменения статуса карточки (`force-transition`) реализован в приложении, но не описан в требованиях к системе и не используется бизнес-процессом. Изменение статуса карточки в норме происходит только по событиям жизненного цикла (данные КОРУС, подтверждение операций подписания). Ручная смена статуса в обход статусной модели создаёт риск непоследовательных данных и несёт необоснованную поверхность API/тестов/документации, которую нужно поддерживать.

## What Changes

- **BREAKING** Удалён REST-эндпоинт `POST /api/etrn-cargo/{id}/force-transition` — принудительная смена статуса карточки больше невозможна через API.
- Удалён метод `forceTransition` из `EtrnService` / `EtrnServiceImpl` (включая константу allowlist `ALLOWED_TRANSITION_FROM` и запись в `etrn_audit` для этого действия).
- Удалён DTO `ForceTransitionRequest`.
- Удалено исключение `InvalidTransitionException` и его обработчик в `GlobalExceptionHandler` (использовалось только в force-transition).
- Удалены unit-тесты force-transition в `EtrnServiceImplTest`, `EtrnControllerImplTest`, `GlobalExceptionHandlerTest`.
- Обновлены спеки: `etrn-card` (требование Force-transition), `etrn-error` (блок `InvalidTransitionException` и сообщение о `reason`), `unit-tests` (сценарии force-transition).
- Обновлена контрактная документация `_documents/knowledge/contract.md` (раздел «Принудительный переход статуса»).

## Capabilities

### New Capabilities

—

### Modified Capabilities

- `etrn-card`: удаляется требование «Force-transition» — карточка переводится в целевой статус только по событиям жизненного цикла, ручной перевод исключён.
- `etrn-error`: удаляется блок `InvalidTransitionException` (400 «Недопустимый переход…») и упоминание сообщения «Поле reason обязательно для force-transition» в описании `BadRequestException`.
- `unit-tests`: удаляются сценарии `EtrnServiceImpl — force-transition валидация` и `EtrnServiceImpl — force-transition без причины`; требование 100% покрытия service сохраняется (покрытие нового, меньшего перечня методов).

## Impact

- **Код (main):** `EtrnController` + `EtrnControllerImpl` (эндпоинт), `EtrnService` + `EtrnServiceImpl` (метод + allowlist), `dto/ForceTransitionRequest`, `exceptions/InvalidTransitionException`, `exceptions/GlobalExceptionHandler` (обработчик).
- **Тесты:** `EtrnServiceImplTest` (секция forceTransition, 7 тестов), `EtrnControllerImplTest` (тест `forceTransition_shouldReturnDto`), `GlobalExceptionHandlerTest` (2 теста по `InvalidTransitionException`).
- **Спеки:** `openspec/specs/etrn-card`, `etrn-error`, `unit-tests`.
- **Документация:** `_documents/knowledge/contract.md` (раздел 4).
- **БД:** не затрагивается. Историческая роль-права на `POST /{id}/force-transition/` в схеме `authentication` становится неиспользуемой — безвредна, очистка (если потребуется) выполняется отдельным DBA-скриптом вне этого изменения; исторический скрипт `_documents/scripts/D-05.013.000/after/fill_roles.sql` не модифицируется.
- **API-контракт:** нарушающее изменение — клиенты, использующие `POST /{id}/force-transition`, получат 404 (ручка не зарегистрирована). Статусы карточки (`EtrnCardStatus`) и механизм lock не меняются.
