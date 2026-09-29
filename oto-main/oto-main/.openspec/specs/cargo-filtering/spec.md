# Cargo Filtering

## Purpose

Система фильтрации заявок на перевозку грузов (cargo) с поддержкой множественных критериев поиска, включая фильтрацию по группам исполнителей.

## Requirements

### Requirement: Фильтрация по группам исполнителей

Система должна поддерживать три режима фильтрации заявок по группам исполнителей через параметры `emptyExecutorGroup` и `executorGroupIds`.

| Режим | emptyExecutorGroup | executorGroupIds | Поведение |
|-------|-------------------|------------------|-----------|
| Все группы | `false` | `[]` (пустой) | Фильтрация отсутствует, отображаются все заявки |
| Без групп | `true` | `[]` (пустой) | Отображаются только заявки без группы исполнителя |
| Конкретные группы | `false` | `[id1, id2, ...]` | Отображаются заявки, соответствующие выбранным группам |

#### Scenario: Все группы — фильтрация отсутствует

- **GIVEN** запрос с `executorGroupIds = []` и `emptyExecutorGroup = false` (или `null`)
- **THEN** в Criteria добавляются условия по другим фильтрам, но **не** добавляется условие по `executorGroupId`
- **AND** возвращаются все заявки, соответствующие остальным критериям фильтрации

#### Scenario: Без групп — только заявки без группы

- **GIVEN** запрос с `executorGroupIds = []` (или `null`) и `emptyExecutorGroup = true`
- **THEN** в Criteria добавляется условие `executorGroupId IS NULL`
- **AND** возвращаются только заявки, у которых не указана группа исполнителя
  TRANSPORT-43954
#### Scenario: Конкретные группы — фильтрация по ID

- **GIVEN** запрос с `executorGroupIds = [uuid1, uuid2]` и `emptyExecutorGroup = false` (или `null`)
- **THEN** в Criteria добавляется условие `executorGroupId IN (uuid1, uuid2)`
- **AND** возвращаются только заявки, принадлежащие указанным группам исполнителей

#### Scenario: emptyExecutorGroup=true имеет приоритет

- **GIVEN** запрос с `executorGroupIds = [uuid1, uuid2]` и `emptyExecutorGroup = true`
- **THEN** в Criteria добавляется условие `executorGroupId IS NULL` (режим «Без групп»)
- **AND** условие `executorGroupId IN (...)` **не** добавляется
- **AND** возвращается только заявки без группы исполнителя

### Requirement: DTO фильтрации с nullable булевым флагом

DTO запроса фильтрации (`CargoRequestDto`) должен содержать поле `isEmptyExecutorGroup` типа `Boolean` (обёрточный тип).

#### Scenario: Тип поля поддерживает три состояния

- **GIVEN** DTO запроса с полем `isEmptyExecutorGroup`
- **THEN** поле может принимать значения: `null` (не отправлено), `true` (выбрано «Без групп»), `false` (выбрано «Все группы» или не указано)
- **AND** используется `Boolean.TRUE.equals()` для безопасной проверки (без NPE при `null`)
