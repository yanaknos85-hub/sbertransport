# Архитектура проекта Cargo

> **Высокоуровневый обзор архитектуры.** Для деталей по конкретному модулю — см. `src/modules/<X>/<X>.architecture.md`.
> **Дата:** 7 августа 2026 г.

---

## 📑 Указатель

- [Микросервисная архитектура (Module Federation)](#-микросервисная-архитектура-module-federation)
- [Inversion of Control (Inversify)](#-inversion-of-control-inversify)
- [Поток данных](#-поток-данных)
- [Слои приложения](#-слои-приложения)
- [Авторизация и аутентификация](#-авторизация-и-аутентификация)
- [Роутинг](#-роутинг)
- [Сборка и окружения](#-сборка-и-окружения)

---

## 🧬 Микросервисная архитектура (Module Federation)

Проект `cargo` — это **Remote-микросервис** в экосистеме SberTransport, который подключается к **хост-приложению** через **Module Federation**.

### Экспортируемые модули

Определены в `scripts/exposes.js`:

```js
module.exports = {
  './version':   './version.json',
  './App':       './src/app/prod/App',
  './AppProvider': './src/app/prod/AppProvider',
  './menu':      './src/ui/SideMenu/Menu',
  './routes':    './src/constants/constants.routes',
};
```

### Подключаемые remotes

Определены в `scripts/remotes.js`:

```js
module.exports = {
  auth: 'auth @http://front-micro-auth.ift.transport.apps.a6gabrx6.k8s.delta.sbrf.ru/auth.js?v=[Date.now()]',
  // ... другие микросервисы
};
```

### Shared-зависимости

Определены через `scripts/constants.js` → `getMfShared(IS_REMOTE)`:
- React, ReactDOM, MobX, antd, react-router-dom, react-query — общие чанки
- `@sber-sbertransport/*` — ядро MF и UI-kit

### Где это реализовано

- `src/mf/MFLoader.tsx` — загрузка модулей из remotes
- `src/mf/debug.ts` — отладочные хелперы MF
- `src/app/prod/App.tsx` — корневой компонент для Remote Mode
- `src/app/dev/` — альтернативная точка входа для локального запуска как самостоятельного приложения

---

## 🧩 Inversion of Control (Inversify)

DI-контейнер реализован через `inversify` + обёртку из `@sber-sbertransport/mf-core`.

### Архитектура IoC

```
@mff-core (rootContainer)
  └── cargo container (parent = rootContainer)
        ├── IAuthStore, IAuthService
        ├── IEmployeeStore, IEmployeeService
        ├── IPlannerStore, IPlannerService      ← кастомный
        ├── ICargoStore, ICargoService          ← кастомный
        └── ILogger, IHttpService, ...
```

### Ключевые файлы

| Файл | Назначение |
|---|---|
| `src/ioc/ioc.container.ts` | `AppStore()` — создание кастомного контейнера с `parent = rootContainer` |
| `src/ioc/ioc.stores.ts` | `initAppStore(container)` — биндинги кастомных DI-сторов |
| `src/ioc/ioc.types.ts` | `TYPES` — Symbol-токены для всех сервисов (`TYPES.IPlannerStore` и т.п.) |
| `src/ioc/ioc.context.ts` | `AppStoreContext`, `useAppStore()` — React-контекст |
| `src/ioc/ioc.storeNames.ts` | `enum StoreNames` — строковые ключи для `IAppStore` |
| `src/ioc/ioc.hooks.ts` | Хуки для стора |
| `src/ioc/ioc.errors.ts` | Коды ошибок IoC |

### Регистрация кастомного стора

**Шаг 1.** Добавить интерфейс в `ioc.types.ts`:

```ts
export const TYPES = {
  // ...
  IPlannerStore: Symbol.for('IPlannerStore'),
  IPlannerService: Symbol.for('IPlannerService'),
};
```

**Шаг 2.** Реализовать стор с декораторами Inversify:

```ts
// src/stores/Planner/DIPlanner.store.ts
import { injectable, inject } from 'inversify';
import { action, observable } from 'mobx';

@injectable()
export class DIPlannerStore implements IPlannerStore {
  @inject(TYPES.IPlannerService)
  private service!: IPlannerService;

  @observable checkedOrdersListStore = [] as OrderListMinimalType[];

  // ...
}
```

**Шаг 3.** Зарегистрировать в `ioc.stores.ts`:

```ts
export type IAppStore = {
  [StoreNames.plannerStore]: IPlannerStore;
  [StoreNames.cargoStore]: ICargoStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IPlannerStore>(TYPES.IPlannerStore).to(DIPlannerStore);
  container.bind<IPlannerService>(TYPES.IPlannerService).to(DIPlannerService);
  // ...

  return {
    plannerStore: container.get<IPlannerStore>(TYPES.IPlannerStore),
    // ...
  } as IAppStore;
}
```

### Использование в компоненте

```tsx
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

const Planner: FC = observer(() => {
  const { plannerStore, http, process, logger } = useAppStoreContext();

  const handleClick = () => {
    plannerStore.someAction();
  };

  return <Button onClick={handleClick}>...</Button>;
});
```

> ⚠️ `useAppStoreContext` бросает ошибку, если компонент рендерится **до** инициализации стора (`throw new Error('There was an attemption of Stores usage before initiation.')`). Это страховка от race conditions.

---

## 🔄 Поток данных

```
┌─────────────────┐
│   Component     │ ← observer (mobx-react)
│   (React 16)    │
└────────┬────────┘
         │ useContext
         ▼
┌─────────────────┐
│  Module Context │ ← createCallableCtx
│  (UI-состояние) │   (фильтры, пагинация, видимость)
└────────┬────────┘
         │ useContext
         ▼
┌─────────────────┐         ┌─────────────────┐
│   MobX Store    │ ──────► │   API service   │
│  (бизнес-логика)│         │ (axios + react- │
│   через DI      │         │  query v2)      │
└─────────────────┘         └────────┬────────┘
                                     │ HTTP
                                     ▼
                            ┌─────────────────┐
                            │    Backend      │
                            │   (REST API)    │
                            └─────────────────┘
```

### Ключевые особенности

1. **UI-состояние** живёт в **Module Context** (через `createCallableCtx`), а не в сторе. Это позволяет сбрасывать его при размонтировании модуля.
2. **Бизнес-состояние** живёт в **DI-сторе** (через Inversify). Оно переживает размонтирование компонента.
3. **Серверное состояние** управляется через **react-query v2** (`useAPI`/`useAPIMutation`).
4. **API-сервисы** изолированы от UI и могут вызываться как из стора, так и напрямую из компонента.

---

## 📚 Слои приложения

### 1. Presentation Layer (UI)

- Функциональные компоненты (`React.FC`)
- Стилизация через `styled-components` (`*.styles.ts`) **+** SCSS-модули (`*.module.scss`)
- Подписка на observable через `observer` (`mobx-react`)
- antd v4 + `@sber-sbertransport/ui-kit` — базовые компоненты
- Кастомные компоненты в `src/shared/components/` и `src/modules/<X>/Components/`

### 2. State Layer (Business Logic)

- MobX 5 с декораторами `@observable`/`@action`/`@computed`
- `makeObservable` НЕ используется (это v6+)
- Два типа сторов:
  - **DI-сторы** (`@injectable()`, через Inversify) — для кастомной бизнес-логики (`plannerStore`, `cargoStore`)
  - **Прямые сторы** — обычные классы, импортируются напрямую

### 3. Context Layer (UI-State)

- `createCallableCtx` из `utils/createCallableContext.tsx`
- Хранит UI-состояние модуля: фильтры, пагинация, видимость элементов, координаты карты
- Provider оборачивает роутер модуля, а не всю страницу

### 4. Service Layer (API)

- axios для HTTP + interceptors для auth header
- react-query v2 для кэширования и управления состоянием запросов
- Обёртки `useAPI`/`useAPIMutation` из `src/api/index.ts`
- io-ts для валидации контрактов

### 5. Data Layer

- io-ts типы в `*.types.ts`
- Статические типы через `t.TypeOf<typeof X>`
- Константы API в `src/constants/constants.api.ts`

---

## 🔐 Авторизация и аутентификация

### Режимы запуска (env-переменные)

| Переменная | Значение | Описание |
|---|---|---|
| `REACT_APP_BASIC_AUTH` | `TRUE` | Basic-авторизация (логин/пароль в заголовке) |
| `REACT_APP_MOCKED_AUTH` | `TRUE` | Замоканная авторизация (без реального токена) |
| `REACT_APP_MOCKED_API` | `TRUE` | Моки API (ответы из локальных файлов) |
| `REACT_APP_NETWORK_LOOP` | `DEV_CARGO`, `DEV_AUTOPARK`, `DEV_AUTOSERVICE`, `ST`, `NT`, `IFT` | Стенд для подключения |
| `REACT_APP_DEBUG` | `TRUE` | Debug-режим |
| `REACT_APP_NAME` | `cargo` | Имя микросервиса |
| `REACT_APP_REMOTE` | `TRUE` / `FALSE` | Remote или Host mode |

### Типовые команды запуска

```bash
yarn start                                    # Remote mode (хост-приложение)
yarn start-auth:dev-cargo                     # + Basic + cargo backend
yarn start-auth-mocked-login:dev-cargo        # + замоканный логин
yarn start-auth-mocked-api:dev-cargo          # + замоканные API
yarn start-auth-mocked-login-api:dev-cargo    # + замоканный логин + замоканные API
```

### Auth flow

1. Хост-приложение инициализирует сессию и передаёт токен
2. axios interceptor добавляет токен в каждый запрос
3. При 401 — interceptor делает refresh или редирект на логин (через `@sber-sbertransport/mf-core`)

---

## 🛣 Роутинг

### Конвенция

- **React Router DOM v5** (`Switch`, `Route`, `useHistory`, `useParams`) — НЕ v6+
- Маршруты определяются как константы в `src/constants/constants.routes.ts`
- Префиксы: `EXTERNAL`, `APP`, `MAIN`, `MULTI_LOGISTICS`, и т.д.

### Структура

```ts
// src/constants/constants.routes.ts
export const EXTERNAL = '/client';
export const APP = '/cargo';
export const MAIN = `${EXTERNAL}${APP}`;          // /client/cargo

export const HOME = `${MAIN}/home`;
export const MULTI_LOGISTICS = `${MAIN}/multi-logistics`;
export const PLANNER = `${MULTI_LOGISTICS}/planner`;
export const PLANNER_ROUTE_CREATE = `${PLANNER}/route-create`;
// ... и т.д.
```

### Главный роутер

`src/app/prod/AppRouter.tsx`:

```tsx
const PlannerRouter = lazy(() => import('modules/Planner/PlannerRouter'));

<Switch>
  <Route path={routes.HOME} component={Home} />
  <Route path={routes.MULTI_LOGISTICS} component={PlannerRouter} />
  <Route exact path={routes.MAIN} component={() => <Redirect to={routes.HOME} />} />
  <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
</Switch>
```

### Роутер модуля

Каждый модуль имеет свой `Router.tsx`, который:
- Импортируется через `React.lazy()` (ленивая загрузка)
- Оборачивается в `Suspense` (на уровне главного роутера)
- Внутри использует `CustomRoute` для хлебных крошек

```tsx
// src/modules/Planner/PlannerRouter.tsx
import { PlanerProvider } from 'modules/Planner/context/PlannerContext';
import { CustomRoute } from 'shared/components/Breadcrumbs/CustomRoutes';

export const Router: FC = () => (
  <PlanerProvider>
    <Switch>
      <Route exact path={routes.MULTI_LOGISTICS} component={MPlanner} />
      <CustomRoute path={routes.MULTI_LOGISTICS} bc="Планировщик маршрутов">
        <Switch>
          <CustomRoute path={routes.PLANNER_ROUTE_CREATE} component={RouteDetailed} bc="Создание маршрута" />
          {/* ... */}
        </Switch>
      </CustomRoute>
    </Switch>
  </PlanerProvider>
);
```

> ⚠️ Обратите внимание: `CustomRoute` (вместо `Route`) добавляет `bc` (breadcrumb) в `BreadcrumbsContext`.

---

## 🏗 Сборка и окружения

### Сборка

- **Craco** (конфиг в `scripts/index.js`) — обёртка над Create React App
- **Webpack** (под капотом) с модулями Module Federation
- **Babel** (`.babelrc`) — для legacy-транспиляции
- **TypeScript** (`tsconfig.json: baseUrl: ./src`, `experimentalDecorators: true`, `strict: true`)

### Команды

```bash
yarn build           # Production
yarn build:debug     # Debug-режим (для отладки на стенде)
yarn build:sigma     # Сборка для Sigma
```

### Docker

- `Dockerfile` — сборка образа `disp-cargo`
- Порт: `8085`
- Базовый образ: `snx/sbel9`
- Пользователь: `syngx`

### CI/CD

- `devsecops-config.yml` — конфиг безопасности
- `sonar-project.properties` — конфиг SonarQube (coverage exclusions)
- `lefthook.yml` — git hooks (pre-commit: lint, pre-push: node + package check)
- `commitlint.config.js` — проверка формата commit-сообщений (conventional commits с префиксом `TRANSPORT-`)

---

## 📊 Краткая диаграмма зависимостей

```
┌────────────────────────────────────────────────────────────────┐
│                          Host App                               │
│  (sber-sbertransport platform, основное приложение)            │
└────────────────────────┬───────────────────────────────────────┘
                         │ Module Federation
                         ▼
┌────────────────────────────────────────────────────────────────┐
│                         Cargo (Remote)                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AppProvider (I18n, UIKit, Map2GIS, AppStoreContext,     │  │
│  │                BreadcrumbsContext, SettingsContext,      │  │
│  │                OrganizationContext, ReactQueryCache)     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    AppRouter                              │  │
│  │  (Switch с lazy-загрузкой PlannerRouter, Home, 404)      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Modules (40 шт): Planner, CargoRegistry, Contractors,  │  │
│  │                   TariffSettings, Employees, ...         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Stores (55+): DIPlannerStore, DICargoStore, + доменные │  │
│  │                 сторы (Employee, Departments, ...)      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  API (57 сервисов): useAPI, useAPIMutation, axios       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Backend (REST API): cargo, autopark, autoservice        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

---

> **Версия:** 1.0 (7 августа 2026 г.)
> **Источник:** `src/ioc/`, `src/app/prod/AppRouter.tsx`, `src/constants/constants.routes.ts`, `scripts/`.
