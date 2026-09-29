# Архитектура модуля Planner (SberTransport Cargo)

> **См. также общий контекст проекта:**
> - [`/GIGACODE.md`](../../../../GIGACODE.md) — главный контекст (стек, конвенции, decision log)
> - [`/docs/PROJECT_MAP.md`](../../../docs/PROJECT_MAP.md) — карта модулей/сторов/API
> - [`/docs/ARCHITECTURE.md`](../../../docs/ARCHITECTURE.md) — высокоуровневая архитектура (MF, DI, поток данных)
> - [`/docs/PATTERNS.md`](../../../docs/PATTERNS.md) — типовые паттерны (MobX-стор, io-ts, useAPI, DnD, модалки)
> - [`/docs/DOMAIN_GLOSSARY.md`](../../../docs/DOMAIN_GLOSSARY.md) — словарь (груз, маршрут, ЭТрН, титулы, lock-паттерн)
> - [`/docs/STATE_MANAGEMENT.md`](../../../docs/STATE_MANAGEMENT.md) — устройство MobX (DIPlannerStore)
> - [`/docs/API_LAYER.md`](../../../docs/API_LAYER.md) — устройство API (useAPI, useAPIMutation)
> - [`/docs/PITFALLS.md`](../../../docs/PITFALLS.md) — известные грабли (catch(null), magic strings, _New суффиксы)
> - [`openspec/specs/etrn-signing/spec.md`](../../../openspec/specs/etrn-signing/spec.md) — спека подписания ЭТрН

## 📋 Обзор

**Модуль**: `src/modules/Planner/` — бизнес-модуль **Планировщика поездок/маршрутов** для грузовой админки SberTransport.

**Назначение**: Управление созданием, редактированием, просмотром и мониторингом маршрутов и заявок на перевозку грузов. Включает два основных режима:
- **Планировщик** (Planner) — создание и редактирование маршрутов
- **Журнал маршрутов** (Monitor/RouteTable) — просмотр и мониторинг исполненных маршрутов

**Ключевые возможности**:
- Drag-and-drop заявок в маршруты (нативный HTML5 DnD)
- Фильтрация по статусам, датам, регионам, подразделениям
- Карта (OpenLayers) с маркерами и полилиниями
- Детальный просмотр/редактирование маршрутов и заявок
- Пагинация, сортировка, копирование ID

**Стек**: React 16 + TypeScript 5 + MobX + Ant Design + styled-components + react-query + io-ts + moment.js + OpenLayers

---

## 📁 Структура модуля

```
src/modules/Planner/
├── PlannerRouter.tsx              # Корневой роутер модуля (ленивая загрузка)
├── types.ts                       # ~1000+ строк — все io-ts типы (Route, Order, Monitor)
├── constants.ts                   # Константы модуля (маршруты, enum-ы)
├── context/
│   └── PlannerContext.tsx         # UI-контекст (createCallableCtx) — фильтры, пагинация, карта
├── Components/
│   ├── Main/
│   │   ├── index.tsx             # Точка входа — Tab с вкладками (Планировщик / Журнал)
│   │   └── Main.styles.ts        # Стили для вкладок (styled(Tabs))
│   ├── Planner/                   # Основной планировщик
│   │   ├── Planner.tsx           # Оркестратор (Routes + Orders + Map + модалки)
│   │   ├── Planner.styles.ts     # Стили
│   │   └── hooks/
│   │       ├── useOrdersQuery.ts  # Хук-прослойка для заявок
│   │       ├── useRoutesQuery.ts   # Хук-прослойка для маршрутов
│   │       └── __tests__/         # Тесты хуков
│   ├── Orders/                    # Заявки (папка с ~15 подкомпонентами)
│   │   └── Components/           # Orders, OrdersList, OrderListItem, Header, CargoInfo...
│   ├── Routes/                    # Маршруты (папка с ~10 подкомпонентами)
│   │   └── Components/           # Routes, RoutesList, RouteListItem_New...
│   ├── Monitor/                   # Журнал маршрутов
│   │   ├── RouteTable/
│   │   │   ├── index.tsx         # Основная таблица (antd Table)
│   │   │   ├── useTableFields.tsx # Конфигурация колонок
│   │   │   ├── RouteCancelModal.tsx # Модалка отмены
│   │   │   └── hooks/
│   │   │       └── useMonitorQuery.ts # Хук для монитора
│   │   └── DetailedCard/
│   │       ├── ShowOrder.tsx     # Детальный просмотр (журнал)
│   │       └── ...
│   ├── RouteDetailed/            # Детальный просмотр/создание маршрута
│   ├── OrderDetailed/            # Детальный просмотр заявки
│   ├── Map/                      # — (используется shared/Map)
│   ├── Modals/                   # Модальные окна
│   │   ├── Filters/              # OrdersFilters, RoutesFilters
│   │   ├── MonitorFilters/       # Фильтры монитора
│   │   ├── RoutePreviewModal/    # Предпросмотр маршрута
│   │   ├── AddToRouteModal/      # Добавление в маршрут
│   │   ├── NewRouteModal/        # Создание маршрута
│   │   ├── RouteCancelModal/     # Отмена маршрута
│   │   └── ...
│   ├── Pagination/               # Кастомный Pagination
│   ├── SortMenu/                 # Сортировка (SortMenu + SortConstants)
│   ├── Button/                   # Общие кнопки
│   └── Icon/                    # SVG-иконки (ReactComponent)
├── EtrnSignature/                # Третья вкладка — Подписание ЭТрН (электронная транспортная накладная)
│   ├── EtrnSignature.tsx         # observer: Filters + Suspense+Table+Pagination+EtrnModal
│   ├── EtrnSignature.test.tsx
│   ├── constants.ts              # EtrnStatus, EtrnStatusNames, getStatusTone, ETRN_TOOLTIPS
│   ├── types.ts                  # EtrnSignatureResponse, SearchEtrnResponse, EtrnFilters (io-ts)
│   ├── cryptoPro.interface.ts    # Контракт интеграции с КриптоПро (УКЭП)
│   ├── styles.module.scss
│   ├── hooks/
│   │   ├── useEtrnQuery.ts       # pageSetting + enabled = activeTab === Tab.etrn
│   │   └── useEtrnQuery.test.tsx
│   ├── utils/                    # Хелперы (форматирование статусов/титулов)
│   └── components/
│       ├── Filters/
│       │   ├── EtrnFilters.tsx   # Поиск по humanReadableId + кнопка «Фильтры» (в разработке)
│       │   └── styles.module.scss
│       ├── HeaderWithIcon/       # Заголовок колонки с tooltip
│       ├── Table/
│       │   ├── useTableFields.tsx # Конфигурация колонок (HeaderWithIcon, status badge, currentTitle)
│       │   ├── useTableFields.test.tsx
│       │   └── styles.module.scss
│       └── Card/
│           ├── EtrnModal.tsx     # observer: Tabs + lock acquire/release + условный «Подписать УКЭП»
│           ├── EtrnModal.test.tsx
│           ├── types.ts          # EtrnCardDto + коды статусов/титулов/lock/signingOp
│           ├── styles.module.scss
│           ├── icons/
│           └── components/
│               ├── TitleChain/   # Цепочка титулов T1–T4 с подписантами
│               ├── ParticipantBlock/  # Отправитель / получатель / перевозчик
│               ├── PepBlock/     # Основание ПЭП
│               └── CargoAndRoute/# Груз + маршрут
├── images/                       # SVG-иконки (arrowLeft, autoPlanning, bigTruck...)
├── styles.module.scss           # SCSS-модули (глобальные)
└── mockedData.tsx               # Замоканные данные
```

