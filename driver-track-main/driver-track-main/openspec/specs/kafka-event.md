# Spec: kafka-event

## Overview
Публикация фактических дистанций поездок в Kafka для downstream-потребителей.

## Event Schema
```json
{
  "id": "UUID",
  "distances": {
    "FORMULA": 12.5,
    "TWO_GIS": 13.0
  }
}
```

## Event Flow
1. `ExpectedRouteScheduler` запускается каждые 1 минуту
2. `RouteService.createFactRoute()` вычисляет фактические маршруты
3. `TripFactDistanceSender.send()` собирает `Map<RouteSource, RouteDTO>` для всех поездок
4. Формируется `TripFactDistanceMessage` с tripId и дистанциями по источникам
5. Сообщение публикуется в Kafka topic (название определяется конфигурацией)

## Data Contract
```
TripFactDistanceMessage:
  - id: UUID (идентификатор поездки)
  - distances: Map<String, Double> (источник → дистанция в км)
```

## Error Handling
- Ошибка отправки → логирование + исключение propagates to scheduler
- Downstream-потребитель не получил → no retry policy в коде (зависит от Kafka config)
