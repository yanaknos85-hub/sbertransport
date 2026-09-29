## Why

Бэкенд реализует серверную проверку допуска пользователя к подписанию Титула 3 ЭТрН через `GET /api/v1/etrn/signing/eligibility`. Frontend должен потреблять результат этой проверки, блокировать UI при отсутствии допуска и показывать причины запрета. Текущий UI карточки ЭТрН имеет disabled-кнопки «Подписать» и «Документ», которые нужно активировать/деактивировать на основе eligibility-ответа.

## What Changes

- Добавить новый API-эндпоинт `ETRN_CARGO_ELIGIBILITY` и hook `useCheckSigningEligibility()` (mutation)
- Вызывать eligibility как mutation при открытии карточки ЭТрН (параллельно с `useEtrnCard`)
- Кнопки «Подписать УКЭП» и «Посмотреть документ» disabled по умолчанию, разблокируются только при `isSuccess=true`
- При любой ошибке (5xx, timeout, network) — кнопки остаются disabled (единый паттерн: `disabled = isLoading || !isSuccess`)
- Обработка ошибок через `onError` mutation: `logger.toMessage('error', getErrorMessage(error))`
- Добавить маппинг 13 reasonCodes → русские тексты

## Capabilities

### New Capabilities
- `etrn-signing-eligibility`: Проверка допуска подписанта ЭТрН через серверный eligibility API, активация UI и отображение причин запрета

### Modified Capabilities
- (существующие спеки не затрагиваются на уровне требований)

## Impact

| Область | Описание |
|---------|----------|
| `src/constants/constants.api.ts` | + `ETRN_CARGO_ELIGIBILITY` constant |
| `src/api/etrn-signature/etrn-signature.ts` | + `useCheckSigningEligibility` mutation |
| `src/modules/Planner/Components/EtrnSignature/components/Card/types.ts` | + `SigningEligibilityResponse` io-types |
| `src/modules/Planner/Components/EtrnSignature/constants.ts` | + `ETRN_REASON_CODES_MAP` маппинг |
| `src/modules/Planner/Components/EtrnSignature/components/Card/EtrnModal.tsx` | + eligibility check (mutation), button state |
