# Spec: batch-coordinate-api

## Overview
Пакетная отправка GPS-координат водителя для сохранения точек, потерянных при отсутствии интернета.

## API Contract

### Batch Save Driver Coordinates
**POST** `/api/track/driver/batch`

**Request:**
```json
{
  "tripId": "string (UUID, required)",
  "points": [
    {
      "latitude": 55.7558,
      "longitude": 37.6173,
      "timestamp": "2026-07-12T10:00:00Z"
    }
  ]
}
```

**Response:**
```
(empty)
```

**Status Codes:**
- 200 — точки сохранены успешно
- 400 — batch size > 500 (Size) или валидация полей не пройдена
- 404 — driver not found for user {userId}
- 500 — internal error

**Business Rules:**
- Максимум 500 точек на запрос
- Невалидные точки пропускаются, валидные сохраняются
- tripId сохраняется как есть, без проверки владения водителем
- Timestamp из точки сохраняется в БД (не заменяется на now())
- userId извлекается из JWT токена
- Для каждой валидной точки создаётся запись в `driver_track.coordinate`

## Validation Rules

### BatchCoordinateRequest
| Field | Type | Required | Rules |
|-------|------|:--------:|-------|
| tripId | UUID | yes | не null, корректный UUID формат |
| points | array | yes | не null, max 500 элементов |

### BatchPointDTO
| Field | Type | Required | Rules |
|-------|------|:--------:|-------|
| latitude | double | yes | не null, диапазон [-90, 90] (валидируется в service) |
| longitude | double | yes | не null, диапазон [-180, 180] (валидируется в service) |
| timestamp | string (ISO-8601 UTC) | yes | не null, формат `2026-07-12T10:00:00Z` |

## Error Scenarios
- `batch size > 500` → 400 Bad Request: `"Batch size must not exceed 500 points"` (Jakarta Validation @Size)
- `driver not found` → 404 Not Found: `"Driver not found for user {userId}"`
- `invalid latitude/longitude` → точка пропускается, остальные сохраняются
- `null timestamp` → точка пропускается, остальные сохраняются
