## 1. Создать Jackson-конвертеры для LocalDate

- [ ] 1.1 Создать `LocalDateSerializer` в `ru.sber.transport.etrn.config.converters` с форматом `yyyy-MM-dd` через `DateTimeFormatter.ISO_LOCAL_DATE`
- [ ] 1.2 Создать `LocalDateDeserializer` в `ru.sber.transport.etrn.config.converters` с парсингом через `DateTimeFormatter.ISO_LOCAL_DATE`
- [ ] 1.3 Добавить поддержку `Converter<LocalDate, String>` для совместимости с Spring `@RequestParam` (по аналогии с `IsoLocalDateTimeSerializer`)

## 2. Обновить DTO

- [ ] 2.1 Заменить `@JsonFormat(pattern = "dd.MM.yyyy")` в `DispatcherDto` на `@JsonSerialize(using = LocalDateSerializer.class)` + `@JsonDeserialize(using = LocalDateDeserializer.class)` для `issueDate` и `expiryDate`
- [ ] 2.2 Добавить `@JsonSerialize`/`@JsonDeserialize` аннотации в `AttorneyCheckResponseDto` для полей `issueDate` и `expiryDate`
- [ ] 2.3 Обновить `example` в `@Schema` на `"2025-01-15"` для всех полей дат в обоих DTO

## 3. Тестирование

- [ ] 3.1 Написать unit-тесты для `LocalDateSerializer` (валидная дата, минимальная, максимальная)
- [ ] 3.2 Написать unit-тесты для `LocalDateDeserializer` (валидная строка, невалидная строка)
- [ ] 3.3 Написать integration-тест для эндпоинта `attorneyCheck`: убедиться, что `issueDate`/`expiryDate` возвращаются в формате `yyyy-MM-dd`

## 4. Финализация

- [ ] 4.1 Проверить сборку проекта (`mvn compile` / `mvn verify`)
- [ ] 4.2 Обновить CHANGELOG / commit message
