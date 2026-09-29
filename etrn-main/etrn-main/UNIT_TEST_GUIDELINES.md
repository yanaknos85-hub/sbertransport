# Руководство по написанию unit-тестов

## Общие принципы

### 1. Цель unit-тестов
Unit-тесты должны:
- Проверять корректность работы одного компонента изолированно
- Быть быстрыми и стабильными
- Быть легко читаемыми и понятными
- Покрывать основные сценарии и граничные условия

### 2. Акроним F.I.R.S.T.
- **Fast** — тесты должны выполняться быстро
- **Independent** — тесты не должны зависеть друг от друга
- **Repeatable** — тесты должны давать одинаковый результат в любой среде
- **Self-Validating** — тесты должны сами проверять результат (без ручной интерпретации)
- **Timely** — тесты должны писаться вовремя (до или вместе с кодом)

---

## Структура теста

### Правило 3A (Arrange-Act-Assert)

Каждый тест должен следовать структуре:

```java
@Test
@DisplayName("Описание теста")
void testMethodName() {
    // Arrange — подготовка данных
    // Act — выполнение тестируемой операции
    // Assert — проверка результатов
}
```

### Порядок импортов

```java
// 1. Стандартные библиотеки Java
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// 2. Импорт статических методов AssertJ (используется вместо org.junit.jupiter.api.Assertions)
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 3. Библиотеки тестирования (JUnit, Spring Test, Mockito и т.д.)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 4. Классы из проекта (по пакетам)
import ru.sberbank.ditsib.transport.YourPackage.YourClass;
```

**Важно:** Используйте **только AssertJ** для утверждений. Не импортируйте и не используйте `org.junit.jupiter.api.Assertions`.

---

## Управление импортами

**Важно:** В тестовых классах **не должно быть неиспользуемых импортов**. Удаляйте импорты, которые не используются в коде теста.

❌ **НЕ так:**
```java
import org.junit.jupiter.api.Test;
import java.util.List;  // импорт не используется
import java.util.ArrayList;  // импорт не используется

class MyTest {
    @Test
    void testMethod() {
        // ...
    }
}
```

✅ **Всегда так:**
```java
import org.junit.jupiter.api.Test;

class MyTest {
    @Test
    void testMethod() {
        // ...
    }
}
```

---

## Наименование тестов

### Правила
1. **`@DisplayName`** на **русском языке** - описывает что тестируется
2. **Имя метода** на **английском языке** (camelCase) - техническое имя
3. Формат имени метода: `действие_условие_результат` или `когда_условие_тогда_результат`

### Примеры
```java
@Test
@DisplayName("Получение заявки по идентификатору")
void getRequestById() { ... }

@Test
@DisplayName("Добавление заявки с некорректным адресом должно вернуть ошибку")
void addRequestWithIncorrectAddress_shouldReturnValidationError() { ... }

@Test
@DisplayName("Отклонение заявки с указанием причины")
void declineRequestWithReason() { ... }
```

### Язык @DisplayName
Все `@DisplayName` должны быть написаны на **русском языке**. Исключение — имена методов (camelCase, английский язык), которые остаются техническими идентификаторами.

✅ **Правильно:**
```java
@DisplayName("Получение журнала маршрутов в работе — заявка с истекшим КС видна без фильтра по дате")
void searchRoutelistsInProgressWithExpiredDeadlineTest() { ... }
```

❌ **Неправильно:**
```java
@DisplayName("Get routelist journal in progress — expired deadline visible without date filter")
void searchRoutelistsInProgressWithExpiredDeadlineTest() { ... }
```

---

## Используемые библиотеки

### Основные библиотеки (из pom.xml)

