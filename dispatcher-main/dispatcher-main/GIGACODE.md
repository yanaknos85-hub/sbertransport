# app_platform_dispatcher

## Project Overview

**Dispatcher Service** (Диспетчерская) — это Spring Boot microservice для управления транспортной диспетчерской системой. Сервис предоставляет REST API для управления сущностями: контрагентами, водителями, диспетчерами, автомобилями, сменами и поездками.

### Key Technologies

- **Language**: Java 21
- **Framework**: Spring Boot 3.x (с использованием Spring Cloud)
- **Build Tool**: Maven (модульная структура)
- **Persistence**: Spring Data JPA, PostgreSQL, Liquibase
- **Messaging**: Apache Kafka (Spring Cloud Stream)
- **Security**: SberTransport security stack (OAuth2/JWT)
- **API Documentation**: OpenAPI 3.0
- **Containerization**: Jib (Docker image building)
- **Orchestration**: OpenShift/Kubernetes

### Architecture

Проект имеет модульную структуру из двух модулей:

| Модуль | Описание |
|--------|----------|
| `application` | Основное Spring Boot приложение с REST API, бизнес-логикой, JPA сущностями |
| `messaging` | Kafka сообщения и функционал для интеграционных взаимодействий |

**Main Components:**
- **Controllers**: REST endpoints для всех сущностей (Dispatcher, Driver, Contractor, Vehicle, Shift, Autopark)
- **Services**: Бизнес-логика и транзакционные операции
- **Repositories**: JPA репозитории к PostgreSQL
- **DTOs**: Объекты передачи данных (с поддержкой проекций: SELECT, FULL)
- **Feign Clients**: Интеграция с внешними сервисами (UserDataConfirmation, Trips, Corporate)
- **Kafka Producers/Consumers**: Асинхронная коммуникация через Kafka

### Core Entities

| Сущность | Описание |
|----------|----------|
| `Contractor` | Контрагент (организация-поставщик транспортных услуг) |
| `Dispatcher` | Диспетчер (сотрудник контрагента) |
| `Driver` | Водитель с атрибутами и лицензиями |
| `Vehicle` | Транспортное средство (пассажирское/грузовое) |
| `Autopark` | Автопарк (группировка ТС) |
| `Shift` | Смена водителя с конфликтами |
| `Trip` | Поездка с статусами (PLANNED, ACTIVE, COMPLETED) |

## Building and Running

### Prerequisites

- Java 21+ (OpenJDK)
- Maven 3.8+
- PostgreSQL 14+ (для локальной разработки)
- Kafka (для локальной разработки с messaging)

### Local Development

**Установка зависимостей:**
```bash
mvn clean install -DskipTests
```

**Запуск приложения:**
```bash
# Запуск через Maven
mvn spring-boot:run -pl application

# Запуск скомпилированного JAR
java -jar target/dispatcher.jar
```

**Профили:**
- `default` — локальный запуск
- `kubernetes` — запуск в OpenShift
- `secman` — интеграция с системой безопасности

### Testing

```bash
# Запуск unit-тестов
mvn test

# Запуск тестов с покрытием
mvn test jacoco:report

# Запуск integration тестов
mvn verify
```

### Building Docker Images

```bash
# Сборка образа через Jib
mvn jib:build -pl application

# Сборка с тегом
mvn jib:build -pl application -Dimage-tag=dispatcher:my-version

# Локальная сборка в Docker daemon
mvn jib:dockerBuild -pl application
```

### OpenShift/Kubernetes Deployment

Используются конфигурации из `_documents/openshift/`:

```bash
# Создание ConfigMaps
oc apply -f _documents/openshift/configMap.yml

# Развертывание приложения
oc apply -f _documents/openshift/deployment.yml
oc apply -f _documents/openshift/service.yml

# Или через pipeline (автоматически)
# CI/CD автоматически собирает и деплоит образ dispatcher
```

## Development Conventions

### Code Style

