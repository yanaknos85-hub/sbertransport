# GigaCode Context — disp-platform

> Диспетчерский фронтенд-микросервис **Platform** в экосистеме SberTransport (Module Federation, React 16 + TS 5 + MobX + Ant Design 4 + react-query 2). По умолчанию слушает порт **7005**, работает в режиме `REACT_APP_REMOTE=TRUE` (подключается к host-приложению).
>
> Источники правды по смежным темам — в самом низу этого документа (см. [Source of Truth](#source-of-truth)).

---

## Tech Stack

Технологический стек фиксируется в `package.json` (там же — актуальные версии). Ключевые подсистемы:

- **React 16** + **TypeScript 5** (strict mode)
- **MobX 5** + **mobx-react** — клиентское состояние (классовые сторы + DI)
- **react-query 2.26** — серверное состояние (через кастомные хуки `useAPI` / `useAPIMutation`)
- **Ant Design 4** + **@sber-sbertransport/ui-kit** — UI-компоненты
- **styled-components 5** + **SCSS modules** — стилизация
- **inversify** — DI-контейнер (см. [Dependency Injection](#dependency-injection))
- **Module Federation 2** (`@sber-sbertransport/mf-core`) — архитектура микросервиса
- **io-ts** + **fp-ts** + **ramda** — валидация типов и функциональные утилиты
- **axios** — HTTP (через `IHttpService` из MF-core, не напрямую)
- **d3** — визуализация графиков
- **react-beautiful-dnd** — drag&drop

> ⚠️ Не дублируй версии в этом документе — они устаревают. Истина — `package.json`.

---

## Building & Running

```bash
nvm use                          # версия Node.js из .nvmrc (>= 18)
yarn ci                          # rm -rf node_modules && yarn install --frozen-lockfile
yarn start-auth:dev-cargo       # пример запуска с Basic-auth в cargo-окружении
```

Все доступные скрипты — `yarn run` (полный список в `package.json`). Основные паттерны:

| Паттерн | Назначение |
|---|---|
| `yarn start` | Remote-режим (подключение к host-приложению) |
| `yarn start:dev-{env}` | Self-hosted режим + окружение (cargo, autopark, autoservice, sbertransport, st, nt, ift) |
| `yarn start-auth:dev-{env}` | + Basic-авторизация (логин/пароль) |
| `yarn start-auth-mocked-login:dev-{env}` | + замоканный логин |
| `yarn start-auth-mocked-api:dev-{env}` | + моки API |
| `yarn start-auth-mocked-login-api:dev-{env}` | + мок логина и моки API |
| `yarn build` | Production-сборка с версионированием |
| `yarn build:debug` | Отладочная сборка (`REACT_APP_DEBUG=TRUE`) |
| `yarn build:sigma` | Sigma-сборка |
| `yarn sort-imports` | Детерминированная сортировка импортов во всём `src/` |
| `yarn up` | Обновление трёх MF-зависимостей (`mf-core`, `tool-kit`, `ui-kit`) |

Docker: `docker build -t disp-platform . && docker run -p 8085:8085 disp-platform`.

---

## Testing

```bash
yarn test           # запуск всех тестов
yarn test:ci        # в CI-режиме с покрытием (V8 провайдер)
yarn test:clear     # сброс кэша Jest
```

**Конфигурация:** `jest.config.ts` (preset `ts-jest`, окружение `jsdom`, `testTimeout: 50000`).
**Setup:** `src/setupTests.ts` — мокает `window.matchMedia`, `ResizeObserver`, `IntersectionObserver`.
**Моки ассетов:** `__mocks__/fileMock.js`, `__mocks__/styleMock.js`, `__mocks__/svgMock.jsx` (подключены через `moduleNameMapper`).

**Исключения покрытия** (`collectCoverageFrom` в `jest.config.ts`):

| Исключение | Причина |
|---|---|
| `src/api/**` | тонкие обёртки над `http`-сервисом |
| `src/app/**`, `src/context/**` | точка монтирования, без бизнес-логики |
| `src/i18n/**`, `src/ioc/**`, `src/mf/**`, `src/styles/**` | инфраструктура |
| `**/constants/**`, `**/types/**`, `**/*.constants.ts`, `**/*.types.ts`, `**/interfaces/**` | декларации без поведения |
| `**/__mocks__/**`, `**/__fixtures__/**`, `**/__tests__/**`, `**/*.test.*`, `**/*.spec.*`, `**/*.stories.*` | тесты и моки |

Подробные правила для тестов — см. skill `unit-test-generator` (вызывается специализированным агентом `unit-test-writer`).

---

## Linting & Git Hooks

```bash
yarn lint           # eslint src
yarn lint:fix       # авто-исправление
```

Конфигурация: `eslint.config.js` (на базе `@sber-sbertransport/tool-kit`).
Git hooks через [Lefthook](https://github.com/evilmartians/lefthook) (`lefthook.yml`):

- `commit-msg` — conventional commits
- `pre-commit` — TypeScript + ESLint
- `pre-push` — проверка версии Node.js и консистентности пакетов

---

## Project Structure

```
src/
├── api/                # API-сервисы (useAPI / useAPIMutation хуки + io-ts типы)
├── app/                # Точка монтирования (dev/ и prod/)
├── components/         # Переиспользуемые UI-компоненты
├── constants/          # Константы уровня приложения
├── context/            # Глобальные React-контексты
├── hooks/              # Кастомные React-хуки
├── i18n/               # Интернационализация (ru + en)
├── ioc/                # DI-контейнер (inversify)
├── mf/                 # Module Federation: MFLoader, debug
├── modules/            # Бизнес-модули (Reports, Schedule2.0, Staff, Support, Trip, Trips)
├── styles/             # Глобальные стили
├── types/              # Глобальные TypeScript-типы
├── ui/                 # Layout для self-hosted запуска
└── utils/              # Утилиты (см. src/utils/UTILS.md)
```

> **Подробности по любой подсистеме** — в `ls src/<dir>` и соответствующих README. Каталоги `api/`, `components/`, `hooks/`, `modules/` намеренно плоские и самодокументируемые.

---

## Module Federation

Микросервис работает как **remote** под управлением host-приложения. Конфигурация — в `scripts/`:

| Файл | Назначение |
|---|---|
| `scripts/exposes.js` | Какие модули отдаются host-приложению (`./Trips`, `./Schedule`, и т.п.) |
| `scripts/remotes.js` | Какие remote-микросервисы потребляются |
| `scripts/index.js` | Главный Craco-конфиг |
| `scripts/constants.js` | Константы окружения |
| `scripts/devServer/` | Локальный dev-сервер |

**Runtime-утилиты в `src/mf/`:**

- `MFLoader.tsx` — обёртка для `lazy()`, которая ловит ошибки загрузки remote-модулей и показывает `DefaultFallback` (`components/ErrorBoundary/ErrorBoundary`) с кодом `CustomErrorCode.MF_LOAD`.
- `debug.ts` — режим отладки MF.

**Правила:**

- `shared`-чанки (React, MobX, Ant Design и т.д.) настраиваются в `scripts/index.js`. Изменение конфигурации shared может сломать host.
- Точка входа в self-hosted режиме — `src/app/dev/AppRouter.tsx`; в prod — модули монтируются host-приложением через `exposes.js`.

---

## State Management — MobX

Состояние приложения живёт в **классовых сторах**, зарегистрированных в DI-контейнере (см. [Dependency Injection](#dependency-injection)).

**Базовые правила:**

- Сторы — классы с полями `@observable` и методами-`@action` (или `makeAutoObservable`/`makeObservable`).
- Методы, передаваемые как callbacks, объявляются стрелочными (`=>`), чтобы не терять `this`. Альтернатива — `@action.bound`.
- В компонентах используется `@observer` (из `mobx-react`) — без него observer-компонент не реагирует на изменения.
- Внутри React-функций: `useObserver(() => ...)` для локальных observer-блоков.
- Сторы получают зависимости через DI (`@inject(TYPES.IXxxService)`), а не напрямую импортируют сервисы.

**Доступ к store-у из компонента:**

```ts
import { useAppStore } from 'ioc';

const { authStore } = useAppStore();
```

Если нужно выйти за пределы React (например, в axios-interceptor), используй `useGeneralStoreContext` из `@sber-sbertransport/mf-core` или работай через сервис, а не напрямую со стором.

**Имена сторов** — единый enum в `src/ioc/ioc.storeNames.ts` (`StoreNames`).

---

## API Layer

Каждый API-сервис — отдельный каталог в `src/api/<name>/` со структурой:

```
api/<name>/
├── <name>.api.ts        # хуки useAPI / useAPIMutation / useWebsocket
├── <name>.constants.ts  # URL-паттерны (константы — строки)
└── <name>.types.ts      # io-ts типы / интерфейсы / type-helpers
```

Полный список сервисов: `analytics`, `autopark`, `branches`, `contractors`, `dispatcher-room`, `dispatchers`, `drivers`, `profile`, `schedule2.0`, **`shift-conflicts`**, `track`, `trips`, `trips-cargo`, `trips-reports`, `user`, `vehicles`.

### Ключевая конвенция: декларативное расширение `Cache`

В каждом `*.api.ts` обязательно объявляется расширение модуля `api`:

```ts
export const PASS_TRIP_KEY = 'pass-trip';

declare module 'api' {
  interface Cache {
    passTrip: {
      key: [typeof PASS_TRIP_KEY, UUID, UUID];   // queryKey-кортеж
      value: PassTrip;                           // тип ответа
    };
  }
}
```

Это даёт строгую типизацию хуков `useAPI` / `useAPIMutation` по `key` и `value`. Без этого расширения хук не скомпилируется.

### Кастомные хуки (обёртки над react-query 2)

Все хуки экспортируются из `src/api/index.ts` (`import { useAPI, useAPIMutation, useAPIQueryCache, useWebsocket } from 'api'`):

| Хук | Назначение |
|---|---|
| `useAPI(key, queryFn, config?)` | GET-запрос. Тип `key` берётся из `Cache`, тип результата — автоматически. |
| `useAPIMutation(mutationFn, config)` | Мутация. `config.onSuccess/onError` получают `{ cache, result, variables, process, t, logger }`. |
| `useAPIQueryCache()` | Доступ к кэшу для ручной инвалидации / оптимистичных апдейтов. |
| `useWebsocket(route, codec, opts)` | WebSocket-подписка с ping/pong и автореконнектом (используется в `drivers`, `trips`). |

`queryFn` получает `{ http, process }` из MF-core (`IHttpService`, `ResponseService`). **Никогда не вызывай `axios` напрямую** — только через `http`.

### Правила

- URL-паттерны — в `*.constants.ts` (например, `${TRIPS_SERVICE}/contractor/:contractorId/trip/:tripId/`) — никаких магических строк в `*.api.ts`.
- Параметры query-ключа всегда идут после `KEY`-константы: `[KEY_NAME, ...params]`.
- После мутации, затрагивающей список, вызывай `cache.invalidateQueries([LIST_KEY, ...])`.

Подробные шаблоны — skill `api-skill-creator` (агент `api-writer`).

---

## Dependency Injection

Используется **inversify**. Точка входа — `src/ioc/ioc.container.ts`:

```ts
const container = new Container({
  defaultScope: 'Singleton',
  autoBindInjectable: true,
});
container.parent = generalContextStore.rootContainer;  // из MF-core
```

**Файлы:**

| Файл | Назначение |
|---|---|
| `ioc.types.ts` | `TYPES` — все DI-символы (`Symbol.for('IXxxService')`, `Symbol.for('IXxxStore')`). Единственное место объявления символов. |
| `ioc.container.ts` | Создание `Container`, биндинг сервисов и сторов приложения. |
| `ioc.stores.ts` | `initAppStore(container)` — точка регистрации сторов. |
| `ioc.context.ts` | `AppStoreContext` + `useAppStore()` — React-доступ к store-у. |
| `ioc.storeNames.ts` | `StoreNames` — enum имён сторов (`authStore`, `configStore`, `rootStore`). |
| `ioc.errors.ts` | Сообщения DI-ошибок. |
| `index.ts` | Реэкспорт всего (`useAppStore`, `TYPES`, `initAppStore`, `AppStoreContext`, `StoreNames`). |

**Как добавить новый стор/сервис:**

1. Объяви `TYPES.IXxxStore` и `TYPES.IXxxService` в `ioc.types.ts`.
2. Зарегистрируй биндинг в `ioc.container.ts` (или автобиндинг через `injectable()`).
3. Используй в коде через `useAppStore().xxxStore` или `inject(TYPES.IXxxService)`.

**Scope** — по умолчанию `Singleton` (один экземпляр на приложение). Не меняй без необходимости.

---

## Styling

- **Глобально:** `styled-components` (см. `resolutions` в `package.json` — версия зафиксирована как `^5.3.0`).
- **UI-компоненты:** `@sber-sbertransport/ui-kit` + `antd@4.17.2`. Версии — в `package.json`, не дублируй.
- **Локальные стили модуля:** `*.module.scss` (рядом с компонентом), `*.module.css` через `tsconfig` plugin `typescript-plugin-css-modules`.
- **`craco-less`** подключён для override-стилей Ant Design через `scripts/index.js`.

> Темизация (ThemeProvider) в этом микросервисе не используется — тема определяется на уровне host-приложения.

---

## Conventions

### Naming

- **Компоненты:** `PascalCase` (`TripsTable`, `SetDriverModal`), папка в `src/components/<Name>/` с `index.ts`.
- **Хуки:** `useXxx` (`useFilters`, `useMyTrips`).
- **Сторы:** `XxxStore` (`AuthStore`, `ConfigStore`).
- **Сервисы:** `IXxxService` / `XxxService` (DI-символы: `TYPES.IXxxService`).
- **API-хуки:** экспорт константы ключа в `UPPER_SNAKE` (`PASS_TRIP_KEY`), имя хука — `useXxx`.
- **Папки:** `kebab-case` (`shift-conflicts`, `schedule2.0`).
- **Файлы:** для компонентов — `index.tsx` экспорт + служебные `<Name>.<Role>.tsx` (`FiltersModal.tsx`, `StatusConfirm.tsx`).

### Imports

- **Порядок:** `yarn sort-imports` раскладывает импорты по 19 группам (`scripts/sort-imports.js`):
  1. `react`
  2. `@sber-sbertransport`
  3. external (другие библиотеки)
  4. `api`, `constants`, `hooks`, `context`, `utils`, `i18n`, `shared`, `ioc`, `mf`, `types`, `ui`, `modules`, `components`
  4. local (`./`, `../`)
  4. assets, styles, styles-module
- Запускай `yarn sort-imports` перед коммитом — pre-commit хук это проверяет.

### React

- Только функциональные компоненты. Никаких классовых компонентов.
- Хуки — по правилам React: не вызывать в условиях/циклах; cleanup в `useEffect` (таймеры, подписки, `AbortController`).
- MobX-action **нельзя** вызывать из render-функции — оборачивай в `useEffect` или обработчик события.

### TypeScript

- `strict: true` в `tsconfig.json`. `noImplicitAny: false` (наследие) — формально `any` не запрещён, но в новом коде избегай.
- `experimentalDecorators: true` + `emitDecoratorMetadata: true` (для inversify/MobX-декораторов).
- `baseUrl: ./src` — алиасы импорта без префикса `src/`: `import { useAppStore } from 'ioc'` (а не `'../../ioc'`).
- Utility-типы (`Pick`, `Omit`, `Partial`, `Readonly`) предпочтительнее дублирования интерфейсов.
- `noEmit: true` — компиляция только для проверки типов.

### Анти-паттерны

- ❌ Прямой `axios.get(...)` — только через `IHttpService` (через `useAPI`/`useAPIMutation`).
- ❌ Магические URL-строки в `*.api.ts` — выноси в `*.constants.ts`.
- ❌ Мутация `useState`/`MobX`-объектов напрямую — spread/map/filter для новых копий.
- ❌ Забытые промисы (`void fetch(...)`) — оборачивай в `try/catch` или явно игнорируй через `ignore()` из `utils`.
- ❌ `var`, нестрогое сравнение, callback-хелл — не используй.
- ❌ Классы для UI-компонентов.

---

## Environment Variables

| Переменная | Значения | Описание |
|---|---|---|
| `REACT_APP_NAME` | `platform` | Имя микросервиса |
| `REACT_APP_REMOTE` | `TRUE` / `FALSE` | Режим Remote (host) или Self-hosted (dev) |
| `REACT_APP_DEBUG` | `TRUE` | Debug-режим |
| `REACT_APP_NETWORK_LOOP` | `DEV_AUTOPARK`, `DEV_CARGO`, `DEV_AUTOSERVICE`, `DEV_SBERTRANSPORT`, `ST`, `NT`, `IFT` | Окружение API |
| `REACT_APP_BASIC_AUTH` | `TRUE` | Basic-авторизация |
| `REACT_APP_MOCKED_AUTH` | `TRUE` | Замоканная авторизация |
| `REACT_APP_MOCKED_API` | `TRUE` | Моки API |

---

## Source of Truth

| Тема | Документ |
|---|---|
| Каталог всех утилит (`src/utils/`) | **`src/utils/UTILS.md`** |
| Документация модуля **Reports** | `src/modules/Reports/README.md` |
| Документация модуля **Schedule2.0** | `src/modules/Schedule2.0/README.md` |
| Документация модуля **Staff** | `src/modules/Staff/README.md` |
| Документация модуля **Support** | `src/modules/Support/README.md` |
| Документация модуля **Trip** | `src/modules/Trip/README.md` |
| Документация модуля **Trips** | `src/modules/Trips/README.md` |
| Детальные правила ревью (9 направлений) | `instructions.md` |
| Основной README проекта | `Readme.md` |
| Аудит зависимостей | `DEPS_AUDIT_REPORT.md` |
| Пример отчёта по конкретной области | `code-review-ShiftEditLayer.md` |

> ⚠️ Перед любой задачей внутри модуля (`refactor`, `fix`, `feat`, добавление компонента/контекста/модалки) — **сначала прочитай `README.md` этого модуля**. Там точки входа, реальная структура, API-хуки, i18n-ключи и известные ограничения.

---

## Common Tasks

> Полные шаблоны и примеры — в соответствующих skill-ах (`api-skill-creator`, `module-readme-gen`, `unit-test-generator`). Ниже — короткая навигация.

**Новый API-эндпоинт:**
1. Создай `src/api/<name>/` с `<name>.constants.ts`, `<name>.types.ts`, `<name>.api.ts`.
2. Объяви расширение `declare module 'api' { interface Cache { ... } }` в `*.api.ts`.
3. Используй `useAPI` или `useAPIMutation`. Подробности — skill `api-skill-creator` (агент `api-writer`).

**Новый компонент:**
1. `src/components/<Name>/<Name>.tsx` + `index.ts` для экспорта.
2. Если компонент сложный — добавь README в каталоге модуля.

**Новый маршрут:**
1. Константа в `src/constants/routes.constants.ts`.
2. Route в `src/app/prod/AppRouter.tsx` (prod) и/или `src/app/dev/AppRouter.tsx` (self-hosted).
3. Компонент в `src/modules/<Module>/`.

**Новый тест:**
1. Рядом с тестируемым файлом: `Foo.spec.tsx` или `Foo.test.tsx`.
2. Моки `useAPI`/`useAPIMutation` — через `useAPIQueryCache` mock или `jest.mock('api')`. Подробности — skill `unit-test-generator` (агент `unit-test-writer`).

**Новый DI-сервис:**
1. Добавь `TYPES.IXxxService` (и при необходимости `IXxxStore`) в `src/ioc/ioc.types.ts`.
2. Зарегистрируй биндинг в `src/ioc/ioc.container.ts`.
3. Получай через `useAppStore().xxxService` или `inject(TYPES.IXxxService)`.

---

## Gotchas

- **`noImplicitAny: false`** в `tsconfig.json` — формально `any` не запрещён, но в новом коде используй `unknown` с type-guard или явный тип.
- **MobX-action из render** — вызывает warning и потенциальные бесконечные циклы. Оборачивай в `useEffect` или обработчик.
- **`matchMedia`, `ResizeObserver`, `IntersectionObserver`** не существуют в jsdom по умолчанию — замоканы в `src/setupTests.ts`. Если тест падает на этих API — проверь, что setup-файл подключён (он в `setupFilesAfterEach` в `jest.config.ts`).
- **MF-loader ошибки** ловятся через `MFLoader` (`src/mf/MFLoader.tsx`), не вручную.
- **shift-conflicts** — добавлен как API-сервис, но отсутствовал в старых списках документации. Не забывай его при поиске связанной логики.
- **`@sber-sbertransport/mf-core`** — источник `useGeneralStoreContext`, `IHttpService`, `ResponseService`, `ILogger`. Не дублируй эти абстракции локально.
- **`react-query` 2.26** — старая мажорная версия. API отличается от v3+ (`useQueryCache`, `useMutation` без generics). Не копируй примеры из современных туториалов.

---

## Commit Convention

Conventional Commits, scope = имя модуля (`Trips`, `Trip`, `Staff`, `Reports`, `Schedule2.0`, `Support`, `Drivers`).

```
feat(Trips): добавить фильтрацию по статусу
fix(Reports): исправить дублирование данных
docs(Readme): обновить инструкцию по установке
refactor(Drivers): вынести логику в сервис
test(Table): добавить тесты сортировки
```

---

## Reviewer Checklist

Детальная инструкция — `instructions.md` (171 стр., 9 направлений). Краткий список:

1. **Общие** — единообразие, отсутствие дублей, обработка ошибок.
2. **TypeScript** — типизация, utility-типы, generics, `as const`.
3. **JavaScript** — ES6+, иммутабельность, промисы, мемоизация.
4. **React** — функциональные компоненты, хуки по правилам, мемоизация.
5. **Производительность** — `React.memo`, виртуализация списков, ленивая загрузка.
6. **Безопасность** — XSS, инъекции, безопасное хранение данных.
7. **Архитектура** — MobX-сторы, разделение ответственности.
8. **Читаемость** — naming, размер функций, форматирование.
9. **Логика** — состояния загрузки/ошибки/пустоты, валидация.

Дополнительно: [Confluence — Рекомендации по коду](https://confluence.sberbank.ru/pages/viewpage.action?pageId=15056208286).

---

## Notes for GigaCode

- Используй **yarn** для управления пакетами (`yarn add ...`).
- Запускай тесты через `yarn test`, линтер через `yarn lint` после изменений.
- **Functional components only.** Никаких классов для UI.
- **MobX** — `observable` / `action` / `computed` + стрелочные методы в классах.
- **API-слой** — строго через `useAPI` / `useAPIMutation` с декларацией `Cache`. Никакого прямого axios.
- **DI** — символы только в `ioc.types.ts`, регистрация — в `ioc.container.ts`.
- **Imports** — сортируй через `yarn sort-imports` (19 групп).
- **Никогда** не делай `force push`, `rebase`, любые git-команды без явной просьбы пользователя.
- Перед задачей в конкретном модуле — прочитай его `README.md`.
- Перед задачей в `src/utils/` — прочитай `src/utils/UTILS.md`.
