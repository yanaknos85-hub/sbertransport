## Задачи

### 1. Создать DTO для ответа

**Файл:** `application/src/main/java/ru/sber/transport/etrn/dto/AttorneyCheckResponseDto.java`

**Описание:** Java record для ответа от внешней системы. Содержит информацию о статусе доверенности.

**Структура:**
```java
public record AttorneyCheckResponseDto(
    boolean hasValidAttorney,
    AttorneyInfoDto attorneyInfo
) {}
```

**Где взять:** Создать новый DTO по аналогии с существующими DTO из `ru.sber.transport.etrn.dto`.

**Чек-лист:**
- [ ] DTO создан как Java record
- [ ] Поля: `hasValidAttorney` (boolean), `attorneyInfo` (nullable)
- [ ] Аннотации `@Schema` из Swagger/OpenAPI

---

### 2. Создать Feign-клиент для Dispatcher-сервиса

**Файл:** `application/src/main/java/ru/sber/transport/etrn/client/DispatcherClient.java`

**Описание:** OpenFeign-клиент для вызова `DispatcherController.getSelfProfile()`.

**Метод:**
```java
@GetMapping(value = "/self/dispatcher/", produces = MediaType.APPLICATION_JSON_VALUE)
DispatcherDto getSelfProfile(@RequestHeader("Authorization") String token);
```

**Где взять:** Скопировать паттерн из `as-sbertransport-cargoGIGA/srm-cargo/src/main/java/ru/sberbank/ditsib/transport/srm/service/prediction/PredictionClient.java`

**Чек-лист:**
- [ ] Аннотация `@FeignClient` с именем `dispatcher-client`
- [ ] Метод `getSelfProfile()` с `@GetMapping`
- [ ] Параметр `@RequestHeader("Authorization")` для токена
- [ ] Возвращает `DispatcherDto` (import из `ru.sber.transport.dispatcher.dto`)

---

### 3. Создать конфигурацию Feign-клиента

**Файл:** `application/src/main/java/ru/sber/transport/etrn/config/DispatcherClientConfiguration.java`

**Описание:** Конфигурация для `DispatcherClient` — базовый URL, timeout, interceptors.

**Где взять:** Скопировать паттерн из `as-sbertransport-cargoGIGA/srm-cargo/src/main/java/ru/sberbank/ditsib/transport/srm/config/PredictionClientConfiguration.java`

**Чек-лист:**
- [ ] `@Configuration` annotation
- [ ] `@FeignClient` configuration для `dispatcher-client`
- [ ] Базовый URL из `application.yml` (property `dispatcher.service.url`)
- [ ] Timeout настройки (connect/read)

---

### 4. Добавить метод в контроллер (интерфейс)

**Файл:** `application/src/main/java/ru/sber/transport/etrn/controller/EtrnController.java`

**Описание:** Добавить метод в существующий интерфейс контроллера.

**Добавить:**
```java
@PostMapping("/attorneyCheck")
@Operation(summary = "Проверка доверенностей", description = "Проверка доверенностей текущего сотрудника")
ResponseEntity<AttorneyCheckResponseDto> attorneyCheck(
        @Parameter(hidden = true) Authentication authentication
);
```

**Где добавить:** После существующих методов `unlock()` в конце интерфейса.

**Чек-лист:**
- [ ] Метод добавлен в интерфейс `EtrnController`
- [ ] URL: `/attorneyCheck`
- [ ] Метод: `POST`
- [ ] Parameter: `@Parameter(hidden = true) Authentication authentication`
- [ ] Swagger: `@Operation` с summary/description

---

### 5. Реализовать метод в контроллере

**Файл:** `application/src/main/java/ru/sber/transport/etrn/controller/impl/EtrnControllerImpl.java`

**Описание:** Реализация метода `attorneyCheck` с вызовом Feign-клиента.

**Реализация:**
```java
@Override
public ResponseEntity<AttorneyCheckResponseDto> attorneyCheck(Authentication authentication) {
    // Извлечь token из authentication
    // Вызвать dispatcherClient.getSelfProfile(token)
    // Преобразовать DispatcherDto → AttorneyCheckResponseDto
    // Вернуть ResponseEntity
}
```

**Где добавить:** После существующих методов `unlock()` в классе `EtrnControllerImpl`.

**Чек-лист:**
- [ ] Метод добавлен в `EtrnControllerImpl`
- [ ] Внедрён `DispatcherClient` через constructor injection
- [ ] Извлечение Bearer-токена из `Authentication`
- [ ] Вызов `dispatcherClient.getSelfProfile(token)`
- [ ] Маппинг `DispatcherDto → AttorneyCheckResponseDto`
- [ ] Обработка `FeignException` → кастомное исключение
- [ ] Коды ответов: `200 OK` (успех), `403/404` (ошибка)

---

### 6. Настроить application.yml

**Файл:** `application/src/main/resources/application.yml`

**Описание:** Добавить URL Dispatcher-сервиса в конфигурацию.

**Добавить:**
```yaml
dispatcher:
  service:
    url: ${DISPATCHER_SERVICE_URL:http://localhost:8080}
```

**Чек-лист:**
- [ ] Свойство `dispatcher.service.url` добавлено в `application.yml`
- [ ] Поддержка环境变量 через `${...}`
- [ ] Дефолтное значение для локальной разработки

---

### 7. Добавить OpenFeign в pom.xml

**Файл:** `application/pom.xml`

**Описание:** Добавить зависимость `spring-cloud-starter-openfeign`, если ещё не добавлена.

**Добавить:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Чек-лист:**
- [ ] Зависимость добавлена в `pom.xml`
- [ ] Включена аннотация `@EnableFeignClients` в main application класс

---

## Порядок выполнения

1. ✅ **Задача 7** — Добавить OpenFeign в pom.xml (зависимость уже есть)
2. ✅ **Задача 6** — Настроить application.yml (конфигурация)
3. ✅ **Задача 1** — Создать DTO `AttorneyCheckResponseDto`
4. ✅ **Задача 2** — Создать Feign-клиент `DispatcherClient`
5. ✅ **Задача 3** — Создать конфигурацию `DispatcherClientConfiguration`
6. ✅ **Задача 4** — Добавить метод в интерфейс `EtrnController`
7. ✅ **Задача 5** — Реализовать метод в `EtrnControllerImpl`
