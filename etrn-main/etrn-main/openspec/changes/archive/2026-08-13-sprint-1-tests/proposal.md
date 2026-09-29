## Why

Проект `app_cargo_etrn` находится на этапе архитектуры и специфицирования, без тестового покрытия код был непригоден для безопасной разработки. Необходимо установить инфраструктуру unit- и integration-тестирования, следуя стилю эталонного проекта `as-sbertransport-cargo`, и довести покрытие до приемлемого уровня для итеративной разработки.

## What Changes

- Создана тестовая инфраструктура: `TestCommon.java` (extends KafkaTest), `application-test.yml`, зависимости (embedded-postgres, kafka-functional, awaitility)
- Написаны unit-тесты для утилит и исключений: ContextHelper, GlobalExceptionHandler, checked exceptions, enums, IsoLocalDateTimeSerializer
- Написаны unit-тесты для сервисов: EtrnServiceImpl, Department/Employee/OrganizationServiceImpl, LockServiceImpl (уже был)
- Написаны unit-тесты для мапперов: EtrnMapper, Department/Employee/OrganizationMapper
- Написаны unit-тесты для scheduling-процессора и JPA-моделей (prePersist, preUpdate)
- Написаны integration-тесты для контроллера (EtrnControllerImpl) и listener-ов (Organization/Department/Employee)

## Capabilities

### New Capabilities
- `test-infrastructure`: Базовая тестовая инфраструктура — embedded-PostgreSQL, TestCommon, application-test.yml
- `unit-tests`: Unit-тесты для service-слоя, mapper-ов, util-классов, enums, exception-хендлеров
- `integration-tests`: Integration-тесты для контроллеров и Kafka-листенеров

### Modified Capabilities
<!-- None — no existing spec-level requirements changed -->

## Impact

- `application/src/main/java/ru/sber/transport/etrn/` — все main-пакеты
- `application/pom.xml` — добавлены test-зависимости (embedded-postgres, kafka-functional, awaitility)
- `application/src/test/resources/application.yml` — тестовый профиль
- Новые тесты: 128 тестов, 89% покрытие (47% → 89%)
- Остается непокрытым: JPA-entity boilerplate (prePersist/preUpdate без JPA-контекста, ~3%), EtrnApplication (root, ~3%)