| Библиотека | Версия | Назначение |
|-----------|--------|------------|
| **JUnit Jupiter** | 5.11.x | Фреймворк для unit-тестов |
| **Mockito** | 5.x | Мокирование зависимостей |
| **AssertJ** | 3.26.x | Fluent API для утверждений (входит в spring-boot-starter-test) |
| **Spring Boot Test** | 3.5.12 | Интеграция с Spring |
| **Embedded Postgres** | 4.10 | Локальная БД для интеграционных тестов |
| **Awaitility** | 4.0.3 | Асинхронные проверки |
| **WireMock** | 3.0.1 | Мокирование HTTP-сервисов |

### Рекомендуемые статические импорты

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
```

---

## Использование var в тестах

В тестах **не используйте явную типизацию** для локальных переменных, когда тип очевиден из контекста. Используйте `var`:

❌ **НЕ так:**
```java
ReportTaskFilterDto requestDTO = ReportTaskFilterDto.builder().build();
String result = response.getBody();
OrderEntity entity = OrderEntity.builder().build();
```

✅ **Всегда так:**
```java
var requestDTO = ReportTaskFilterDto.builder().build();
var result = response.getBody();
var entity = OrderEntity.builder().build();
```

### Когда использовать var

| Ситуация | Пример | Использовать var |
|----------|--------|------------------|
| Создание через builder | `var dto = Dto.builder().build()` | ✅ Да, тип очевиден |
| Результат вызова метода | `var result = service.getMethod()` | ✅ Да, тип в сигнатуре |
| Stream операции | `var filtered = list.stream().filter(...)` | ✅ Да |
| Jackson object reader | `var result = objectMapper.readValue(..., TypeReference)` | ✅ Да |
| Объявление с неявным типом | `new SomeClass()` | ✅ Да |

### Когда можно использовать явную типизацию

| Ситуация | Пример | Допустимо |
|----------|--------|-----------|
| Примитивы для читаемости | `int count = list.size()` | ✅ Да (опционально) |
| Булевы значения | `boolean isActive = true` | ✅ Да (опционально) |
| Когда тип не очевиден | `var result = someMethod()` (где `someMethod()` неочевиден) | ❌ Указать явно |

**Важно:** `var` улучшает читаемость тестов и сокращает объем кода, при этом сохраняя строгую типизацию на уровне компиляции.

---

## Аннотации и конфигурация

### Unit-тесты (без Spring контекста)

```java
@DisplayName("Описание компонента")
@ExtendWith(MockitoExtension.class)
class ClassNameTest {

    @Mock
    private SomeDependency dependency;

    @BeforeEach
    void setUp() {
        // Дополнительная настройка, если нужна (опционально)
    }

    @Test
    void testMethod() {
        // Arrange-Act-Assert
    }
}
```

**Важно:** Тестовый класс, методы с аннотациями `@Test`, `@BeforeEach`, `@BeforeAll`, `@AfterEach`, `@AfterAll` **не должны иметь модификаторов доступа** (`public`, `private`, `protected`). Используйте package-private (по умолчанию).

❌ **НЕ так:**
```java
public class ClassNameTest {
    @Test
    public void testMethod() { ... }
    @BeforeEach
    public void setUp() { ... }
}
```

✅ **Всегда так:**
```java
class ClassNameTest {
    @Test
    void testMethod() { ... }
    @BeforeEach
    void setUp() { ... }
}
```

❌ **НЕ так:**
```java
class ClassNameTest {

    @Mock
    private SomeDependency dependency;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
```

✅ **Всегда так:**
```java
@ExtendWith(MockitoExtension.class)
class ClassNameTest {

    @Mock
    private SomeDependency dependency;

    // @BeforeEach не нужен, моки инициализируются автоматически
}
```

**Причина:** `@ExtendWith(MockitoExtension.class)` — это современный, рекомендуемый подход Mockito 5.x. Он обеспечивает:
- Более чистый и компактный код
- Лучшую интеграцию с JUnit 5
- Автоматическую инициализацию моков без необходимости вызова `openMocks()`
- Поддержку всех возможностей Mockito без дополнительной настройки

### Integration-тесты (с Spring контекстом)

```java
@DisplayName("Описание контроллера/сервиса")
@SpringBootTest(
    properties = {
        "spring.main.cloud-platform=none",
        "logger.level.root=debug"
    }
)
@EmbeddedPostgres
class ComponentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SomeService service;