---

## 🔀 Маршрутизация (PlannerRouter)

### Router

```tsx
// src/modules/Planner/PlannerRouter.tsx
export const Router: FC = (): JSX.Element => (
  <>
    <PlanerProvider>                          // Контекст UI-состояния
      <Switch>
        <Route path={routes.MULTI_LOGISTICS} component={MPlanner} />  // Главная (вкладки)
        <CustomRoute path={routes.MULTI_LOGISTICS}>
          <Switch>
            <CustomRoute path={routes.PLANNER_ROUTE_CREATE}     component={RouteDetailed} />
            <CustomRoute path={routes.PLANNER_ROUTE_LIST}       component={EditRoutesList} />
            <CustomRoute path={routes.PLANNER_ROUTE_DETAILED/:id} component={RouteDetailed} />
            <CustomRoute path={routes.PLANNER_ORDER_DETAILED/:id}  component={OrderDetailed} />
            <CustomRoute path={routes.PLANNER_JOURNAL_DETAILED/:id} component={MShowOrder} />
            <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
          </Switch>
        </CustomRoute>
      </Switch>
    </PlanerProvider>
  </>
);
```

### Пути (из `constants/constants.routes`)

| Константа | Значение | Компонент |
|-----------|----------|-----------|
| `MULTI_LOGISTICS` | `/cargo/multi-logistics` | MPlanner |
| `PLANNER_ROUTE_CREATE` | `/planner/route-create` | RouteDetailed |
| `PLANNER_ROUTE_LIST` | `/planner/route-list` | EditRoutesList |
| `PLANNER_ROUTE_DETAILED/:id` | `/planner/route-detailed/:id` | RouteDetailed |
| `PLANNER_ORDER_DETAILED/:id` | `/planner/order-detailed/:id` | OrderDetailed |
| `PLANNER_JOURNAL_DETAILED/:id` | `/planner/journal/:id` | MShowOrder |

### Ленивая загрузка
Все компоненты загружаются через `React.lazy()` — это снижает размер бандла начальной загрузки. Каждый компонент обёрнут в `<Suspense>` (через `CustomRoute`).

### Синхронизация `activeTab` с URL (`?tab=...`)
В `Main/index.tsx` (внутри `MPlanner`) есть три вкладки: `Tab.planner`, `Tab.journal`, `Tab.etrn`. Активная вкладка синхронизируется с query-параметром `tab`:

- При изменении вкладки через `<Tabs onChange>` → `plannerStore.setActiveTabKey(activeKey)` + `history.push` с нужным `?tab=...` (для `planner` параметр сбрасывается).
- При обновлении страницы / прямом переходе по URL — `useMemo(() => new URLSearchParams(location.search))` **синхронно** (до рендера дочерних `Planner`/`RouteTable`/`EtrnSignature`) выставляет `activeTab` в `plannerStore`. Без этого дочерние хуки успевали бы отправить запросы с неверной активностью (`enabled`).

Это критично для `EtrnSignature` и `Monitor/RouteTable`: оба используют `enabled = plannerStore.activeTab === Tab.X` в своих `useQuery`-хуках, чтобы не слать запросы, пока вкладка не активна.

---

## 🗃️ Управление состоянием

### Двойная система: MobX + React Context

#### 1. MobX-стор (DIPlannerStore) — бизнес-логика

**Расположение**: `src/stores/Planner/DIPlanner.store.ts`
**Подключение**: Через Inversify DI (`@injectable`, `@inject`)

**Основные observable-поля**:

| Поле | Тип | Назначение |
|------|-----|-----------|
| `checkedOrdersListStore` | `OrderListMinimalType[]` | Выбранные заявки для объединения |
| `routeStore` | `RouteType` | Текущий просматриваемый/редактируемый маршрут |
| `draggedOrder` | `OrderType` | Перетаскиваемая заявка (DnD) |
| `ordersListStore` | `Page<OrderType>` | Список заявок с пагинацией |
| `routesListStore` | `Page<RouteType>` | Список маршрутов с пагинацией |
| `setRoutesListPageSize` | `{ page; size }` | Пагинация маршрутов |
| `setOrdersListPageSize` | `{ page; size }` | Пагинация заявок |
| `setMonitorListPageSize` | `{ page; size }` | Пагинация журнала |
| `setEtrnListPageSetting` | `{ page; size }` | **Пагинация списка ЭТрН** (изолирована от журнала) |
| `monitorFilters` | `MonitorFiltersType` | Фильтры для журнала |
| `initialMonitorFilters` | `MonitorFiltersType` | Начальные фильтры для сброса |
| `activeTab` | `Tab` (enum: `planner` / `journal` / `etrn`) | Активная вкладка (`Main/index.tsx`) |

