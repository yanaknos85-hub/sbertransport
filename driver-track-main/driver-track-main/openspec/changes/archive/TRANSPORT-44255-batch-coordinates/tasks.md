# Tasks: TRANSPORT-44255 Batch Coordinates

- [x] 1. Создать DTO `BatchCoordinateRequest` и `BatchPointDTO`
- [x] 2. Добавить валидацию `BatchCoordinateRequest` (max 500 points, required fields)
- [x] 3. Добавить `POST /batch` в `RouteController`
- [x] 4. Добавить метод `saveBatchPoints()` в `CoordinateService`
- [x] 5. Реализовать `CoordinateServiceImpl.saveBatchPoints()`
- [x] 6. Добавить unit-тесты для валидации batch DTO
- [x] 7. Добавить unit-тесты для `saveBatchPoints()`
- [x] 8. Протестировать endpoint вручную