    @MockitoBean
    private ExternalDependency dependency;

    // Подготовка данных через SQL-скрипты
    @Sql(scripts = "/sql/init_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @BeforeEach
    void setUp() {
        // Дополнительная настройка, если нужна (опционально)
    }

    @Test
    @Transactional
    void testEndpoint() throws Exception {
        // Тест с MockMvc для контроллеров
    }
}
```

### Ключевые аннотации

| Аннотация | Описание |
|-----------|----------|
| `@DisplayName` | Читаемое название теста (русский язык) |
| `@BeforeEach` | Выполняется перед каждым тестом |
| `@BeforeAll` | Выполняется один раз для всего класса |
| `@Sql` | Загрузка данных из SQL-скрипта перед тестом |
| `@EmbeddedPostgres` | Запуск встроенной PostgreSQL для интеграционных тестов |
| `@WithMockUser` | Имитация аутентифицированного пользователя (для безопасности) |

---

## Работа с моками и спайями

### @Mock vs @MockitoBean

| Аннотация | Когда использовать | Поведение |
|-----------|-------------------|-----------|
| `@Mock` | Unit-тесты без Spring контекста (с `@ExtendWith(MockitoExtension.class)`) | Полностью фейковый объект, не участвует в автодеплое Spring |
| `@MockitoBean` | **Integration-тесты со Spring контекстом** (`@SpringBootTest`) | Мок, созданный Spring'ом, участвует в автодеплое, может быть autowired в тестируемый компонент |

### Важно: Не используйте `@MockBean`

❌ **НЕ так:**
```java
import org.springframework.boot.test.mock.mockito.MockBean;

@MockBean
private SomeService service;
```

✅ **Всегда так:**
```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@MockitoBean
private SomeService service;
```

### Причина

В проекте используется **устаревшая аннотация `@MockBean` из Spring Boot**, которая помечена как `@Deprecated`. Правильная аннотация - `@MockitoBean` из `spring-test` модуля.

### Примеры использования

```java
// Полное мокирование
@Mock
private TariffResolver tariffResolver;

// Мок в Spring контексте
@MockitoBean
private AuthorizationManager<RequestAuthorizationContext> roleCheckService;
```

---

## Работа с моками и спайями

### @Mock vs @MockitoBean

| Аннотация | Когда использовать | Поведение |
|-----------|-------------------|-----------|
| `@Mock` | Unit-тесты без Spring | Полностью фейковый объект |
| `@MockitoBean` | Integration-тесты | Мок, созданный Spring'ом (автодеплой) |

### Примеры использования

```java
// Полное мокирование
@Mock
private TariffResolver tariffResolver;

// Мок в Spring контексте
@MockitoBean
private AuthorizationManager<RequestAuthorizationContext> roleCheckService;
```

### Мокирование методов

```java
// Возврат фиксированного значения
when(tariffResolver.calculateByType(any(), any()))
    .thenReturn(tariffResponse);

// Возврат по условию
when(geoDataResolver.getAddress(eq(addressString)))
    .thenReturn(Optional.of(addressInfo));

// Возврат списка
when(vspDataResolver.getVspByFullNumber(any()))
    .thenReturn(Collections.singletonList(vspResponse));

// Генерация исключения
when(repository.findById(any()))
    .thenThrow(new RequestNotFoundException(id));
```

---

## Работа с данными

### Подготовка данных через SQL-скрипты

Данные для тестов должны создаваться через SQL-скрипты, расположенные в `src/test/resources/sql/`:

```sql
-- src/test/resources/sql/compensation_table.sql
insert into compensation_cargo.compensation_request (
    id, trip_id, humanreadableid, trip_humanreadableid,
    recipient_id, organization_id, cost_center, cost,
    status, transport_type, control_date, approval_date,
    source, active
) values
('cf8f42df-998a-48b4-b349-1a33f0c1072b',
    '3e0c77bf-c851-4e03-a01e-96273f4b5b8e',
    'OT-0025-00120720',
    'OT-0025-00120721',
    'cf8f42df-998a-48b4-b349-1a33f0c1072b',
    'cf8f42df-998a-48b4-b349-1a33f0c1072b',
    'MVZ1',
    1111,
    'COMPENSATION_WAITING_FOR_PAYMENT',
    'DEDICATED',
    '2025-05-08 10:00:00',
    '2025-05-08 10:00:00',
    'EXCHANGE_CARGO',
    true);
```

**Примечание:** Используйте фиксированные даты вместо `now()` для соблюдения принципа Repeatable (тесты должны давать одинаковый результат в любой среде).

Загрузка скрипта в тесте:

```java
@Test
@DisplayName("Проверка создания заявки")
@Sql(scripts = "/sql/compensation_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@WithMockUser(username = USER3_ID_STR, authorities = {"ROLE_ADMIN_DATA_MASTER"})
@Transactional
void test_create() throws Exception {
    // Тест использует данные из SQL-скрипта
}
```

**Примечание:** Для интеграционных тестов используйте аннотацию `@Transactional` - она откатывает изменения после каждого теста, что избавляет от необходимости ручной очистки данных.

### Когда использовать @BeforeAll

Для создания данных, общих для всех тестов в классе:

```java
@BeforeAll
public static void setUpAll() {
    // Создание данных через repository.save() для всех тестов
    organization1 = organizationRepository.saveAndFlush(organization1);
}
```

**Примечание:** SQL-скрипты предпочтительны для наборов данных. `@BeforeAll` используется, когда нужна гибкость в создании данных (динамические данные, результаты вызовов методов и т.д.).

**Примечание:** В проекте есть базовый класс `TestCommon` с общими константами (ID, имена, адреса и т.д.) и утилитами. Наследуйте тестовые классы от него при необходимости для доступа к общим данным.

---

## Порядок размещения методов в тестовом классе

Методы в тестовых классах должны быть расположены в следующем порядке:

1. **Поля класса** (приватные final/не-final)
2. **`@BeforeAll` методы** (статические, для всей группы тестов)
3. **`@BeforeEach` методы** (для каждого теста)
4. **`@AfterEach` / `@AfterAll` методы** (при необходимости)
5. **`@Test` методы** (public по умолчанию, в порядке убывания видимости)
6. **`private` методы** — вспомогательные методы для тестов (идут в самом конце)

**Важно:** Методы с аннотацией `@Test` **не должны иметь модификаторов доступа** (`public`, `private`, `protected`). JUnit 5 позволяет методам тестов иметь любой модификатор доступа, но в проекте принято использовать методы без модификаторов (package-private по умолчанию).

### Пример правильной структуры:

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса заказов")
class OrderServiceTest {

    // 1. Поля класса
    @Mock
    private OrderRepository repository;

    @Mock
    private NotificationService notificationService;

    private OrderService orderService;

    private UUID orderId;

    // 2. @BeforeAll метод (один раз для всех тестов)
    @BeforeAll
    static void setUpAll() {
        // Общая подготовка данных для всех тестов
    }

    // 3. @BeforeEach метод (перед каждым тестом)
    @BeforeEach
    void setUp() {
        // Подготовка перед каждым тестом
        orderService = new OrderService(repository, notificationService);
        orderId = UUID.randomUUID();
    }

    // 4. @AfterEach / @AfterAll (если нужны)
    @AfterEach
    void tearDown() {
        // Очистка после каждого теста
    }

    // 5. @Test методы (без модификаторов доступа)
    @Test
    @DisplayName("Создание заказа")
    void testCreateOrder() {
        // Тест код
    }

    @Test
    @DisplayName("Получение заказа по ID")
    void testGetOrderById() {
        // Тест код
    }

    // 6. Private вспомогательный метод для тестов
    private void setupOrderInRepository(UUID orderId) {
        var order = OrderEntity.builder().id(orderId).build();
        when(repository.findById(orderId)).thenReturn(Optional.of(order));
    }
}
```

### Пояснение

- **Test методы без модификаторов** — основные тесты, проверяют публичный API класса (package-private по умолчанию)
- **Private вспомогательные методы** — используются для дублирующейся подготовки данных внутри тестов, не являются тестами сами по себе. Они идут **в самом конце** класса, после всех `@Test` методов
- Следование этому порядку улучшает читаемость и поддерживаемость тестов
- Методы `@BeforeEach` и `@BeforeAll` вынесены отдельно для быстрого поиска логики подготовки

---

## Утверждения (Assertions)

**Важно:** В проекте используется **только AssertJ** для утверждений. `org.junit.jupiter.api.Assertions` **не используется**.

### AssertJ — Fluent API для утверждений

#### Цепочечное использование assertThat

AssertJ поддерживает fluent API, что позволяет объединять несколько утверждений в цепочку для улучшения читаемости и сокращения кода:

```java
// Проверка списка с несколькими утверждениями
assertThat(result).isNotNull().hasSize(1);

// Проверка объекта из списка
assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
assertThat(result.get(0).getType()).isEqualTo("WORK");
assertThat(result.get(0).getDateFrom()).isEqualTo(LocalDate.of(2024, 1, 20));

// Эквивалентно (более verbose вариант):
// assertThat(result).isNotNull();
// assertThat(result).hasSize(1);
// assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
// assertThat(result.get(0).getType()).isEqualTo("WORK");
// assertThat(result.get(0).getDateFrom()).isEqualTo(LocalDate.of(2024, 1, 20));
```

**Преимущества цепочечного использования:**
- Сокращение объема кода
- Улучшение читаемости - логически связанные проверки идут подряд
- Сохранение fluent API стиля AssertJ

**Важно:** Разделяйте цепочки на логические блоки для читаемости:

```java
// ✅ Хорошо: логические блоки разделены
assertThat(result).isNotNull().hasSize(1);
assertThat(result.get(0).getDate()).isEqualTo(expectedDate);
assertThat(result.get(0).getType()).isEqualTo(expectedType);

// ❌ Плохо: слишком длинная цепочка, трудно читать
assertThat(result).isNotNull().hasSize(1).isEqualTo(expected).extracting(...)...;
```

#### Основные утверждения

```java
// Простые проверки
assertThat(result).isNotNull();
assertThat(list).hasSize(3);
assertThat(string).startsWith("CR-");
assertThat(booleanValue).isTrue();
assertThat(booleanValue).isFalse();
assertThat(value).isNull();

// Проверка объектов
assertThat(actual)
    .extracting("id", "status", "cost")
    .containsExactly(expectedId, expectedStatus, expectedCost);

// Проверка по полям
assertThat(actual)
    .hasFieldOrPropertyWithValue("id", expectedId)
    .hasFieldOrPropertyWithValue("status", expectedStatus);

// Проверка исключений
assertThatExceptionOfType(RequestNotFoundException.class)
    .isThrownBy(() -> service.getMethod(requestId))
    .withMessage("Заявка не найдена, id: " + requestId);

// Проверка отсутствия исключения
assertThatCode(() -> service.getMethod(any()))
    .doesNotThrowAnyException();

// Сравнение списков
assertThat(actualList).hasSize(expectedSize);
assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);

// Сравнение дат
assertThat(actualDate).isEqualTo(expectedDate);
assertThat(actualDateTime).isBefore(expectedDateTime);

// Сравнение строк (без учета регистра)
assertThat(actualString).isEqualToIgnoringCase(expectedString);
assertThat(actualString).startsWithIgnoringCase(prefix);

// Проверка optional
assertThat(optionalValue).isPresent();
assertThat(optionalValue).hasValueSatisfying(value ->
    assertThat(value).isEqualTo(expected));
```

### Что запрещено

❌ **НЕ так:**
```java
import static org.junit.jupiter.api.Assertions.*;

// Запрещенные методы (из JUnit Assertions):
assertEquals(expected, actual);
assertNotEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertSame(expected, actual);
assertNotSame(expected, actual);
assertThrows(Exception.class, () -> {});
```

✅ **Всегда так:**
```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(actual).isEqualTo(expected);
assertThat(condition).isTrue();
assertThat(value).isNull();
```

**Примечание:** Метод `assertAll` из AssertJ технически возможен, но в проекте его лучше избегать - разбивайте сложные проверки на отдельные тесты.

---

## Параметризованные тесты

```java
@ParameterizedTest
@ValueSource(strings = {
    "Доп офис №0001/123",
    "доп офис №0001/123",
    "Доп. офис №0001/123"
})
@DisplayName("Поиск МВЗ по разным вариантам названия")
void addRequestWithMvzByDepartment(String depName) {
    // Тест с разными входными значениями
}

@ParameterizedTest
@CsvSource({
    "2024-09-02, 4",
    "2024-09-02, 0",
    "2024-09-02, -1"
})
void testWithDifferentDates(LocalDate date, int shift) {
    // Тест с комбинациями параметров
}
```

---

## Работа с контроллерами (MockMvc)

```java
@Test
@DisplayName("Создание заявки")
@WithMockUser(username = USER_ID, roles = "GUEST")
void createRequestTest() throws Exception {
    var requestDto = CreateRequestDto.builder()
        .departmentId(departmentId)
        .destinations(destinations)
        .build();
    
    mockMvc.perform(post("/api/requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value("APPROVED"));
}

@Test
@DisplayName("Валидация некорректного запроса")
void validateInvalidRequest() throws Exception {
    var invalidRequest = new InvalidRequestDto();
    
    mockMvc.perform(post("/api/requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
}
```

---

## Работа с безопасностью (Security)

```java
// С JWT токеном
mockMvc.perform(post("/api/endpoint")
        .with(jwt().jwt(builder -> builder.jti(USER_ID)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(content))
    .andExpect(status().isOk());

// С mock пользователем
mockMvc.perform(get("/api/endpoint")
        .with(mockUser().username(USER_ID).roles("GUEST")))
    .andExpect(status().isOk());

// С авторизацией по ролям
mockMvc.perform(post("/api/admin")
        .with(mockUser().username(USER_ID).roles("ADMIN")))
    .andExpect(status().isOk());
```

---

## Тестирование мапперов

**Важно:** Мапперы тестируются **только с помощью Unit-тестов**, без использования Spring контекста.

### Почему Unit-тесты?

- MapStruct генерирует реализацию маппера на этапе компиляции
- Мапперы не имеют зависимостей, которые требуют Spring контекста
- Unit-тесты работают быстрее и проще для мапперов

### Правильный подход

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты маппера заказа")
class OrderMapperTest {

    @Mock
    private SomeDependency dependency; // Если маппер зависит от других компонентов

    @BeforeEach
    void setUp() {
        // Инициализация, если нужно
    }

    @Test
    @DisplayName("Преобразование Entity в DTO")
    void testEntityToDto() {
        // Arrange
        var entity = OrderEntity.builder()
            .id(UUID.randomUUID())
            .status(OrderStatus.NEW)
            .build();

        // Act
        var dto = OrderMapper.INSTANCE.toDto(entity);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus().name());
    }

    @Test
    @DisplayName("Преобразование DTO в Entity")
    void testDtoToEntity() {
        // Arrange
        var dto = OrderDto.builder()
            .id(UUID.randomUUID().toString())
            .status("NEW")
            .build();

        // Act
        var entity = OrderMapper.INSTANCE.toEntity(dto);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(UUID.fromString(dto.getId()));
    }
}
```

### Что запрещено для мапперов

❌ **НЕ так:**
```java
@SpringBootTest
class OrderMapperTest {
    @Autowired
    private OrderMapper mapper; // Ненужное использование Spring контекста
}
```

### Допущения при тестировании мапперов

1. **Прямая инициализация реализации MapStruct** - для тестирования можно использовать `new MapperImpl()`, так как MapStruct генерирует реализацию на этапе компиляции
2. **Нет необходимости в @Mock** - если маппер не зависит от других компонентов, не требуется добавлять аннотацию `@Mock`
3. **Нет необходимости в @BeforeEach** - инициализацию маппера можно выполнить сразу как поле класса

### Правильный подход (упрощенный)

```java
@DisplayName("Тесты календарного маппера")
class CalendarMapperTest {