**Пример**:
```tsx
@injectable()
class DIPlannerStore {
  @observable activeTab: Tab = Tab.planner;
  @observable ordersListStore: Page<OrderType> = { content: [], totalElements: 0, totalPages: 0 };
  @observable routeStore: RouteType | null = null;

  @action.bound setActiveTabKey(key: Tab) { this.activeTab = key; }
  @action.bound saveOrdersToStore(orders: Page<OrderType>) { this.ordersListStore = orders; }
  @action.bored addOrderToRoute(order: OrderType) { ... } // добавление в список
  @action.bound setEtrnPageSettings({ page, size }) { this.setEtrnListPageSetting = { page, size }; }
}
```

**Доступ из компонентов**:
```tsx
const { planner } = useAppStoreContext(); // Inversify DI контейнер
```

#### 2. React-контекст (PlannerContext) — UI-состояние

**Расположение**: `src/modules/Planner/context/PlannerContext.tsx`
**Тип**: `createCallableCtx` (из `utils/createCallableContext`)

Хранит **только UI-состояние** (не бизнес-логику):

| Состояние | Тип | Назначение |
|-----------|-----|-----------|
| `isVisibleRoutesFilters` | `boolean` | Видимость модалки фильтров маршрутов |
| `isVisibleOrdersFilters` | `boolean` | Видимость модалки фильтров заявок |
| `isVisibleMonitorFilters` | `boolean` | Видимость модалки фильтров монитора |
| `routeFilters` | `{}` | Текущие фильтры для маршрутов |
| `monitorFilters` | `MonitorFiltersType` | Фильтры для монитора |
| `isRouteVisible` | `boolean` | Режим отображения маршрутов |
| `isOrderVisible` | `boolean` | Режим отображения заявок |
| `routeMode` | `'list' \| 'choose' \| 'detailed'` | Режим: список / выбор / детальный |
| `sortingRouteProperty` | `string` | Поле сортировки маршрутов |
| `sortingOrderProperty` | `string` | Поле сортировки заявок |
| `directionRouteAsc` | `boolean` | Направление сортировки маршрутов |
| `directionOrderAsc` | `boolean` | Направление сортировки заявок |
| `coordinates` | `CoordinatesType[]` | Координаты для карты |
| `markers` | `MarkersType[]` | Маркеры для карты |

**Доступ**:
```tsx
const { usePlanner, PlanerProvider } = createCallableCtx(useHook, { name: 'PlanerProvider' });
// В компонентах:
const { isRouteVisible, routeFilters } = usePlanner();
```

**Почему контекст, а не MobX?** — для UI-состояния, которое не требует реактивности (фильтры, модалки, пагинация). MobX хранит только данные с сервера.

---

## 🌐 Запросы к API (react-query)

### Слой-прослойка (Hook Layer)

Каждый тип запроса обёрнут в хук-прослойку, который:
1. Принимает параметры из **контекста организации** (`organizationId` / `executorGroupIds`)
2. Принимает параметры из **UI-контекста** (фильтры, сортировка, пагинация)
3. Управляет **активностью** (`enabled: isActive`) в зависимости от вкладки

**Пример — `useOrdersQuery`**:
```tsx
// src/modules/Planner/Components/Planner/hooks/useOrdersQuery.ts
export const useOrdersQuery = () => {
  const { planner } = useAppStoreContext();
  const { orderFilters, sortingOrderProperty, directionOrderAsc } = usePlanner();
  const { organizationId, executorGroupId, isOrganization } = useOrganizationContext();
  const isActive = planner.activeTab === Tab.planner;

  const query = isOrganization
    ? { organizationId }
    : { executorGroupIds: ..., emptyExecutorGroup: ... };

  return useSearchOrders({
    ...orderFilters, ...query,
    sortSetting: { property: sortingOrderProperty, directionAsc: directionOrderAsc },
    pageSetting: { page: ..., size: ... },
  }, { enabled: isActive });
};
```

### Прямые API-вызовы (из `api/planner.ts`)

Файл `src/api/planner.ts` (~494 строки) содержит все хуки:

| Хук | Метод | Эндпоинт | Назначение |
|-----|-------|---------|-----------|
| `useSearchRoutes` | `POST` | `PLANNER_SEARCH_ROUTES_MULTIPLE` | Поиск маршрутов (пагинация+фильтры) |
| `useSearchOrders` | `POST` | `PLANNER_SEARCH_ORDERS_MULTIPLE` | Поиск заявок (пагинация+фильтры) |
| `useSearchMonitorRoutes` | `POST` | `REQUESTS_JOURNAL_ROUTES_MULTIPLE` | Поиск для журнала (мониторинг) |
| `useGetRoute` | `GET` | `PLANNER_GET_ROUTE_MULTIPLE/:routeId` | Получение одного маршрута |
| `useGetOrder` | `GET` | `PLANNER_GET_ORDER_MULTIPLE/:orderId` | Получение одной заявки |
| `useCreateRoute` | `POST` | `PLANNER_CREATE_ROUTE_MULTIPLE` | Создание маршрута |
| `useDeleteRoute` | `DELETE` | `PLANNER_DELETE_ROUTE_MULTIPLE` | Удаление маршрута |
| `useUpdateRoute` | `POST` | `PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE` | Обновление маршрута |
| `useUpdateRouteV2` | `POST` | `PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE_V2` | Обновление (v2) |
| `useUpdateRouteStatus` | `POST` | `PLANNER_UPDATE_ROUTE_STATUS_MULTIPLE` | Смена статуса |
| `useDeleteAddressPoint` | `DELETE` | `PLANNER_DELETE_ADDRESS_POINT_MULTIPLE` | Удаление точки |
| `useSendContractorRoute` | `PUT` | `PLANNER_SEND_TO_CONTRACTOR_MULTIPLE` | Отправка контрагенту |
| `useCancelRoute` | `PATCH` | `CANCEL_ROUTE` | Отмена маршрута |
| `useAutoPlanning` | `POST` | `AUTO_PLANNING_MULTIPLE` | Автопланирование |
| `useUpdateLoaders` | `PATCH` | `REQUESTS_CHANGE_ROUTE` | Обновление грузчиков |
| `useChangeCargoTransferTime` | `POST` | (waiting for API) | Плановая дата отправления |
| `useChangeCargoShipmentTime` | `POST` | (waiting for API) | Фактическая дата доставки |
| `useSearchDepartments` | `GET` | `GET_ACTIVE_DEPARTMENTS` | Поиск подразделений |
| `useGetRegions` | `GET` | `/geo-zones/` | Регионы |

