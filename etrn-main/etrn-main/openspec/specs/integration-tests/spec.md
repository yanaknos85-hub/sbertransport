## Purpose

Определить требования к интеграционному тестированию компонентов системы ЭТрН: контроллеров API и Kafka-листенеров.

## Requirements

### Requirement: Integration-тесты покрывают контроллеры с MockMvc
Система ДОЛЖНА иметь integration-тесты для контроллеров с `@AutoConfigureMockMvc(addFilters = false)`.

#### Scenario: POST /create возвращает 200
- **WHEN** отправляется `POST /` с `EtrnCreateRequest`
- **THEN** возвращается статус 200 OK с `EtrnDto` в теле

#### Scenario: GET /{id} для существующей ЭТрН
- **WHEN** отправляется `GET /{id}`
- **THEN** возвращается статус 200 OK с `EtrnDetailDto`

#### Scenario: GET /{id} для несуществующей ЭТрН
- **WHEN** отправляется `GET /{nonExistentId}`
- **THEN** возвращается статус 404 Not Found

#### Scenario: GET /list с фильтрами
- **WHEN** отправляется `GET /list` с `SearchEtrnDto`
- **THEN** возвращается статус 200 OK с `Page<EtrnJournalDto>`

### Requirement: Integration-тесты покрывают Kafka-листенеры
Система ДОЛЖНА иметь integration-тесты для Kafka-листенеров с использованием `KafkaTest`.

#### Scenario: OrganizationListener — новая организация
- **WHEN** публикуется `OrganizationMessage` с `deleted=false`
- **THEN** `OrganizationService.save()` вызывается один раз

#### Scenario: OrganizationListener — удаление
- **WHEN** публикуется `OrganizationMessage` с `deleted=true`
- **THEN** `OrganizationService.delete()` вызывается

#### Scenario: DepartmentListener — новая организация
- **WHEN** публикуется `DepartmentMessage` с `deleted=false`
- **THEN** `DepartmentService.save()` вызывается один раз

#### Scenario: EmployeeListener — авторизация
- **WHEN** публикуется `EmployeeMessage` с `deleted=false`
- **THEN** `EmployeeService.save()` вызывается
