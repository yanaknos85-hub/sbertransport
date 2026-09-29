## 1. Сборка и инфраструктура

- [x] 1.1 Создать `pom.xml` — parent POM + module (jib, jacoco, flatten)
- [x] 1.2 Создать `application/` module с Java 21 + Spring Boot 3.x
- [x] 1.3 Настроить `application.yml` (порт 8227, БД `etrn_cargo`, OAuth2, HikariCP)
- [x] 1.4 Настроить `application-kafka.yml` — bindings для 4 топиков репликации

## 2. Модель данных

- [x] 2.1 Создать `Etrn.java` — JPA-сущность (таблица `etrn_cargo.etrn`, JSONB: `title_chain`, `verifications`, `lock_info`)
- [x] 2.2 Создать `EtrnAudit.java` — JPA-сущность (таблица `etrn_cargo.etrn_audit`, FK `etrn_id`)
- [x] 2.3 Создать Liquibase changelog (`etrn`, `etrn_audit`, employee, organization, department, position, organization_group, shedlock, urls, roles)
- [x] 2.4 Создать `EtrnRepository` + `EtrnAuditRepository` (имена без Card)

## 3. DTO и мапперы

- [x] 3.1 Создать `EtrnJournalDto` (record, 10 полей) — журнал/рабочая очередь
- [x] 3.2 Создать `EtrnDetailDto` (record, все поля)
- [x] 3.3 Создать `EtrnCreateRequest`, `ForceTransitionRequest`, `SearchEtrnDto` (records)
- [x] 3.4 Создать `EtrnMapper` (MapStruct, Spring DI)

## 4. Сервисный слой

- [x] 4.1 Создать `EtrnService` (интерфейс + `EtrnServiceImpl`)
- [x] 4.2 Реализовать `create` (humanReadableId → etrn + audit, idempotent)
- [x] 4.3 Реализовать `getById` + `list` (POST `/list` с pageSetting/sortSetting/фильтрами)
- [x] 4.4 Реализовать `forceTransition` (POST `/force-transition` с валидацией allowlist)
- [x] 4.5 Реализовать lock/unlock (JSONB `lock_info` + аудит, TTL, scheduler `scheduleAutoUnlock`)

## 5. Контроллеры

- [x] 5.1 Создать `EtrnController` (интерфейс) + `EtrnControllerImpl` (REST, Spring @RestController)
- [x] 5.2 Добавить валидацию (jakarta validation, humanReadableId, force-transition reason)

## 6. Репликация справочников

> Репликация из корпоративных топиков через `ru.sberbank.ditsib.transport.messaging.messages` (внешняя библиотека)

- [x] 6.1 Создать Entity: `Employee.java`, `Organization.java`, `Department.java`, `OrganizationGroup.java` (схема `etrn_cargo`)
- [x] 6.2 Создать Repository: `EmployeeRepository`, `OrganizationRepository`, `DepartmentRepository`, `OrganizationGroupRepository`
- [x] 6.3 Создать Service: `EmployeeService`, `OrganizationService`, `DepartmentService` (upsert + delete-by-flag)
- [x] 6.4 Создать `ListenerConfig.java` — 3 functional beans (Consumer<Message<T>>: department, employee, organization)
- [x] 6.5 Использовать external Message-классы: `DepartmentMessage`, `EmployeeMessage`, `OrganizationMessage`, `PositionMessage`
- [x] 6.6 Liquibase-миграции на реплицируемые таблицы: `employee`, `organization`, `department`, `organization_group`

## 7. Обработка ошибок

- [x] 7.1 Создать `EtrnNotFoundException`, `BadRequestException`, `LockConflictException`, `InvalidTransitionException`, `UserNotFoundException`
- [x] 7.2 Добавить `GlobalExceptionHandler` (@RestControllerAdvice) для единого формата ошибок

## 8. Утилиты и конфигурация

- [x] 8.1 Создать `ContextHelper` — извлечение userId/roles из OAuth2 Authentication
- [x] 8.2 Создать `IsoLocalDateTimeSerializer` — сериализация LocalDateTime в ISO формат
- [x] 8.3 Проверить `mvn compile` — сборка без ошибок

## 9. Интеграции и тесты

> КОРУС-интеграция, unit- и integration-тесты вынесены в `openspec/changes/sprint-1-tests/`
