# Известные грабли и предупреждения

> **«Что нельзя трогать», «что нельзя предлагать», «где будут ошибки».**
> **Дата:** 7 августа 2026 г.

---

## 📑 Указатель

1. [Версии библиотек зафиксированы](#1-версии-библиотек-зафиксированы)
2. [28 модулей без линта](#2-28-модулей-без-линта)
3. [Исключения из coverage](#3-исключения-из-coverage)
4. [Legacy vs _New компоненты](#4-legacy-vs-_new-компоненты)
5. [Магические строки vs enum](#5-магические-строки-vs-enum)
6. [catch(() => null) — подавление ошибок](#6-catch--null--подавление-ошибок)
7. [Прямые вызовы API в сторах](#7-прямые-вызовы-api-в-сторах)
8. [moment.js — legacy](#8-momentjs--legacy)
9. [MobX v5 — нет makeAutoObservable](#9-mobx-v5--нет-makeautoobservable)
10. [React Router v5 — НЕ v6](#10-react-router-v5--не-v6)
11. [react-query v2 — особая сигнатура](#11-react-query-v2--особая-сигнатура)
12. [io-ts v2 — особый синтаксис](#12-io-ts-v2--особый-синтаксис)
13. [Миграция на новые версии — нет](#13-миграция-на-новые-версии--нет)
14. [Custom-файлы GigaCode](#14-custom-файлы-gigacode)

---

## 1. Версии библиотек зафиксированы

**Источник:** `package.json: resolutions`, `package.json: dependencies`.

| Библиотека | Версия | Запрещено |
|---|---|---|
| React | `16.14.0` | ❌ v17, v18, v19 |
| React Router DOM | `5.3.0` | ❌ v6, v7 |
| MobX | `^5.15.4` | ❌ v6+ |
| Ant Design | `4.17.2` | ❌ v5+ |
| React Query | `2.26.4` | ❌ v3+ |
| TypeScript | `5.2.2` | ⚠️ Не ниже 5.0 |
| io-ts | `^2.2.4` | ❌ v3+ |

**Правило:** если решение требует новой версии — значит, решение неверное. Ищи другой способ.

**Причины:**
- Module Federation с хост-приложением — общие чанки ломаются
- Типы рассчитаны на конкретные версии
- Кастомные хелперы (`useAPI`, `createCallableCtx`) используют API конкретных версий

---

## 2. 28 модулей без линта

**Источник:** `eslint.config.js: ignores`.

**Из линтинга исключены:**
- `CargoPackage`, `CargoRegistry`, `CargoType`
- `Contractors`, `DeadlineSettings`, `Departments`
- `Employees`, `EmployeesAttributes`, `Engineers`
- `Geo`, `Home`, `ImportReport`, `Locations`
- `NewTariffs`, `OrderExecution`, `Organizations`
- `Page404`, `PersonalRegistry`, `Planner`
- `Positions`, `PublicRegistry`, `Registry`
- `Roles`, `ServiceMetrics`, `ServiceSettings`
- `SharedRides`, `TariffSettings`, `TaxiRegistry`
- `TripDetailed`, `TripPurposes`, `TripSettings`
- `UploadButton`, `WorkingGroups`

**Что это значит:**
- ❌ **НЕ предлагать** lint-фиксы для файлов в этих модулях (prettier, unused-vars, и т.п.)
- ✅ Можно предлагать рефакторинг, исправление багов, улучшение архитектуры
- ✅ Соблюдать code style всё равно нужно (для консистентности)

**Перед коммитом:** `yarn lint` пропустит эти модули, но если меняются файлы в линтуемых модулях — линт обязателен.

---

## 3. Исключения из coverage

**Источник:** `jest.config.ts: collectCoverageFrom`.

**Не покрываются тестами (coverage не считается):**

| Путь | Что это |
|---|---|
| `src/api/**` | Все API-сервисы |
| `src/app/**` | Корневой App.tsx, AppProvider, AppRouter |
| `src/context/**` | React-контексты |
| `src/i18n/**` | Интернационализация |
| `src/ioc/**` | DI-контейнер |
| `src/mf/**` | Module Federation |
| `src/styles/**` | Глобальные стили |
| `src/constants/**` | Глобальные константы |
| `src/types/**` | Типы |
| `**/*.types.ts`, `**/*.interfaces.ts` | Файлы типов |
| `**/*.constants.ts` | Файлы констант |
| `**/*.test.*`, `**/*.spec.*`, `**/*.stories.*` | Сами тесты |
| `**/__mocks__/**`, `**/__fixtures__/**`, `**/__tests__/**` | Служебные директории |

**Что это значит:**
- ❌ **НЕ писать** тесты для этих файлов — они не попадут в coverage
- ✅ Можно писать тесты для **другого** кода, который их использует (компоненты, сторы, утилиты)
- ✅ Можно писать smoke-тесты для API (но coverage не вырастет)

---

## 4. Legacy vs _New компоненты

**Проблема:** в Planner (и других модулях) одновременно существуют старые и новые версии компонентов. Старые **не помечены `@deprecated` явно**.

**Примеры:**
- `AddressBlockDetailed` vs `AddressBlockDetailed_New`
- `RouteParams` vs `RouteParams_New`
- `Tools` vs `Tools_New`
- `EditRoutesList/index.tsx` — альтернативный список маршрутов

**Что делать:**

1. **При ревью** проверять:
   - Не используется ли старая версия в новом коде
   - Не дублируется ли логика между старой и новой версией
   - Есть ли TODO/comment на старом компоненте о планах удаления

2. **При разработке новой фичи:**
   - Использовать новую версию (`*_New`)
   - Не рефакторить старый код, если не задача этого

3. **При удалении старого:**
   - Сначала проверить, что нигде не используется (`grep`)
   - Удалить одним коммитом
   - Удалить связанные тесты и стили

---

## 5. Магические строки vs enum

**Проблема:** в коде встречаются сравнения вида `card?.currentTitle === 'T2'` — магические строки без типизации.

**Что делать:**

```ts
// ❌ Плохо — магическая строка
{card?.currentTitle === 'T2' && <SignButton />}

// ✅ Хорошо — через enum
import { TitleType } from 'modules/Planner/Components/EtrnSignature/constants';

{card?.currentTitle === TitleType.T2 && <SignButton />}
```

**Преимущества enum:**
- Автокомплит в IDE
- Защита от опечаток (`'t2'` vs `'T2'`)
- Единая точка правды
- JSDoc с описанием значений

**Где искать «магические строки»:**

```bash
grep -rn "'T1'\|'T2'\|'T3'\|'T4'" src/modules/Planner/Components/EtrnSignature/
grep -rn "'READY_TO_SIGN'\|'SIGNED'\|'EXPIRED'" src/
```

**Исключение:** DTO остаётся `t.union([t.null, t.string])` (runtime-валидация). Enum — для UI-слоя.

---

## 6. catch(() => null) — подавление ошибок

**Проблема:** `catch(() => null)` подавляет ошибки. Пользователь не видит, что пошло не так. Ошибка «теряется» в консоли.

**Пример антипаттерна:**

```ts
// ❌ Плохо — пользователь не узнает об ошибке
useMutation({
  mutationFn: () => createRoute(data),
  onError: () => null,
});
```

**Как правильно:**

```ts
// ✅ Хорошо — пользователь видит ошибку
useAPIMutation(
  ({ http }, data) => http.post(PLANNER_CREATE_ROUTE, data),
  {
    onError: ({ error, logger }) => {
      logger.toNotify('error', 'Не удалось создать маршрут', error);
    },
  }
);
```

**Где искать:**

```bash
grep -rn "catch(() => null)" src/
grep -rn "catch(() => {})" src/
```

---

## 7. Прямые вызовы API в сторах

**Правило:** сторы **не должны** вызывать `axios`/`fetch` напрямую. Только через **сервис** (`@inject(TYPES.X)`).

**Почему:**
- Сервис легко мокать в тестах стора
- Разделение ответственности (HTTP ≠ бизнес-логика)
- Переиспользование HTTP-логики (auth, interceptors, error handling)

**Антипаттерн:**

```ts
// ❌ Плохо — axios в сторе
@injectable()
export class DIPlannerStore {
  @observable orders: Order[] = [];

  async fetchOrders() {
    const response = await axios.get('/api/orders');
    this.orders = response.data;
  }
}
```

**Правильный паттерн:**

```ts
// ✅ Хорошо — через сервис
@injectable()
export class DIPlannerStore {
  @inject(TYPES.IPlannerService)
  private service!: IPlannerService;

  @action
  async fetchOrders(filters: OrdersFiltersType) {
    try {
      const response = await this.service.searchOrders(filters);
      runInAction(() => { this.ordersListStore = response; });
    } catch (error) {
      this.logger.toNotify('error', 'Не удалось загрузить заявки', error);
    }
  }
}
```

---

## 8. moment.js — legacy

**Проблема:** `moment.js` — устаревшая библиотека (>200KB, давно deprecated). Используется в проекте для работы с датами.

**Правила:**

1. **В существующем коде** — продолжать использовать moment (для консистентности)
2. **В новом коде** — предпочтительнее `dayjs` или `date-fns` (если нужно)
3. **При рефакторе** — переводить на `dayjs` постепенно, не делать миграцию «в лоб»

**См. также:** `docs/PATTERNS.md` → «Работа с датами».

---

## 9. MobX v5 — нет makeAutoObservable

**Проблема:** код написан в стиле MobX 5 (декораторы `@observable`/`@action`/`@computed`). В MobX 6+ появились `makeObservable`/`makeAutoObservable` — их **нельзя** использовать.

**Антипаттерн:**

```ts
// ❌ Плохо — это API MobX 6+
constructor() {
  makeObservable(this, {
    field: observable,
    method: action,
  });
}

// ❌ Плохо — это API MobX 6+
constructor() {
  makeAutoObservable(this);
}
```

**Правильный паттерн (MobX 5):**

```ts
@injectable()
export class DIPlannerStore {
  @observable
  field: SomeType;

  @action
  method() {
    // ...
  }

  @computed
  get derivedValue() {
    // ...
  }
}
```

---

## 10. React Router v5 — НЕ v6

**Проблема:** в проекте используется React Router DOM v5. В v6+ API полностью изменился.

**Что нельзя:**

```tsx
// ❌ v6+ API
import { Routes, Route, useNavigate } from 'react-router-dom';

<Routes>
  <Route path="/" element={<Home />} />
</Routes>

const navigate = useNavigate();
```

**Что можно:**

```tsx
// ✅ v5 API
import { Switch, Route, useHistory, useParams } from 'react-router-dom';

<Switch>
  <Route path="/" component={Home} />
</Switch>

const history = useHistory();
const { id } = useParams<{ id: string }>();
```

---

## 11. react-query v2 — особая сигнатура

**Проблема:** в проекте react-query v2. Сигнатура `useQuery` сильно отличается от v3+.

**Не использовать напрямую** — используйте обёртки `useAPI`/`useAPIMutation` из `src/api/index.ts`.

**Почему обёртки:**
- Типобезопасные ключи кэша (через `declare module 'api'`)
- Декодирование через io-ts
- Стандартные `onSuccess`/`onError` с `logger.toNotify`

**Пример:**

```ts
// ✅ Хорошо — через обёртку
import { useAPI } from 'api';

const { data, isLoading, error } = useAPI([KEY, id], ({ http }) =>
  http.get(URL, { urlParams: { id } }).then(process.decodeResponseData())
);

// ❌ Плохо — напрямую react-query
import { useQuery } from 'react-query';

const { data, isLoading, error } = useQuery([KEY, id], () => fetch(URL).then(r => r.json()));
```

---

## 12. io-ts v2 — особый синтаксис

**Проблема:** в проекте io-ts v2. В v3+ синтаксис изменился.

**Что нельзя:**

```ts
// ❌ v3+ API
import * as t from 'io-ts';

const MyType = t.type({
  field: t.string,
});
```

**Что можно:**

```ts
// ✅ v2 API (но это очень похоже на v3)
import * as t from 'io-ts';

const MyType = t.type({
  field: t.string,
});
```

**Разница между v2 и v3 минимальна** для базовых типов. Основные отличия в namespace и некоторых хелперах. При сомнениях — проверять версию в `package.json`.

---

## 13. Миграция на новые версии — нет

**Правило:** **не предлагать** миграцию на новые версии библиотек. Это противоречит зафиксированным версиям и ломает Module Federation.

**Альтернативы:**
- Найти решение, которое работает с текущей версией
- Использовать полифилл, если API отличается
- Спросить пользователя, если другого выхода нет

**Когда пересматривать:**
- При выходе нового мажорного релиза хост-приложения с поддержкой новых версий
- При явном решении команды о миграции

---

## 14. Custom-файлы GigaCode

### `openspec/` — спецификации

- `openspec/specs/<X>/spec.md` — описание фичи в формате REQ/SCN (EARS-подобный)
- `openspec/changes/archive/` — архив завершённых изменений
- `openspec/config.yaml` — `schema: spec-driven`

**При работе над фичей из спеки** — обязательно прочитать `spec.md` для понимания требований.

### `openspec/` vs старые форматы

В проекте есть `src/modules/<X>/<X>.architecture.md` (например, `Planner.architecture.md`) — это **старая форма** документации модуля. Новые фичи документируются через `openspec/specs/`.

### `.gigacode/` — служебная директория GigaCode

- `.gigacode/skills/` — навыки проекта (`git-workflow.md`, `TEST.md`)
- `.gigacode/plans/` — планы задач (пример: `PLAN.enum-title-type.md` в корне)

**Не путать** с пользовательскими скиллами в `~/.gigacode/skills/`.

### Файлы инструкций (`AGENTS.md` / `instructions.md`)

В проекте есть несколько типов:
- `AGENTS.md` (root) — корневой файл с правилами для AI-агентов (версии, скиллы, конфиги)
- `instructions.md` (root) — общая инструкция для AI-ревьюера
- `instructions-suggestions.md` (root) — Planner-специфичные советы
- `src/modules/Planner/AGENTS.md` — детальные рекомендации для модуля Planner
- `src/modules/CargoRegistry/AGENTS.md` — то же для CargoRegistry
- `src/modules/Registry/AGENTS.md` — то же для Registry

**Это разные файлы с разным назначением.** Все читать перед задачей ревью.

---

## 🆘 Чек-лист перед коммитом

- [ ] `yarn lint` проходит (если менялись файлы в линтуемых модулях)
- [ ] `yarn test` проходит
- [ ] Тесты не пишутся для файлов из `collectCoverageFrom`-исключений
- [ ] Магические строки заменены на enum (если применимо)
- [ ] `catch(() => null)` заменены на `logger.toNotify`
- [ ] Сторы вызывают API только через сервисы
- [ ] Commit message соответствует conventional commits с префиксом `TRANSPORT-`
- [ ] Если меняли модуль из ESLint ignores — линт этого модуля пропускается

---

> **Версия:** 1.0 (7 августа 2026 г.)
> **Источник:** `eslint.config.js`, `jest.config.ts`, `package.json`, `commitlint.config.js`, реальный код проекта, `instructions.md`, `instructions-suggestions.md`, модульные `AGENTS.md`.
