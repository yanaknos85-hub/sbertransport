# GIGACODE.md — Контекст проекта для AI-агентов

> **Главный контекстный файл проекта.** Заполняет пробел, описанный в `AGENTS.md` («без `GIGACODE.md` нельзя трогать код»).
> **Аудитория:** AI-ассистенты (GigaCode, Copilot, Cursor и т.п.).
> **Дата заполнения:** 7 августа 2026 г.
> **Версия проекта:** см. `version.json`.

---

## 📑 Указатель по документации

| Файл | Что внутри |
|---|---|
| [`AGENTS.md`](./AGENTS.md) | Краткие правила: зафиксированные версии библиотек, скиллы, быстрые ссылки на конфиги |
| [`Readme.md`](./Readme.md) | Онбординг для людей (запуск, деплой, Module Federation) |
| [`instructions.md`](./instructions.md) | Чек-лист AI-ревьюера (9 направлений + специфика Planner) |
| [`instructions-suggestions.md`](./instructions-suggestions.md) | Planner-специфичные советы ревьюеру |
| `docs/PROJECT_MAP.md` | Карта `src/`: модули, сторы, API, shared-компоненты |
| `docs/ARCHITECTURE.md` | Высокоуровневая архитектура: MF, DI, поток данных |
| `docs/PATTERNS.md` | Типовые паттерны «как писать код» |
| `docs/DOMAIN_GLOSSARY.md` | Словарь предметной области (грузоперевозки, ЭТрН, оргструктура) |
| `docs/STATE_MANAGEMENT.md` | Устройство MobX: структура стора, регистрация в IoC, список сторов |
| `docs/API_LAYER.md` | Устройство API: react-query v2, конвенции, error handling |
| `docs/PITFALLS.md` | Известные грабли: ESLint ignores, legacy, антипаттерны |
| `src/modules/<X>/AGENTS.md` | Правила и инструкции для конкретного модуля (Planner, CargoRegistry, Registry) |
| `src/modules/<X>/<X>.architecture.md` | Архитектура модуля (например, `Planner.architecture.md`) |
| `openspec/specs/<X>/spec.md` | Спека фичи в формате EARS/REQ/SCN (Spec-Driven Development) |
| `openspec/changes/archive/` | Архив завершённых изменений |

---

## 🛠️ Стек

| Категория | Технология | Версия |
|---|---|---|
| UI | React | `16.14.0` |
| Язык | TypeScript | `5.2.2` |
| State (глобальный) | MobX | `^5.15.4` (классы + `@observable`/`@action`/`@computed`, **НЕ** v6+ — нет `makeAutoObservable`) |
| UI-kit | Ant Design | `4.17.2` |
| State (серверный) | React Query | `2.26.4` (используется через `useAPI`/`useAPIMutation` хелперы) |
| Routing | React Router DOM | `5.3.0` (`Switch`, `useHistory`, `useParams` — НЕ v6+) |
| DI | Inversify | (через `@sber-sbertransport/mf-core`) |
| Карты | OpenLayers (`ol` + `rlayers`), Leaflet (`leaflet` + `react-leaflet`) | — |
| Drag-and-drop | **Нативные HTML5 API** (НЕ react-dnd) | — |
| Стили | `styled-components 5.3.3` + SCSS-модули (`styles.module.scss`) | — |
| Валидация типов | `io-ts ^2.2.4` + `fp-ts` | — |
| HTTP | `axios` | — |
| Дата | `moment.js` (legacy, не использовать в новом коде — лучше `dayjs`) | — |
| Module Federation | `@sber-sbertransport/mf-core`, `@sber-sbertransport/tool-kit`, `@sber-sbertransport/ui-kit` | — |
| Тесты | Jest + ts-jest + React Testing Library | — |
| Линт | ESLint (конфиг через `@sber-sbertransport/tool-kit/tslint`) | — |
| Git hooks | Lefthook (pre-commit: lint, pre-push: node + package check) | — |
| Сборка | Craco (через `scripts/index.js`) | — |

### ⚠️ Версии зафиксированы

**Запрещено** обновлять мажорные версии без согласования (см. `AGENTS.md`). Обновление ломает совместимость с хост-приложением Module Federation.

---

## 📁 Структура (упрощённая)