#### ЭТрН — отдельный модуль (`src/api/etrn-signature/etrn-signature.ts`)

| Хук | Метод | Эндпоинт | Назначение |
|-----|-------|---------|-----------|
| `useSearchEtrn` | `POST` | `${ETRN_CARGO}/list` (через `MOCKED_API_PREFIX`) | Поиск карточек ЭТрН (пагинация, `keepPreviousData: true`) |
| `useEtrnCard` | `GET` | `${ETRN_CARGO}/:cardId` | Получение одной карточки |
| `useAcquireLock` | `PUT` | `${ETRN_CARGO}/:cardId/lock` | Захват блокировки (409 → конфликт) |
| `useReleaseLock` | `DELETE` | `${ETRN_CARGO}/:cardId/lock` | Снятие блокировки при закрытии модалки |

Особенности:
- **Кеш-ключи**: `etrnCard` и `etrnSearch` (в `api/etrn-signature/constants.ts`).
- **`useEtrnCard(id, { suspense: false })`** — **обязательно** передавать `{ suspense: false }`, иначе Suspense заблокирует рендер всей таблицы ЭТрН при переходе на вкладку. Это намеренный контракт, не дефолт `useAPI`.
- **`useAcquireLock` / `useReleaseLock`** — мутации через `useAPIMutation` с `retry: false`.
- **`useSearchEtrn`** — `keepPreviousData: true`, `retry: 3` (поведение по умолчанию `useAPI`).

**Паттерн**:
```tsx
export const useSearchRoutes = (
  query: RoutesFiltersType | {} | undefined,
  config?: QueryConfig<RouteResponseType, unknown>
) => useAPI(
  ['routesMulti', query],                              // cache key
  ({ http, process }) => http
    .post<RouteResponseType>(ENDPOINT_URL, query)
    .then(process.decodeResponseData())                  // decode через io-ts
    .catch(() => [] as any),                            // fallback на пустой массив
  { keepPreviousData: true, ...config }                 // keepPreviousData для пагинации
);
```

---

## 🧩 Компонентная иерархия (Planner)

```
PlannerRouter.tsx (root — lazy load)
└── PlanerProvider (context)
    └── MPlanner (Main — Tab вкладок)
        ├── Planner (observer — агрегатор)
        │   ├── Routes (список маршрутов)
        │   │   ├── Tools_New (инструменты — фильтр+сортировка+обновление)
        │   │   ├── Controls_New (кнопки)
        │   │   ├── RoutesList → RouteListItem_New
        │   │   │   ├── StatusLabel (статус)
        │   │   │   ├── RouteParams (вес/объём/авто) — drag-and-drop
        │   │   │   └── RouteAddressesList → AdressBlockDetailed
        │   │   └── Pagination (кастомная)
        │   ├── Orders (список заявок)
        │   │   ├── OrderSwitch (переключатель)
        │   │   ├── OrderControls (фильтр+сортировка+автопланирование)
        │   │   ├── OrdersList → OrderListItem → ...
        │   │   │   ├── Header (дата, ID, контрагент)
        │   │   │   ├── CargoInfo (информация о грузе)
        │   │   │   └── AddressBlockDetailed (адреса)
        │   │   └── Pagination
        │   ├── MapComponent (OpenLayers — shared)
        │   │   ├── markers (маркеры)
        │   │   └── polylines (линии)
        │   ├── RoutePreviewModal (предпросмотр)
        │   └── Modals:
        │       ├── OrdersFilters (модалка фильтров заявок)
        │       └── RoutesFilters (модалка фильтров маршрутов)
        └── EtrnSignature (Подписание ЭТрН — вкладка "Подписание ЭТрН", lazy)
            ├── EtrnFilters (поиск по humanReadableId + кнопка «Фильтры» — в разработке)
            ├── Table (antd, колонки из useTableFields)
            │   ├── HeaderWithIcon (заголовок колонки с tooltip для T1–T4)
            │   └── статус-бейдж (EtrnStatusNames + getStatusTone)
            ├── Pagination (custom — page/size из plannerStore.setEtrnListPageSetting)
            └── EtrnModal (модалка детального просмотра)
                ├── Tabs: general | documents | checks | history
                ├── TitleChain (цепочка T1–T4 с подписантами)
                ├── PepBlock (основание ПЭП)
                ├── ParticipantBlock (отправитель/получатель/перевозчик)
                ├── CargoAndRoute (груз + маршрут)
                └── actionBarRight: [Закрыть] + [Подписать УКЭП] (только если currentTitle === TitleType.T2)
        └── RouteTable (Монитор — вкладка "Журнал маршрутов")
            ├── Filters (модалка фильтров)
            ├── Table (antd Table с колонками из useTableFields)
            │   └── EditableField (редактирование статуса/дат)
            ├── Pagination (кастомная)
            └── RouteCancelModal (отмена маршрута)
```

