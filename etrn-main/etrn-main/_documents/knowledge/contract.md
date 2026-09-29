# API-контракт для фронта — ЭТрН (etrn-service)

**Base URL:** `/api/etrn-cargo`
**Порт:** `8227`

---

## 1. Создание ЭТрН

```
POST /api/etrn-cargo
```

### Request body

```json
{
  "humanReadableId": "ЭТрН-772812",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "timeZone": "Asia/Yekaterinburg"
}
```

| Поле | Тип | Обязательность | Описание |
|------|-----|---------------|----------|
| `humanReadableId` | string | **Да** | Уникальный бизнес-ключ |
| `senderName` | string | Нет | Название грузоотправителя |
| `receiverName` | string | Нет | Название грузополучателя |
| `carrierName` | string | Нет | Название перевозчика |
| `timeZone` | string | Нет | Часовой пояс |

### Response 201

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "humanReadableId": "ЭТрН-772812",
  "status": "IDENTIFIED",
  "currentTitle": null,
  "sla": null,
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "createdAt": "2026-07-22T10:00:00",
  "updatedAt": "2026-07-22T10:00:00"
}
```

### Response 400

```json
{
  "timestamp": "2026-07-22T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Поле humanReadableId обязательно"
}
```

---

## 2. Список ЭТрН (рабочая очередь)

```
POST /api/etrn-cargo/list
```

### Request body

```json
{
  "pageSetting": {
    "page": 0,
    "size": 100
  },
  "sortSetting": {
    "directionAsc": false,
    "property": "CREATION_DATE"
  },
  "humanReadableId": "ЭТрН-772812",
  "statusFilter": "IDENTIFIED",
  "lockedByMe": "550e8400-e29b-41d4-a716-446655440000"
}
```

| Поле | Тип | Обязательность | Описание |
|------|-----|---------------|----------|
| `pageSetting` | object | **Да** | `page` (int), `size` (int) |
| `sortSetting` | object | Нет | `directionAsc` (boolean), `property` (string) |
| `humanReadableId` | string | Нет | Поиск по конкретной ЭТрН |
| `statusFilter` | string | Нет | Значения: `IDENTIFIED`, `WAIT_CONDITIONS`, `READY_FOR_BANK_ACTION`, `PROCESS_COMPLETED` |
| `lockedByMe` | UUID | Нет | `null` = без фильтра. UUID = ЭТрН, где `lock_info.userId = {uuid}` |

### Response 200

```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

---

## 3. Детальная информация об ЭТрН

```
GET /api/etrn-cargo/{id}
```

### Response 200

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "humanReadableId": "ЭТрН-772812",
  "applicationNumber": "OT-0001-0002931",
  "routeNumber": "CT-0001-0002931",
  "status": "IDENTIFIED",
  "sla": "00:42",
  "timeZone": "Asia/Yekaterinburg",
  "currentTitle": "T2",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "cargoDescription": "Оборудование, 5 мест",
  "cargoPlaces": 5,
  "cargoWeightKg": 1250.00,
  "route": "Екатеринбург — Москва",
  "mrpaExpiresAt": "2026-07-31",
  "cargoLength": 120.0,
  "cargoWidth": 80.0,
  "cargoHeight": 100.0,
  "sesFullName": "Иванов Иван Иванович",
  "sesRole": "Генеральный директор",
  "sesEventDatetime": "2026-07-21T10:00:00",
  "sesEventId": "PE-001",
  "titleChain": [
    {"title": "T1", "signedAt": "2026-07-21T10:00:00", "signedBy": "Иванов Иван Иванович"},
    {"title": "T2", "signedAt": "2026-07-21T11:00:00", "signedBy": "Петров Пётр Петрович"},
    {"title": "T3", "signedAt": null, "signedBy": null},
    {"title": "T4", "signedAt": null, "signedBy": null}
  ],
  "verifications": {
    "checks": [
      {"name": "Формат ЭТрН (XML)", "passed": true},
      {"name": "КНД", "passed": true},
      {"name": "XSD версия", "passed": true}
    ],
    "overallPassed": true,
    "verifiedAt": "2026-07-22T10:15:00"
  },
  "lockInfo": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "lockUntil": "2026-07-23T14:20:00"
  },
  "active": true,
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-22T12:30:00",
  "version": 0
}
```

### Response 404

```json
{
  "timestamp": "2026-07-22T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "ЭТрН 3fa85f64-... не найдена"
}
```

---

## 5. Блокировка карточки (lock)

Блокировка устанавливается **при открытии модального окна подписания Т3** и предотвращает одновременное подписание другим пользователем.

- **Тип:** краткоживущий lease (TTL)
- **Длительность:** `lock.ttl-seconds` (по умолчанию 300 сек = 5 минут, конфигурируемо)
- **Ограничение:** не более одной активной блокировки на карточку
- **Статус карточки НЕ меняется** при установке блокировки
- **Аудит:** каждое действие (lock / unlock / auto-unlock) записывается в `etrn_audit`
- **Аутентификация:** `userId` извлекается из OAuth2 JWT-токена — **не передается в теле запроса**

### 5.1. Установка блокировки

```
PUT /api/etrn-cargo/{id}/lock
```

**Request body:** не требуется — `userId` определяется из JWT-токена.

#### Response 200

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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
  "lockInfo": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "lockUntil": "2026-07-23T14:20:00"
  },
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-22T14:15:00",
  "version": 0
}
```