```
src/
├── api/                  # 57 сервисов (axios + react-query v2)
│   └── {service}/        # сложный сервис: .api.ts + .types.ts + .constants.ts
├── app/
│   ├── dev/              # локальный запуск как самостоятельное приложение
│   └── prod/             # Remote Mode для Module Federation
├── components/           # глобальные компоненты (Cards.tsx, DownloadButton.tsx)
├── constants/            # routes, app, env, api, geo
├── context/              # Organization.context, …
├── fonts/, styles/       # глобальные стили
├── i18n/                 # интернационализация
├── ioc/                  # Inversify DI-контейнер
│   ├── ioc.container.ts  # корневой AppStore()
│   ├── ioc.stores.ts     # биндинги кастомных сторов (cargoStore, plannerStore)
│   ├── ioc.types.ts      # Symbol-токены (TYPES.*)
│   ├── ioc.context.ts    # AppStoreContext + useAppStore()
│   ├── ioc.storeNames.ts # enum StoreNames
│   └── ioc.hooks.ts      # хуки для стора
├── mf/                   # MFLoader, debug.ts
├── modules/              # 40 бизнес-модулей
├── shared/               # 58 переиспользуемых компонентов, 15 хуков, form
├── stores/               # в основном io-ts DTO-модели (*.interface.ts); MobX-стор: DIPlanner.store.ts (DICargoStore см. modules/OrderExecution)
├── typing/               # типы для сторонних библиотек
├── ui/                   # Layout для локального запуска (SideMenu)
└── utils/                # 80+ утилит
```

**Ключевые файлы:**
- `src/constants/constants.routes.ts` — все маршруты (`PLANNER`, `CONTRACTORS`, `MULTI_LOGISTICS` и т.д.)
- `src/constants/constants.api.ts` — все эндпоинты API (`ETRN_CARGO_LIST`, `PLANNER_*` и т.д.)
- `src/constants/constants.app.ts` — енумы статусов, форматы дат, и т.п. (`EmployeeStatus`, `TripRequestStatuses`)
- `src/ioc/ioc.stores.ts` — **биндинги кастомных сторов** (на 2026-08: `cargoStore`, `plannerStore`)

---

## 📜 Конвенции

### Именование
- **Файлы**: `kebab-case` для папок, `PascalCase` для компонентов, `camelCase` для утилит
- **Компоненты**: `PascalCase`, лежат в папке с тем же именем (`Button/Button.tsx`)
- **Хуки**: `use*` префикс (`useApiOrders`, `usePagination`)
- **Сторы**: `PascalCase.store.ts` (`Planner.store.ts`, `Cargo.store.ts`)
- **Контексты**: `*.context.tsx` или `*.context.ts`
- **Тесты**: `*.test.ts(x)` рядом с тестируемым файлом

### Импорты
1. Сторонние библиотеки (`react`, `mobx`, `antd`)
2. Глобальные алиасы (`api`, `components`, `shared`, `modules`, `utils`, `constants`, `stores`, `context`, `i18n`, `hooks` — через `baseUrl: ./src`)
3. Локальные относительные (`./Button`, `../types`)
- **Абсолютные пути через `baseUrl`** (`tsconfig.json: baseUrl: ./src`)
- **Не использовать** длинные относительные пути `../../../../components/...`

### Стилизация
- Двойная система: **`styled-components`** (`*.styles.ts` для файлов с JSX, `*.styles.ts` для объектов) **+ SCSS-модули** (`styles.module.scss`)
- antd-компоненты стилизуются через `styled(Button)`, **не** через `className`
- SVG-иконки: `import { ReactComponent as Icon } from './icon.svg'`
- Общие стили: `src/shared/styles/styles.ts`

### Структура бизнес-модуля
```
src/modules/<Module>/
├── <Module>Router.tsx              # корневой роутер модуля (lazy)
├── <Module>.architecture.md        # описание архитектуры (Planner — есть, остальные нет)
├── AGENTS.md                       # правила и инструкции модуля (Planner, CargoRegistry, Registry)
├── types.ts                        # io-ts типы
├── constants.ts                    # enum-ы, константы модуля
├── context/<Module>Context.tsx     # UI-контекст через createCallableCtx
├── Components/
│   ├── Main/                       # точка входа
│   ├── <Subdomain>/                # подразделы
│   ├── Modals/                     # модальные окна
│   └── styles.module.scss
└── Pagination/, SortMenu/, Icon/   # вспомогательные
```