---

## 🗂️ Типизация (io-ts)

Файл `types.ts` содержит ~1000+ строк типов, все — через `io-ts`:

**Базовые типы**:
```tsx
import * as t from 'io-ts';

export const RouteType = t.type({
  id: t.string,
  humanReadableId: t.string,
  waypoints: t.array(WaypointType),
  cost: t.number,
  weight: t.number,
  distance: t.number,
  volume: t.number,
  status: RouteStatusEnum,
  segments: t.array(SegmentType),
  ...
});
```

**Статический тип**:
```tsx
type RouteType = t.TypeOf<typeof RouteType>;
```

**Enum через io-ts**:
```tsx
const RouteStatusEnum = t.keyof({
  CARGO_PLANNING: null,
  CARGO_PLANNING_FINISHED: null,
  IN_ROUTE: null,
  COMPLETED: null,
  CANCELLED: null,
});
```

**Ответы API**:
```tsx
const RouteResponse = t.type({
  content: t.array(RouteType),
  totalElements: t.number,
  totalPages: t.number,
});
```

**Типы из контекста**:
```tsx
const MonitorFiltersType = t.partial({             // partial — все поля опциональны
  id: t.string,
  statusSet: t.array(t.string),
  ...
});
```

---

## 🖱️ Drag-and-drop (нативная HTML5)

**Реализация**: без библиотек (без react-dnd).

**Механизм**:
1. `OrderListItem` имеет `draggable={isRouteVisible}`:
   ```tsx
   <div draggable={isRouteVisible}
        onDragStart={() => planner.dragOrder(order, ordersList)}>
   ```
2. `RouteListItem_New` — контейнер для дропа:
   ```tsx
   <div onDragOver={(e) => e.preventDefault()}
        onDrop={() => planner.dragOrderToRoute(draggedOrder, routeId)}>
   ```
3. `Planner.tsx` — обработка поднятия/опускания:
   - При `onDrop` — `planner.dragOrderToRoute(routeId, orderId)`
   - После дропа — `refetchRoutes()` + `refetchOrders()`

**Ограничения**:
- Нет react-dnd, только `onDragStart` / `onDragOver` / `onDrop`
- Нет визуальной индикации (gost-курсор по умолчанию)
- Нет поддержки мобильных (touch)

---

## 🔍 Фильтрация (модальные окна)

**Типы фильтров**:
- **Маршруты (RoutesFilters)**: ID, статус, регионы, даты, подразделения, контрагенты
- **Заявки (OrdersFilters)**: ID, статус, регионы, даты, отделы
- **Монитор (MonitorFilters)**: ID, статус, регионы, даты, контракторы

**Паттерн**:
```tsx
const getFilteredRoutes = (params: RoutesFiltersType) => {
  const request = {
    ...params,
    regionFrom: params.regionFrom || [],
    regionTo: params.regionTo || [],
    statusSet: params.statusSet ? [params.statusSet] : [],
    creationDateRange: {                                        // Дата -> UTC ISO
      start: moment(params.creationDateRange[0]).utc().startOf('day').toISOString(),
      end: moment(params.creationDateRange[1]).utc().endOf('day').toISOString(),
    },
    ...
  };
  setRouteFilters(request);                                    // Сохраняем в контекст
  handleCloseRoutesFilters();                                   // Закрываем модалку
  defaultSortRoute();                                          // Сбрасываем сортировку
};
```

**Сброс пагинации**: `setRoutePage(START_PAGE)` — при изменении любого фильтра.

---

## 📊 Журнал маршрутов (Monitor/RouteTable)

**Расположение**: `src/modules/Planner/Components/Monitor/RouteTable/index.tsx`

**Архитектура**:
```
RouteTable (observer)
├── Filters (модалка — вся панель фильтров)
├── Table (antd)
│   ├── колонки: id, status, author, creationTime, desiredDate, shipmentTime...
│   └── EditableField (редактируемые ячейки)
├── Pagination (кастомная)
├── MonitorFilters (модалка фильтров монитора)
└── RouteCancelModal (отмена)
```

**Колонки** (из `useTableFields` → `TableFieldConfig`):
```tsx
interface TableFieldConfig {
  title: string;                      // Заголовок (из useTranslation)
  dataIndex: keyof MonitorRouteType;  // Поле данных
  render: (value, record) => ReactNode; // Рендер (с EditableField)
  editable: boolean;                   // Редактируемое?
  width: number;                       // Ширина
  ...
}
```

**Хук-прослойка** (`useMonitorQuery`):
```tsx
export const useMonitorQuery = () => {
  const { planner } = useAppStoreContext();
  const { sortingRouteProperty, directionRouteAsc } = usePlanner();
  const { organizationId, executorGroupId, isOrganization } = useOrganizationContext();
  const isJournalActive = planner.activeTab === Tab.journal;

  const orgOrExecutorGroupQuery = isOrganization
    ? { organizationId }
    : { executorGroupIds: ..., emptyExecutorGroup: ... };

  return useSearchMonitorRoutes({
    ...planner.monitorFilters, ...orgOrExecutorGroupQuery,
    sortSetting: { property: sortingRouteProperty, directionAsc: directionRouteAsc },
    pageSetting: { page: planner.setMonitorListPageSize.page, size: ... },
  }, { enabled: isJournalActive });
};
```

---

## 🗺️ Карта (OpenLayers)

**Расположение**: `shared/components/Map/MapComponent` (переиспользуемый компонент)

**Интеграция в Planner**:
```tsx
<MapComponent
  markers={markers as WaypointModel[]}
  polylines={mapPolylines as Segment[]}
  dragging
  zoomControl
  zoomControlPosition={isOrderVisible ? 'bottomCenter' : 'bottomRight'}
  fitToShowAllGeometry
/>
```

**Синхронизация**:
- `handleWaypoints(waypoints, id)` — обновляет `markers` в контексте
- `handleOrderWaypoints(waypoints, id)` — аналогично для заявок

---

