# Tasks: Обновить логику фильтрации заявок по группам исполнителей

## Задача 1: Изменить тип поля `isEmptyExecutorGroup` в CargoRequestDto

**Файл:** `application/src/main/java/ru/sberbank/transport/oto/cargo/dto/cargo/CargoRequestDto.java`

**Что сделать:**
- Изменить тип поля `isEmptyExecutorGroup` с `boolean` на `Boolean` (обёрточный тип)
- Это позволит различать три состояния: `null` (не отправлено), `true` (выбрано "Без групп"), `false` (выбрано "Все группы" или не указано)

**Текущий код:**
```java
@Schema(description = "UUID групп исполнителей")
boolean isEmptyExecutorGroup
```

**Новый код:**
```java
@Schema(description = "Флаг фильтрации заявок без группы исполнителя", example = "true")
Boolean isEmptyExecutorGroup
```

**Критерии готовности:**
- Поле переименовано и тип изменён на `Boolean`
- Файл компилируется

---

## Задача 2: Обновить логику фильтрации в OtoEngineerServiceImpl

**Файл:** `application/src/main/java/ru/sberbank/transport/oto/cargo/service/impl/OtoEngineerServiceImpl.java`

**Что сделать:**
- Заменить текущую логику фильтрации по executorGroupIds на корректную с обработкой трёх режимов (if-else)

**Текущий код (строки ~103-110):**
```java
if (!CollectionUtils.isEmpty(dto.executorGroupIds()) && !dto.isEmptyExecutorGroup()) {
    predicate = b.and(predicate, root.get(Request_.executorGroupId).in(dto.executorGroupIds()));
}

if (dto.isEmptyExecutorGroup()) {
    predicate = b.and(predicate, root.get(Request_.executorGroupId).isNull());
}
```

**Новый код:**
```java
// Режим 1: Фильтрация по конкретным группам исполнителей
if (!CollectionUtils.isEmpty(dto.executorGroupIds()) 
    && !Boolean.TRUE.equals(dto.isEmptyExecutorGroup())) {
    predicate = b.and(predicate, root.get(Request_.executorGroupId).in(dto.executorGroupIds()));
    
// Режим 2: Фильтрация по заявкам без группы исполнителя
} else if (Boolean.TRUE.equals(dto.isEmptyExecutorGroup())) {
    predicate = b.and(predicate, root.get(Request_.executorGroupId).isNull());
    
// Режим 3: Фильтрация отсутствует (Все группы) — ничего не добавляем
}
```

**Пояснение:**
- `Boolean.TRUE.equals()` — безопасная проверка для `Boolean` типа (не выбросит NPE при null)
- `if-else` конструкция исключает пересечение условий
- Режим 3 неявный — если не выполнились первые два условия, фильтрация не применяется

**Критерии готовности:**
- Логика заменена на if-else структуру
- Три режима корректно обрабатываются
- Файл компилируется

---

## Задача 3: Добавить/обновить unit-тесты для новых режимов фильтрации

**Файл:** `application/src/test/java/ru/sberbank/transport/oto/cargo/service/impl/OtoEngineerServiceImplTest.java` (создать или обновить существующий)

**Что сделать:**
- Написать тесты для трёх режимов фильтрации

**Сценарии тестирования:**

1. **Тест: "Все группы" — фильтрация отсутствует**
   - `executorGroupIds` = `[]` (пустой список или null)
   - `isEmptyExecutorGroup` = `false`
   - Ожидаемый результат: в predicate не добавляются условия по executorGroupId

2. **Тест: "Без групп" — только заявки без группы**
   - `executorGroupIds` = `[]` (пустой список или null)
   - `isEmptyExecutorGroup` = `true`
   - Ожидаемый результат: в predicate добавляется условие `isNull(executorGroupId)`

3. **Тест: "Конкретные группы" — фильтрация по ID**
   - `executorGroupIds` = `[uuid1, uuid2]`
   - `isEmptyExecutorGroup` = `false`
   - Ожидаемый результат: в predicate добавляется условие `in(executorGroupId, uuid1, uuid2)`

4. **Тест: "emptyExecutorGroup=true" имеет приоритет**
   - `executorGroupIds` = `[uuid1, uuid2]`
   - `isEmptyExecutorGroup` = `true`
   - Ожидаемый результат: срабатывает режим "Без групп", условие `in(...)` не добавляется

**Критерии готовности:**
- Все 4 сценария покрыты тестами
- Тесты проходят успешно

---

## Задача 4: Интеграционное тестирование и ручная проверка

**Что сделать:**
- Запустить существующие тесты проекта для проверки регрессии
- Проверить сборку проекта

**Команды:**
```bash
mvn clean test -Dtest=OtoEngineerServiceImplTest
mvn clean compile
```

**Критерии готовности:**
- Все тесты проходят
- Проект собирается без ошибок

---

## Порядок выполнения

1. Задача 1 — изменить DTO
2. Задача 2 — обновить логику фильтрации
3. Задача 3 — написать тесты
4. Задача 4 — проверка и регрессия

Общий объём: ~4 задачи.
