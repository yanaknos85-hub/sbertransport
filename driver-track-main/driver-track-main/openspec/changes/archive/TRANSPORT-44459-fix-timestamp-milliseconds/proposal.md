# Proposal: Fix millisecond timestamp handling in batch endpoint

## Why

POST `/api/track/driver/batch` endpoint принимает 13-значные Unix timestamps (миллисекунды) и интерпретирует их как unix-секунды. Jackson десериализует `1787295660000` как `Instant.ofEpochSecond(1787295660000)` → дата `58607-02-23` сохраняется в PostgreSQL `driver_track.coordinate`.

**Решение:** включить поддержку unix ms через `jackson.deserialization.read-date-timestamps-as-nanoseconds: false`

**TRANSPORT-44459** — баг-репорт от QA, статус To Do, minor priority.

## What Changes

### Добавляется:
1. **Настройка Jackson** `read-date-timestamps-as-nanoseconds: false` — позволяет принимать unix timestamp в миллисекундах корректно
2. **Обновление spec** `batch-coordinate-api.md` — указываем поддержку обоих форматов

### Не меняется:
- API contract (те же request/response)
- Существующая валидация координат
- Логика контроллера

### Non-goals:
- Не меняем одиночный endpoint `/api/track/driver/` (там `now()`)
- Не меняем Kafka listener (там `now()`)
- Не обновляем существующие некорректные данные в БД

## Impact

### Затронутые модули:
| Модуль | Изменение |
|--------|-----------|
| `application.yml` | добавлена настройка Jackson |
| `service/impl/CoordinateServiceImpl.java` | без изменений |
| `openspec/specs/batch-coordinate-api.md` | обновление spec |

### Риски:
- **Низкий** — добавлена 1 настройка в config
- Оба формата поддерживаются: ISO-8601 и unix ms

### Regression:
- Существующие клиенты с ISO-8601 timestamps — не затронуты
