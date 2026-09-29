## Context

Сервис `app_cargo_etrn` (порт 8227) использует Spring Boot с Jackson для сериализации API-ответов. В `application.yml` установлена опция `write_dates_as_timestamps: true`, что приводит к сериализации `LocalDate`/`LocalDateTime` как числовых timestamp-ов по умолчанию.

Текущее состояние дат в DTO:
- `AttorneyCheckResponseDto` — **без аннотаций**, даты сериализуются как timestamp (числа)
- `DispatcherDto` — `@JsonFormat(pattern = "dd.MM.yyyy")` (формат RU)

В экосистеме `as-sbertransport-cargoGIGA` используется единый паттерн: `@JsonSerialize(using = LocalDateSerializer.class)` + `@JsonDeserialize(using = LocalDateDeserializer.class)` из библиотеки `ru.sberbank.ditsib.converters`, где формат — ISO `yyyy-MM-dd`.

Проект `app_cargo_etrn` не имеет зависимости `ditsib-converters`, поэтому требуется создание локальных конвертеров в `config.converters`.

## Goals / Non-Goals

**Goals:**
- Создать локальные `LocalDateSerializer` и `LocalDateDeserializer` с форматом `yyyy-MM-dd`
- Привести `AttorneyCheckResponseDto` и `DispatcherDto` к единому паттерну сериализации
- Обновить Swagger-описания на ISO-совместимые примеры

**Non-Goals:**
- Изменение формата для других DTO вне attorneyCheck-контура
- Подключение внешней библиотеки `ditsib-converters`
- Изменение бизнес-логики `AttorneyCheckServiceImpl`

## Decisions

### Decision 1: Локальные конвертеры вместо внешней зависимости
**Выбор:** Создать `LocalDateSerializer` и `LocalDateDeserializer` в пакете `ru.sber.transport.etrn.config.converters`.
**Рациона:** Проект не зависит от `ru.sberbank.ditsib.converters`. Добавление новой зависимости ради двух классов прецедента не стоит. Существующий паттерн `IsoLocalDateTimeSerializer` показывает, что в проекте приняты локальные конвертеры.

### Decision 2: Формат `yyyy-MM-dd` (ISO_LOCAL_DATE)
**Выбор:** Использовать `DateTimeFormatter.ISO_LOCAL_DATE`.
**Рationale:** Согласуется с `as-sbertransport-cargoGIGA` и общепринятым ISO 8601. Лучше для сравнения, парсинга и международных интеграций.

### Decision 3: Замена `@JsonFormat` на `@JsonSerialize`/`@JsonDeserialize`
**Выбор:** Заменить `@JsonFormat(pattern = "dd.MM.yyyy")` в `DispatcherDto` на `@JsonSerialize(using = LocalDateSerializer.class)` + `@JsonDeserialize(using = LocalDateDeserializer.class)`.
**Rationale:** `@JsonFormat` работает, но кастомные сериализаторы дают больше контроля (например, поддержка `Converter` для Spring MVC `@RequestParam`) и единый паттерн во всём проекте.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Ломает клиентов, ожидающих `dd.MM.yyyy` от `DispatcherDto` | `DispatcherDto` — ответ от Feign-клиента (входящий), формат не меняем для incoming — только `AttorneyCheckResponseDto` (outgoing). `issueDate`/`expiryDate` в `DispatcherDto` при incoming Feign — `@JsonFormat` можно оставить как есть, но для консистентности заменить. Если клиенты парсят `dd.MM.yyyy` — добавить `@JsonFormat` и на new converter. |
| Ломает клиентов, ожидающих timestamps от `AttorneyCheckResponseDto` | Текущий формат (timestamp) точно ломает клиентов, ожидающих строку. ISO — стандарт. |

## Migration Plan

1. Создать `LocalDateSerializer.java` и `LocalDateDeserializer.java` в `config/converters/`
2. Заменить `@JsonFormat` в `DispatcherDto` на `@JsonSerialize`/`@JsonDeserialize`
3. Добавить `@JsonSerialize`/`@JsonDeserialize` в `AttorneyCheckResponseDto`
4. Обновить `example` в `@Schema` на `"2025-01-15"`
5. Прописать unit-тесты для новых конвертеров

## Open Questions

- **OQ-01**: Нужно ли заменить `@JsonFormat` в `DispatcherDto` (входящий DTO от Feign)? Если dispatcher-service возвращает даты в формате `dd.MM.yyyy`, то замена на ISO-конвертеры сломает десериализацию. Рекомендуется оставить `@JsonFormat` для входящих DTO или изменить формат на dispatcher-сервисе.
