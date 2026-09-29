# Spec: etrn-card

## MODIFIED Requirements

### Requirement: Создание ЭТрН
Система SHALL создать запись ЭТрН при получении POST /api/etrn-cargo.

- **Вход:** `humanReadableId`, `senderName`, `receiverName`, `carrierName`, `timeZone`
- **Выход (201):** `EtrnDto` — `id`, `humanReadableId`, `status`, `currentTitle`, `sla`, `senderName`, `receiverName`, `carrierName`, `createdAt`, `updatedAt`
- **Транзакция:** 1 unit-of-work — `etrn` + `etrn_audit`
- **Идемпотентность:** при повторном запросе с тем же `humanReadableId` — обновление, не дублирование
- **Схема:** `etrn_cargo`
- **Статус при создании:** `IDENTIFIED` (переход `READY_TO_SIGN` — Sprint 2)

#### Scenario: Успешное создание
- **WHEN** POST /api/etrn-cargo с валидным `humanReadableId`
- **THEN** 201 + `etrn` со статусом `IDENTIFIED`

#### Scenario: Дубликат
- **WHEN** POST /api/etrn-cargo с существующим `humanReadableId`
- **THEN** 201 (создана новая) — idempotency на уровне БД (UNIQUE constraint)

---

### Requirement: Список ЭТрН (рабочая очередь)
Система SHALL возвращать список ЭТрН с пагинацией и сортировкой.

- **Тело:** `pageSetting.page`, `pageSetting.size`, `sortSetting.directionAsc`, `sortSetting.property`
- **Фильтры:** `humanReadableId` (string), `statusFilter` (string), `lockedByMe` (UUID)
- **Ответ:** `Page<EtrnJournalDto>` — 10 полей

#### DTO: EtrnJournalDto

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

#### Scenario: Первая страница
- **WHEN** POST /api/etrn-cargo/list c `pageSetting: {page: 0, size: 100}`
- **THEN** 200 + первые 100 записей

#### Scenario: Сортировка по дате создания
- **WHEN** POST /api/etrn-cargo/list c `sortSetting: {directionAsc: false, property: "CREATION_DATE"}`
- **THEN** 200 + записи от новых к старым

---

### Requirement: Детальная информация
Система SHALL возвращать полную информацию об ЭТрН по UUID.

#### DTO: EtrnDetailDto

```json
{
  "id": "uuid",
  "humanReadableId": "ЭТрН-772812",
  "status": "IDENTIFIED",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "timeZone": "Asia/Yekaterinburg",
  "sla": "00:42",
  "currentTitle": null,
  "active": true,
  "titleChain": [],
  "verifications": null,
  "lockInfo": null,
  "createdAt": "2026-07-22T10:00:00",
  "updatedAt": "2026-07-22T10:00:00",
  "version": 0
}
```

#### Scenario: Успешный запрос
- **WHEN** GET /api/etrn-cargo/{uuid}
- **THEN** 200 + `EtrnDetailDto` (все поля)

#### Scenario: Не найдена
- **WHEN** GET /api/etrn-cargo/{unknown}
- **THEN** 404 + `EtrnNotFoundException` с русским описанием

---

### Requirement: Force-transition
Система SHALL принудительно переводить ЭТрН в целевой статус.

- **POST** `/api/etrn-cargo/{id}/force-transition` → `{"targetStatus": "...", "reason": "..."}`
- **Допустимые исходные статусы (allowlist):** `WAIT_CONDITIONS`, `WAIT_KORUS_CONFIRMATION`, `OUTCOME_UNKNOWN`, `ERROR`
- **Обязательно:** поле `reason` (запись в `etrn_audit`)
- **Аудит:** запись в `etrn_audit` с `action` = "Принудительный переход: ..."

#### Scenario: Успешный force-transition
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `OUTCOME_UNKNOWN` в `PROCESS_COMPLETED`
- **THEN** 200 + `status` = `PROCESS_COMPLETED` + запись в `etrn_audit`

#### Scenario: Недопустимый переход
- **WHEN** POST /api/etrn-cargo/{id}/force-transition из `IDENTIFIED` в `PROCESS_COMPLETED`
- **THEN** 400 + `InvalidTransitionException` с русским описанием

#### Scenario: Без причины
- **WHEN** POST /api/etrn-cargo/{id}/force-transition без `reason`
- **THEN** 400 + `BadRequestException` с русским описанием