### Типизация через io-ts
- Все API-контракты описываются через `io-ts` (`t.type`, `t.union`, `t.intersection`, `t.keyof`)
- Статический TS-тип выводится через `TypeOf`: `type RouteType = t.TypeOf<typeof RouteType>`
- Хелпер для enum: `utils/ioTypeFromEnum.ts` → `ioTypeFromEnum('RouteStatus', RouteStatusEnum)`
- Декодирование ответов: `process.decodeResponseData()` (через `useAPI`)

### Обработка ошибок
- API-ошибки: `logger.toNotify('error', message, error)` (antd `notification.error`)
- Предупреждения: `logger.toMessage(message)` (antd `message.warning`)
- Антипаттерн: `catch(() => null)` — подавляет ошибки (см. `docs/PITFALLS.md`)

---

## 🧠 Decision Log

> Архитектурные решения, которые не очевидны из кода. **Пересмотр решений — только явный, через эту секцию.**

### Версии библиотек зафиксированы
- **Что:** React 16, Router 5, MobX 5, antd 4, react-query 2, io-ts 2.
- **Почему:** Module Federation с хост-приложением; обновление ломает общие чанки и типы.
- **Когда пересмотреть:** при выходе нового мажорного релиза хост-приложения с поддержкой новых версий.

### Магические строки для статусов/титулов → enum
- **Что:** сравнение статусов через `enum` (`TripRequestStatuses.APPROVED`), не через `'APPROVED'`.
- **Почему:** опечатки (`'APPROVE'` vs `'APPROVED'`), нет автокомплита, нет единой точки правды.
- **Исключение:** io-ts DTO остаётся `t.union([t.null, t.string])` — UI-слой использует enum **параллельно** с runtime-валидацией.

### Drag-and-drop без библиотеки
- **Что:** HTML5 Drag & Drop API нативно, без `react-dnd` или `react-beautiful-dnd`.
- **Почему:** минимизация зависимостей, контроль над поведением в Planner.
- **Когда пересмотреть:** если появится сложная логика (вложенные списки, multi-source drag).

### Module Federation с cargo-remote
- **Что:** `cargo` экспортирует `./App`, `./AppProvider`, `./menu`, `./routes`, `./version`.
- **Почему:** единая точка интеграции с хост-приложением.
- **Shared-зависимости:** через `scripts/constants.js` (`getMfShared`).

### Inversify как DI
- **Что:** глобальные сторы (`cargoStore`, `plannerStore`) биндятся в `ioc.stores.ts`, доступ через `useAppStoreContext().<store>`.
- **Почему:** разделение ответственности, тестируемость (можно подменить стор моком).
- **Когда пересмотреть:** если число сторов вырастет (>10) — может понадобиться иерархия.

### MobX через `@observable` декораторы
- **Что:** `makeObservable` не используется — декораторы `@observable`/`@action`/`@computed` через `experimentalDecorators: true`.
- **Почему:** MobX 5 не имеет `makeAutoObservable` (это v6+).
- **Когда пересмотреть:** при переходе на MobX 6+.

### Контекст UI-состояния через `createCallableCtx`
- **Что:** `createCallableCtx` из `utils/createCallableContext.tsx` для UI-состояния модуля (фильтры, пагинация, видимость).
- **Почему:** отделение UI-state от бизнес-логики (которая в DI-сторе).

### Спецификации через OpenSpec
- **Что:** фичи документируются в `openspec/specs/<X>/spec.md` в формате REQ/SCN (EARS-подобный).
- **Почему:** единый формат описания требований, привязка к тестам и API-эндпоинтам.
- **Пример:** `openspec/specs/etrn-signing/spec.md` — спека подписания ЭТрН.

---

## 📚 Словарь предметной области

Подробный глоссарий — в `docs/DOMAIN_GLOSSARY.md`. Краткая выжимка:

