## Why

**Задача:** добавить метод в `EtrnController` для проверки доверенностей сотрудника через интеграцию с внешней системой (Dispatcher-сервис).

**Контекст:** При работе с ЭТрН необходимо подтверждать, что сотрудник, осуществляющий действия с накладной, имеет действующую доверенность. Проверка осуществляется через существующий Dispatcher-сервис, который возвращает профиль диспетчера (`DispatcherDto`) и содержит информацию о доверенностях.

**Решения, принятые в ходе проектирования:**
- Метод добавляется в существующий `EtrnController` (без создания новых контроллеров)
- Интеграция через OpenFeign — создаётся Feign-клиент для Dispatcher-сервиса
- Endpoint: `GET /api/etrn-cargo/attorneyCheck` — без path-параметра, т.к. проверка по текущему пользователю (Authentication)
- Метод: `POST` (для проверки доверенности сотрудника)
- Response: `200 OK` — доверенность есть, `403/404` — ошибка с текстом (нет доверенности или просрочена)

---

## What Changes

### Новые возможности

- **Проверка доверенностей** (POST /api/etrn-cargo/attorneyCheck) — проверка доверенностей текущего сотрудника через Feign-клиент к Dispatcher-сервису
- **Feign-клиент** — `DispatcherClient` для интеграции с Dispatcher-сервисом (метод `getSelfProfile`)
- **Конфигурация** — `DispatcherClientConfiguration` для настройки Feign-клиента

### Modified Capabilities

- *Нет.* Это новая capability.

### New Capabilities

- `attorney-check` — проверка доверенностей сотрудника через интеграцию с Dispatcher-сервисом

---

## Impact

### Affected code

- `application/src/main/java/ru/sber/transport/etrn/controller/EtrnController.java` — добавление метода `attorneyCheck`
- `application/src/main/java/ru/sber/transport/etrn/controller/impl/EtrnControllerImpl.java` — реализация метода
- `application/src/main/java/ru/sber/transport/etrn/client/DispatcherClient.java` — новый Feign-клиент
- `application/src/main/java/ru/sber/transport/etrn/config/DispatcherClientConfiguration.java` — конфигурация Feign-клиента
- `application/src/main/java/ru/sber/transport/etrn/dto/AttorneyCheckResponseDto.java` — DTO ответа

### Dependencies

- **New:** `spring-cloud-starter-openfeign` (если ещё не добавлен)
- **Existing:** Dispatcher-сервис (`DispatcherController.getSelfProfile` → `DispatcherDto`)

### Integration points

- Dispatcher-сервис: `GET /self/dispatcher/` (вход: `Authentication` из токена, выход: `DispatcherDto`)
