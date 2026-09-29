# Spec: geo-client-integration

## Overview
Клиент для интеграции с geo-сервисом по построению фактического маршрута по GPS-координатам с поддержкой батчинга.

## API Contract

**gRPC Endpoint:** `GeoServiceGrpc.getRouteByCoords()`

**Request DTO:** `GeoDescriptor.RouteRecreationRequest`
```json
{
  "coordinates": [
    {
      "latitude": double,
      "longitude": double,
      "time": long
    }
  ]
}
```

**Response DTO:** `GeoDescriptor.RouteResponse`
```json
{
  "distance": long (мeters),
  "segments": [
    {
      "distance": long (meters),
      "coordinates": [
        { "latitude": double, "longitude": double }
      ]
    }
  ]
}
```

**Status Codes:**
- `OK` — маршрут построен успешно
- `INVALID_ARGUMENT` — координаты вне допустимого диапазона
- `RESOURCE_EXHAUSTED` — превышен лимит сервиса

## Workflow

### Route Recreation with Batching
1. **Вход** — `GeoClientImpl.recreateRoute(coords)` получает список `CoordinateRecord`
2. **Разбивка** — координаты делятся на батчи по `batchSize` точек (по умолчанию 500)
3. **Дублирование стыков** — последняя точка каждого батча (кроме последнего) дублируется в начало следующего батча
4. **Отправка батчей** — каждый батч отправляется в geo-сервис отдельным gRPC-запросом
5. **Сборка результатов:**
   - `distance` — суммирование distance из всех батчей
   - `points` — объединение всех точек из всех батчей в один список с удалением дублей через сопоставление по координатам
6. **Ответ** — `RouteDTO` с суммарным distance и единым списком точек без дублей

### Batch Division
```
coords = [C0, C1, ..., C1249]
batchSize = 500

batch[0] = [C0, ..., C499]
batch[1] = [C499, C500, ..., C999]    ← C499 дублирована
batch[2] = [C999, C1000, ..., C1249]  ← C999 дублирована
```

### Point Matching (Duplicate Removal)
```
allPoints = results[0].points
lastLat = last(points[0]).latitude
lastLon = last(points[0]).longitude

for each result[i] starting from index 1:
    for point in result[i].points:
        if point.latitude == lastLat && point.longitude == lastLon:
            continue   // пропускаем дублированную точку
        allPoints.add(point)
        lastLat = point.latitude
        lastLon = point.longitude
```

### Configuration
- `geo.batch-size` (config: `${GEO_BATCH_SIZE:500}`) — максимальный размер батча

## Error Scenarios
- geo-сервис отбрасывает валидную точку → match не сработает, точка добавится, distance будет на 1 сегмент меньше (несущественно)
- geo-сервис вернёт ошибку → весь метод выбросит исключение (частичная обработка не требуется)
- Пустой список координат → метод вернёт пустой маршрут (обрабатывается в `CalculateRouteService`)

## Data Contract

### RouteDTO
```
RouteDTO:
  - distance: Double (км, суммарный по всем батчам)
  - time: Duration (null)
  - segments: List<SegmentDTO> (1 сегмент со всеми точками)
```

### SegmentDTO
```
SegmentDTO:
  - distance: Double (км, суммарный по всем батчам)
  - time: Duration (null)
  - points: List<GeoWaypointDTO> (объединённый список без дублей)
```

### GeoWaypointDTO
```
GeoWaypointDTO:
  - latitude: double (обязательно)
  - longitude: double (обязательно)
```
