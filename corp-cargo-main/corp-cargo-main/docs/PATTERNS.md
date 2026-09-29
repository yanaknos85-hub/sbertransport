# Типовые паттерны проекта Cargo

> **«Как писать код» в проекте.** Гайд, а не чек-лист ревью.
> Для деталей по конкретному модулю — см. `src/modules/<X>/<X>.architecture.md`.
> **Дата:** 7 августа 2026 г.

---

## 📑 Указатель

1. [MobX-стор: структура файла](#1-mobx-стор-структура-файла)
2. [UI-контекст через `createCallableCtx`](#2-ui-контекст-через-createcallablectx)
3. [API через `useAPI`/`useAPIMutation`](#3-api-через-useapiuseapimutation)
4. [io-ts для типизации API](#4-io-ts-для-типизации-api)
5. [Drag-and-drop (нативный HTML5)](#5-drag-and-drop-нативный-html5)
6. [Модалки через Container](#6-модалки-через-container)
7. [Стилизация: `styled-components` + SCSS-модули](#7-стилизация-styled-components--scss-модули)
8. [Импорты и абсолютные пути](#8-импорты-и-абсолютные-пути)
9. [Хлебные крошки через CustomRoute](#9-хлебные-крошки-через-customroute)
10. [Копирование ID в буфер обмена](#10-копирование-id-в-буфер-обмена)
11. [Обработка ошибок](#11-обработка-ошибок)
12. [Работа с датами](#12-работа-с-датами)
13. [Тестирование react-query хуков](#13-тестирование-react-query-хуков)

---

## 1. MobX-стор: структура файла

**Где:** `src/stores/<Domain>/<Domain>.store.ts` или `src/modules/<X>/stores/<Domain>/<Domain>.store.ts` (для DI-сторов).

### DI-стор (через Inversify)

```ts
// src/stores/Planner/DIPlanner.store.ts
import { injectable, inject } from 'inversify';
import { action, computed, observable, runInAction } from 'mobx';
import { TYPES } from 'ioc/types';
import type { ILogger, IHttpService, ResponseService } from '@sber-sbertransport/mf-core';
import type { IPlannerService, IPlannerStore } from './Planner.interface';
import type { RouteType } from 'modules/Planner/types';

@injectable()
export class DIPlannerStore implements IPlannerStore {
  @inject(TYPES.IPlannerService)
  private service!: IPlannerService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  // ============ Observable ============
  @observable
  routeStore: RouteType | undefined;

  @observable
  ordersListStore: OrdersListMinimalStoreType = {
    content: [],
    totalElements: 0,
    totalPages: 0,
  };

  // ============ Computed ============
  @computed
  get hasOrders(): boolean {
    return this.ordersListStore.content.length > 0;
  }

  // ============ Actions ============
  @action
  setRoute(route: RouteType | undefined): void {
    this.routeStore = route;
  }

  @action
  async fetchOrders(filters: OrdersFiltersType): Promise<void> {
    try {
      const response = await this.service.searchOrders(filters);
      runInAction(() => {
        this.ordersListStore = response;
      });
    } catch (error) {
      this.logger.toNotify('error', 'Не удалось загрузить заявки', error);
    }
  }
}
```

### Ключевые правила

- **`@observable`** для всех полей состояния
- **`@action`** для всех мутаций (синхронных и асинхронных)
- **`@computed`** для производных значений
- **`runInAction`** для обновления observable из `await`-блока (чтобы MobX не ругался)
- **`makeObservable` НЕ используется** — это MobX 6+. У нас MobX 5.
- **`@injectable()`** обязателен для DI-сторов
- **Сервис** (`@inject(TYPES.X)`) — это отдельный класс с HTTP-вызовами; стор делегирует запросы ему

---

## 2. UI-контекст через `createCallableCtx`

**Где:** `src/utils/createCallableContext.tsx`.

**Когда использовать:** для UI-состояния модуля (фильтры, пагинация, видимость элементов, координаты карты).

### Базовый паттерн

```tsx
// src/modules/<Module>/context/<Module>Context.tsx
import { createCallableCtx } from 'utils/createCallableContext';
import { useState, useCallback } from 'react';

interface <Module>UIState {
  isFiltersVisible: boolean;
  filters: FiltersType;
  page: number;
  setFilters: (filters: FiltersType) => void;
  setPage: (page: number) => void;
  toggleFilters: () => void;
}

function createUIState(): <Module>UIState {
  const [isFiltersVisible, setFiltersVisible] = useState(false);
  const [filters, setFilters] = useState<FiltersType>(DEFAULT_FILTERS);
  const [page, setPage] = useState(1);

  const toggleFilters = useCallback(() => setFiltersVisible(v => !v), []);

  return { isFiltersVisible, filters, page, setFilters, setPage, toggleFilters };
}

export const [<Module>Provider, use<Module>Context] = createCallableCtx(
  createUIState,
  { name: '<Module>Provider' }
);
```

### Использование в компоненте

```tsx
// src/modules/<Module>/Components/<X>/<X>.tsx
import { usePlannerContext } from 'modules/Planner/context/PlannerContext';

const SomeComponent: FC = () => {
  const { filters, setFilters, toggleFilters } = usePlannerContext();

  return (
    <div>
      <Button onClick={toggleFilters}>Фильтры</Button>
      <FiltersPanel filters={filters} onChange={setFilters} />
    </div>
  );
};
```

### Когда НЕ использовать createCallableCtx

- **Для бизнес-логики** → используйте DI-стор
- **Для серверного состояния** → используйте react-query (`useAPI`)
- **Для одноразового состояния** (открыт/закрыт попап) → обычный `useState`

---

## 3. API через `useAPI`/`useAPIMutation`

**Где:** `src/api/index.ts`.

### `useAPI` — для GET-запросов

```ts
// src/api/etrn-signature/etrn-signature.ts
import { useAPI, APIQueryResult } from 'api';
import { AxiosError } from 'axios';
import { QueryConfig } from 'react-query';
import { EtrnCardDtoType } from 'modules/Planner/Components/EtrnSignature/components/Card/types';

export const useEtrnCard = (
  id: string | null,
  config?: QueryConfig<EtrnCardDtoType, AxiosError>
): APIQueryResult<EtrnCardDtoType, AxiosError> => useAPI(
  [ETRN_CARD_CACHE_KEY, id as string],
  ({ http, process }) => http
    .get(ETRN_CARGO_CARD, { urlParams: { cardId: id as string } })
    .then(process.decodeResponseData()),
  {
    enabled: Boolean(id),
    retry: false,
    ...config,
  }
);
```

### `useAPIMutation` — для POST/PUT/DELETE

```ts
export const useAcquireLock = () => useAPIMutation(
  ({ http, process }, { cardId }) => http
    .put(ETRN_CARGO_LOCK, {}, { urlParams: { cardId } })
    .then(process.getResponseData),
  {
    onSuccess: ({ cache, result, variables, process, t, logger }) => {
      // инвалидировать кэш
      cache.invalidateQueries([ETRN_CARD_CACHE_KEY, variables.cardId]);
      // или обновить точечно
      cache.setQueryData([ETRN_CARD_CACHE_KEY, variables.cardId], result);
    },
    onError: ({ error, variables, logger }) => {
      logger.toNotify('error', 'Не удалось получить блокировку', error);
    },
  }
);
```

### Декларация ключа кэша

```ts
// src/api/etrn-signature/etrn-signature.ts
declare module 'api' {
  interface Cache {
    etrnCard: {
      key: [typeof ETRN_CARD_CACHE_KEY, string];
      value: EtrnCardDtoType;
    };
  }
}
```

> ⚠️ Декларация в `declare module 'api'` нужна для **type-safe ключей кэша**. Без неё `useAPI` не сможет вывести тип `value` из `key`.

### URL-параметры через `urlParams`

```ts
http.get(ETRN_CARGO_CARD, { urlParams: { cardId: 'abc123' } })
// → GET /etrn-cargo/abc123
```

---

## 4. io-ts для типизации API

### Базовый паттерн

```ts
// src/modules/Planner/types.ts
import * as t from 'io-ts';

export const RouteType = t.type({
  id: t.string,
  humanReadableId: t.string,
  status: t.string,
  createdAt: t.string,
});

export type RouteType = t.TypeOf<typeof RouteType>;
```

### Enum через `t.keyof`

```ts
export const RouteStatus = t.keyof({
  CREATED: 'CREATED',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  FINISHED: 'FINISHED',
  CANCELED: 'CANCELED',
});

export type RouteStatus = t.TypeOf<typeof RouteStatus>;
```

### Enum через `ioTypeFromEnum` (TS-enum → io-ts)

```ts
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum TitleType {
  T1 = 'T1',
  T2 = 'T2',
  T3 = 'T3',
  T4 = 'T4',
}

// Конвертация TS-enum → io-ts Type
export const TitleTypeIo = ioTypeFromEnum<TitleType>('TitleType', TitleType);
```

### Опциональные поля и union-ы

```ts
export const RouteType = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    description: t.string,
    comment: t.string,
  }),
]);

// nullable
export const CurrentTitleType = t.union([t.null, t.string]);
```

### Декодирование ответа

Через `process.decodeResponseData()` (метод из `ResponseService`):

```ts
({ http, process }) => http
  .get(ETRN_CARGO_CARD, { urlParams: { cardId } })
  .then(process.decodeResponseData())  // ← декодирует через io-ts тип из Cache
```

> Если тип не зарегистрирован в `declare module 'api'`, декодирование не сработает.

---

## 5. Drag-and-drop (нативный HTML5)

**Используется в:** `src/modules/Planner/Components/Planner/Planner.tsx` и связанных.

### Без библиотек (react-dnd, react-beautiful-dnd) — **только HTML5 API**.

#### Источник (draggable)

```tsx
const OrderListItem: FC<Props> = ({ order, onDragStart }) => {
  const isRouteVisible = useRouteVisibility();

  return (
    <div
      draggable={isRouteVisible}
      onDragStart={(e) => {
        onDragStart(order.id);
        e.dataTransfer.setData('text/plain', order.id);  // для Firefox
      }}
      className={styles.orderItem}
    >
      {/* ... */}
    </div>
  );
};
```

#### Цель (drop target)

```tsx
const RouteListItem: FC<Props> = ({ routeId, onDrop }) => {
  const [isDragOver, setIsDragOver] = useState(false);

  return (
    <div
      onDragOver={(e) => {
        e.preventDefault();  // обязательно!
        setIsDragOver(true);
      }}
      onDragLeave={() => setIsDragOver(false)}
      onDrop={(e) => {
        e.preventDefault();
        setIsDragOver(false);
        const orderId = e.dataTransfer.getData('text/plain');
        onDrop(orderId, routeId);
      }}
      className={cn(styles.routeItem, { [styles.dragOver]: isDragOver })}
    >
      {/* ... */}
    </div>
  );
};
```

### Важно

- **`e.preventDefault()` в `onDragOver`** — обязательно, иначе drop не сработает
- **`e.dataTransfer.setData('text/plain', ...)`** — для совместимости с Firefox
- **Визуальная индикация** — через CSS-класс (`dragOver`)

---

## 6. Модалки через Container

**Где:** `src/shared/components/Modal/Modal.tsx`.

### Базовый паттерн

```tsx
import { Container } from 'shared/components/Modal/Modal';
import { Button } from 'shared/components/Button';

const SomeModal: FC<Props> = observer(({ isVisible, onClose, onSubmit }) => (
  <Container
    visible={isVisible}
    title="Создание маршрута"
    onCancel={onClose}
    footer={[
      <Button key="cancel" onClick={onClose}>Отмена</Button>,
      <Button key="submit" type="primary" onClick={onSubmit}>Создать</Button>,
    ]}
  >
    <Form>
      {/* поля */}
    </Form>
  </Container>
));
```

### Конвенция в модулях

- Модалки модуля лежат в `src/modules/<X>/Components/Modals/<ModalName>/`
- Каждая модалка — отдельная папка с `index.tsx` + опционально `*.styles.ts`
- Пропсы типизированы явно
- `observer` оборачивает модалку, если она зависит от observable

---

## 7. Стилизация: `styled-components` + SCSS-модули

### Когда что использовать

| Стиль | Когда | Пример |
|---|---|---|
| `*.styles.ts` (styled-components) | JSX внутри styled-компонентов, темная тема, динамика | `Button.styles.ts`, `Modal.styles.ts` |
| `styles.module.scss` | Статические стили, модули компонентов | `styles.module.scss` рядом с компонентом |
| `*.styles.ts` (без JSX) | Утилитарные объекты стилей, темы | `shared/styles/styles.ts`, `Planner.styles.ts` |

### styled-components для antd

```tsx
import styled from 'styled-components';
import { Button } from 'antd';

export const StyledButton = styled(Button)`
  margin-right: 8px;
  background-color: ${({ theme }) => theme.colorPrimary};
`;
```

> ⚠️ Стилизация antd через `styled(Button)` — **предпочтительнее**, чем через `className`.

### SCSS-модули

```tsx
import styles from './styles.module.scss';

<div className={styles.container}>
  <span className={styles.title}>...</span>
</div>
```

---

## 8. Импорты и абсолютные пути

### Конвенция (`tsconfig.json: baseUrl: ./src`)

```ts
// ✅ Хорошо — абсолютные пути через baseUrl
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { logger } from 'ioc/ioc.container';
import { ETRN_CARGO_CARD } from 'constants/constants.api';
import { Planner.architecture.md } from 'modules/Planner/Planner.architecture.md';
import { formatRubles } from 'utils/formatRubles';

// ❌ Плохо — длинные относительные пути
import { useAppStoreContext } from '../../../../shared/hooks/useAppStoreContext';
```

### Порядок импортов

1. Сторонние библиотеки (`react`, `mobx`, `antd`, `react-query`)
2. Глобальные алиасы (`api`, `shared`, `modules`, `stores`, `constants`, `utils`, `hooks`, `context`, `i18n`)
3. Локальные относительные (`./Button`, `../types`)

```ts
// Пример
import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import { Form, Input } from 'antd';

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Button } from 'shared/components/Button';
import { logger } from 'utils/logger';

import { SomeLocalComponent } from './SomeLocalComponent';
import { LocalType } from './types';
```

---

## 9. Хлебные крошки через CustomRoute

**Где:** `src/shared/components/Breadcrumbs/CustomRoutes.tsx`.

### Использование

```tsx
import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';

// Вместо <Route> используем <CustomRoute> и передаём bc (breadcrumb)
<CustomRoute
  path={routes.PLANNER_ROUTE_CREATE}
  component={RouteDetailed}
  bc="Создание маршрута"
/>
```

### Что делает CustomRoute

1. Добавляет элемент в `BreadcrumbsContext`
2. Рендерит переданный компонент
3. Автоматически очищает хлебные крошки при размонтировании

---

## 10. Копирование ID в буфер обмена

### Утилита

```ts
// utils/copyToClipboard.ts
export const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    // Fallback для старых браузеров
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    const result = document.execCommand('copy');
    document.body.removeChild(textArea);
    return result;
  }
};
```

### Использование

```tsx
import { notification } from 'antd';
import { copyToClipboard } from 'utils/copyToClipboard';

const handleCopy = async (id: string) => {
  const success = await copyToClipboard(id);
  if (success) {
    notification.success({ message: 'ID скопирован' });
  } else {
    notification.error({ message: 'Не удалось скопировать' });
  }
};
```

---

## 11. Обработка ошибок

### API-ошибки

```ts
import { useAPIMutation } from 'api';

const useCreateRoute = () => useAPIMutation(
  ({ http }, data: CreateRouteDto) => http.post(PLANNER_CREATE_ROUTE, data),
  {
    onSuccess: ({ cache, result }) => {
      cache.invalidateQueries([ROUTES_CACHE_KEY]);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', 'Не удалось создать маршрут', error);
    },
  }
);
```

### Антипаттерн

```ts
// ❌ Плохо — подавление ошибки
.catch(() => null)

// ✅ Хорошо — логирование
.catch((error) => {
  logger.toNotify('error', 'Операция не удалась', error);
  return null;
})
```

### Предупреждения

```ts
logger.toMessage('Нельзя удалить последние 2 точки маршрута');
```

---

## 12. Работа с датами

**⚠️ `moment.js` — legacy-библиотека.** Используется в существующем коде, но в новом коде предпочтительнее `dayjs` или `date-fns`.

### Форматы (`src/constants/constants.app.ts`)

```ts
export const DATE_FORMAT = {
  DATE: 'DD.MM.YYYY',
  DATE_WITH_TIME: 'DD.MM.YYYY HH:mm',
  ISO: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
};
```

### Конвертация в UTC ISO

```ts
import moment from 'moment';

const utcIsoString = moment.utc().startOf('day').toISOString();
// "2026-08-07T00:00:00.000Z"
```

### Парсинг

```ts
const parsed = moment(rawString, DATE_FORMAT.DATE_WITH_TIME);
```

---

## 13. Тестирование react-query хуков

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

// Использование
const { result, waitFor } = renderHook(() => useEtrnCard('abc123'), {
  wrapper: createWrapper(),
});

await waitFor(() => result.current.isSuccess);
expect(result.current.data).toEqual(mockCard);
```

### Моки API

```ts
jest.mock('api/etrn-signature', () => ({
  useEtrnCard: jest.fn(() => ({
    data: mockCard,
    isLoading: false,
    error: null,
    refetch: jest.fn(),
  })),
}));
```

### Исключения из coverage

- `*.test.tsx`, `*.spec.tsx`, `*.stories.tsx` — сами тесты не учитываются
- `__tests__/`, `__mocks__/`, `__fixtures__/` — целиком
- `api/`, `ioc/`, `constants/`, `types/`, `*.types.ts`, `*.constants.ts` — не покрываются тестами

---

> **Версия:** 1.0 (7 августа 2026 г.)
> **Источник:** `instructions.md`, `instructions-suggestions.md`, `src/modules/Planner/AGENTS.md`, реальный код проекта.
