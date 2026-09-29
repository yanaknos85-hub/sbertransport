# Design: ЭТрН — Подписание

## Архитектура модуля

```
src/modules/Planner/Components/EtrnSignature/
├── EtrnSignature.tsx                     ← страница-обёртка с таблицей и фильтрами
├── constants.ts
├── types.ts
├── styles.module.scss
├── components/
│   ├── Card/
│   │   ├── components/
│   │   │   ├── CargoAndRoute/            ← блок груза и маршрута
│   │   │   ├── ParticipantBlock/         ← блок участников (sender/receiver/carrier)
│   │   │   ├── PepBlock/                 ← блок ПЭП (управляющее лицо)
│   │   │   ├── TitleChain/               ← цепочка титулов T1–T4
│   │   │   ├── EtrnModal.tsx             ← модальное окно (lock, tabs)
│   │   │   ├── EtrnModal.test.tsx
│   │   │   └── styles.module.scss
│   │   ├── types.ts
│   │   ├── __tests__/
│   │   └── icons/                        ← PNG иконки (cargo, truck, route...)
│   ├── Filters/
│   │   ├── EtrnFilters.tsx               ← поиск по ID + кнопка «Фильтры»
│   │   └── styles.module.scss
│   ├── HeaderWithIcon/                   ← хедер страницы
│   │   ├── HeaderWithIcon.tsx
│   │   ├── index.ts
│   │   └── HeaderWithIcon.test.tsx
│   └── Table/
│       ├── useTableFields.tsx            ← колонки + данные строк
│       └── useTableFields.test.tsx
├── hooks/
│   ├── useEtrnQuery.ts                   ← хук запроса данных ЭТрН
│   └── useEtrnQuery.test.tsx
├── EtrnSignature.test.tsx
└── styles.module.scss
```

## API-слой

API-сервисы находятся в `src/api/etrn-signature/etrn-signature.ts` (не в модуле):

- `useEtrnCard(cardId)` — GET /etrn-cargo/{id}
- `useAcquireLock()` — PUT /etrn-cargo/{cardId}/lock
- `useReleaseLock()` — DELETE /etrn-cargo/{cardId}/lock

`EtrnModal` импортирует их напрямую из `api/etrn-signature/etrn-signature`.

## Стейт-менеджмент

### MobX сторы

- **Карточка:** MobX стор в `components/Card/stores/` — состояние карточки, lock-инфо, данные ЭТрН

### Хуки

- `useEtrnQuery` (в корне `EtrnSignature/`) — хук запроса данных

### Взаимодействие слоёв

```
EtrnSignature (страница)
    │
    ├── HeaderWithIcon              ← заголовок страницы
    │
    ├── EtrnFilters                 ← поиск по ID + кнопка «Фильтры»
    │
    ├── WorklistTable               ← таблица Ant Design
    │       │
    │       └── клик на строку ──▶ EtrnModal(cardId, visible=true)
    │                                       │
    │                                       ├── useAcquireLock() ──▶ PUT /lock (5 мин TTL)
    │                                       ├── useEtrnCard(cardId) ──▶ GET /etrn-cargo/{id}
    │                                       ├── useReleaseLock() ──▶ DELETE /lock при закрытии
    │                                       │
    │                                       ├── Tabs: general / documents / checks / history
    │                                       ├── TitleChain ──▶ titleChain из карточки
    │                                       ├── PepBlock ──▶ sesFullName, sesRole, sesEventDatetime
    │                                       ├── ParticipantBlock ──▶ sender/receiver/carrier
    │                                       ├── CargoAndRoute ──▶ cargo + route
    │
    └── EtrnSignature styles          ← общие стили страницы
```

## Ключевые решения

1. **Модальное окно управляет собой.** `EtrnModal` сам вызывает `useAcquireLock` при mount и `useReleaseLock` при unmount. Не требует пропсов через все компоненты-родители.

2. **Lock — ответственность карточки.** Рабочий лист не знает о блокировке. Lock POST/DELETE происходит внутри `EtrnModal`. Блокировка на 5 минут, если пользователь не закрыл карточку раньше.

3. **Заглушка подписания.** Кнопка «Подписать УКЭП» всегда `disabled` в Stage 1.

4. **Обработка 409.** При конфликте блокировки модалка открывается в read-only — все поля отображаются, все кнопки disabled.

5. **Tabs в карточке.** 4 вкладки: general (сводка), documents, checks, history. Содержимое last three — заглушки.

6. **Фильтры.** `EtrnFilters` содержит поиск по ID и кнопку «Фильтры». Фильтрация на текущий момент не подключена к логике таблицы.

## Компонентные контракты

### EtrnModal

| Prop | Type | Description |
|------|------|-------------|
| `cardId` | `string` | UUID карточки |
| `visible` | `boolean` | Состояние видимости |
| `onClose` | `() => void` | Callback закрытия |
| `onSigned` | `() => void` | Callback после подписания (опционально) |

### TitleChain

| Prop | Type | Description |
|------|------|-------------|
| `titles` | `TitleChainItem[]` | Массив титулов |
| `empty` | `boolean` | Если true — показать «Ожидание данных от Корус» |

### ParticipantBlock

| Prop | Type | Description |
|------|------|-------------|
| `data.sender` | `string` | Имя отправителя |
| `data.receiver` | `string` | Имя получателя |
| `data.contractor` | `string` | Имя перевозчика |

### PepBlock

| Prop | Type | Description |
|------|------|-------------|
| `title.name` | `string` | ФИО |
| `title.role` | `string` | Роль |
| `title.date` | `string` | Дата события |
| `title.id` | `string` | ID события |

### CargoAndRoute

| Prop | Type | Description |
|------|------|-------------|
| `data.cargo` | `string` | Описание груза |
| `data.route` | `string` | Маршрут |
