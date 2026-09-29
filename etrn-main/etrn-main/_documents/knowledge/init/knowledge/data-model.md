# Модель данных сервиса ЭТрН (etrn-service)

> **Статус:** Единственный источник истины по атрибутивному составу.
> **Версия:** 1.0 (финальная, 21.07.2026)
> **Пакет:** `ru.sber.transport.etrn`
> **Порт:** `8227`

---

## 1. Базовые конвенции

| Слой | Технология | Стиль |
|------|-----------|-------|
| DTO | Java **record** | `@Schema` (Swagger), без Lombok |
| Entity | `@Entity` + `@Table(schema = "etrn")` | `@Data` / `@Builder` / `@NoArgsConstructor` / `@AllArgsConstructor` |
| Репозиторий | Spring Data `JpaRepository<E, UUID>` | `@Repository` |
| Маппер | **MapStruct** (`@Mapper(componentModel = "spring")`) | `@Lookup` для бинов |
| Сервис | **Lombok** (`@Slf4j`, `@RequiredArgsConstructor`, `@Service`) | `@Transactional(readOnly = true)` |
| БД | PostgreSQL, HikariCP, Liquibase | Схема `etrn` |
| Трейсинг | Micrometer Tracing + Brave | опционально |
| Безопасность | OAuth2 Resource Server (RS512) | `@EnableFeignClients`, JWKS |
| Error-handling | `@ResponseStatus` на исключениях | `class BadRequestException extends RuntimeException` |
| Kafka | Опционально (Stage 1 — REST/Feign) | |

---

## 2. Каталог сущностей (28+)

### 2.1. `etrn` — Карточка ЭТрН (корень)

| Поле | Тип JPA | Тип БД | Обязательность | Ограничения |
|------|---------|--------|---------------|-------------|
| `id` | `UUID` | `UUID` (PK) | REQUIRED | генерируется |
| `humanReadableId` | `String` | `VARCHAR(100)` | REQUIRED | `UNIQUE`, формат `ЭТрН-772812` |
| `applicationNumber` | `String` | `VARCHAR(32)` | NULLABLE | |
| `routeNumber` | `String` | `VARCHAR(32)` | NULLABLE | |
| `status` | `String` | `VARCHAR(64)` | NULLABLE | inline enum |
| `timeZone` | `String` | `VARCHAR(64)` | NULLABLE | IANA |
| `senderName` | `String` | `VARCHAR(256)` | NULLABLE | |
| `receiverName` | `String` | `VARCHAR(256)` | NULLABLE | |
| `carrierName` | `String` | `VARCHAR(256)` | NULLABLE | |
| `sla` | `String` | `VARCHAR(16)` | NULLABLE | |
| `titleType` | `String` | `VARCHAR(16)` | NULLABLE | T3 |
| `cargoDescription` | `String` | `VARCHAR(512)` | NULLABLE | |
| `cargoPlaces` | `Integer` | `INT` | NULLABLE | |
| `cargoWeightKg` | `BigDecimal` | `NUMERIC(10,2)` | NULLABLE | |
| `route` | `String` | `VARCHAR(512)` | NULLABLE | |
| `mrpaExpiresAt` | `LocalDate` | `DATE` | NULLABLE | |
| `cargoLength` | `Double` | `DOUBLE PRECISION` | NULLABLE | см |
| `cargoWidth` | `Double` | `DOUBLE PRECISION` | NULLABLE | см |
| `cargoHeight` | `Double` | `DOUBLE PRECISION` | NULLABLE | см |
| `sesFullName` | `String` | `VARCHAR(256)` | NULLABLE | |
| `sesRole` | `String` | `VARCHAR(128)` | NULLABLE | |
| `sesEventDatetime` | `LocalDateTime` | `TIMESTAMP WITHOUT TZ` | NULLABLE | |
| `sesEventId` | `String` | `VARCHAR(64)` | NULLABLE | |
| `titleChain` | `String` | `JSONB` | NULLABLE | |
| `verifications` | `String` | `JSONB` | NULLABLE | |
| `active` | `Boolean` | `BOOLEAN` | DEFAULT true | soft-delete |
| `createdAt` | `LocalDateTime` | `TIMESTAMP WITHOUT TZ` | REQUIRED | |
| `updatedAt` | `LocalDateTime` | `TIMESTAMP WITHOUT TZ` | REQUIRED | |

**Индексы:**
```sql
UNIQUE (humanreadableid);
INDEX (status, active);
INDEX (created_at);
```

### 2.2. `etrn_audit` — История действий

| Поле | Тип JPA | Тип БД                 | Обязательность |
|------|---------|------------------------|---------------|
| `id` | `UUID` | `UUID` (PK)            | REQUIRED |
| `etrnId` | `UUID` | `UUID` (FK → etrn.id)  | REQUIRED |
| `action` | `String` | `VARCHAR(512)`         | REQUIRED |
| `createdAt` | `LocalDateTime` | `TIMESTAMP WITHOUT TZ` | NULLABLE |
| `details` | `String` | `JSONB`                | NULLABLE |

**Индекс:**
```sql
INDEX (etrn_id, created_at DESC);
```

**Примеры `action`:** `ЭТрН создана`, `Подписан УКЭП`, `Подписан Титул 1`

