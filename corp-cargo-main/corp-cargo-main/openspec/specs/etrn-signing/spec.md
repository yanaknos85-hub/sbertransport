## Purpose

Описывает функциональность страницы «Подписание ЭТрН» в Планировщике: таблица рабочей очереди, модальная карточка ЭТрН, блокировка и заглушка подписания Т3.
## Requirements
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

### Requirement: Signing Eligibility Check

The system SHALL check the signer's eligibility when opening the Etrn card modal. The `useCheckSigningEligibility` mutation calls `GET /etrn-cargo/attorneyCheck` in parallel with `useEtrnCard`.

#### Scenario: Check is called when card opens
- **WHEN** user opens the Etrn card modal (visible=true, cardId present)
- **THEN** the system calls `useCheckSigningEligibility()` mutation in parallel with `useEtrnCard`

#### Scenario: Check is not called when card is closed
- **WHEN** the Etrn card is closed (visible=false)
- **THEN** the mutation is not called

#### Scenario: Check is re-called on re-open
- **WHEN** user closed the card and re-opens it
- **THEN** the mutation is called again

### Requirement: Buttons unlocked only on successful check

Buttons "Sign with UK Sign" and "View Document" are disabled by default. Button state depends on mutation status:
- `isLoading = true` → buttons disabled
- `isSuccess = true` → buttons enabled
- `isError = true` → buttons disabled

#### Scenario: Buttons enabled on successful check
- **WHEN** mutation returned 200 OK
- **THEN** `isSuccess = true` → buttons "Sign with UK Sign" and "View Document" enabled

#### Scenario: Buttons blocked during loading
- **WHEN** mutation is in progress (isLoading)
- **THEN** `isLoading = true` → buttons "Sign with UK Sign" and "View Document" disabled

#### Scenario: Buttons blocked on any error
- **WHEN** mutation returned 4xx, 5xx or network error
- **THEN** `isError = true` → buttons "Sign with UK Sign" and "View Document" disabled

### Requirement: Error handling via notification

On mutation error the system SHALL show a notification via `logger.toMessage('error', getErrorMessage(error))`.

#### Scenario: Error 403 Forbidden
- **WHEN** eligibility returned 403
- **THEN** `logger.toMessage('error', getErrorMessage(error))` is called
- **THEN** buttons remain disabled

#### Scenario: Error 500 Internal Server Error
- **WHEN** eligibility returned 500
- **THEN** `logger.toMessage('error', getErrorMessage(error))` is called
- **THEN** buttons remain disabled

### Requirement: Title fetch before signing

When the user clicks "Sign with UK Sign" button, the system SHALL fetch the XML payload for the title T3 via `GET /etrn-cargo/{etrnId}/title/` and pass the `content` field to the `SignT3Modal` for signing via КриптоПро.

The hook `useGetEtrnTitle` is called with `enabled: false`; the request is triggered manually via `refetch()` on the click handler. The modal is opened **only after** a successful response. On error, `logger.toMessage('error', getErrorMessage(error))` is called and the modal is not opened.

Response format:
```json
{
  "fileName": "first_title_20260821.xml",
  "content": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZpcnN0VGl0bGU+PC9GaXJzdFRpdGxlPg==",
  "creationTime": "2026-08-21T10:30:00"
}
```

#### Scenario: Successful fetch opens modal with content
- **WHEN** user clicks "Sign with UK Sign"
- **AND** `GET /etrn-cargo/{etrnId}/title/` returns 200 with valid DTO
- **THEN** the request is sent
- **AND** the `SignT3Modal` is opened with `content` prop equal to the response `content` field

#### Scenario: Failed fetch does not open modal
- **WHEN** user clicks "Sign with UK Sign"
- **AND** `GET /etrn-cargo/{etrnId}/title/` returns an error
- **THEN** the `SignT3Modal` is NOT opened
- **AND** `logger.toMessage('error', getErrorMessage(error))` is called

#### Scenario: No automatic fetch on card open
- **WHEN** the Etrn card is opened
- **AND** the user has not clicked "Sign with UK Sign"
- **THEN** `GET /etrn-cargo/{etrnId}/title/` is NOT called (because `enabled: false`)

#### Scenario: Empty content does not open modal
- **WHEN** user clicks "Sign with UK Sign"
- **AND** `GET /etrn-cargo/{etrnId}/title/` returns 200 with `content: ''`
- **THEN** the `SignT3Modal` is NOT opened
- **AND** `logger.toMessage('error', Etrn.card.titleContentEmpty)` is called
- **THEN** the КриптоПро plugin is never invoked with an empty payload

### Requirement: Submit signed title after signing

After successful signing in КриптоПро, the system SHALL submit the signed payload to the backend via `POST /etrn-cargo/{etrnId}/title/send` with body:

```json
{
  "fileName": "first_title_20260821.xml",
  "file": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZpcnN0VGl0bGU+PC9GaXJzdFRpdGxlPg==",
  "signature": "MEUCIQD1a2Gx...base64encoded",
  "creationTime": "2026-08-21T10:30:00"
}
```

`file` is the original base64-XML from the GET response (not decoded). `signature` is the base64 payload from КриптоПро. `fileName` and `creationTime` come from the same GET response.

The hook `useSendEtrn` (in `src/api/etrn-signature/etrn-signature.ts`) wraps `useAPIMutation` with `urlParams: { etrnId: cardId }` and the body above. The mutation is called from `SignT3Modal.handleOk` immediately after `signFileByCertificate` resolves.

