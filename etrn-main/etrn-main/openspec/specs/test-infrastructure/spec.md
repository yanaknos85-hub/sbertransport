## Purpose

Определить требования к тестовой инфраструктуре: использование embedded PostgreSQL, тестовой конфигурации и зависимостей.

## Requirements

### Requirement: Тестовая инфраструктура использует embedded PostgreSQL
Система ДОЛЖНА использовать embedded PostgreSQL для integration-тестов вместо Testcontainers или внешних баз данных.

#### Scenario: EmbeddedPostgres запускается автоматически
- **WHEN** на классе `@SpringBootTest` теста указана аннотация `@EmbeddedPostgres`
- **THEN** встроенный экземпляр PostgreSQL запускается на случайном порту и заменяет DataSource приложения

#### Scenario: Схема тестов изолирована
- **WHEN** integration-тесты запускаются против embedded PostgreSQL
- **THEN** каждый тест выполняется в схеме `etrn_cargo` с `ddl-auto=none` и `deferred-datasource-initialization=true`

### Requirement: Тестовая конфигурация отключает неосновные компоненты
Система ДОЛЖНА отключить планировщик, проверку ролей и внешние зависимости в тестовом профиле.

#### Scenario: Планирование отключено
- **WHEN** загружается тестовый профиль `application-test.yml`
- **THEN** свойство `scheduling.enabled` установлено в `false` и `role.check.enabled` в `false`

#### Scenario: Безопасность настроена для анонимных тестов
- **WHEN** загружается тестовый профиль
- **THEN** `security.enabled=false` и OAuth2 resourceserver настроен с тестовым публичным ключом

### Requirement: Тестовые зависимости соответствуют стандартам монолита
Система ДОЛЖНА использовать те же библиотеки для тестирования, что и эталонный проект `as-sbertransport-cargo`.

#### Scenario: Необходимые зависимости присутствуют
- **WHEN** анализируется `application/pom.xml`
- **THEN** в нём присутствуют `embedded-postgres` (test), `kafka-functional` (test), `awaitility` (test)
