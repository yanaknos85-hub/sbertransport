## Context

Раздел «Подписание ЭТрН» карточки ЭТрН (EtrnSignature) содержит disabled-кнопки «Подписать УКЭП» и «Посмотреть документ». Бэкенд реализует серверную проверку допуска через `GET /api/v1/etrn/signing/eligibility`, которая учитывает роль «Подписант УКЭП», Справочник диспетчеров, МЧД и готовность Титула 3. Frontend должен потреблять результат и управлять UI.

Текущая структура:
```
EtrnSignature (страница)
└── EtrnModal (карточка)
    ├── useEtrnCard() — GET /:cardId
    ├── useAcquireLock() — PUT /:cardId/lock
    ├── useReleaseLock() — DELETE /:cardId/lock
    └── useCheckSigningEligibility() — GET /attorneyCheck (mutation)
        └── Кнопки блокируются по статусу mutation
```

## Goals / Non-Goals

**Goals:**
- Интегрировать eligibility API в EtrnModal
- Кнопки enabled только при успешном ответе mutation, disabled во всех остальных случаях
- Обработка ошибок через notification (logger.toMessage)
- Минимизировать обработку ошибок: единый паттерн `disabled = isLoading || !isSuccess`

**Non-Goals:**
- Защита маршрута на уровне AppRouter
- Реализация CryptoPro adapter / выбора сертификата
- Локальное подписание и передача подписи backend
- Повторная проверка eligibility при session/create и sign (не в scope задачи)
- Изменение серверного контракта

## Decisions

### Decision 1: Вызов eligibility как mutation
```
EtrnModal visible && cardId → 
  useEtrnCard(cardId) ──┐ (parallel query)
  useCheckSigningEligibility() ─┘ (mutation)
```
**Rationale:** Mutation не кэшируется, каждый вызов — свежий запрос к бэкенду. eligibility не зависит от данных карточки и не блокируется lock-status. Параллельный вызов уменьшает время загрузки.

### Decision 2: Единый паттерн disabled = isLoading || !isSuccess
```
isLoading = true  → disabled = true (по умолчанию)
isSuccess = true  → disabled = false
isError = true    → disabled = true
```
**Rationale:** Один оператор на все сценарии. Не нужно обрабатывать разные типы ошибок. Неизвестный статус = запрет. Безопаснее и проще в поддержке.

### Decision 3: Обработка ошибок через onError
```typescript
onError: ({ error, logger }) => {
  logger.toMessage('error', getErrorMessage(error));
}
```
**Rationale:** Mutation обрабатывает ошибки внутри `onError`, ошибка не выбрасывается наружу и не крашит приложение. Notification показывает пользователю текст ошибки. Не нужно ErrorBoundary.

### Decision 4: Локализация reasonCodes на frontend
Маппинг `ETRN_REASON_CODES_MAP` хранится в `constants.ts` модуля EtrnSignature.

**Rationale:** Backend возвращает machine-readable коды. Локализация на frontend соответствует требованию задачи.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| eligibility API добавляет задержку к загрузке карточки | Параллельный вызов с useEtrnCard |
| Маппинг reasonCodes рассинхронизируется с бэкендом | Маппинг — полная константа из ТЗ (13 кодов) |
| Кнопки остаются disabled после исправления причины | Пользователь должен перезакрыть/открыть карточку |

## Migration Plan

1. Развернуть фронтенд + бэкенд одновременно (zero-downtime)
2. Если eligibility endpoint ещё не готов — frontend получает ошибку, кнопки остаются disabled (как сейчас)
3. Rollback: откат только frontend

## Open Questions

- Нет (все вопросы решены на этапе exploration)