| Термин | Что это |
|---|---|
| **Груз (Cargo)** | Единица перевозки с параметрами (вес, объём, описание, габариты) |
| **Маршрут (Route)** | Упорядоченная последовательность точек (погрузка/разгрузка) с привязанными грузами |
| **Заявка (Order)** | Запрос на перевозку груза по определённому маршруту |
| **Контрагент (Contractor)** | Юрлицо-партнёр (грузоотправитель/грузополучатель/перевозчик) |
| **Подразделение (Department)** | Структурная единица организации |
| **Исполнительная группа (Executor Group)** | Группа сотрудников, ответственных за обработку заявок |
| **ЭТрН (Etrn)** | Электронная транспортная накладная. Титулы T1–T4 (грузоотправитель → перевозчик-водитель → грузополучатель → перевозчик-приёмка). Подписание через УКЭП (КриптоПро). |
| **Lock-паттерн** | Блокировка карточки ЭТрН на 5 минут при открытии (PUT /lock). См. `openspec/specs/etrn-signing/spec.md` |
| **MRPА** | Место разгрузки/погрузки адреса |
| **Организация (Organization)** | Корневая сущность для всех данных (multi-tenancy) |
| **Сотрудник (Employee)** | Пользователь системы с привязкой к подразделению |

---

## ⚠️ Известные расхождения и предупреждения

### 1. ESLint ignores (28 модулей)
Из линтинга исключены: `Planner`, `Contractors`, `TariffSettings`, `CargoRegistry`, `CargoPackage`, `CargoType`, `DeadlineSettings`, `Departments`, `Employees`, `EmployeesAttributes`, `Engineers`, `Geo`, `Home`, `ImportReport`, `Locations`, `NewTariffs`, `OrderExecution`, `Organizations`, `Page404`, `PersonalRegistry`, `Positions`, `PublicRegistry`, `Registry`, `Roles`, `ServiceMetrics`, `ServiceSettings`, `SharedRides`, `TaxiRegistry`, `TripDetailed`, `TripPurposes`, `TripSettings`, `UploadButton`, `WorkingGroups`.
**Не предлагать lint-фиксы для этих модулей** (см. `eslint.config.js`).

### 2. Jest coverage excludes
Покрытие не считается для: `api/`, `app/`, `context/`, `i18n/`, `ioc/`, `mf/`, `styles/`, `constants/`, `types/`, `*.test.*`, `*.spec.*`, `*.stories.*`, `__mocks__/`, `__fixtures__/`, `__tests__/`.
**Не писать тесты для этих путей** — они не учитываются.

### 3. Legacy vs `_New` компоненты
В Planner одновременно существуют `AddressBlockDetailed` + `AddressBlockDetailed_New`, `RouteParams` + `RouteParams_New`, `Tools` + `Tools_New`. Старые компоненты **не помечены `@deprecated` явным образом**.
**При ревью:** проверить, не дублируется ли логика, и не используется ли старая версия в новом коде.

### 4. `GIGACODE.md` заполнялся итеративно
Файл был пустым до 7 августа 2026 г. Часть контекста могла быть утрачена. **При обнаружении расхождений** — обновить этот файл.

### 5. Custom-файлы ссылок
- `openspec/config.yaml: schema: spec-driven` — фичи документируются по OpenSpec
- `.gigacode/plans/PLAN.*.md` — планы сложных задач (пример: `PLAN.enum-title-type.md`)

---

## 🚀 Быстрые команды (для AI)

```bash
# Запуск линтинга (только для линтуемых модулей)
yarn lint

# Запуск тестов
yarn test
yarn test:clear       # очистить кэш
yarn test:ci          # CI-режим с coverage

# Локальный запуск
yarn start                                    # Remote mode (хост)
yarn start-auth:dev-cargo                     # с Basic-auth + cargo backend
yarn start-auth-mocked-login-api:dev-cargo    # мок-авторизация + мок-API

# Сборка
yarn build           # production
yarn build:debug     # debug
yarn build:sigma     # для Sigma

# Типы
yarn tsc --noEmit
```

---

## 🔍 Что читать в первую очередь

Перед началом работы с конкретной задачей:

1. **Этот файл** — для общей ориентации
2. **`AGENTS.md`** — для кратких правил
3. **`docs/PROJECT_MAP.md`** — чтобы найти «куда смотреть»
4. **`src/modules/<X>/<X>.architecture.md`** — если работа в конкретном модуле
5. **`openspec/specs/<X>/spec.md`** — если задача по фиче со спекой (например, `etrn-signing`)
6. **`instructions.md`** + **`instructions-suggestions.md`** — если задача «провести ревью кода»

---

> **Версия контекста:** 1.0 (7 августа 2026 г.)
> **Контакт:** см. `Readme.md` и `package.json: author`.
