# Tasks: TRANSPORT-44459 Fix millisecond timestamp handling

## Task 1: ms → seconds конвертация в CoordinateMapper

**Файл:** `application/src/main/java/ru/sber/transport/driver_track/mapper/CoordinateMapper.java`

**Действия:**
- В `mapTimestamp()` извлечь ms число: `long millis = instant.getEpochSecond()`
- Конвертировать: `Instant.ofEpochMilli(millis)`
- Вернуть: `LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)`

**Критерий готовности:** `1787295660000` → `2026-07-21 15:01:00`

---

## Task 2: Обновление spec batch-coordinate-api.md

**Файл:** `openspec/specs/batch-coordinate-api.md`

**Действия:**
- Убрать правила валидации диапазона (10 лет / 5 лет)
- Убрать правила про ms → seconds конвертацию
- Добавить пример request с unix ms timestamp
- Указать: `timestamp` — ISO-8601 string или unix number (ms)
- Business Rule: `unix timestamp обрабатывается напрямую`

**Критерий готовности:** Spec отражает фактическое поведение кода

---

## Task 3: Unit tests

**Файл:** `application/src/test/java/ru/sber/transport/driver_track/mapper/CoordinateMapperTest.java`

**Тесты:**
- [ ] `mapTimestamp()` с ms timestamp (1787295660000) → 2026-07-21 15:01:00
- [ ] `mapTimestamp()` с `null` → `null`
- [ ] `mapTimestamp()` с ISO-8601 → корректно

**Критерий готовности:** Все тесты проходят

---

## Task 4: Integration tests

**Файл:** `application/src/test/java/ru/sber/transport/driver_track/controller/impl/RouteControllerImplTest.java`

**Тесты:**
- [ ] `POST /batch` с unix ms timestamp → дата 2026, не 58607
- [ ] `POST /batch` с ISO-8601 timestamp → корректно

**Критерий готовности:** Все интеграционные тесты проходят

---

## Task 5: Build & verification

**Действия:**
- `mvn clean compile -pl application` — компиляция успешна
- `mvn test` — все тесты зелёные

**Критерий готовности:** Сборка чистая
