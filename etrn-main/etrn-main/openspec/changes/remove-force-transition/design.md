## Context

В приложении `app_cargo_etrn` реализован механизм `forceTransition` (`POST /api/etrn-cargo/{id}/force-transition`),
позволяющий произвольно переводить карточку ЭТрН из статуса, не описанного в бизнес-требованиях. Фактическая
используемость равна нулю: статусы `ERROR` и `OUTCOME_UNKNOWN` на уровне карточки не существуют, а allowlist
содержит только `WAIT_CONDITIONS` и `WAIT_KORUS_CONFIRMATION`. Исключение `InvalidTransitionException` и его
обработчик используются исключительно для этой ручки.

## Goals / Non-Goals

**Goals:**
- Удалить force-transition как архитектурно избыточный компонент.
- Удалить связанный код, DTO, исключения, тесты и спецификации.
- Сохранить стабильность: lock-механизм, CRUD, журнал, КОРУС-интеграции, статусы карточки — без изменений.

**Non-Goals:**
- Не модифицировать статусную модель карточки (переходы по событиям).
- Не удалять `InvalidTransitionException` как класс — удаляем только его использование. Если класс больше нигде
  не используется — удаляем полностью.
- Не удалять запись в `etrn_audit` для других действий (создание, lock/unlock).
- Не трогать Liquibase-миграции и схему БД.

## Decisions

### Решение 1: Полное удаление ручки и связанного кода

**Почему не partial / no-op.**
Force-transition не описан в требованиях, не используется и требует поддержки (allowlist, аудит, тесты, спецификации).
Часть кода (allowlist) уже сужена в спринте `remove-redundant-etrn-card-statuses`; полное удаление завершает чистку.

**Порядок удаления:**

1. **API-слой:** `EtrnController.forceTransition` — удалить метод из интерфейса и `EtrnControllerImpl`.
2. **Service-слой:** `EtrnService.forceTransition` — удалить из интерфейса и `EtrnServiceImpl` (включая `ALLOWED_TRANSITION_FROM` и import `InvalidTransitionException`).
3. **DTO:** `ForceTransitionRequest` — удалить файл целиком.
4. **Exceptions:** `InvalidTransitionException` — удалить файл; обработчик `handleInvalidTransition` из `GlobalExceptionHandler` — удалить. Проверить, что нигде больше не используется.
5. **Тесты:** удалить тесты, использующие `forceTransition`, `InvalidTransitionException`, `ForceTransitionRequest`.
6. **Спеки:** удалить/обновить соответствующие требования в `etrn-card`, `etrn-error`, `unit-tests`.
7. **Контракт:** удалить раздел 4 из `_documents/knowledge/contract.md`.

### Решение 2: `InvalidTransitionException` — полное удаление

Класс используется только в `forceTransition`. Проверка `grep` подтверждает: 12 ссылок — все в force-transition.
Удаляем класс целиком и обработчик в `GlobalExceptionHandler`.

### Решение 3: fill_roles.sql не модифицируется

Скрипт `_documents/scripts/D-05.013.000/after/fill_roles.sql` — исторический, DBA-уровня. Удаление строки с
`POST /{id}/force-transition/` — вне scope. Невычисленный permission безопасен: эндпоинт удалён на уровне Spring.

## Risks / Trade-offs

| Риск | Вероятность | Митигация |
|------|-------------|-----------|
| Клиентское приложение использует `/force-transition` | Низкая (фича не в требованиях) | Breaking-change в proposal; при наличии клиентов — отдельный change |
| `InvalidTransitionException` используется в заархивированных спринтах | Нет влияния на runtime | Архивные спеки — документация; на код не влияют |
| Удаление из спецификаций ломает архаивацию | Нет | Delta-спеки на удаление требований — штатный сценарий OpenSpec |

## Migration Plan

1. **Код:** single commit — удаление файлов + методов + тестов + спеков.
2. **Тестирование:** `mvn test` — все оставшиеся тесты проходят.
3. **Сборка:** `mvn compile` — отсутствие compilation errors.
4. **Rollback:** откат одного коммита возвращает все файлы и метод.

## Open Questions

Нет.