---

## 3. JSONB-структуры

### `title_chain` — Цепочка титулов (4 элемента: T1–T4)

```json
{"title": "T1", "signedAt": "2026-07-22T10:00:00", "signedBy": "Иванов И.И."}
```

| Поле | Тип | Значения |
|------|-----|---------|
| `title` | `String` | `T1`, `T2`, `T3`, `T4` |
| `signedAt` | `LocalDateTime` | `ISO_LOCAL_DATE_TIME`, NULL если не подписан |
| `signedBy` | `String` | Наименование подписанта из КОРУС, NULL |

### `verifications` — Проверки

```json
[{"name": "Формат ЭТрН (XML)", "passed": true}]
```

| Поле | Тип | Значения |
|------|-----|---------|
| `name` | `String` | Название проверки |
| `passed` | `Boolean` | `true` / `false` |

---

## 4. Расхождения со смежными документами

| Документ                             | Расхождение                                | Фикс в data-model.md                                                                                                                                                                                                                |
|--------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `references/03-data-model.md`        | 28 сущностей vs 2 в data-model.md          | **Разделение**: `data-model.md` — core (`etrn`, `etrn_audit`), `references/03-data-model.md` — 28 сущностей полного контура                                                                                                         |
| `references/05-state-model.md`       | `status` — enum vs `status VARCHAR(64)`    | Inline-строка без FK                                                                                                                                                                                                                |
| `references/04-processes.md`         | Нет `reconciliation` для `OUTCOME_UNKNOWN` | **Закрыто** (change `remove-redundant-etrn-card-statuses`): статус `OUTCOME_UNKNOWN` на уровне карточки удалён, reconciliation на уровне карточки не требуется; неопределённый исход фиксируется на уровне `SigningOperationStatus` |
| `references/06-base-contour-epic.md` | `BE-08` = `BE-01`                          | Дубль, зафиксирован в `inconsistencies.md`                                                                                                                                                                                          |

---

## 5. ER-диаграмма

```
┌──────────────────────────────────────────────┐
│              etrn                            │
├──────────────────────────────────────────────┤
│ id                      UUID PK              │
│ humanreadableid       VARCHAR(100) UNIQUE   │
│ application_number      VARCHAR(32)           │
│ route_number            VARCHAR(32)           │
│ status                  VARCHAR(64)           │— inline enum
│ time_zone               VARCHAR(64)           │
│ sender_name             VARCHAR(256)          │
│ receiver_name           VARCHAR(256)          │
│ carrier_name            VARCHAR(256)          │
│ sla                     VARCHAR(16)           │
│ title_type              VARCHAR(16)           │
│ cargo_description       VARCHAR(512)          │
│ cargo_places            INT                   │
│ cargo_weight_kg         NUMERIC(10,2)         │
│ route                   VARCHAR(512)          │
│ mrpa_expires_at         DATE                  │
│ cargo_length            DOUBLE PRECISION      │
│ cargo_width             DOUBLE PRECISION      │
│ cargo_height            DOUBLE PRECISION      │
│ ses_full_name           VARCHAR(256)          │
│ ses_role                VARCHAR(128)          │
│ ses_event_datetime      TIMESTAMP WITHOUT TZ  │
│ ses_event_id            VARCHAR(64)           │
│ title_chain             JSONB                 │
│ verifications           JSONB                 │
│ active                  BOOLEAN DEFAULT true   │
│ created_at              TIMESTAMP WITHOUT TZ  │
│ updated_at              TIMESTAMP WITHOUT TZ  │
└──────────────────────┬───────────────────────┘
                       │ 1:N
                       ▼
┌──────────────────────────────────────────────┐
│           etrn_audit                         │
├──────────────────────────────────────────────┤
│ id                  UUID PK                  │
│ etrn_id        UUID FK                       │
│ action              VARCHAR(512)             │
│ created_at          TIMESTAMP WITHOUT TZ     │
│ details             JSONB                    │
└──────────────────────────────────────────────┘
```

---

## 6. Примечания

1. **`time_zone`** — одно поле на бизнес-таблицу, IANA name. Применяется фронтом к значимым датам. `created_at` / `updated_at` — без конвертации.
2. **`humanreadableid`** — уникальный, `ЭТрН-772812`. Аналог `humanReadableId` из смежных сервисов.
3. **`status`** — inline-строка, код из enum. Без FK на справочники.
4. **`title_chain[].status`** — inline: `NOT_STARTED`, `SIGNED`, `REJECTED`, `EXPIRED`.
5. **`verifications[].passed`** — boolean, без справочника.
6. **`signed_at`** — строка `YYYY-MM-DD HH24:MI:SS` без timezone.
7. **`active`** — soft-delete, `BOOLEAN DEFAULT true`.
8. **Габариты** — `cargo_length / cargo_width / cargo_height`, `DOUBLE PRECISION`, см.
9. **Аудит** — `etrn_card_audit` растёт линейно → партиционирование по `created_at` (месяц/квартал).
10. **PDF-документы** на текущем этапе не предусмотрены.
11. **Кэш Caffeine не используется** — инфраструктурный кэш не нужен, бизнес-слой без повторяемых чтений.