## 📑 Подписание ЭТрН (EtrnSignature)

### Назначение и место в модуле

Электронная транспортная накладная (ЭТрН) — третья вкладка `MPlanner` (`Tab.etrn`). Реализует:
- список карточек ЭТрН с фильтрами и пагинацией;
- детальный просмотр карточки в модалке (`EtrnModal`);
- блокировку карточки на время просмотра (lock acquire/release);
- подписание титула Т3 через КриптоПро УКЭП (см. раздел «КриптоПро (УКЭП)» ниже).

### Жизненный цикл блокировки (lock)

`EtrnModal` держит локальное состояние `LockStatus = 'loading' | 'active' | 'conflict'`:

```tsx
const [lockStatus, setLockStatus] = useState<LockStatus>('loading');

// Acquire lock on open
useEffect(() => {
  if (!visible) return;
  setLockStatus('loading');
  acquireLock({ cardId }, {
    onSuccess: () => setLockStatus('active'),
    onError: err => {
      // 409 — карточка уже заблокирована другим пользователем
      if (err?.response?.status === 409) setLockStatus('conflict');
      else setLockStatus('conflict');
    },
  });
}, [visible, cardId]);

// Release lock on close
const handleClose = useCallback(() => {
  releaseLock({ cardId }, {
    onSettled: () => onClose?.(),
  });
}, [cardId, onClose, releaseLock]);
```

При `lockStatus === 'conflict'` показывается красный баннер `Etrn.card.lockConflictBanner` (`#fff2f0` фон, `#ff4d4f` цвет).

### Условный рендер кнопки «Подписать УКЭП»

В `actionBarRight` модалки кнопка «Подписать УКЭП» рендерится только если текущий титул карточки равен `TitleType.T2` (перевозчик/водитель — единственный, кто подписывает на этом титуле):

```tsx
import { TitleType } from '../../constants';

// ...

<div className={styles.actionBarRight}>
  <button className={styles.closeButton} onClick={handleClose}>
    {Etrn.card.close}
  </button>
  {card?.currentTitle === TitleType.T2 && (
    <button className={styles.signButton} disabled>
      {Etrn.card.signUkep}
    </button>
  )}
</div>
```

Кнопка активна при `card?.currentTitle === TitleType.T2 && lockStatus === 'active'`. Клик открывает `SignT3Modal` для подписания через КриптоПро (подробности — раздел «КриптоПро (УКЭП)» ниже). Условие сравнения `card?.currentTitle === TitleType.T2` корректно обрабатывает все edge cases: `undefined` (карточка ещё не загружена), `null`, любые другие титулы (`TitleType.T1`/`T3`/`T4`).

**Сравнение через enum, не через магическую строку:** enum `TitleType` живёт в `constants.ts` (`EtrnSignature/constants.ts`) и определяет UI-хелпер для сравнений (`T1`–`T4` как строковые литералы). DTO `currentTitle` остаётся типизирован как `t.union([t.null, t.string])` — runtime-валидация не меняется. Это дополняющие друг друга слои: io-ts валидирует форму ответа API, enum — единая точка правды для UI-сравнений. Подробнее — в Decision Log `GIGACODE.md`.

### Титулы (Title)

Контракт кодов титулов в `components/Card/types.ts`:

```ts
export const TitleTypeCode = t.union([
  t.literal('T1'), t.literal('T2'), t.literal('T3'), t.literal('T4'),
]);
```

| Титул | Назначение |
|-------|-----------|
| `T1` | Отправление груза — Сведения о грузе и его передаче для перевозки |
| `T2` | Приём груза перевозчиком — Подтверждение приёма груза для перевозки |
| `T3` | Приёмка груза получателем — Сведения о фактической приёмке груза |
| `T4` | Завершение перевозки — Завершающие сведения о выполнении перевозки |

Цепочка титулов рендерится через `TitleChain` (CSS: статус-бейдж + коннекторы между карточками). Описание титулов — в `ETRN_TOOLTIPS.currentTitle` (массив из 4 строк, по строке на титул).

### Статусы карточки и тонирование

`EtrnStatus` enum + `EtrnStatusNames` (человекочитаемые названия):

| Код | Название |
|-----|----------|
| `IDENTIFIED` | Идентификация |
| `WAIT_KORUS_DATA` | Ожидание данных от КОРУС |
| `WAIT_CONDITIONS` | Ожидание проверки условий |
| `READY_FOR_BANK_ACTION` | Готов к подписанию |
| `WAIT_KORUS_CONFIRMATION` | Подтверждение от КОРУС |
| `PROCESS_COMPLETED` | Процесс завершён |

`getStatusTone(status)` возвращает `'green'` для `READY_FOR_BANK_ACTION`, `WAIT_KORUS_CONFIRMATION`, `PROCESS_COMPLETED`, иначе `'gray'`. Применяется к статус-бейджу в колонке `status` таблицы.

### Поток данных (от таблицы до модалки)

1. `EtrnSignature` через `useEtrnQuery()` вызывает `useSearchEtrn` с `enabled = plannerStore.activeTab === Tab.etrn`.
2. На смену `organizationId` (через `useOrganizationContext`) хук `useEtrnQuery` сбрасывает страницу на 0 (`useEffect` → `plannerStore.setEtrnPageSettings({ page: 0, size })`).
3. `useTableFields({ onOpenCard })` возвращает конфигурацию колонок. Колонка `humanReadableId` рендерит кликабельный `<div>` (cursor: pointer) с обработчиком `onOpenCard(record.id)`.
4. `handleOpenCard(cardId)` выставляет `selectedCardId` → `isModalOpen = selectedCardId !== ''`.
5. `EtrnModal` при `visible=true` запускает `useEtrnCard(cardId, { suspense: false })` + `acquireLock`. При конфликте — красный баннер.
6. `handleClose` — `releaseLock` с `onSettled: () => onClose()` → сбрасывает `selectedCardId`.

### i18n

