# Spec: etrn-org-replication

## Purpose

Определение требований к репликации справочников (сотрудники, организации, департаменты) из корпоративных топиков Kafka.

## Requirements

### Requirement: Репликация справочников
Система SHALL реплицировать справочники сотрудников, организаций и департаментов из корпоративных топиков Kafka.

**Источники:** внешняя библиотека `ru.sberbank.ditsib.transport.messaging.messages` (в поставке `app_cargo_oto`).

**Топики:**
- `service.organization.employee` → `employee` (schema: `etrn_cargo`)
- `service.organization` → `organization` (schema: `etrn_cargo`)
- `service.organization.department` → `department` (schema: `etrn_cargo`)
- `service.organization.position` → репликация **не реализована** (заглушка)

**Выбранный подход:** Spring Cloud Stream functional (Consumer<Message<T>>), без @KafkaListener.

### Реализованные Consumer-бены (ListenerConfig.java)

```java
@Bean
public Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentService ds) { ... }

@Bean
public Consumer<Message<EmployeeMessage>> employeesInput(EmployeeService es, DepartmentService ds) { ... }

@Bean
public Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationService os, OrganizationGroupRepository ggr) { ... }
```

### Message-классы (из внешней библиотеки)

```java
ru.sberbank.ditsib.transport.messaging.messages.{
    EmployeeMessage,
    OrganizationMessage,
    DepartmentMessage,
    PositionMessage
}
```

### База данных

Реплицированные таблицы находятся в схеме `etrn_cargo`:

| Таблица | Ключевые поля | FK |
|---------|---------------|-----|
| `employee` | id PK, userId, personnelNumber, firstName, lastName, departmentId | department_id → department(id) |
| `organization` | id PK, digitId, officialName, address, organizationGroup_id | organization_group_id → organization_group(id) |
| `department` | id PK, parentId, organizationId, humanReadableId, departmentName | — |
| `organization_group` | id PK, name, internal | — |

### Мапперы (MapStruct)

- `EmployeeMapper.fromMessage(EmployeeMessage)` → `Employee`
- `OrganizationMapper.fromMessage(OrganizationMessage)` → `Organization`
- `DepartmentMapper.fromMessage(DepartmentMessage)` → `Department`

#### Scenario: Подписание T3
- **WHEN** подписан T3
- **THEN** `titleChain.T3.signedBy` = наименование подписанта из КОРУС + `titleChain.T3.signedAt` = текущее время

#### Scenario: Ошибка подписания
- **WHEN** данные из КОРУС некорректны или недоступны
- **THEN** запись в `etrn_audit` с деталями ошибки, статус карточки не меняется
