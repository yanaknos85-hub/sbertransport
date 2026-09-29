# Spec: etrn-queue

## Purpose

Определение требований к рабочей очереди ЭТрН для банковских пользователей.

## Requirements

### Requirement: Рабочая очередь
Система SHALL предоставлять список ЭТрН для рабочей очереди Банка.

- **Метод:** POST /api/etrn-cargo/list
- **Тело запроса:** `pageSetting` (page, size) + `sortSetting` (directionAsc, property) + фильтры
- **Ответ:** `Page<EtrnJournalDto>` — 10 полей

### DTO: EtrnJournalDto

```json
{
  "id": "uuid",
  "humanReadableId": "ЭТрН-772812",
  "sla": "00:42",
  "status": "IDENTIFIED",
  "currentTitle": null,
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "createdAt": "2026-07-22T10:00:00",
  "updatedAt": "2026-07-22T10:00:00"
}
```

| Поле | Тип | Источник |
|------|-----|----------|
| `id` | UUID | `etrn.id` |
| `humanReadableId` | String | `etrn.humanReadableId` |
| `sla` | String | `etrn.sla` |
| `status` | String | `etrn.status` |
| `currentTitle` | String | Вычисляется в `EtrnMapper`: последний титул в `titleChain` с заполненными `signedAt` и `signedBy`, при этом все предыдущие титулы (0..N) также должны быть подписаны. При нарушении последовательности — `null`. |
| `senderName` | String | `etrn.sender_name` |
| `receiverName` | String | `etrn.receiver_name` |
| `carrierName` | String | `etrn.carrier_name` |
| `createdAt` | LocalDateTime | `etrn.created_at` |
| `updatedAt` | LocalDateTime | `etrn.updated_at` |

### Тело запроса

```json
{
  "pageSetting": {"page": 0, "size": 100},
  "sortSetting": {"directionAsc": false, "property": "CREATION_DATE"},
  "humanReadableId": "ЭТрН-772812",
  "statusFilter": "IDENTIFIED",
  "lockedByMe": "uuid-текущего-пользователя"
}
```

| Поле | Тип | Обязательность | Описание |
|------|-----|---------------|----------|
| `pageSetting` | object | REQUIRED | page (int) + size (int) |
| `sortSetting` | object | OPTIONAL | directionAsc (boolean) + property (String) |
| `humanReadableId` | String | OPTIONAL | Поиск по конкретной ЭТрН |
| `statusFilter` | String | OPTIONAL | null = "Все заявки". Иначе код статуса: `IDENTIFIED`, `READY_FOR_BANK_ACTION`, `PROCESS_COMPLETED` |
| `lockedByMe` | UUID | OPTIONAL | null = без фильтра. UUID = поиск ЭТрН, где `lock_info.userId = {uuid}` |

### Проекция очереди
Система SHALL включать все ЭТрН (включая active = false) в POST /api/etrn-cargo/list.

#### Scenario: Первая страница
- **WHEN** POST /api/etrn-cargo/list с `pageSetting: {page: 0, size: 20}`
- **THEN** 200 + первые 20 записей

#### Scenario: Сортировка по дате создания
- **WHEN** POST /api/etrn-cargo/list с `sortSetting: {directionAsc: false, property: "CREATION_DATE"}`
- **THEN** 200 + записи от новых к старым

#### Scenario: Поиск по humanReadableId
- **WHEN** POST /api/etrn-cargo/list с `{"humanReadableId": "ЭТрН-772812"}`
- **THEN** 200 + карточка с указанным humanReadableId

#### Scenario: Фильтр по статусу
- **WHEN** POST /api/etrn-cargo/list с `{"statusFilter": "IDENTIFIED"}`
- **THEN** 200 + только ЭТрН со статусом IDENTIFIED

#### Scenario: Фильтр "По моей блокировке"
- **WHEN** POST /api/etrn-cargo/list с `{"lockedByMe": "550e8400-e29b-41d4-a716-446655440000"}`
- **THEN** 200 + только ЭТрН, где `lock_info.userId = "550e8400-e29b-41d4-a716-446655440000"`
