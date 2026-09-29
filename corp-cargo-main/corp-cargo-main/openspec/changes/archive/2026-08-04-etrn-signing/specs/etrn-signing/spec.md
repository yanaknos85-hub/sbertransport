## Purpose

This change merges 4 existing specs (`etrn-card-modal`, `etrn-lock`, `etrn-signing-worklist`, `etrn-title3-sign`) into a single `etrn-signing` spec. All requirements are preserved without modification.

## MODIFIED Requirements

### Requirement: Worklist Table

The system SHALL display a paginated table of Etrn in the "Etrn Signing" tab.

**Etrn List:** `POST /etrn-cargo/list`

**Request body:**
```json
{
  "organizationId": "uuid",
  "pageSetting": {
    "page": 0,
    "size": 20
  }
}
```

**Response:**
```json
{
  "totalPages": 5,
  "totalElements": 100,
  "size": 20,
  "content": [
    {
      "id": "uuid",
      "humanReadableId": "ЭТрН-772812",
      "sla": "00:42",
      "status": "READY_TO_SIGN",
      "currentTitle": "T2",
      "senderName": "ООО Грузоотправитель",
      "receiverName": "АО Грузополучатель",
      "carrierName": "ООО ТК Перевозчик"
    }
  ],
  "number": 0,
  "first": true,
  "last": false
}
```

#### Scenario: Table displays with first page data
- **WHEN** the user opens the "Etrn Signing" tab
- **THEN** the system sends `POST /etrn-cargo/list` and displays columns:
  - `humanReadableId` (Etrn number)
  - `status` (status)
  - `sla` (control deadline)
  - `currentTitle` (current title)
  - `senderName`, `receiverName`, `carrierName` (participants)

#### Scenario: Clicking a row opens the card modal
- **WHEN** the user clicks on `humanReadableId`
- **THEN** the system opens `EtrnModal` with `cardId = record.id`

#### Scenario: Pagination works
- **WHEN** the user navigates to the next page
- **THEN** the system sends `POST /etrn-cargo/list` with `{ pageSetting: { page: 1, size: 20 } }` and updates the table

### Requirement: Summary Display

The card modal SHALL display summary information about the Etrn.

**Response GET /etrn-cargo/{id} (200):**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "humanReadableId": "ЭТрН-772812",
  "applicationNumber": "OT-0001-0002931",
  "routeNumber": "CT-0001-0002931",
  "status": "READY_TO_SIGN",
  "sla": "00:42",
  "timeZone": "Asia/Yekaterinburg",
  "currentTitle": "T2",
  "senderName": "ООО Грузоотправитель",
  "receiverName": "АО Грузополучатель",
  "carrierName": "ООО ТК Перевозчик",
  "cargoDescription": "Оборудование, 5 мест",
  "cargoPlaces": 5,
  "cargoWeightKg": 1250.00,
  "cargoLength": 120.0,
  "cargoWidth": 80.0,
  "cargoHeight": 100.0,
  "route": "Екатеринбург — Москва",
  "mrpaExpiresAt": "2026-07-31",
  "sesFullName": "Иванов Иван Иванович",
  "sesRole": "Генеральный директор",
  "sesEventDatetime": "2026-07-21T10:00:00",
  "sesEventId": "PE-001",
  "titleChain": [
    {"title": "T1", "signedAt": "2026-07-21T10:00:00", "signedBy": "uuid-сотрудника"},
    {"title": "T2", "signedAt": "2026-07-21T11:00:00", "signedBy": "uuid-сотрудника"},
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

**What is displayed in the modal:**
- `humanReadableId` — Etrn number in the header
- `status` — card status
- `sla` — control deadline
- `applicationNumber` — application number
- `routeNumber` — route number
- `senderName`, `receiverName`, `carrierName` — participants block
- `cargoDescription`, `cargoPlaces`, `cargoWeightKg` — cargo block
- `route` — route
- `mrpaExpiresAt` — MRPА expiry date
- `cargoLength`, `cargoWidth`, `cargoHeight` — dimensions (optional)
- `sesFullName`, `sesRole`, `sesEventDatetime`, `sesEventId` — authorized person block
- `titleChain` — title chain T1–T4
- `lockInfo` — current lock

#### Scenario: Empty title chain
- **WHEN** `titleChain` is an empty array or null
- **THEN** the message "Waiting for data from Korus" is displayed instead of the title chain block

### Requirement: Card Lock

The system SHALL lock Etrn when opening the modal card and unlock when closing it. The lock expires after **5 minutes** if the user has not closed the card.

**Endpoints:**
- `PUT /api/etrn-cargo/{cardId}/lock` — body: `{}` (userId extracted from JWT token)
- `DELETE /api/etrn-cargo/{cardId}/lock` — body: `{}` (userId extracted from JWT token)
- `cardId` = Etrn id (used as lock identifier)

**Responses:**
- `200 OK` — full card with updated `lockInfo`
- `409 Conflict` — `{ message, lockedBy, lockUntil }` (card already locked by another user)

#### Scenario: Lock on open
- **WHEN** the user clicks on the Etrn number in the table
- **THEN** the system sends `PUT /lock` without a body and opens the card
- **AND** `userId` is extracted from the JWT token on the server
- **AND** the lock is set for 5 minutes
- **AND** other users see the Etrn as locked

#### Scenario: Unlock on close
- **WHEN** the user closes the modal card (X / Escape / click outside)
- **THEN** the system sends `DELETE /lock` without a body
- **AND** the lock is removed, `lockInfo` becomes `null`

#### Scenario: Lock expiration
- **WHEN** 5 minutes have passed since the user opened the card without closing it
- **THEN** the lock expires, the signing button becomes disabled

#### Scenario: Lock conflict
- **WHEN** the card is already locked by another user when trying to open it (409)
- **THEN** the card opens in read-only mode without signing capability
- **AND** the message "Card locked by another user. Signing is unavailable." is displayed

### Requirement: T3 UK Sign — Stub

The "Sign with UK Sign" button SHALL be displayed in the card but **disabled** (disabled).
The signing logic implementation is moved to a separate task.

#### Scenario: "Sign with UK Sign" button is displayed
- **WHEN** the card is open, the lock is acquired, and data is loaded
- **THEN** the "Sign with UK Sign" button is displayed in the card in a disabled state (`disabled`)
