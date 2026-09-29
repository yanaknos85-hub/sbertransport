## Why

Водитель мобильного приложения может потерять интернет-соединение во время поездки. Без batch-отправки потерянные GPS-точки будут утеряны, трек поездки оборвётся, а расчёт пробега — некорректным.

Необходимо поддерживать отправку закэшированных на устройстве координат пакетом.

## What Changes

- Новый endpoint `POST /api/track/driver/batch` для пакетной отправки точек
- DTO `BatchCoordinateRequest` с полем `points[]` (latitude, longitude, timestamp)
- Лимит батча: max 500 точек
- Сохранение timestamp из точки (не `now()`)
- Пропуск невалидных точек, сохранение валидных
- Без проверки tripId на владение водителем

### Non-goals

- Не меняем логику существующего `POST /api/track/driver`
- Не меняем расчёт маршрута и шедулер
- Не добавляем ретраи/повторные попытки
- Не меняем отправку в Kafka

## Impact

| Модуль | Изменение |
|--------|-----------|
| `controller.RouteController` | Добавить `POST /batch` |
| `dto.BatchCoordinateRequest`, `BatchPointDTO` | Новые DTO |
| `service.CoordinateService` | Метод `saveBatchPoints()` |
| `service.impl.CoordinateServiceImpl` | Реализация батч-сохранения |
| `config.BatchProperties` (опционально) | Лимит батча |

Зависимости: нет новых.  
Риски: минимальные — только добавление эндпоинта.