Все строки — через `useTranslation()`:
- `t.Planner.Etrn.*` — заголовки колонок таблицы (`numberEtrn`, `status`, `sla`, `currentTitle`, `senderName`, `receiverName`, `carrierName`).
- `t.Etrn.card.*` — содержимое модалки (`humanReadableIdPlaceholder`, `viewDocument`, `close`, `signUkep`, `lockConflictBanner`, `titleChainTitle`, `titleChainWaiting`, `titleLabels`, `statuses`, `tabs.general`, `tabs.documents`, `tabs.checks`, `tabs.history`, `documentsContent`, `checksContent`, `historyContent`).

### КриптоПро (УКЭП)

Реализовано полноценное подписание титула Т3 через `crypto-pro-actual-cades-plugin` (зависимость проекта).

**Структура (`Components/EtrnSignature/components/Sign/`):**
- `SignT3Modal.tsx` — основная модалка: оркестрирует шаги (init → выбор сертификата → мутация). Использует `useCryptoPro`, `useSendEtrn`. На успехе закрывается и зовёт `onSigned` колбэк родителя.
- `hooks/useCryptoPro.ts` — инициализация CSP: `cadesplugin.async_spawn()` → `CreateObjectAsync('CAdESCOM.About')` → возвращает `cadesplugin`. Состояния: `loading | ready | error`.
- `constants/CryptoPro.ts` — enum `Messages` с диагностическими строками CSP.
- `components/Loading/` — экран ожидания (antd `Spin` + локализованный текст `Etrn.signModal.loading`).
- `components/Error/` — экран ошибки (текст + кнопка «Повторить» → ретригер хука).
- `components/Certificates/` — список сертификатов из `CAdESCOM.Store`; первая запись выбрана по умолчанию. Декодирование полей — `b64DecodeUnicode` из `src/utils/Misc.ts`.
- `useSendEtrn` (в `api/etrn-signature/etrn-signature.ts`) — мутация отправки подписанного Т3. **Заглушка** через `useAPIMutation`, всегда резолвит `{ success: true }` — реальная интеграция с бэкендом в следующих задачах.

**Кнопка «Подписать УКЭП»** в `EtrnModal` отображается только при `currentTitle === TitleType.T2 && lockStatus === 'active'`. Клик открывает `SignT3Modal`. На успехе `EtrnModal` тоже закрывается, вызывается `releaseLock` (в `onSettled`), `onSigned` → `refetch` worklist + `message.success`.

**i18n:** блок `Etrn.signModal.{title, okText, loading, error, certificates}` в `src/i18n/ru/index.ts`.

**Важно:** модалка `SignT3Modal` **НЕ** использует `destroyOnClose` — внутренний state сбрасывается через `useEffect` при повторном открытии, чтобы не инициализировать CSP повторно.

### Фильтры (в разработке)

`EtrnFilters` сейчас содержит:
- поле поиска по `humanReadableId` (`disabled`, обёрнуто в `<Tooltip title="В разработке">`);
- кнопку «Фильтры» (`disabled`, обёрнута в `<Tooltip title="В разработке">`).

Расширение фильтрации — отдельная задача.

---

## 🧪 Тестирование

**Тесты хуков** (`__tests__/`):

**`useOrdersQuery.test.tsx`**:
```tsx
jest.mock('api/planner', () => ({
  useSearchOrders: jest.fn(() => ({ data: [], isLoading: false })),
}));

jest.mock('modules/Planner/context/PlannerContext', () => ({
  usePlanner: jest.fn(() => ({
    orderFilters: {},
    sortingOrderProperty: 'createdAt',
    directionOrderAsc: true,
  })),
}));

// В тесте:
const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
const wrapper = ({ children }) => <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;

it('передает organizationId при isOrganization=true', () => {
  (useOrganizationContext as jest.Mock).mockReturnValue({
    organizationId: 'org-123',
    executorGroupId: [],
    isOrganization: true,
  });

  const { result } = renderHook(() => useOrdersQuery(), { wrapper });
  expect(useSearchOrders).toHaveBeenCalledWith(
    expect.objectContaining({ organizationId: 'org-123' }),
    { enabled: true }
  );
});
```

### Тесты ЭТрН

**`useEtrnQuery.test.tsx`** (`hooks/useEtrnQuery.test.tsx`):
- проверка `enabled: true` при `activeTab === Tab.etrn` и `enabled: false` иначе;
- сброс страницы на 0 при смене `organizationId` (через `useEffect` → `plannerStore.setEtrnPageSettings({ page: 0, size })`);
- реактивность `query` на изменение `setEtrnListPageSetting`.

**`useTableFields.test.tsx`** (`components/Table/useTableFields.test.tsx`):
- проверка, что колонка `currentTitle` рендерит заголовок с `HeaderWithIcon` и tooltip;
- проверка, что `humanReadableId` — кликабельный `<div>`, вызывающий `onOpenCard(record.id)`.

**`EtrnSignature.test.tsx`** (корневой компонент):
- мок `useAppStoreContext` с `plannerStore.setEtrnListPageSetting` и `plannerStore.setEtrnPageSettings`;
- проверка передачи `page`/`size` в `Pagination` из `plannerStore.setEtrnListPageSetting`;
- проверка передачи `setPagination` из `plannerStore.setEtrnPageSettings`.

**`EtrnModal.test.tsx`** (`components/Card/EtrnModal.test.tsx`):
- моки `useEtrnCard` / `useAcquireLock` / `useReleaseLock` через `jest.mock('api/etrn-signature/etrn-signature-card', ...)`;
- проверка вызова `acquireLock` при монтировании с `visible=true`;
- проверка `releaseLock` при клике на «Закрыть»;
- проверка рендера компонентов внутри модалки: `TitleChain`, `ParticipantBlock`, `PepBlock`, `CargoAndRoute`;
- плейсхолдер «Ожидание данных от Корус» при пустом `titleChain`.

