# API-слой проекта Cargo

> **Подробно об устройстве HTTP-слоя**: axios + react-query v2 + io-ts + кастомные обёртки.
> **Дата:** 7 августа 2026 г.

---

## 📑 Указатель

- [Архитектура API-слоя](#-архитектура-api-слоя)
- [Каталог API-сервисов](#-каталог-api-сервисов)
- [Конвенция файлов](#-конвенция-файлов)
- [Хелперы useAPI / useAPIMutation](#-хелперы-useapi--useapimutation)
- [Типизация через io-ts](#-типизация-через-io-ts)
- [Auth header injection](#-auth-header-injection)
- [Error handling](#-error-handling)
- [Кэширование и инвалидация](#-кэширование-и-инвалидация)
- [Тестирование API](#-тестирование-api)
- [Константы эндпоинтов](#-константы-эндпоинтов)

---

## 🏗 Архитектура API-слоя

```
┌─────────────────────────────────────────────────────┐
│                   Component                          │
│  const { data, isLoading, error } = useEtrnCard(id) │
└──────────────────┬──────────────────────────────────┘
                   │ useAPI hook
                   ▼
┌─────────────────────────────────────────────────────┐
│   src/api/<service>/<service>.api.ts                │
│   useAPI([CACHE_KEY, params], ({http, process}) =>  │
│     http.get(URL, { urlParams }).then(process...)    │
│   )                                                  │
└──────────────────┬──────────────────────────────────┘
                   │ http + process (from useAppStoreContext)
                   ▼
┌─────────────────────────────────────────────────────┐
│     @sber-sbertransport/mf-core                      │
│     IHttpService (axios wrapper) + ResponseService   │
│     (io-ts decode, error handling)                   │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP
                   ▼
┌─────────────────────────────────────────────────────┐
│                   Backend                            │
│           cargo / autopark / autoservice             │
└─────────────────────────────────────────────────────┘
```

---

## 📚 Каталог API-сервисов

### Простые сервисы (один файл `*.ts`)

См. полный список в `docs/PROJECT_MAP.md`. Ключевые:

| Файл | Что делает |
|---|---|
| `planner.ts` | **Главный сервис Planner** — ~20 эндпоинтов (routes, orders, monitor, contractors, journal) |
| `cargo-registry-search.ts` | Поиск грузов |
| `personal-search.ts` / `public-register-search.ts` / `register-search.ts` | Поиск по реестрам |
| `tariffs.ts` / `tariffs-cargo.ts` | Тарифы |
| `shared-rides.ts` / `shared-ride-settings.ts` | Совместные поездки |
| `roles.ts` / `positions.ts` / `departments.ts` | Оргструктура |
| `profile.ts` / `reset-pass.ts` / `upload.ts` | Профиль и загрузка |
| `statusCancellationCodes.ts` | Коды отмены |
| `travel-status.ts` | Статусы поездки |
| `user-agent.ts` | User Agent |

### Сложные сервисы (папка: `*.api.ts` + `*.types.ts` + `*.constants.ts`)

| Сервис | Эндпоинты |
|---|---|
| `contractors/` | Контрагенты (расширенный API) |
| `departments/` | Подразделения (расширенный) |
| `employee/` | Сотрудники (расширенный) |
| `engineer/` | Инженеры |
| **`etrn-signature/`** | **ЭТрН: lock/unlock, card, search** |
| `geo/` | Гео (расширенный) |
| `limits/` | Лимиты |
| `organizations/` | Организации |

---

## 📝 Конвенция файлов

### Простой сервис (1 файл)

```ts
// src/api/some-service.ts
import { useAPI, APIQueryResult } from 'api';
import { AxiosError } from 'axios';
import { QueryConfig } from 'react-query';
import { SOME_ENDPOINT } from 'constants/constants.api';

export const useSomeQuery = (
  params: SomeParams,
  config?: QueryConfig<SomeResponse, AxiosError>
): APIQueryResult<SomeResponse, AxiosError> => useAPI(
  ['someQuery', params],
  ({ http }) => http.get(SOME_ENDPOINT, { params }).then(r => r.data),
  config
);

export default { useSomeQuery };
```

### Сложный сервис (папка)

```
src/api/some-service/
├── some-service.api.ts        # хуки
├── some-service.constants.ts  # ключи кэша, локальные константы
└── some-service.types.ts      # io-ts типы (если нужны локально)
```

**Пример:** `src/api/etrn-signature/etrn-signature.ts` (хуки) + `constants.ts` (CACHE_KEY) + типы из `modules/Planner/Components/EtrnSignature/types.ts`.

---

## 🎣 Хелперы useAPI / useAPIMutation

**Где:** `src/api/index.ts`. **Критический файл — в нём определена обёртка над react-query v2.**

### useAPI — для GET/POST (queries)

```ts
import { useAPI, APIQueryResult } from 'api';

export const useEtrnCard = (
  id: string | null,
  config?: QueryConfig<EtrnCardDtoType, AxiosError>
): APIQueryResult<EtrnCardDtoType, AxiosError> => useAPI(
  [ETRN_CARD_CACHE_KEY, id as string],                      // key
  ({ http, process }) => http                               // queryFn
    .get(ETRN_CARGO_CARD, { urlParams: { cardId: id as string } })
    .then(process.decodeResponseData()),                     // декодирование через io-ts
  {
    enabled: Boolean(id),                                   // условие выполнения
    retry: false,                                           // без повторов
    keepPreviousData: true,                                 // для плавной пагинации
    ...config,                                              // override'ы
  }
);
```

### useAPIMutation — для POST/PUT/DELETE

```ts
import { useAPIMutation, updateQueryCache } from 'api';

export const useAcquireLock = () => useAPIMutation(
  ({ http }, { cardId }: { cardId: string }) => http
    .put(ETRN_CARGO_LOCK, {}, { urlParams: { cardId } })
    .then(r => r.data),
  {
    onSuccess: ({ cache, result, variables }) => {
      // Инвалидация кэша
      cache.invalidateQueries([ETRN_CARD_CACHE_KEY, variables.cardId]);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', 'Не удалось получить блокировку', error);
    },
  }
);
```

### Декларация ключа кэша (type-safe)

```ts
// В файле сервиса
declare module 'api' {
  interface Cache {
    etrnCard: {
      key: [typeof ETRN_CARD_CACHE_KEY, string];
      value: EtrnCardDtoType;
    };
  }
}
```

> Без `declare module 'api'` `useAPI` не сможет вывести тип `value` из `key`.

---

## 🔤 Типизация через io-ts

### Базовый паттерн

```ts
// src/modules/Planner/Components/EtrnSignature/types.ts
import * as t from 'io-ts';

export const EtrnCardDtoType = t.type({
  id: t.string,
  humanReadableId: t.string,
  status: t.string,
  currentTitle: t.union([t.null, t.string]),  // nullable
  sla: t.string,
  senderName: t.string,
  receiverName: t.string,
  carrierName: t.string,
  // ... другие поля
});

export type EtrnCardDtoType = t.TypeOf<typeof EtrnCardDtoType>;
```

### Декодирование ответа

Через `process.decodeResponseData()`:

```ts
({ http, process }) => http
  .get(ETRN_CARGO_CARD, { urlParams: { cardId } })
  .then(process.decodeResponseData())  // ← декодирует через io-ts тип из Cache
```

**Что делает `decodeResponseData`:**
1. Достаёт тип из `Cache[key]` (через объявление в `declare module 'api'`)
2. Декодирует ответ через `Type.decode()`
3. Если декодирование успешно — возвращает данные
4. Если ошибка — бросает исключение

### Опциональные поля

```ts
export const EtrnFiltersType = t.partial({
  humanReadableId: t.string,
  status: t.string,
  page: t.number,
  size: t.number,
});

export type EtrnFiltersType = t.TypeOf<typeof EtrnFiltersType>;
```

### Конвертация TS-enum → io-ts Type

Через утилиту `ioTypeFromEnum`:

```ts
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum TitleType {
  T1 = 'T1', T2 = 'T2', T3 = 'T3', T4 = 'T4',
}

export const TitleTypeIo = ioTypeFromEnum<TitleType>('TitleType', TitleType);
```

---

## 🔐 Auth header injection

Auth header добавляется через **axios interceptor** в `@sber-sbertransport/mf-core`.

**Как это работает:**
1. Хост-приложение инициализирует сессию и передаёт токен
2. При каждом HTTP-запросе interceptor добавляет заголовок `Authorization: Bearer <token>`
3. При 401 — interceptor делает refresh или редирект на логин

**Для AI-агента:** добавлять заголовки вручную **не нужно**. Достаточно использовать `http` из `useAppStoreContext`.

---

## ❌ Error handling

### Через `logger.toNotify`

```ts
import { useAPIMutation } from 'api';

const useUpdateRoute = () => useAPIMutation(
  ({ http }, data: UpdateRouteDto) => http.put(PLANNER_UPDATE_ROUTE, data),
  {
    onError: ({ error, logger }) => {
      logger.toNotify('error', 'Не удалось обновить маршрут', error);
      // → antd notification.error(...)
    },
  }
);
```

### Через `logger.toMessage` (предупреждения)

```ts
logger.toMessage('Нельзя удалить последние 2 точки маршрута');
// → antd message.warning(...)
```

### Глобальный обработчик 401

Через axios interceptor в `@sber-sbertransport/mf-core` — редирект на логин при 401.

### ⚠️ Антипаттерн

```ts
// ❌ Подавление ошибки
.catch(() => null)

// ✅ Логирование + явная обработка
.catch((error) => {
  logger.toNotify('error', 'Операция не удалась', error);
  return null;
})
```

---

## 💾 Кэширование и инвалидация

### Ключи кэша

**Все ключи** объявляются через `declare module 'api'`:

```ts
declare module 'api' {
  interface Cache {
    etrnCard: {
      key: [typeof ETRN_CARD_CACHE_KEY, string];
      value: EtrnCardDtoType;
    };
    etrnSearch: {
      key: [typeof ETRN_SEARCH_CACHE_KEY, Record<string, unknown> | undefined];
      value: SearchEtrnResponseType;
    };
  }
}
```

### Опции кэширования

```ts
useAPI(
  [CACHE_KEY, params],
  queryFn,
  {
    enabled: Boolean(id),          // включён ли запрос
    retry: false,                  // повторы при ошибке
    staleTime: 5 * 60 * 1000,      // 5 минут (по умолчанию 0)
    cacheTime: 10 * 60 * 1000,     // 10 минут
    keepPreviousData: true,        // для плавной пагинации
    refetchOnWindowFocus: false,   // для админки обычно false
    ...config,
  }
);
```

### Инвалидация после мутации

```ts
useAPIMutation(mutationFn, {
  onSuccess: ({ cache, result, variables }) => {
    // Точечная инвалидация
    cache.invalidateQueries([ETRN_CARD_CACHE_KEY, variables.cardId]);

    // Или массовая
    cache.invalidateQueries([ETRN_SEARCH_CACHE_KEY]);

    // Или ручное обновление кэша
    cache.setQueryData([ETRN_CARD_CACHE_KEY, variables.cardId], result);
  },
});
```

### Обновление кэша вручную

```ts
import { useAPIQueryCache, updateQueryCache } from 'api';

const cache = useAPIQueryCache();

// Обновить кэш для конкретного ключа
updateQueryCache(cache, [ETRN_CARD_CACHE_KEY, cardId], (old) => ({
  ...old,
  status: 'SIGNED',
}));
```

---

## 🧪 Тестирование API

### Обёртка с QueryClientProvider

```tsx
import { renderHook } from '@testing-library/react-hooks';
import { QueryClient, QueryClientProvider } from 'react-query';
import React, { ReactNode } from 'react';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

it('должен загрузить карточку', async () => {
  const { result, waitFor } = renderHook(() => useEtrnCard('abc123'), {
    wrapper: createWrapper(),
  });

  await waitFor(() => result.current.isSuccess);
  expect(result.current.data).toEqual(mockCard);
});
```

### Моки HTTP

```ts
jest.mock('api', () => ({
  ...jest.requireActual('api'),
  useAPI: jest.fn(),
}));
```

### Расположение тестов

- Тесты **простых** сервисов (1 файл) — рядом с сервисом: `src/api/__tests__/<service>.test.ts`
- Тесты **сложных** сервисов (папка) — внутри папки: `src/api/<service>/__tests__/`

> ⚠️ Сами API-файлы **исключены из coverage** (см. `jest.config.ts: collectCoverageFrom`). Тесты пишутся, но в coverage не учитываются.

---

## 🌐 Константы эндпоинтов

**Все URL эндпоинтов** хранятся в `src/constants/constants.api.ts`:

```ts
// Примеры
export const ETRN_CARGO_LIST = '/etrn-cargo/list';
export const ETRN_CARGO_CARD = '/etrn-cargo/{cardId}';
export const ETRN_CARGO_LOCK = '/etrn-cargo/{cardId}/lock';
export const ETRN_CARGO_TITLE = '/etrn-cargo/{etrnId}/title/';
export const ETRN_CARGO_TITLE_SEND = '/etrn-cargo/{etrnId}/title/send';

export const PLANNER_CREATE_ROUTE_MULTIPLE = '/planner/route';
export const PLANNER_SEARCH_ROUTES_MULTIPLE = '/planner/routes/search';
// ... и т.д.
```

**Использование:**

```ts
import { ETRN_CARGO_CARD } from 'constants/constants.api';

http.get(ETRN_CARGO_CARD, { urlParams: { cardId: 'abc123' } });
// → GET /etrn-cargo/abc123
```

**Префиксы для моков:**

```ts
import { MOCKED_API_PREFIX } from 'constants/constants.api';

http.post(`${MOCKED_API_PREFIX}${ETRN_CARGO_LIST}`, query);
// → POST /__mocked__/etrn-cargo/list (при REACT_APP_MOCKED_API=TRUE)
```

---

## 📋 Шпаргалка по API-слою

| Задача | Что использовать |
|---|---|
| GET-запрос с кэшированием | `useAPI(key, queryFn, config)` |
| POST/PUT/DELETE | `useAPIMutation(mutationFn, { onSuccess, onError })` |
| Декодировать ответ через io-ts | `process.decodeResponseData()` |
| Инвалидировать кэш | `cache.invalidateQueries([KEY, params])` |
| Принудительно перезапросить | `cache.refetchQueries([KEY, params], opts?)` |
| Обновить кэш вручную | `cache.setQueryData([KEY, params], newData)` |
| Показать ошибку пользователю | `logger.toNotify('error', message, error)` |
| Показать предупреждение | `logger.toMessage(message)` |
| URL-параметры | `{ urlParams: { key: value } }` |
| Условное выполнение запроса | `{ enabled: Boolean(condition) }` |
| Type-safe ключ кэша | `declare module 'api' { interface Cache { ... } }` |

---

> **Версия:** 1.1 (7 августа 2026 г.)
> **Источник:** `src/api/index.ts`, `src/api/etrn-signature/etrn-signature.ts`, `src/api/planner.ts`, `openspec/specs/etrn-signing/spec.md`, реальный код проекта.
