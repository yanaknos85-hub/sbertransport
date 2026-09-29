## Context

- **Проект:** `etrn-service` (Java 21, Spring Boot, схема `etrn`)
- **Техстек:** Spring Boot, Spring Data JPA, MapStruct, OpenFeign, Lombok
- **Стандарты:** REST API v6.1, Swagger (@Operation, @Tag), DTO через Java records
- **Существующий паттерн интеграции:** Feign-клиент с конфигурацией (из `as-sbertransport-cargoGIGA`: `PredictionClientConfiguration` + `PredictionClient`)
- **Внешний сервис:** Dispatcher-сервис (`app_platform_dispatcher`), метод `DispatcherController.getSelfProfile()`
  - Endpoint: `GET /self/dispatcher/`
  - Вход: `Authentication authentication` (извлекается из токена)
  - Выход: `DispatcherDto` — содержит информацию о доверенностях

---

## Design

### Архитектура вызова

```
EtrnController.attorneyCheck(Authentication)
    → EtrnControllerImpl.attorneyCheck(Authentication)
        → DispatcherClient.getSelfProfile(Authentication)
            → Dispatcher-сервис: GET /self/dispatcher/
                → DispatcherDto (response)
            → AttorneyCheckResponseDto (mapped)
```

### Ключевые решения

1. **Endpoint:** `POST /api/etrn-cargo/attorneyCheck` (без path-параметра)
   - Метод: `POST` (проверка/действие)
   - Без `id` в URL — проверка по текущему пользователю (через `Authentication`)

2. **Feign-клиент:** `DispatcherClient`
   - Аналогичен `PredictionClient` из `as-sbertransport-cargoGIGA`
   - Конфигурация: `DispatcherClientConfiguration` (базовый URL, timeout, interceptors)
   - Метод: `GET /self/dispatcher/` с `Authentication`

3. **DTO:**
   - **Request:** отсутствует (пользователь из `Authentication`)
   - **Response:** `AttorneyCheckResponseDto`
     - Успех: `200 OK` — доверенность есть
     - Ошибка: `403/404` — доверенности нет или просрочена (с текстом ошибки в response body)

4. **Обработка ошибок:**
   - `FeignException` → маппинг в кастомное исключение с русским описанием
   - Коды ответов: `200 OK` (успех), `403 Forbidden` (доверенность просрочена), `404 Not Found` (доверенность не найдена)

---

## API Контракт

### Request

```
POST /api/etrn-cargo/attorneyCheck
Authorization: Bearer <token>
Content-Type: application/json
```

### Response — Успех

```json
HTTP/1.1 200 OK
{
  "hasValidAttorney": true,
  "attorneyInfo": {
    "attorneyId": "uuid",
    "issuerOrgName": "Организация-доверитель",
    "issueDate": "2025-01-15",
    "expiryDate": "2026-01-15"
  }
}
```

### Response — Ошибка

```json
HTTP/1.1 403 Forbidden
{
  "errorCode": "ATTORNEY_NOT_FOUND",
  "errorMessage": "У сотрудника отсутствует действующая доверенность"
}
```

---

## Файлы для создания/модификации

| Файл | Действие | Описание |
|------|----------|----------|
| `EtrnController.java` | modify | Добавить метод `attorneyCheck` |
| `EtrnControllerImpl.java` | modify | Реализовать метод `attorneyCheck` |
| `DispatcherClient.java` | create | Feign-клиент для Dispatcher-сервиса |
| `DispatcherClientConfiguration.java` | create | Конфигурация Feign-клиента |
| `AttorneyCheckResponseDto.java` | create | DTO ответа |
| `application.yml` | modify | Добавить URL Dispatcher-сервиса |
