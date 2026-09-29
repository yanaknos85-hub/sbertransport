## Task Group 1: Page & Table

- [x] 1.1 `EtrnSignature.tsx` — страница-обёртка (tabs, layout)
- [x] 1.2 `components/Table/useTableFields.tsx` — хук для колонок таблицы
- [x] 1.3 `components/Filters/EtrnFilters.tsx` — поиск по ID + кнопка «Фильтры»
- [x] 1.4 `EtrnSignature.test.tsx` — тесты страницы

## Task Group 2: Modal

- [x] 2.1 `components/Card/EtrnModal.tsx` — модальное окно с tabs (general, documents, checks, history)
- [x] 2.2 Lock flow: `useAcquireLock` → `useReleaseLock` (PUT/DELETE /lock)
- [x] 2.3 Data: `useEtrnCard` (GET /etrn-cargo/{id})
- [x] 2.4 Lock conflict (409) → read-only mode with banner
- [x] 2.5 `EtrnModal.test.tsx` — тесты модалки

## Task Group 3: Modal Components

- [x] 3.1 `components/Card/components/TitleChain/` — цепочка титулов T1–T4
- [x] 3.2 `components/Card/components/ParticipantBlock/` — участники (sender/receiver/carrier)
- [x] 3.3 `components/Card/components/PepBlock/` — ПЭП (управляющее лицо)
- [x] 3.4 `components/Card/components/CargoAndRoute/` — груз и маршрут

## Task Group 4: API & Hooks

- [x] 4.1 `src/api/etrn-signature/etrn-signature.ts` — хуки (useEtrnCard, useAcquireLock, useReleaseLock)
- [x] 4.2 `hooks/useEtrnQuery.ts` — хук запроса данных
- [x] 4.3 `hooks/useEtrnQuery.test.tsx` — тесты хука
- [x] 4.4 `components/Card/api/` — API-сервисы для карточки

## Task Group 5: Stores

- [x] 5.1 MobX стор для карточки и lock

## Task Group 6: Styling & Localization

- [x] 6.1 `styles.module.scss` — стили страницы и модалки
- [x] 6.2 Иконки в `components/Card/icons/`
- [x] 6.3 `constants.ts` и `types.ts` — константы и типы
- [x] 6.4 i18n переводы для ЭТрН
