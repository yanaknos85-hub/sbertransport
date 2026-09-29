# Spec: route-api

## Overview
REST API для сохранения точек трекинга водителей и получения плановых/фактических маршрутов.

## API Contract

### Save Point
**POST** /api/driver-track/ — `RouteControllerImpl.savePointInfo()`

**Request:**
```json
{
  "latitude": 55.7558,
  "longitude": 37.6173
}
```

**Response:**
```
200 OK (void)
```

**Status Codes:**
- `200` — точка успешно сохранена
- `400` — валидация не пройдена (отсутствуют latitude/longitude)
- `401` — неверный JWT токен

**Business Logic:**
- `CoordinateService` — извлекает userId из JWT и сохраняет координату в БД
- JWT token ID используется как UUID идентификатор водителя

### Get Route
**GET** /api/driver-track/ — `RouteControllerImpl.getRoute()`

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|:--------:|---------|-------------|
| tripId | UUID | да | — | Идентификатор поездки |
| sourceType | RouteSource | нет | FORMULA | Источник данных: FORMULA или TWO_GIS |
| routeType | RouteType | нет | FACT | Тип маршрута: FACT (фактический) или EXPECTED (плановый) |

**Response:**
```json
{
  "distance": 12.5,
  "time": "PT1H30M",
  "segments": [
    {
      "distance": 5.2,
      "time": "PT45M",
      "points": [
        { "latitude": 55.7558, "longitude": 37.6173 }
      ]
    }
  ]
}
```

**Status Codes:**
- `200` — маршрут успешно получен
- `400` — невалидный tripId
- `404` — поездка не найдена
- `401` — неверный JWT токен

**Business Logic:**
- При `routeType = FACT` — `RouteService.getRoute()` возвращает фактический маршрут из БД
- При `routeType = EXPECTED` — `ExpectedRouteService.getExpectedRoute()` возвращает плановый маршрут

## Validation Rules
| Field | Type | Required | Rules |
|-------|------|:--------:|-------|
| latitude | double | да | @NotNull, валидный диапазон широты |
| longitude | double | да | @NotNull, валидный диапазон долготы |
| tripId | UUID | да | Валидный UUID формат |