#### Response 409 — карточка уже заблокирована другим пользователем

```json
{
  "message": "Карточка уже заблокирована",
  "lockedBy": "550e8400-e29b-41d4-a716-446655440000",
  "lockUntil": "2026-07-23T14:20:00"
}
```

**Идемпотентность:** повторный `PUT /lock` с тем же `userId` при активной блокировке — возвращает 200 без изменений `lock_info`.

#### Audit

При успешной установке блокировки записывается: `"Установлена блокировка (lock) пользователем <userId>"`

### 5.2. Снятие блокировки

Выполняется **при закрытии модального окна** (кнопка «Закрыть», «Отмена» или навигация прочь).

```
DELETE /api/etrn-cargo/{id}/lock
```

**Request body:** не требуется — `userId` определяется из JWT-токена.

#### Response 200

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-22T14:16:00",
  "version": 1
}
```

#### Response 400 — блокировку пытается снять не тот пользователь

```json
{
  "timestamp": "2026-07-22T14:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Блокировку может снять только пользователь, который её установил"
}
```

#### Response 400 — блокировка не установлена

```json
{
  "timestamp": "2026-07-22T14:20:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Блокировка не установлена"
}
```

#### Response 200 — истёкшая блокировка

Если `lockUntil < now` — блокировка считается снятой автоматически, возвращается 200 + `lockInfo: null`, пишется аудит `"Снята истёкшая блокировка (unlock) пользователем <userId>"`.

#### Audit

При успешной разблокировке записывается: `"Снята блокировка (unlock) пользователем <userId>"`

### 5.3. Автоматическое снятие (истечение TTL)

- Если `lock_info.lockUntil < now` → `lock_info` сбрасывается в `null`
- **Scheduler:** `EtrnServiceImpl.scheduleAutoUnlock()`, cron `0 0/5 * * * *` (каждые 5 минут)
- Проверка истечения выполняется **при каждом запросе** к `/lock`, `DELETE /{id}/lock`, а также scheduler'ом
- Истёкший lock не возвращается как `lockedByMe` в списке
- При попытке `/lock` на истёкшем lock — блокировка устанавливается заново (как если бы её не было)
- При автоматическом снятии scheduler'ом пишется аудит: `"Автоматически снята истёкшая блокировка с ЭТрН <etrnId>"`

### 5.4. Поведение при подписании

Перед началом операции подписания система повторно проверяет:

1. Т3 в статусе `READY_TO_SIGN` (card = `READY_FOR_BANK_ACTION`)
2. Lock активен и принадлежит текущему пользователю (`lock_info.userId == currentUser`)
3. Lock не истёк (`lock_info.lockUntil > now`)

Если условие 2 или 3 не выполнено → 403/409 (чужой lock):

```json
{
  "message": "Карточка уже заблокирована",
  "lockedBy": "550e8400-e29b-41d4-a716-446655440000",
  "lockUntil": "2026-07-23T14:20:00"
}
```