#### Scenario: Successful submit after signing
- **WHEN** user clicks "Подписать и отправить" in `SignT3Modal`
- **AND** `signFileByCertificate` resolves with a base64 signature
- **THEN** `POST /etrn-cargo/{etrnId}/title/send` is sent with body `{ fileName, file, signature, creationTime }`
- **AND** `file` equals the original `content` from the previous GET (base64, not decoded)
- **AND** on `200` response, `onSigned()` is called and the modal closes

#### Scenario: Failed submit does not close modal
- **WHEN** `POST /etrn-cargo/{etrnId}/title/send` returns 4xx/5xx or network error
- **THEN** `addMessage(Messages.SigningError)` is called
- **AND** the `SignT3Modal` remains open
- **AND** `onSigned()` is NOT called
- **AND** the user can retry by clicking "Подписать и отправить" again

#### Scenario: signing_error from КриптоПро prevents POST
- **WHEN** `signFileByCertificate` rejects (CSP error, no certificate, signing failed)
- **THEN** `POST` is NOT sent
- **AND** `addMessage(Messages.SigningError)` is called
- **AND** the `SignT3Modal` remains open

### Requirement: CryptoPro mock mode for local development

The `useCryptoPro` hook SHALL support a mock-mode bypass for environments without a КриптоПро USB-token or browser plugin (local dev / CI / demo). When `REACT_APP_MOCKED_CRYPTO_PRO=TRUE` is set in `.env`, the hook MUST:

- skip calls to `getSystemInfo` and `getCertificates` from `crypto-pro-actual-cades-plugin`,
- immediately resolve with `messages: []` and `certificates: MOCK_CERTIFICATES` (a hardcoded test certificate from `utils/cryptoPro.mock.ts`),
- never block rendering on plugin availability.

This mode is intended **only** for dev/staging. In production the flag MUST be unset.

#### Scenario: Mock mode is enabled
- **WHEN** `IS_MOCKED_CRYPTO_PRO` is true
- **AND** the `SignT3Modal` is opened
- **THEN** the certificate list contains the mock certificate
- **AND** `getSystemInfo` is not called

#### Scenario: Mock mode is disabled (production)
- **WHEN** `IS_MOCKED_CRYPTO_PRO` is false or unset
- **AND** the `SignT3Modal` is opened
- **THEN** the hook calls `getSystemInfo` and `getCertificates` from the КриптоПро plugin

### Requirement: ReasonCodes mapping

The system SHALL contain a mapping of 13 reasonCodes to Russian texts in the `EtrnSignature` module (`ETRN_REASON_CODES_MAP`).

#### Scenario: NO_SIGNER_ROLE mapping
- **WHEN** reasonCode = "NO_SIGNER_ROLE"
- **THEN** displayed text: "Нет роли «Подписант УКЭП»"

#### Scenario: USER_INACTIVE mapping
- **WHEN** reasonCode = "USER_INACTIVE"
- **THEN** displayed text: "Пользователь неактивен"

#### Scenario: NOT_BANK_EMPLOYEE mapping
- **WHEN** reasonCode = "NOT_BANK_EMPLOYEE"
- **THEN** displayed text: "Не является сотрудником Банка"

#### Scenario: DISPATCHER_NOT_FOUND mapping
- **WHEN** reasonCode = "DISPATCHER_NOT_FOUND"
- **THEN** displayed text: "Диспетчер не найден"

#### Scenario: DISPATCHER_INACTIVE mapping
- **WHEN** reasonCode = "DISPATCHER_INACTIVE"
- **THEN** displayed text: "Диспетчер неактивен"

#### Scenario: ETRN_SIGNING_DISABLED mapping
- **WHEN** reasonCode = "ETRN_SIGNING_DISABLED"
- **THEN** displayed text: "Подписание ЭТрН отключено"

#### Scenario: ATTORNEY_NUMBER_MISSING mapping
- **WHEN** reasonCode = "ATTORNEY_NUMBER_MISSING"
- **THEN** displayed text: "Не указан номер МЧД"

#### Scenario: ATTORNEY_NOT_YET_VALID mapping
- **WHEN** reasonCode = "ATTORNEY_NOT_YET_VALID"
- **THEN** displayed text: "МЧД ещё не действует"

#### Scenario: ATTORNEY_EXPIRED mapping
- **WHEN** reasonCode = "ATTORNEY_EXPIRED"
- **THEN** displayed text: "Истёк срок действия МЧД"

#### Scenario: ATTORNEY_DATES_INVALID mapping
- **WHEN** reasonCode = "ATTORNEY_DATES_INVALID"
- **THEN** displayed text: "Неверные даты МЧД"

#### Scenario: ORGANIZATION_MISMATCH mapping
- **WHEN** reasonCode = "ORGANIZATION_MISMATCH"
- **THEN** displayed text: "Не совпадает организация"

#### Scenario: AUTHORITY_RECORD_AMBIGUOUS mapping
- **WHEN** reasonCode = "AUTHORITY_RECORD_AMBIGUOUS"
- **THEN** displayed text: "Запись полномочий неоднозначна"

#### Scenario: AUTHORITY_SOURCE_UNAVAILABLE mapping
- **WHEN** reasonCode = "AUTHORITY_SOURCE_UNAVAILABLE"
- **THEN** displayed text: "Источник данных недоступен"

