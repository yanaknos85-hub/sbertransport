у GIGACODE.md — Правила проекта app_cargo_etrn

> **Проект:** `app_cargo_etrn` — сервис электронных транспортных накладных (ЭТрН) в контуре СберТранспорт
> **Пакет:** `ru.sber.transport.etrn` | **Порт:** `8000` (сервер.port в pom.xml) | **БД:** PostgreSQL, схема
`etrn_cargo`
> **Версия:** 1.0 (10.08.2026)

---

## Содержание

1. [Обзор](#1-обзор)
2. [Технологический стек](#2-технологический-стек)
3. [Структура проекта](#3-структура-проекта)
4. [Правила кодирования](#4-правила-кодирования)
5. [Архитектурные паттерны](#5-архитектурные-паттерны)
6. [Статусная модель](#6-статусная-модель)
7. [Тестирование](#7-тестирование)
8. [Работа с БД и миграации](#8-работа-с-бд-и-миграции)
9. [Git workflow](#9-git-workflow)
10. [Интеграции](#10-интеграции)
11. [Безопасность](#11-безопасность)
12. [Инфраструктура и деплой](#12-инфраструктура-и-деплой)
13. [Источники (SSOT)](#13-источники-ssot)

---

## 1. Обзор

Сервис управления жизненным циклом электронных транспортных накладных (ЭТрН) — от создания карточки до подписания УКЭП и
подтверждения КОРУС (оператор ЭДО).

**Ключевые инварианты:**

- Одна карточка = одна ЭТрН — цепочка Т1–Т4 не создаёт отдельных карточек
- Банк подписывает только Т3 (Титул 3) — остальные титулы внешние, read-only
- Этап завершён только после `ACCEPTED_BY_KORUS` — локальной подписи недостаточно
- Физическое удаление запрещено — после `PROCESS_COMPLETED` меняется проекция (soft-delete)
- `routeContext` — только внешний контекст — не создаёт маршрут АС СберТранспорт

---

## 2. Технологический стек

| Компонент                  | Технология                                        | Версия / Зависимость                                                   |
|----------------------------|---------------------------------------------------|------------------------------------------------------------------------|
| **Язык**                   | Java                                              | 21                                                                     |
| **Фреймворк**              | Spring Boot                                       | 3.5.12                                                                 |
| **Сборка**                 | Maven                                             | (parent: `org.springframework.boot:spring-boot-starter-parent:3.5.12`) |
| **БД**                     | PostgreSQL                                        | HikariCP, schema: `etrn_cargo`                                         |
| **Миграции**               | Liquibase                                         | `db/changelog-master.yml`                                              |
| **ORM**                    | Spring Data JPA + Hibernate                       | `ddl-auto: validate`                                                   |
| **Маппинг**                | MapStruct                                         | 1.6.3                                                                  |
| **Ломбок**                 | Lombok                                            | 1.18.38                                                                |
| **DTO**                    | Java records + `@Schema` (Swagger)                | —                                                                      |
| **Кеш / Lock**             | ShedLock                                          | 6.8.0                                                                  |
| **Kafka**                  | Spring Cloud Stream + kafka-functional            | 4.6.0.2                                                                |
| **Feign**                  | Spring Cloud OpenFeign + LoadBalancer             | —                                                                      |
| **Контейнеризация**        | jib-maven-plugin                                  | 3.4.6                                                                  |
| **Тесты**                  | JUnit 5 + Mockito + AssertJ + embedded-postgres   | JUnit 5.11, Mockito 5.x, AssertJ 3.26                                  |
| **Кодпокрытие**            | JaCoCo                                            | 0.8.13                                                                 |
| **Parent POM**             | `ru.sber.transport:java-parent`                   | 4.13.1 (через Spring Boot parent)                                      |
| **Внутренние зависимости** | transport-core, authorization, role-check-starter | 3.24, 4.10, 3.18                                                       |

---

## 3. Структура проекта

```
app_cargo_etrn/
├── pom.xml                              # parent POM
├── GIGACODE.md                          # эти правила (единый источник)
├── UNIT_TEST_GUIDELINES.md              # детальные правила тестирования
├── application/
│   ├── pom.xml                          # module POM
│   └── src/
│       ├── main/java/ru/sber/transport/etrn/
│       │   ├── EtrnApplication.java     # @Microservice + @EnableFeignClients
│       │   ├── config/                  # конфигурации (Web, Security, Lock, Scheduling)
│       │   ├── controller/              # REST API (EtrnController + impl/)
│       │   ├── database/
│       │   │   ├── dao/                 # Spring Data Repositories
│       │   │   └── model/               # JPA Entities
│       │   ├── dto/                     # Java records (request/response)
│       │   ├── enums/                   # Статусы, типы, роли
│       │   ├── exceptions/              # Бизнес-исключения + GlobalExceptionHandler
│       │   ├── mapper/                  # MapStruct мапперы
│       │   ├── messaging/               # Kafka listeners
│       │   ├── schedulers/              # Периодические задачи
│       │   ├── service/                 # Сервисы (интерфейс + impl/)
│       │   └── util/                    # Утилиты
│       ├── main/resources/
│       │   ├── application.yml          # основной конфиг
│       │   ├── application-*.yml        # профили (consul, kafka)
│       │   ├── database.yml             # БД-конфиг
│       │   ├── management.yml           # actuator
│       │   └── db/changelog/            # Liquibase миграции
│       └── test/java/ru/sber/transport/etrn/
│           ├── TestCommon.java          # общие константы и утилиты
│           └── ...                      # тесты по структуре main
├── _documents/                          # Архитектурные документы (SSOT)
│   ├── knowledge/                       # Анализ, требования, модели
│   └── contract.md                      # Контрактные документы
├── openspec/                            # OpenSpec framework
│   └── changes/                         # Спринты / изменения
└── references/                          # Ссылочные документы (выжимки)
```

**Правила пакетов:**

- Корневой пакет: `ru.sber.transport.etrn`
- Слои: `controller`, `service`, `mapper`, `database.dao`, `database.model`, `dto`, `enums`, `exceptions`, `config`,
  `messaging`, `schedulers`, `util`
- Каждый сервис — интерфейс (`EtrnService`) + реализация (`EtrnServiceImpl` в `impl/`)
- Каждый контроллер — интерфейс (`EtrnController`) + реализация (`EtrnControllerImpl` в `impl/`)
- Мапперы — отдельные классы в `mapper/` (MapStruct, `@Mapper(componentModel = "spring")`)

---

## 4. Правила кодирования

### 4.1 Naming

| Элемент          | Правило                              | Пример                                        |
|------------------|--------------------------------------|-----------------------------------------------|
| **Класс**        | PascalCase                           | `EtrnService`, `EtrnController`               |
| **Метод / поле** | camelCase                            | `getEtrnById`, `humanReadableId`              |
| **Константы**    | UPPER_SNAKE_CASE                     | `LOCK_TTL_SECONDS`                            |
| **Пакеты**       | lowercase с точкой                   | `ru.sber.transport.etrn.dto`                  |
| **DTO**          | `Record` + суффикс `Dto` / `Request` | `EtrnDetailDto`, `EtrnCreateRequest`          |
| **Enum values**  | UPPER_SNAKE_CASE                     | `READY_TO_SIGN`, `PROCESS_COMPLETED`          |
| **Логирование**  | **на русском языке**                 | `"Карточка не найдена"`                       |
| **Имена тестов** | camelCase, описательные              | `createCardWithValidData_shouldReturnCreated` |

### 4.2 DTO

- DTO — **Java records** с аннотациями `@Schema` (Swagger)
- Поля, которые могут быть пустыми — `NULLABLE` (непримитивные типы)
- Даты — `LocalDateTime`, `LocalDate`
- Денежные суммы — `BigDecimal`

```java

@Schema(title = "Карточка ЭТрН (список)")
public record EtrnCardListDto(
        @Schema(description = "UUID карточки") UUID id,
        @Schema(description = "Человекочитаемый идентификатор ЭТрН") String humanReadableId,
        @Schema(description = "Статус") String status,
        @Schema(description = "Тип титула") String titleType,
        @Schema(description = "Дата создания") LocalDateTime createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt
) {
}
```

### 4.3 Enums

- Enum-коды хранятся **inline** (без FK на справочники)
- Каждый enum — с описанием жизненного цикла
- Статусы карточки, титула, операции подписания, lock — **раздельные** enum'ы

```java

@Schema(title = "Статус карточки ЭТрН",
        description = "Жизненный цикл карточки электронной транспортной накладной")
public enum EtrnCardStatus {
    IDENTIFIED("Карточка создана, идентичность разрешена"),
    WAIT_KORUS_DATA("Ожидание входящих данных от КОРУС"),
    WAIT_CONDITIONS("Ожидание выполнения условий"),
    READY_FOR_BANK_ACTION("Готовность к действию Банка"),
    WAIT_KORUS_CONFIRMATION("Ожидание подтверждения от КОРУС"),
    PROCESS_COMPLETED("Процесс завершён");

    private final String description;
    // + getDescription()
}
```

### 4.4 Исключения

- Бизнес-исключения — именованные классы в `exceptions/`
- `@ResponseStatus` для REST-кодов
- Сообщения — **на русском языке**

```java

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EtrnNotFoundException extends RuntimeException {
    public EtrnNotFoundException(String humanReadableId) {
        super("Карточка " + humanReadableId + " не найдена");
    }
}
```

### 4.5 MapStruct

- `@Mapper(componentModel = "spring", defaultInjectionStrategy = "constructor")`
- Мапперы автоматически становятся Spring-бинами
- Для тестирования мапперов — **только unit-тесты** (без Spring контекста)
- Использовать `new MapperImpl()` для прямой инициализации

### 4.6 Lombok

- Lombok используется для `@Entity` и DTO (где уместно)
- `lombok-mapstruct-binding` для корректной интеграции MapStruct + Lombok
- `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

## 5. Архитектурные паттерны

### 5.1 Слоистая архитектура

```
Controller (REST) → Service → Mapper (MapStruct) → Repository (JPA) → Entity (DB)
                     ↘ LockService (конкурентный доступ)
                     ↘ Client (Feign) → KORUS / ЦС / Подписание
```

| Слой           | Назначение                 | Аннотация                                      |
|----------------|----------------------------|------------------------------------------------|
| **Controller** | REST API, валидация входа  | `@RestController`, `@Validated`                |
| **Service**    | Бизнес-логика, оркестрация | `@Service`, `@Transactional`                   |
| **Mapper**     | Entity ↔ DTO               | MapStruct `@Mapper(componentModel = "spring")` |
| **Repository** | Доступ к БД                | Spring Data `JpaRepository<E, UUID>`           |
| **Entity**     | JPA-сущность               | `@Entity`, Lombok                              |
| **Client**     | Внешние интеграции         | `@FeignClient` (KORUS, ЦС)                     |

### 5.2 Optimistic locking

- Версионная блокировка через поле `@Version` в Entity
- Конфликт версий → `OptimisticLockException`
- Обработка через `GlobalExceptionHandler`

### 5.3 Idempotent inbound

- Повторный вызов API не создаёт дубликатов
- Проверка по `humanReadableId` / уникальным ключам

### 5.4 Soft-delete

- Поле `active` (Boolean) в Entity
- После `PROCESS_COMPLETED` — `active = false`, данные не удаляются физически

### 5.5 Lock-механизм

- `LockService` — конкурентный доступ к карточке
- TTL — по умолчанию 300 секунд (конфигурируемо)
- Periodic auto-unlock — cron, каждые 5 минут
- `LockStatus`: `ACQUIRING`, `ACTIVE`, `LOCKED_BY_OTHER`, `EXPIRED`, `RELEASED`

---

## 6. Статусная модель

### 6.1 4 уровня статусов

| Уровень        | Enum                     | Ключевые состояния                                                                                                                        |
|----------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **card**       | `EtrnCardStatus`         | `IDENTIFIED` → `WAIT_KORUS_DATA` → `WAIT_CONDITIONS` / `READY_FOR_BANK_ACTION` → `WAIT_KORUS_CONFIRMATION` → `PROCESS_COMPLETED`          |
| **title**      | `EtrnTitleStatus`        | `EXPECTED` → `AVAILABLE`/`WAIT_CONDITIONS` → `READY_TO_SIGN` → `SIGNING` → `SIGNED_LOCALLY` → `SENT_TO_OPERATOR` → `ACCEPTED_BY_OPERATOR` |
| **signing op** | `SigningOperationStatus` | `CREATED` → `SIGNING` → `SIGNED_LOCALLY` → `SENDING_TO_KORUS` → `ACCEPTED_BY_KORUS` (+ `ERROR` / `REJECTED` / `OUTCOME_UNKNOWN`)          |
| **lock**       | `LockStatus`             | `ACQUIRING` / `ACTIVE` / `LOCKED_BY_OTHER` / `EXPIRED` / `RELEASED`                                                                       |

**Важно:** Никогда не смешивать статус карточки со статусом lock/операции.

### 6.2 Начальный статус

Стартовый статус карточки: `EtrnCardStatus.IDENTIFIED`

---

## 7. Тестирование

> **Полные правила:** `UNIT_TEST_GUIDELINES.md`

### 7.1 Unit-тесты

- `@ExtendWith(MockitoExtension.class)` — вместо `@RunWith(MockitoJUnitRunner.class)`
- `@Mock` для моков, `@Spy` для шпионов
- **Package-private** методы (`void`, без `public`)
- **AssertJ** вместо `org.junit.jupiter.api.Assertions`
- `var` для локальных переменных (где тип очевиден)
- Структура: `@DisplayName` (русский) + Arrange-Act-Assert

```java

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса карточек")
class EtrnServiceTest {

    @Mock
    private EtrnRepository repository;

    @BeforeEach
    void setUp() {
        // подготовка (опционально)
    }

    @Test
    @DisplayName("Получение карточки по идентификатору")
    void getCardById() {
        // Arrange
        var card = new Etrn();
        // ...
    }
}
```

### 7.2 Integration-тесты

- `@SpringBootTest(properties = {"spring.main.cloud-platform=none", "logger.level.root=debug"})`
- `@EmbeddedPostgres` — встроенная PostgreSQL
- `@MockitoBean` — **НЕ** `@MockBean` (устаревший)
- SQL-скрипты для данных: `src/test/resources/sql/`
- `@Transactional` — rollback после теста

### 7.3 Структура тестового класса

1. Поля класса
2. `@BeforeAll` (статические, для всей группы)
3. `@BeforeEach` (для каждого теста)
4. `@AfterEach` / `@AfterAll` (при необходимости)
5. `@Test` методы (package-private)
6. `private` вспомогательные методы (в самом конце)

### 7.4 Naming

- `@DisplayName` — **на русском языке**, описательный
- Имя метода — **camelCase (английский)**, формат: `действие_условие_результат`

```java

@Test
@DisplayName("Создание карточки с валидными данными")
void createCardWithValidData_shouldReturnCreated() { ...}

@Test
@DisplayName("Поиск МВЗ по разным вариантам названия")
void searchMvzByVariousNamePatterns() { ...}
```

### 7.5 Утверждения (AssertJ)

```java
// Основные
assertThat(result).

isNotNull();

assertThat(list).

hasSize(3);

assertThat(string).

startsWith("ЭТрН-");

// Сравнение объектов
assertThat(actual)
    .

extracting("id","status","cost")
    .

containsExactly(expectedId, expectedStatus, expectedCost);

// Исключения
assertThatExceptionOfType(EtrnNotFoundException .class)
    .

isThrownBy(() ->service.

getCard(id))
        .

withMessage("Карточка не найдена, id: "+id);

// Отсутствие исключения
assertThatCode(() ->service.

getCard(any()))
        .

doesNotThrowAnyException();

// Optional
assertThat(optionalValue).

isPresent();

assertThat(optionalValue).

hasValueSatisfying(value ->

assertThat(value).

isEqualTo(expected));
```

### 7.6 Мокирование

```java
// Фиксированный возврат
when(repository.findById(any()))
        .

thenReturn(Optional.of(card));

// Возврат по условию
when(geoDataResolver.getAddress(eq(addressString)))
        .

thenReturn(Optional.of(addressInfo));

// Исключение
when(repository.findById(any()))
        .

thenThrow(new EtrnNotFoundException(id));
```

---

## 8. Работа с БД и миграации

### 8.1 JPA

- `ddl-auto: validate` — только для production (не менять!)
- batch_size: 25 (в `database.yml`)
- HikariCP, min-idle: 3

### 8.2 Liquibase

- Мастер-чangelog: `db/changelog-master.yml`
- Чangelogs группируются по датам: `db/changelog/YYYYMMDD/`
- Внутри каждого — `changelog.yml` + `change/001-*.sql`

```
db/changelog/
├── 20260726/
│   ├── changelog.yml
│   └── change/001-create-etrn.sql
└── 20260804/
    ├── changelog.yml
    └── change/001-drop-currenttitle-column.sql
```

### 8.3 SQL для тестов

- Файлы: `src/test/resources/sql/*.sql`
- Загрузка: `@Sql(scripts = "/sql/filename.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)`
- Использовать **фиксированные даты** вместо `now()` (для repeatable-тестов)
- `@Transactional` для автоматического rollback

---

## 9. Git workflow

### 9.1 OpenSpec workflow

- Изменения через `openspec/changes/`
- Каждое изменение — отдельная папка (спринт)
- Любое изменение модели данных → фиксировать в `openspec/changes/`
- Архив завершённых изменений: `openspec/changes/archive/`

### 9.2 Коммиты

- Сообщения — на русском языке
- Формат: `тип: краткое описание`
- Типы: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`

### 9.3 Изменение модели данных

Любое изменение сущностей/миграций → обновлять:

- `openspec/changes/` — delta spec
- `_documents/knowledge/data-model.md` (SSOT по данным)

---

## 10. Интеграции

| Система                 | Канал               | Направление | Данные                                |
|-------------------------|---------------------|-------------|---------------------------------------|
| **ЦС (Центр Сервисов)** | REST (inbound)      | ←           | `POST /cs/title3`                     |
| **КОРУС**               | REST (outbound)     | →           | FETCH_ETRN_STATE, отправка на подпись |
| **Сервис подписания**   | REST (outbound)     | →           | УКЭП-подписание                       |
| **Kafka (организации)** | Spring Cloud Stream | ←           | `service.organization.*` topics       |

### 10.1 Feign Client

- `@EnableFeignClients` на `EtrnApplication`
- Клиенты для внешних сервисов — отдельные интерфейы с `@FeignClient`
- Конфигурация timeout/retry — через `application.yml`

### 10.2 Kafka

- Topics: `service.organization`, `service.organization.employee`, `service.organization.department`,
  `service.organization.position`
- DLQ enabled
- JSON serializer/deserializer

---

## 11. Безопасность

- **OAuth2 Resource Server** (RS512, JWKS)
- `@Microservice` — кастомная аннотация на `EtrnApplication`
- `@EnableFeignClients` — межсервисные вызовы
- Health endpoints — `@NoAuthorize("/monitoring")`
- Тесты: `@WithMockUser` / `mockUser()` для имитации аутентифицированных запросов

### 11.1 JWT-авторизация в тестах

```java
mockMvc.perform(post("/api/endpoint")
        .

with(jwt().

jwt(builder ->builder.

jti(USER_ID)))
        .

contentType(MediaType.APPLICATION_JSON)
        .

content(content))
        .

andExpect(status().

isOk());
```

---

## 12. Инфраструктура и деплой

### 12.1 Docker

Через `jib-maven-plugin`:

```bash
mvn compile jib:build -Djib.to.auth.username=... -Djib.to.auth.password=...
```

- Image tag: `${project.version}-${username}`
- Base image: `registry-ci.delta.sbrf.ru/.../open-jdk:21.0.3`
- JVM flags: `-server`, `-XX:+UseG1GC`, `-XX:MaxRAMPercentage=75`
- Config location: `optional:file:/app/resources/,optional:file:/app/resources/config/`

### 12.2 Config

- `application.yml` — основной конфиг
- `application-*.yml` — профили (`consul`, `kafka`)
- `database.yml` — БД-конфиг (отдельно)
- `management.yml` — actuator endpoints

### 12.3 Scheduling

- ShedLock — распределённая блокировка задач
- `SchedulerLockConfiguration` + `SchedulingConfiguration`
- Auto-unlock cron: каждые 5 минут

---

## 13. Источники (SSOT)

| Документ                 | Расположение                                                       | Назначение                                    |
|--------------------------|--------------------------------------------------------------------|-----------------------------------------------|
| **Эти правила**          | `GIGACODE.md` (корень)                                             | Стек, код-стайл, тесты, архитектура, workflow |
| **Unit-тесты**           | `UNIT_TEST_GUIDELINES.md` (корень)                                 | Детальные правила тестирования                |
| **Архитектура**          | `_documents/knowledge/init/knowledge/architecture.md`              | API-контракты, слои, компоненты               |
| **Модель данных**        | `_documents/knowledge/init/knowledge/data-model.md`                | 28+ сущностей, ER-диаграмма — SSOT по данным  |
| **Терминология**         | `_documents/knowledge/init/knowledge/glossary.md`                  | Доменный словарь                              |
| **Предположения**        | `_documents/knowledge/init/knowledge/assumptions.md`               | 24+ инвариантов (A-01..A-24)                  |
| **Риски**                | `_documents/knowledge/init/knowledge/risks.md`                     | Production gates (G-01..G-04)                 |
| **Open questions**       | `_documents/knowledge/init/knowledge/questions.md`                 | Q-01..Q-08                                    |
| **Непротиворечия**       | `_documents/knowledge/init/knowledge/inconsistencies.md`           | SOLID-нарушения (S-01..S-05)                  |
| **Бизнес-требования**    | `_documents/knowledge/init/references/01-business-requirements.md` | BR-01..BR-09                                  |
| **Системные требования** | `_documents/knowledge/init/references/02-system-requirements.md`   | I-01..I-09                                    |

> **Правило:** При любых изменениях модели данных или архитектуры обновлять соответствующие документы в
`_documents/knowledge/`.