- **Lombok**: Включен для сокращения boilerplate кода (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- **MapStruct**: Используется для маппинга DTO ↔ Entity (annotation processing на этапе компиляции)
- **Logging**: Slf4j (через Spring Boot parent)

### Project Structure

```
application/src/main/java/ru/sber/transport/dispatcher/
├── controller/          # REST API endpoints
│   ├── impl/           # Implementation classes (suffix Impl)
│   └── *.java          # Controller interfaces
├── service/            # Business logic
│   └── impl/           # Service implementations
├── database/
│   ├── dao/           # JPA Repositories
│   └── model/         # JPA Entities
├── dto/               # Data Transfer Objects
│   ├── *.java         # Request/Response DTOs
│   ├── enums/         # Enumerations and projections
│   └── search/        # Search DTOs
├── converters/        # Exception converters, deserializers
├── config/            # Spring configuration
└── DispatcherApplication.java  # Main entry point
```

### Naming Conventions

| Тип | Префикс/Суффикс | Пример |
|-----|-----------------|--------|
| Controller Interface | `XxxController` | `DriverController` |
| Controller Implementation | `XxxControllerImpl` | `DriverControllerImpl` |
| Service Implementation | `XxxServiceImpl` | `ShiftServiceImpl` |
| Repository | `XxxRepository` | `DriverRepository` |
| Entity | `Xxx` (CamelCase) | `Driver`, `ShiftConflict` |
| DTO | `XxxDTO` / `NewXxxDTO` | `DriverDTO`, `NewDriverDTO` |
| Search DTO | `XxxSearchDTO` | `DriverSearchDTO` |

### API Conventions

- **Base path**: `/` (уточняется в OpenAPI спецификациях)
- **Authentication**: JWT Bearer token через Spring Security
- **Pagination**: Поддерживается для списка (`page`, `size`, `sort`)
- **Projections**: Параметр `projection` (SELECT/FULL) для контроля объема данных
- **Partial Updates**: PATCH с массивом `PatchDataV2` для частичного обновления полей

### Database

- **PostgreSQL**: Основная СУБД
- **Liquibase**: Управление миграциями (чек-поинты в `db/changelog/`)
- **HikariCP**: Connection pool
- **Hibernate**: JPA провайдер с extensions (hypersistence-utils)

### Kafka Integration

- **Topic**: Конфигурация через `application.yml` и Vault secrets
- **Avro**: Схемы сообщений в `_documents/api/avro/`
- **Kafka Functional**: Используется стек `ru.sber.transport:kafka-functional`

### Security

- **Authorization**: SberTransport authorization stack
- **Consent Check**: `@SkipConsentCheck` аннотация для пропуска проверки согласия
- **Vault**: Креденциалы и секреты через HashiCorp Vault (Kubernetes integration)

## Key Configuration

### Application Properties

Ключевые свойства (обычно в `application.yml` или environment variables):

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dispatcher
    username: dispatcher
    password: ***
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: dispatcher-consumer
```

### External Services (Feign Clients)

- **UserDataConfirmation**: Подписание ПДН
- **Trips**: Управление поездками
- **Corporate**: Интеграция с корпоративной системой
- **Authorization**: Проверка прав и ролей

## API Documentation

OpenAPI спецификация доступна через Swagger UI (если включен):

```
http://localhost:8080/swagger-ui.html
```

Документация контрагентов: `_documents/api/contractors.yml`

## CI/CD Pipeline

**Pipeline**: CI02351878 (transport dispatcher)

**Теги и ветки:**
- `release/D-*` — релизные ветки
- `D-*` — feature branches
- CI build с тегом `CI02351878-<date>-<counter>-<user>`

**Image Registry:**
```
registry-ci.delta.sbrf.ru/dev/ci02351878/ci02353146_transport_dev/transport/dispatcher:<version>-<user>
```

## Troubleshooting

### Common Issues

1. **Annotation Processing Errors**
   - Убедитесь, что `lombok` и `mapstruct` обработчики настроены в IDE
   - Проверьте `annotationProcessorPaths` в `pom.xml`

2. **Database Connection**
   - Проверьте `application-local.yml` для локального запуска
   - Убедитесь, что PostgreSQL запущен и база создана

3. **Kafka Issues**
   - Проверьте `KAFKA_BROKERS` и `KAFKA_SECURITY_PROTOCOL` environment variables
   - Для локального теста используйте `spring-cloud-stream-test-binder`

### Logging

```bash
# Включить логирование SQL
-Dlogging.level.org.hibernate.SQL=DEBUG

# Включить логирование параметров
-Dlogging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Team & Maintenance

**Contact**: `@@Developers` (AAlZhirov@sberbank.ru, ADmBlandin@sberbank.ru)

**Source Control**: Git в `IdeaProjects/app_platform_dispatcher`

**Module Versions**:
- `parent`: 5.7
- `application`: 5.8
- `messaging`: 5.8
