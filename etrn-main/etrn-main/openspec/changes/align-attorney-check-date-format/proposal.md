## Why

В сервисе ЭТрН даты в DTO-ответах проверки доверенности (`AttorneyCheckResponseDto`, `DispatcherDto`) сериализуются неконсистентно: `AttorneyCheckResponseDto` использует значение по умолчанию Jackson (timestamps из-за `write_dates_as_timestamps: true`), а `DispatcherDto` — `dd.MM.yyyy`. Это расхождения с форматом ISO `yyyy-MM-dd`, принятым в `as-sbertransport-cargoGIGA` (см. `GetRequestDto.approvalDate`). Приведение к единому формату устранит двусмысленность парсинга и обеспечит согласованность с экосистемой СберТранспорта.

## What Changes

- Создать кастомные `LocalDateSerializer` и `LocalDateDeserializer` в пакете `config.converters`, формирующие строку формата `yyyy-MM-dd` (аналогично существующему `IsoLocalDateTimeSerializer`)
- Заменить `@JsonFormat(pattern = "dd.MM.yyyy")` в `DispatcherDto` на `@JsonSerialize`/`@JsonDeserialize` с новыми конвертерами
- Добавить `@JsonSerialize`/`@JsonDeserialize` в `AttorneyCheckResponseDto` для полей `issueDate` и `expiryDate`
- Обновить Swagger-`example` значения на ISO-формат (`"2025-01-15"`) в обеих DTO
- Изменить формат Swagger-описаний для полей дат на ISO-совместимый

## Capabilities

### New Capabilities
- `date-format-converter`: кастомные Jackson `@JsonSerialize`/`@JsonDeserialize` для `LocalDate` с ISO-форматом `yyyy-MM-dd`, используемые в API-ответах для согласования с `as-sbertransport-cargoGIGA`

### Modified Capabilities
- *(none — это изменение реализации, требования API (строковый формат дат) не меняются на семантическом уровне)*

## Impact

- `application/src/main/java/ru/sber/transport/etrn/config/converters/` — новые файлы `LocalDateSerializer.java`, `LocalDateDeserializer.java`
- `application/src/main/java/ru/sber/transport/etrn/dto/AttorneyCheckResponseDto.java` — добавление аннотаций сериализации
- `application/src/main/java/ru/sber/transport/etrn/dto/DispatcherDto.java` — замена `@JsonFormat` на `@JsonSerialize`/`@JsonDeserialize`
- Тесты на контроллер/десериализацию (если есть) для эндпоинта `attorneyCheck`
