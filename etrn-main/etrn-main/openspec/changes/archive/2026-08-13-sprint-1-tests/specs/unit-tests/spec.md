## ADDED Requirements

### Requirement: Unit-тесты покрывают все service-классы
Система ДОЛЖНА иметь unit-тесты со 100% покрытием для service-имплементаций (кроме LockService, который уже был покрыт).

#### Scenario: EtrnServiceImpl — создание ЭТрН
- **WHEN** вызывается `create()` с новым `humanReadableId`
- **THEN** сущность сохраняется, запись аудита создаётся, возвращается `EtrnDto`

#### Scenario: EtrnServiceImpl — идемпотентность
- **WHEN** вызывается `create()` с существующим `humanReadableId`
- **THEN** новая сущность не создаётся, возвращается существующая DTO, save не вызывается

#### Scenario: EtrnServiceImpl — force-transition валидация
- **WHEN** вызывается `forceTransition()` с недопустимым исходным статусом
- **THEN** выбрасывается `InvalidTransitionException`

#### Scenario: EtrnServiceImpl — force-transition без причины
- **WHEN** вызывается `forceTransition()` с пустым `reason`
- **THEN** выбрасывается `BadRequestException`

#### Scenario: DepartmentServiceImpl — findOrCreateById
- **WHEN** вызывается `findOrCreateById()` с несуществующим ID
- **THEN** создаётся новая сущность с указанным ID и сохраняется в репозиторий

#### Scenario: EmployeeServiceImpl — getAuthenticatedEmployee
- **WHEN** вызывается `getAuthenticatedEmployee()` с несуществующим пользователем
- **THEN** выбрасывается `UserNotFoundException`

#### Scenario: OrganizationServiceImpl — CRUD операции
- **WHEN** вызываются `save()`, `findById()`, `delete()`, `get()`
- **THEN** методы делегируются в репозиторий без дополнительной логики

### Requirement: Unit-тесты покрывают все мапперы
Система ДОЛЖНА иметь unit-тесты для MapStruct мапперов.

#### Scenario: EtrnMapper — toDetailDto с пустым titleChain
- **WHEN** сущность `Etrn` имеет null/пустой `titleChain`
- **THEN** `toDetailDto()` возвращает пустой список

#### Scenario: EtrnMapper — toDetailDto с null verifications
- **WHEN** сущность `Etrn` имеет null `verifications`
- **THEN** `toDetailDto()` возвращает null для verifications

#### Scenario: EtrnMapper — lastSignedTitle — null titleChain
- **WHEN** сущность имеет null `titleChain`
- **THEN** `lastSignedTitle()` возвращает null

#### Scenario: DepartmentMapper, EmployeeMapper, OrganizationMapper — fromMessage
- **WHEN** вызывается `fromMessage()` для каждого маппера
- **THEN** поля сообщения корректно маппятся на поля сущности

### Requirement: Unit-тесты покрывают util, exceptions и enums
Система ДОЛЖНА иметь unit-тесты для вспомогательных классов.

#### Scenario: ContextHelper.getUserId — из JWT по jti
- **WHEN** `Authentication` — `JwtAuthenticationToken`
- **THEN** `getUserId()` извлекает UUID из `tokenAttributes.get("jti")`

#### Scenario: ContextHelper.getUserId — из Authentication по name
- **WHEN** `Authentication` не является JWT
- **THEN** `getUserId()` извлекает UUID из `authentication.getName()`

#### Scenario: GlobalExceptionHandler — handleNotFound
- **WHEN** вызывается `handleNotFound(EtrnNotFoundException)`
- **THEN** возвращается `ResponseEntity` со статусом 404 и сообщением

#### Scenario: GlobalExceptionHandler — handleLockConflict
- **WHEN** вызывается `handleLockConflict(LockConflictException)`
- **THEN** возвращается `ResponseEntity` со статусом 409 и телом `LockConflictResponse`

#### Scenario: Enums — все значения имеют описания
- **WHEN** перебираются все значения `EtrnCardStatus`, `EtrnTitleStatus`, `SigningOperationStatus`, `LockStatus`
- **THEN** каждое значение имеет непустое `getDescription()`

### Requirement: Integration-тесты покрывают контроллеры и listeners
Система ДОЛЖНА иметь integration-тесты для API и Kafka-обработчиков.

#### Scenario: EtrnControllerImpl — POST /create
- **WHEN** отправляется `POST /create` с валидным телом
- **THEN** возвращается 200 OK с DTO

#### Scenario: EtrnControllerImpl — GET /{id}
- **WHEN** отправляется `GET /{id}` для существующей ЭТрН
- **THEN** возвращается 200 OK с DetailDto

#### Scenario: EtrnControllerImpl — GET /{id} не найдена
- **WHEN** отправляется `GET /{id}` для несуществующей ЭТрН
- **THEN** возвращается 404 Not Found

#### Scenario: OrganizationListener — новая организация
- **WHEN** публикуется `OrganizationMessage` с `deleted=false`
- **THEN** организация сохраняется через `OrganizationService.save()`

#### Scenario: DepartmentListener — удаление
- **WHEN** публикуется `DepartmentMessage` с `deleted=true`
- **THEN** вызывается `DepartmentService.delete()`

#### Scenario: EmployeeListener — авторизация
- **WHEN** публикуется `EmployeeMessage` с `deleted=false`
- **THEN** сотрудник сохраняется или обновляется
