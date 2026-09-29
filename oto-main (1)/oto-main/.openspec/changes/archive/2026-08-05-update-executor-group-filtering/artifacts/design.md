# Design: Обновить логику фильтрации заявок по группам исполнителей

## Что меняется

### DTO запроса

Добавить поле `emptyExecutorGroup` (boolean) в DTO запроса фильтрации заявок:

```java
// Пример: CargoQueryRequest.java или аналогичный DTO
private List<String> executorGroupIds;
private Boolean emptyExecutorGroup;  // новое поле
```

### Бизнес-логика (Service слой)

Реализовать три ветки логики в методе фильтрации:

```java
public void applyExecutorGroupFilter(CriteriaBuilder cb, Root<Cargo> root, 
                                     List<Predicate> predicates, CargoFilter filter) {
    
    // Режим 1: Все группы — фильтрация отсутствует
    if (!Boolean.TRUE.equals(filter.getEmptyExecutorGroup()) 
        && CollectionUtils.isEmpty(filter.getExecutorGroupIds())) {
        // ничего не добавляем в predicates
        return;
    }
    
    // Режим 2: Без групп — только заявки без группы исполнителя
    if (Boolean.TRUE.equals(filter.getEmptyExecutorGroup())) {
        predicates.add(cb.isNull(root.get("executorGroupId")));
        // или: predicates.add(cb.equal(root.get("executorGroupId"), null));
        return;
    }
    
    // Режим 3: Конкретные группы — фильтрация по ID
    if (!CollectionUtils.isEmpty(filter.getExecutorGroupIds())) {
        predicates.add(root.get("executorGroupId").in(filter.getExecutorGroupIds()));
    }
}
```

### Репозиторийный слой

Модифицировать метод построения Criteria в CargoRepository (или аналогичном), добавив обработку нового поля `emptyExecutorGroup`.

## Где менять

1. **DTO запроса** — файл, содержащий параметры фильтрации запроса (например, `CargoRequest.java`, `CargoFilter.java`, или аналогичный)
2. **Service слой** — метод, применяющий фильтрацию к Criteria/JPQL запросу (например, `CargoService.buildCriteria()`, `CargoRepository.buildQuery()`)
3. **Entity Cargo** — убедиться, что поле `executorGroupId` доступно для фильтрации (обычно это `String` или `UUID`)

## Зависимости

- Не требует изменений в базе данных
- Не требует миграций данных
- Изменение назад совместимо (новое поле nullable)

## Риски

- **Совместимость с фронтендом**: фронтенд должен передавать новое поле `emptyExecutorGroup` в теле запроса
- **Исторические данные**: необходимо убедиться, что заявки без группы корректно хранятся (executorGroupId = null)

## Альтернативы

Рассматривалась альтернатива — кодирование режима «Без групп» через специальный маркер в списке `executorGroupIds` (например, пустая строка). Отказались в пользу явного булевого поля — это более читаемо и безопасно типизировано.
