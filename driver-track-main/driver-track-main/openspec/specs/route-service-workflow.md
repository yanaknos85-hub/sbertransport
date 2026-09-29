# Spec: route-service-workflow

## Overview
Сервисные workflow для обработки точек трекинга, расчёта фактических и плановых маршрутов, а также отправки фактических данных в Kafka.

## Workflow Steps

### Save Point Workflow
1. Пользователь отправляет POST `/api/driver-track/` с `{ latitude, longitude }`
2. `RouteControllerImpl` извлекает JWT token ID и преобразует в UUID
3. `CoordinateService.savePointInfo()` валидирует входные данные
4. `CoordinateRepository.save()` сохраняет точку координат в БД
5. Возвращает 200 OK

### Get Fact Route Workflow
1. Пользователь отправляет GET `/api/driver-track/?tripId=...&routeType=FACT`
2. `RouteService.getRoute()` запрашивает фактические координаты из `FactWaypointsTripRepository`
3. `RouteRepository` агрегирует данные маршрута (сегменты, точки)
4. Формируется `RouteDTO` с distance, time, segments
5. Возвращается 200 OK

### Get Expected Route Workflow
1. Пользователь отправляет GET `/api/driver-track/?tripId=...&routeType=EXPECTED`
2. `ExpectedRouteService.getExpectedRoute()` запрашивает плановые координаты из `ExpectedWaypointsTripRepository`
3. `ExpectedRouteRepository` агрегирует плановые данные
4. Формируется `RouteDTO` с distance, time, segments
5. Возвращается 200 OK

### Create Fact Route (Scheduler)
1. `ExpectedRouteScheduler` запускается по cron (`0 */1 * * * *`)
2. Проверяет флаг `route.scheduler.isEnabled`
3. `RouteService.createFactRoute()` — формирует фактические маршруты из формулы и 2_ГИС
4. Публикует фактическую дистанцию через `TripFactDistanceSender`

### Create Expected Route (Scheduler)
1. `ExpectedRouteScheduler` запускается по cron
2. `ExpectedRouteService.createExpectedRoute()` — формирует плановые маршруты из 2_ГИС
3. Сохраняет в `ExpectedRouteRepository`

## Error Scenarios
- Неверный tripId → возвращает 404 или пустой маршрут
- Отсутствие JWT токена → возвращает 401
- Недостаточно точек координат для построения маршрута → возвращает маршрут с нулевой дистанцией
- Ошибка подключения к БД → выбрасывает RuntimeException → 500

## Data Contract
```
RouteDTO:
  - distance: Double (км)
  - time: Duration (ISO8601 format)
  - segments: List<SegmentDTO>

SegmentDTO:
  - distance: Double (км)
  - time: Duration (ISO8601 format)
  - points: List<GeoWaypointDTO>

GeoWaypointDTO:
  - latitude: double (обязательно)
  - longitude: double (обязательно)
```
