# Архитектура сервиса ЭТрН (etrn-service)

> **Статус:** Черновик проектирования
> **Версия:** 1.0 (21.07.2026)
> **Порт:** `8227`
> **БД:** PostgreSQL, схема `etrn`
> **Пакет:** `ru.sber.transport.etrn`

---

## 1. Maven-структура проекта

```
app_cargo_etrn/
├── pom.xml                              # parent POM
├── application/
│   ├── pom.xml                          # module POM (jar, Java 21)
│   └── src/
│       ├── main/
│       │   ├── java/ru/sber/transport/etrn/
│       │   │   ├── EtrnApplication.java               # @Microservice + @EnableFeignClients
│       │   │   ├── config/
│       │   │   │   ├── WebConfiguration.java           # конвертеры дат
│       │   │   │   └── SecurityConfiguration.java      # OAuth2 RS512
│       │   │   ├── controller/
│       │   │   │   ├── EtrnCardController.java         # REST-интерфейс
│       │   │   │   └── impl/EtrnCardControllerImpl.java
│       │   │   ├── database/
│       │   │   │   ├── dao/
│       │   │   │   │   ├── EtrnCardRepository.java
│       │   │   │   │   └── EtrnCardAuditRepository.java
│       │   │   │   └── model/
│       │   │   │       ├── EtrnCard.java
│       │   │   │       └── EtrnCardAudit.java
│       │   │   ├── dto/
│       │   │   │   ├── EtrnCardListDto.java            # список (9 полей)
│       │   │   │   ├── EtrnCardDetailDto.java          # детально (28 полей)
│       │   │   │   └── EtrnCardCreateRequest.java      # создание (минимально)
│       │   │   ├── enums/
│       │   │   │   ├── EtrnCardStatus.java           # 7 статусов: IDENTIFIED..PROCESS_COMPLETED
│       │   │   │   ├── EtrnTitleStatus.java          # 9 статусов: EXPECTED..REJECTED
│       │   │   │   ├── SigningOperationStatus.java   # 8 статусов: CREATED..ERROR
│       │   │   │   └── LockStatus.java               # 5 статусов: ACQUIRING..RELEASED
│       │   │   ├── exception/
│       │   │   │   ├── BadRequestException.java
│       │   │   │   └── CardNotFoundException.java
│       │   │   ├── mappers/
│       │   │   │   └── EtrnCardMapper.java             # MapStruct
│       │   │   ├── service/
│       │   │   │   ├── EtrnCardService.java            # интерфейс
│       │   │   │   └── impl/EtrnCardServiceImpl.java
│       │   │   └── messaging/
│       │   │       └── ...
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/
│       │           └── changelog-master.yml
│       └── test/
│           └── java/ru/sber/transport/etrn/
└── _documents/                              # helm / миграции / cluster
```

---

## 2. Слои и зависимости

```
Controller (REST) → Service → Mapper (MapStruct) → Repository (JPA) → Entity (DB)
                         ↘ Client (Feign) → KORUS / ЦС
```

| Слой | Назначение | Технология |
|------|-----------|-----------|
| **Controller** | REST API, валидация входа | `@RestController`, `@Validated` |
| **Service** | Бизнес-логика, оркестрация | `@Service`, `@Transactional` |
| **Mapper** | Entity ↔ DTO | MapStruct `@Mapper(componentModel = "spring")` |
| **Repository** | Доступ к БД | Spring Data `JpaRepository<E, UUID>` |
| **Entity** | JPA-сущность | `@Entity`, Lombok |
| **Client** | Внешние интеграции | `@FeignClient` (KORUS, ЦС) |

---

## 3. API-контракты (REST)

### 3.1. POST /api/v1/etrn/cards — создать карточку

**Request (EtrnCardCreateRequest):**
```json
{
  "humanReadableId": "ЭТрН-772812",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "timeZone": "Asia/Yekaterinburg"
}
```

**Response (201) — EtrnCardListDto:**
```json
{
  "id": "uuid",
  "humanReadableId": "ЭТрН-772812",
  "status": "READY_TO_SIGN",
  "titleType": "T3",
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-21T10:00:00"
}
```

### 3.2. GET /api/v1/etrn/cards — список

**Query params:** `status`, `createdAtFrom`, `createdAtTo`, `page`, `size`

**Response:** `Page<EtrnCardListDto>` — 6 полей на строку.

### 3.3. GET /api/v1/etrn/cards/{id} — детальная

**Response (200) — EtrnCardDetailDto:**
```json
{
  "id": "uuid",
  "humanReadableId": "ЭТрН-772812",
  "applicationNumber": "OT-0001-0002931",
  "routeNumber": "CT-0001-0002931",
  "status": "READY_TO_SIGN",
  "timeZone": "Asia/Yekaterinburg",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "sla": "00:42",
  "titleType": "T3",
  "cargoDescription": "Оборудование, 5 мест",
  "cargoPlaces": 5,
  "cargoWeightKg": 1250.00,
  "route": "Екатеринбург — Москва",
  "mrpaExpiresAt": "2026-07-31",
  "cargoLength": 120.0,
  "cargoWidth": 80.0,
  "cargoHeight": 100.0,
  "sesFullName": "Иванов Иван Иванович",
  "sesRole": "Генеральный директор",
  "sesEventDatetime": "2026-07-21T10:00:00",
  "sesEventId": "PE-001",
  "titleChain": [{"title": "T1", "role": "SENDER", "status": "SIGNED"}],
  "verifications": [{"name": "Формат ЭТрН (XML)", "passed": true}],
  "active": true,
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-21T10:00:00"
}
```

### 3.4. PATCH /api/v1/etrn/cards/{id}/status

