## ADDED Requirements

### Requirement: Frontend вызывает проверку допуска при открытии карточки
Система SHALL вызывать `GET /api/v1/etrn/signing/eligibility` при открытии карточки ЭТрН через `useCheckSigningEligibility` mutation, параллельно с загрузкой данных карточки `useEtrnCard`.

#### Scenario: Проверка вызывается при открытии карточки
- **WHEN** пользователь открывает карточку ЭТрН (EtrnModal open, cardId present, visible=true)
- **THEN** система вызывает `useCheckSigningEligibility()` как mutation параллельно с `useEtrnCard`

#### Scenario: Проверка не вызывается при закрытой карточке
- **WHEN** карточка ЭТрН закрыта (visible=false)
- **THEN** mutation не вызывается

#### Scenario: Проверка вызывается при повторном открытии
- **WHEN** пользователь закрыл карточку и открыл её снова
- **THEN** mutation вызывается заново

### Requirement: Кнопки разблокируются только при успешной проверке
Кнопки «Подписать УКЭП» и «Посмотреть документ» disabled по умолчанию. Состояние кнопок зависит от статуса mutation:
- `isLoading = true` → buttons disabled
- `isSuccess = true` → buttons enabled
- `isError = true` → buttons disabled

#### Scenario: Кнопки активированы при успешной проверке
- **WHEN** mutation вернула 200 OK
- **THEN** `isSuccess = true` → кнопки «Подписать УКЭП» и «Посмотреть документ» enabled

#### Scenario: Кнопки заблокированы во время загрузки
- **WHEN** mutation вызывается (запрос в процессе)
- **THEN** `isLoading = true` → кнопки «Подписать УКЭП» и «Посмотреть документ» disabled

#### Scenario: Кнопки заблокированы при любой ошибке
- **WHEN** mutation вернула 4xx, 5xx или сетевую ошибку
- **THEN** `isError = true` → кнопки «Подписать УКЭП» и «Посмотреть документ» disabled

### Requirement: Обработка ошибок через notification
При ошибке mutation система SHALL показывать уведомление пользователю через `logger.toMessage('error', getErrorMessage(error))`.

#### Scenario: Ошибка 403 Forbidden
- **WHEN** eligibility вернул 403
- **THEN** вызывается `logger.toMessage('error', getErrorMessage(error))`
- **THEN** кнопки disabled

#### Scenario: Ошибка 500 Internal Server Error
- **WHEN** eligibility вернул 500
- **THEN** вызывается `logger.toMessage('error', getErrorMessage(error))`
- **THEN** кнопки disabled

### Requirement: ReasonCodes маппинг
Система SHALL содержать маппинг 13 reasonCodes → русские тексты в модуле EtrnSignature (`ETRN_REASON_CODES_MAP`).

#### Scenario: Маппинг NO_SIGNER_ROLE
- **WHEN** reasonCode = "NO_SIGNER_ROLE"
- **THEN** отображается текст: "Нет роли «Подписант УКЭП»"

#### Scenario: Маппинг USER_INACTIVE
- **WHEN** reasonCode = "USER_INACTIVE"
- **THEN** отображается текст: "Пользователь неактивен"

#### Scenario: Маппинг NOT_BANK_EMPLOYEE
- **WHEN** reasonCode = "NOT_BANK_EMPLOYEE"
- **THEN** отображается текст: "Не является сотрудником Банка"

#### Scenario: Маппинг DISPATCHER_NOT_FOUND
- **WHEN** reasonCode = "DISPATCHER_NOT_FOUND"
- **THEN** отображается текст: "Диспетчер не найден"

#### Scenario: Маппинг DISPATCHER_INACTIVE
- **WHEN** reasonCode = "DISPATCHER_INACTIVE"
- **THEN** отображается текст: "Диспетчер неактивен"

#### Scenario: Маппинг ETRN_SIGNING_DISABLED
- **WHEN** reasonCode = "ETRN_SIGNING_DISABLED"
- **THEN** отображается текст: "Подписание ЭТрН отключено"

#### Scenario: Маппинг ATTORNEY_NUMBER_MISSING
- **WHEN** reasonCode = "ATTORNEY_NUMBER_MISSING"
- **THEN** отображается текст: "Не указан номер МЧД"

#### Scenario: Маппинг ATTORNEY_NOT_YET_VALID
- **WHEN** reasonCode = "ATTORNEY_NOT_YET_VALID"
- **THEN** отображается текст: "МЧД ещё не действует"

#### Scenario: Маппинг ATTORNEY_EXPIRED
- **WHEN** reasonCode = "ATTORNEY_EXPIRED"
- **THEN** отображается текст: "Истёк срок действия МЧД"

#### Scenario: Маппинг ATTORNEY_DATES_INVALID
- **WHEN** reasonCode = "ATTORNEY_DATES_INVALID"
- **THEN** отображается текст: "Неверные даты МЧД"

#### Scenario: Маппинг ORGANIZATION_MISMATCH
- **WHEN** reasonCode = "ORGANIZATION_MISMATCH"
- **THEN** отображается текст: "Не совпадает организация"

#### Scenario: Маппинг AUTHORITY_RECORD_AMBIGUOUS
- **WHEN** reasonCode = "AUTHORITY_RECORD_AMBIGUOUS"
- **THEN** отображается текст: "Запись полномочий неоднозначна"

#### Scenario: Маппинг AUTHORITY_SOURCE_UNAVAILABLE
- **WHEN** reasonCode = "AUTHORITY_SOURCE_UNAVAILABLE"
- **THEN** отображается текст: "Источник данных недоступен"