    private final CalendarMapperImpl mapper = new CalendarMapperImpl();

    @Test
    @DisplayName("Преобразование DTO в модель")
    void testCalendarToDto() {
        // Arrange
        var dto = CalendarDto.builder().date(LocalDate.now()).build();

        // Act
        var result = mapper.calendarToDto(dto);

        // Assert
        assertThat(result).isNotNull();
    }
}
```

---

## Рекомендации по покрытию

### Что обязательно тестировать
1. **Business logic** — основная логика сервисов
2. **Validations** — проверки входных данных
3. **Exception handling** — обработка ошибок и исключений
4. **Boundary conditions** — граничные случаи (null, пустые списки, мин/макс значения)
5. **Controller endpoints** — все API endpoints
6. **Mappers** — преобразование DTO <-> Entity

### Что НЕ тестировать
1. Геттеры/сеттеры в DTO
2. Простые лямбды и method references
3. Lombok-генерируемый код (`@Data`, `@Builder`)
4. Простые репозитории (вместо этого тестировать интеграцию)

---

## Примеры из проекта

### Пример 1: Integration Test с SQL-скриптами

```java
@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.main.cloud-platform=none")
@Transactional
@EmbeddedPostgres
@DisplayName("Проверка управленческих задач")
class ReportTaskControllerImplTest extends TestCommon {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Поиск заявок")
    @Sql(scripts = "/sql/report_task.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockUser(username = USER3_ID_STR, authorities = {ROLE_ADMIN_DATA_MASTER, "ROLE_ADMIN_DATA_MASTER"})
    void test_search1() throws Exception {
        var requestDTO = ReportTaskFilterDto.builder().build();

        var request = objectMapper.writeValueAsString(requestDTO);
        ResultActions response = mockMvc.perform(post("/tasks/")
                        .content(request)
                        .header("Authorization", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        CargoContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });

        var result = actual.content.stream()
                .filter(t -> t.getId().equals(UUID.fromString("b5f77f1b-66f6-4e0f-bc5a-f305bcb7bf54")))
                .findAny();
        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo("/files/compensation_report___1761486300015.xlsx");
        assertThat(actual.content).hasSize(6);
    }

    @Test
    @DisplayName("Создание")
    @Sql(scripts = "/sql/compensation_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_create() throws Exception {
        var requestDTO = CompensationSearchDto.builder()
                .statusSet(Set.of(CompensationRequestStatus.COMPENSATION_WAITING_FOR_PAYMENT))
                .build();

        var request = objectMapper.writeValueAsString(requestDTO);
        ResultActions response = mockMvc.perform(post("/tasks/create")
                        .content(request)
                        .header("Authorization", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String actual = response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(actual).contains("\"ok\":true");
    }
}
```

### Пример 2: Service Test с моками

```java
@EmbeddedPostgres
@SpringBootTest(properties = "spring.main.cloud-platform=none")
@DisplayName("Проверка производственного календаря")
class CalendarServiceImplTest {
    
    @Autowired
    private CalendarService calendarService;
    
    @MockitoBean
    private CalendarRepository repository;
    
    @Test
    @DisplayName("Тест выходного дня")
    void testHoliday() {
        // Arrange
        LocalDate holiday = LocalDate.of(2022, 5, 1);
        when(repository.findByDate(holiday))
            .thenReturn(Optional.of(Calendar.builder().date(holiday).type(CalendarTypeEnum.HOLIDAY).build()));
        when(repository.findByDate(holiday.plusDays(1)))
            .thenReturn(Optional.of(Calendar.builder().date(holiday.plusDays(1)).type(CalendarTypeEnum.HOLIDAY).build()));
        
        // Act
        var result = calendarService.getNextWorkDate(holiday);
        
        // Assert
        assertThat(result).isEqualTo(holiday.plusDays(3));
    }
}
```

---

## Чеклист перед коммитом

- [ ] Все тесты проходят (`mvn test`)
- [ ] Покрытие кода ≥ 80% (проверить через `mvn jacoco:report`)
- [ ] Нет дублирования тестов
- [ ] Тесты независимы и могут запускаться в любом порядке
- [ ] Используются правильные аннотации (@Mock, @MockitoBean)
- [ ] Данные подготавливаются через SQL-скрипты (`@Sql`)
- [ ] Описание теста на русском языке в `@DisplayName`
- [ ] Нет System.out.println и логирования в тестах
- [ ] Тесты компилируются без предупреждений
- [ ] **Используется только AssertJ** для утверждений (нет `org.junit.jupiter.api.Assertions`)
- [ ] Для unit-тестов используется `@ExtendWith(MockitoExtension.class)`, а не `MockitoAnnotations.openMocks(this)`
- [ ] Используется цепочечное использование `assertThat` где уместно для сокращения кода
- [ ] SQL-скрипты находятся в `src/test/resources/sql/`
- [ ] Для интеграционных тестов с Spring контекстом используется `@MockitoBean`, а не `@MockBean`
- [ ] Мапперы тестируются только с помощью Unit-тестов (без `@SpringBootTest`)
- [ ] Тестовый класс и методы с `@Test` не имеют модификаторов доступа
- [ ] В тестовом классе отсутствуют неиспользуемые импорты

---

## Полезные команды Maven

```bash
# Запуск всех тестов
mvn test

# Запуск тестов конкретного класса
mvn test -Dtest=ClassNameTest

# Запуск тестов по паттерну
mvn test -Dtest=*ServiceTest

# Генерация отчета о покрытии
mvn jacoco:report

# Запуск с покрытием и отчетом в SonarQube
mvn clean test sonar:sonar
```

---

## Общие ошибки и как их избегать

| Ошибка | Как избежать |
|--------|--------------|
| Зависимость тестов друг от друга | Использовать `@Sql` для загрузки данных, `@BeforeEach` для подготовки |
| Тесты медленные | Мокировать внешние зависимости, использовать встроенную БД (`@EmbeddedPostgres`) |
| Непонятные названия | Использовать `@DisplayName` с описанием на русском |
| Слишком длинные тесты | Разбивать на несколько маленьких тестов |
| Дублирование кода | Выносить общую логику в базовые классы |
| Использование `assertEquals`, `assertTrue` и т.д. | Использовать только AssertJ: `assertThat().isEqualTo()`, `assertThat().isTrue()` |
| Явная типизация в тестах | Использовать `var` для локальных переменных, когда тип очевиден из контекста |
| Использование `@MockBean` вместо `@MockitoBean` | Для интеграционных тестов с `@SpringBootTest` всегда использовать `@MockitoBean` из `org.springframework.test.context.bean.override.mockito` |
| Ручная очистка данных | SQL-скрипты загружают данные, `@Transactional` откатывает транзакцию |
| Данные создают через repository.save() | Использовать SQL-скрипты в `src/test/resources/sql/` |