```json
{"status": "SIGNED"}
```

---

## 4. DTO-классы (Java record)

```java
@Schema(title = "Карточка ЭТрН (список)")
public record EtrnCardListDto(
    @Schema(description = "UUID карточки")
    UUID id,
    @Schema(description = "Человекочитаемый идентификатор ЭТрН")
    String humanReadableId,
    @Schema(description = "Статус")
    String status,
    @Schema(description = "Тип титула")
    String titleType,
    @Schema(description = "Дата создания")
    LocalDateTime createdAt,
    @Schema(description = "Дата обновления")
    LocalDateTime updatedAt
) {}
```

```java
@Schema(title = "Карточка ЭТрН (детально)")
public record EtrnCardDetailDto(
    UUID id,
    String humanReadableId,
    String applicationNumber,    // NULLABLE
    String routeNumber,          // NULLABLE
    String status,
    String timeZone,
    String senderName,           // NULLABLE
    String receiverName,         // NULLABLE
    String carrierName,          // NULLABLE
    String sla,                  // NULLABLE
    String titleType,
    String cargoDescription,     // NULLABLE
    Integer cargoPlaces,         // NULLABLE
    BigDecimal cargoWeightKg,    // NULLABLE
    String route,                // NULLABLE
    LocalDate mrpaExpiresAt,       // NULLABLE
    Double cargoLength,          // NULLABLE
    Double cargoWidth,           // NULLABLE
    Double cargoHeight,         // NULLABLE
    String sesFullName,          // NULLABLE
    String sesRole,              // NULLABLE
    LocalDateTime sesEventDatetime, // NULLABLE
    String sesEventId,          // NULLABLE
    String titleChain,          // JSONB
    String verifications,        // JSONB
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

---

## 5. Enum-классы

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
    // + getDescription()
}

@Schema(title = "Статус титула ЭТрН",
        description = "Жизненный цикл банковского титула (Т3)")
public enum EtrnTitleStatus {
    EXPECTED, AVAILABLE, WAIT_CONDITIONS, READY_TO_SIGN,
    SIGNING, SIGNED_LOCALLY, SENT_TO_OPERATOR, ACCEPTED_BY_OPERATOR, REJECTED
}

@Schema(title = "Статус операции подписания",
        description = "Жизненный цикл УКЭП-подписания ЭТрН")
public enum SigningOperationStatus {
    CREATED, SIGNING, SIGNED_LOCALLY, SENDING_TO_KORUS,
    ACCEPTED_BY_KORUS, REJECTED_BY_KORUS, OUTCOME_UNKNOWN, ERROR
}

@Schema(title = "Статус блокировки",
        description = "Технический статус управления конкурентным доступом")
public enum LockStatus {
    ACQUIRING, ACTIVE, LOCKED_BY_OTHER, EXPIRED, RELEASED
}
```

**Начальный статус карточки:** `EtrnCardStatus.IDENTIFIED`

**Логи:** русский язык.
```java
"Карточка " + humanReadableId + " не найдена"
"Некорректный статус: " + status
```

---

## 6. POM-зависимости (ключевые)

**Parent POM:**
```xml
<parent>
    <groupId>ru.sber.transport</groupId>
    <artifactId>java-parent</artifactId>
    <version>4.13.1</version>
</parent>
```

**Module POM (application):**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator`
- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-loadbalancer`
- `spring-cloud-config-client`
- `micrometer-tracing` + `micrometer-tracing-bridge-brave`
- `postgresql` + `liquibase-core` + `HikariCP`
- `lombok` (provided)
- `mapstruct` + `mapstruct-processor`
- `jackson-datatype-jsr310`
- `embedded-postgres` (test)
- `spring-security-test` (test)
- `spring-boot-starter-test` (test)

**Плагины:** `jib-maven-plugin`, `spring-boot-maven-plugin`, `jacoco`

---

## 7. Интеграции

| Система | Канал | Данные |
|---------|-------|--------|
| **ЦС** | REST (inbound) | `POST /cs/title3` |
| **КОРУС** | REST (outbound) | FETCH_ETRN_STATE |
| **Сервис подписания** | REST (outbound) | УКЭП |

---

## 8. Безопасность

- OAuth2 Resource Server (RS512, JWKS)
- `@Microservice` — кастомная аннотация
- `@EnableFeignClients` — межсервисные вызовы
- `@NoAuthorize("/monitoring")` — health endpoints

---

## 9. Error-handling

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String humanReadableId) {
        super("Карточка " + humanReadableId + " не найдена");
    }
}
```

---

## 10. Аудит

- `etrn_audit` — история действий
- Запись при каждом изменении статуса
- Партиционирование по `created_at` (месяц/квартал)

---

## 11. Docker

Через `jib-maven-plugin`.

---

## 12. Связь с BE (EPIC-00)

| BE | Описание | Архитектурное покрытие |
|----|----------|----------------------|
| BE-01 | Создание карточки | Controller → Service → Repository |
| BE-02 | Статусная модель | Entity + enum `CardStatus` |
| BE-03 | Рабочая очередь | GET /cards + фильтрация по статусу |
| BE-04 | Аудит | `etrn_audit` |
| BE-05 | Интеграция ЦС | inbound REST |
| BE-06 | Интеграция КОРУС | Feign Client |
| BE-07 | Валидация | Service layer |
| BE-08 | = BE-01 (дубль) | — |
| BE-09 | Lock | заглушка |
| BE-10 | Подписание | Feign КОРУС |
| BE-11 | Ошибки | Error-handling |
| BE-12 | Журнал событий | Audit + Event Log |
| BE-13 | Reconciliation | TODO |
| BE-14 | Мониторинг | Actuator + metrics |