> ⚠️ **Известное расхождение моков в `EtrnModal.test.tsx`**: тест мокает `'api/etrn-signature/etrn-signature-card'`, тогда как реальный компонент импортирует из `'api/etrn-signature/etrn-signature'`. Это означает, что моки не перехватывают вызовы из компонента и `useEtrnCard`/`useAcquireLock`/`useReleaseLock` возвращают `undefined` — из-за этого тесты на `useEtrnCard(cardId, { suspense: false })` и `acquireLock` фактически проверяют только факт вызова с правильными аргументами после ручного вызова `mockUseEtrnCard.mockReturnValue(...)` без интеграции с реальным импортом. **Кандидат на ревизию**: поправить путь мока в тесте на `'api/etrn-signature/etrn-signature'`.

---

## 🎨 Стилизация

**Тройная система**:

| Система | Расширение | Применение |
|---------|-----------|-----------|
| `styled-components` | `.styles.ts` | Основной подход (styled.div, styled(antd)) |
| SCSS-модули | `styles.module.scss` | Для AddressBlock, Monitor, Pagination |
| Ant Design | `antd` | Компоненты (Table, Button, Modal, Tabs, Form) |

**SVG-иконки**:
```tsx
import { ReactComponent as Icon } from './icon.svg';     // Через @svgr/webpack
import { ReactComponent as DownArrow } from './assets/DownArrowIcon.tsx'; // Кастомные
```

**shared/styles**:
```tsx
import * as colors from 'shared/styles/colors';          // Общие цвета
import * as S from 'Planner/Planner.styles';              // Локальные стили
```

---

## 🔗 Роутинг (константы и API)

**Константы маршрутов** (из `src/constants/constants.routes`):
```tsx
// src/modules/Planner/PlannerRouter.tsx
const PLANNER_ROUTE_CREATE = `${PLANNER}/route-create`;
const PLANNER_ROUTE_LIST = `${PLANNER}/route-list`;
const PLANNER_ROUTE_DETAILED = `${PLANNER}/route-detailed`;
const PLANNER_ORDER_DETAILED = `${PLANNER}/order-detailed`;
const PLANNER_JOURNAL_DETAILED = `${PLANNER}/journal`;
```

**API-эндпоинты** (из `src/constants/constants.api`):
```tsx
const PLANNER_ROUTE_CARGO_MULTIPLE = 'route-cargo'; // Базовый префикс
// CRUD:
const PLANNER_SEARCH_ROUTES_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/search`;
const PLANNER_GET_ROUTE_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId`;
const PLANNER_DELETE_ROUTE_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId`;
const PLANNER_CREATE_ROUTE_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}`;
// Статусы:
const PLANNER_UPDATE_ROUTE_STATUS_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routelistId/status/:status`;
// Отправка:
const PLANNER_SEND_TO_CONTRACTOR_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId/send/contractor`;
// Отмена:
const CANCEL_ROUTE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/cancel`;
```

---

## ⚠️ Паттерны и антипаттерны

### Паттерны (рекомендованные)
- ✅ **Хуки-прослойки** — инкапсулируют логику параметров, контекста и фильтров
- ✅ **Двойное разделение состояний** (MobX — бизнес, Context — UI)
- ✅ **Ленивая загрузка** — все компоненты через `React.lazy`
- ✅ **keepPreviousData** — для плавной пагинации
- ✅ **io-ts валидация** — типизированные ответы от API
- ✅ **Условный рендер вместо disабла** для кнопок, смысл которых зависит от контекста (например, «Подписать УКЭП» видна только при `currentTitle === TitleType.T2`)
- ✅ **Suspense + `useQuery({ suspense: false })`** для запросов, идущих параллельно с таблицей (паттерн `useEtrnCard`)

### Антипаттерны (избегать)
- ❌ **`catch(() => null)`** — подавляет ошибки, не логирует
- ❌ **Смешение styled-components и SCSS** в одном модуле
- ❌ **Дублирующиеся компоненты** (`_New` без `@deprecated` на старых)
- ❌ **Отсутствие React.memo** на элементах списков
- ❌ **Объекты в пропсах без мемоизации** (ререндер при `useContext`)
- ❌ **Блокировать Suspense таблицы через `useEtrnCard`** — всегда передавать `{ suspense: false }`, иначе рендер таблицы встанет при загрузке карточки
- ❌ **Смешивать состояние lock и состояние модалки** — lock управляется через мутации `acquireLock`/`releaseLock` с `onSettled`, модалка — через локальный `selectedCardId`
- ❌ **Захардкожить магическую строку титула `'T2'` (или `'T1'`/`'T3'`/`'T4'`) в UI-коде** — для сравнений в JSX использовать `enum TitleType` из `EtrnSignature/constants.ts` (даёт автокомплит и единую точку правды). Магические строки допустимы только в io-ts валидаторах DTO (`t.literal(...)`) и в моках тестов (имитация ответа API).

### На что обращать внимание при ревью
1. **Производительность**: все ли компоненты обёрнуты в `React.memo`? Есть ли `useMemo` для контекста?
2. **Типизация**: все ли ответы API проходят через io-ts? Есть ли `any`?
3. **Обработка ошибок**: не замаскирован ли `catch(() => null)`?
4. **Стили**: не смешаны ли SCSS и styled-components?
5. **Тестирование**: есть ли `QueryClientProvider` в обёртке тестов?
6. **ЭТрН**:
   - `useEtrnCard` всегда вызывается с `{ suspense: false }`?
   - `useAcquireLock` обрабатывает 409 как `lockStatus === 'conflict'`?
   - `releaseLock` снимается в `onSettled` (а не `onSuccess`), чтобы лок закрывался и при ошибках?
   - Условие `card?.currentTitle === TitleType.T2` для кнопки «Подписать УКЭП» **не убрано** (это временный контракт, пока не реализовано реальное подписывание)?
   - В моках тестов ЭТрН путь `'api/etrn-signature/etrn-signature-card'` исправлен на `'api/etrn-signature/etrn-signature'`?