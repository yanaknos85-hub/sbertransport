## 1. API-слой

- [x] 1.1 Добавить константу `ETRN_CARGO_ELIGIBILITY` в `constants.api.ts`
- [x] 1.2 Добавить io-types `SigningEligibilityResponse` в `Card/types.ts`
- [x] 1.3 Добавить cache key `ETRN_ELIGIBILITY_CACHE_KEY` в `api/etrn-signature/constants.ts`
- [x] 1.4 Добавить mutation `useCheckSigningEligibility()` в `api/etrn-signature/etrn-signature.ts`
- [x] 1.5 Добавить тип в declaration merge `declare module 'api'` для `etrnEligibility`

## 2. Локализация reasonCodes

- [x] 2.1 Добавить `ETRN_REASON_CODES_MAP` в `EtrnSignature/constants.ts`
- [x] 2.2 Добавить 13 маппингов: NO_SIGNER_ROLE, USER_INACTIVE, NOT_BANK_EMPLOYEE, DISPATCHER_NOT_FOUND, DISPATCHER_INACTIVE, ETRN_SIGNING_DISABLED, ATTORNEY_NUMBER_MISSING, ATTORNEY_NOT_YET_VALID, ATTORNEY_EXPIRED, ATTORNEY_DATES_INVALID, ORGANIZATION_MISMATCH, AUTHORITY_RECORD_AMBIGUOUS, AUTHORITY_SOURCE_UNAVAILABLE

## 3. Интеграция в EtrnModal

- [x] 3.1 Вызвать `useCheckSigningEligibility()` в `EtrnModal` (параллельно с `useEtrnCard`)
- [x] 3.2 Влияние на состояние кнопок: `disabled = eligibilityLoading || !eligibilitySuccess` (isSuccess=true → enabled, всё остальное → disabled)
- [x] 3.3 Обработка ошибок через `onError` mutation с `logger.toMessage('error', getErrorMessage(error))`
- [x] 3.4 Добавлен маппинг 13 reasonCodes в `ETRN_REASON_CODES_MAP`
