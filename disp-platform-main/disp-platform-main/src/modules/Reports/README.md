# Reports

> Модуль отчётности диспетчерской системы SberTransport: аналитика по автопарку и реестры пассажирских перевозок.

## Назначение

Раздел «Отчёты» предоставляет диспетчеру две функциональные области:

- **Аналитика** — агрегированные графики по автопарку (эксплуатация, ремонты, пробег/стоимость, расход топлива, обновление парка).
- **Перевозки** — таблица (реестр) пассажирских поездок с фильтрами, сортировкой, настройкой колонок и выгрузкой в XLS.

Раздел аналитики скрыт от неинтернальных подрядчиков и пользователей без ролей доступа.

## Точка входа

| Что                  | Файл / экспорт                                        |
|----------------------|-------------------------------------------------------|
| Корневой компонент   | `modules/Reports/Reports.tsx` → `Reports` (default)   |
| Роутер               | `modules/Reports/ReportsRouter.tsx` → `ReportsRouter` |

## Где монтируется

`src/app/prod/AppRouter.tsx`:

```tsx
const Reports = lazy(() => import('modules/Reports/ReportsRouter'));
…
<Route path={routes.REPORTS} component={Reports} />
```

Маршрут: `src/constants/routes.constants.ts` → `routes.REPORTS = ${APP}/reports`.

## Роутинг

`ReportsRouter.tsx` (Switch):

| Путь                                  | Назначение                                        |
|---------------------------------------|---------------------------------------------------|
| `${REPORTS}/:tab/:type`               | Вкладка с под-типом (например `trips/passenger`)  |
| `${REPORTS}/:tab`                      | Вкладка                                           |
| `${REPORTS}`                           | `Redirect` → аналитика или пассажирские перевозки |
| `${REPORTS}/trips`                     | `Redirect` → `${REPORTS}/trips/passenger`         |

- Табы заданы в `Reports.constants.ts` → `enum ReportsTabs { Analytics, Trips, CarService, Finance }`. `CarService` и `Finance` закомментированы (скрыты).
- Типы перевозок: `TripsTab/constants/TripsTab.constants.ts` → `enum TripTypes { Passenger, Cargo }`.

## Структура

```
modules/Reports/
├── Reports.tsx              ← вкладки «Аналитика» / «Перевозки»
├── ReportsRouter.tsx        ← Switch + Redirect
├── Reports.constants.ts     ← enum ReportsTabs
├── Analytics/               ← под-модуль аналитики
│   ├── Analytics.tsx
│   ├── charts/              ← 6 графиков
│   ├── components/          ← BarChart, ChartCard, Filters, FilterTabs, SelectMonth
│   ├── context/AnalyticsQuery.ts
│   └── utils/formatToChartData.ts
└── TripsTab/                ← под-модуль перевозок
    ├── TripsTab.tsx
    ├── Passenger.tsx        ← таблица реестра
    ├── components/          ← Filters, ExportBtn, DefaultSort
    ├── constants/TripsTab.constants.ts
    ├── context/TripsTab.queryContext.ts
    ├── hooks/               ← useColumns, useFilters
    └── utils/
```

## Компоненты

### Корневые

| Компонент   | Файл               | Пропсы   | Назначение                                   |
|-------------|--------------------|----------|----------------------------------------------|
| `Reports`   | `Reports.tsx`      | `—`      | Табы модуля; скрывает «Аналитику» для неинтерн. |
| `TripsTab`  | `TripsTab/TripsTab.tsx` | `—` | Таб «Пассажирские перевозки» + провайдер запросов |
| `Passenger` | `TripsTab/Passenger.tsx` | `—` | Реестр поездок (фильтры, таблица, экспорт) |

### Analytics

- `Analytics` — рамочный компонент: заголовок, плашка «Раздел находится в разработке», `AnalyticsQueryProvider`, `Filters`, `Charts`. При отсутствии доступа редиректит на `REPORTS_PASSENGER_TRIPS_ROUTE`.
- `Charts` (export из `Analytics.tsx`) — компоновка графиков из `charts/`.
- `components/Filters` — форма фильтров аналитики (подрядчик, филиалы, тип транспорта/ТС, год, месяцы).
- `components/BarChart`, `components/ChartCard`, `components/FilterTabs`, `components/SelectMonth` — ui-примитивы графиков.

### Charts (`Analytics/charts/`)

| Компонент                    | Хук аналитики              | Содержимое                                |
|------------------------------|----------------------------|-------------------------------------------|
| `VehicleAnalytics`           | `useVehicleAnalytics`, `useBrandsStatistics` | эксплуатация по месяцам + статистика брендов |
| `OneVehicleAnalytics`        | `useOneVehicleExploitation` | детали одного ТС (дата и срок эксплуатации) |
| `RepairAnalytics`            | `useRepairAnalytics`       | количество авто в работе / в ремонте       |
| `MileageCostAnalytics`       | `useMileageCostAnalytics`   | стоимость пробега: топливо / ТО            |
| `FuelConsumptionAnalytics`   | `useFuelConsumptionAnalytics` | расход топлива, л и руб.            |
| `UpdateAnalytics`            | `useVehicleAnalytics`       | состав парка в «в работе» / «не в работе» Checkbox-фильтрами |

### TripsTab

| Компонент        | Файл                              | Назначение                                   |
|------------------|-----------------------------------|----------------------------------------------|
| `Filters`        | `TripsTab/components/Filters`     | поиск по номеру заявки + модальные фильтры (статусы, период, № ТС, № договора) |
| `ExportBtn`      | `TripsTab/components/ExportBtn`    | выгрузка реестра в XLS (модалка параметров)        |
| `DefaultSort`    | `TripsTab/components/DefaultSort`  | выбор сортировки по умолчанию                      |
| `Table`          | `Passenger.tsx` (внутр.) | клиентская таблица `TableStyled` реестра (`useColumns`) |

## Контексты

- `Analytics/context/AnalyticsQuery.ts` → `[useAnalyticsQuery, AnalyticsQueryProvider]` (через `createCallableCtx`). Хранит фильтры `AutoAnalyticsFilters`, причём `contractorIds`/`autoparkIds` зависят от роли/профиля; `noPagination`.
- `TripsTab/context/TripsTab.queryContext.ts` → `[useTripsTabQuery, TripsTabQueryProvider]`. Хранит `query`, `setQuery`, `setSort`, `setPagination` для реестра перевозок.

## API

### `src/api/trips-reports/` (реестр)

| Хук (react-query) | Эндпоинт                              | Параметры |
|-------------------|--------------------------------------------|-----------|
| `useTripsReports` | `GET ${TRIPS_REPORT_LIST}` (`/trips-reports/contractor/:contractorId/`) | `TripsReportsFilters` (пагинация, сортировка, статусы, период, № ТС, № договора) |

Константы: `TRIPS_REPORTS = /trips-reports`, `TRIPS_REPORT = .../files/report/`, `TRIPS_REPORT_LIST = .../contractor/:contractorId/`, `TRIPS_REPORT_SETTINGS = .../settings/reports/columns/`.
См. `src/api/trips-reports/trips-reports.api.ts`, `.constants.ts`, `.types.ts`.

### `src/api/analytics/`

| Хук (react-query)          | Метод / эндпоинт                       |
|----------------------------|----------------------------------------|
| `useVehicleAnalytics`       | `POST` vehicle-analytics               |
| `useOneVehicleExploitation` | `GET` one-vehicle-analytics            |
| `useRepairAnalytics`        | `POST` repair-analytics                |
| `useOneRepairAnalytics`     | `GET` one-repair-analytics             |
| `useMileageCostAnalytics`   | `POST` mileage-cost-analytics          |
| `useFuelConsumptionAnalytics`| `POST` fuel-consumption-analytics     |
| `useAnalyticsTransport`     | `POST` analytics-transport             |
| `useBrandsStatistics`        | `POST` brand-statistics                |

См. `src/api/analytics/analytics.api.ts`, `.constants.ts`, `.types.ts`.

## Хуки

- `hooks/useColumns` (TripsTab) — конфигурация колонок таблицы реестра (`t.Reports.*`), ссылки на детальную страницу поездки `TRIP.replace(':id', id)`.
- `hooks/useFilters` (TripsTab) — значения по умолчанию (сортировка по `request_human_readable_id` asc).
- `Analytics` использует `useSelfAutopark` (`api/contractors`), `useRoleMap`, `useProfile`, `useRole` для контроля доступа.

## i18n

Корневой блок ключей: `t.Reports` (заголовки колонок реестра), `t.Analytics` (заголовки и тултипы графиков).

| Блок        | ru (`src/i18n/ru/index.ts:682`)          | en (`src/i18n/en/index.ts`)  |
|-------------|------------------------------------------|------------------------------|
| `Reports`   | заголовки колонок реестра, `HumanReadableId`, … | — (отсутствует) |
| `Analytics` | `title`, `fields`, `Vehicle`, `Update`, `OneVehicleAnalytics` | — (отсутствует) |

Локальные i18n-ключи отсутствуют; несколько текстов (названия табов, плашка «Раздел находится в разработке», кнопки) захардкожены в JSX (`t.Analytics.title`).

## Стили

- `Analytics/Analytics.module.scss` — `.container`, `.wrapper`, `.analytics`, `.header`, `.title`, `.warning`.
- Подкомпоненты используют свои `*.module.scss` (прим. `components/Filters/Filters.module.scss`).
- Стили подключаются как CSS-modules: `import styles from './<Name>.module.scss'`.

## Тесты

| Файл                                    | Покрыва                                       |
|-----------------------------------------|------------------------------------------------|
| `Analytics/__tests__/Analytics.test.tsx` | рендер `Analytics`, Filters, чартов                  |
| `Analytics/components/Filters/__tests__/` | тесты формы фильтров аналитики          |

Запуск: `yarn test src/modules/Reports`.

## Известные ограничения / TODO

- Раздел «Аналитика» — плашка «Раздел находится в разработке».
- Табы `CarService`, `Finance`, тип `Cargo` скрыты по просьбе ВП.
- Колонки `authorFullName`, `factCost` временно скрыты, пока нет данных с бэка (TODO 33005).

## Как расширять

1. Новый график → `Analytics/charts/<MyChart>/<MyChart>.tsx` + подключить в `Charts` (`Analytics.tsx`).
2. Новый фильтр → правки `TripsTab/components/Filters` + `hooks/useFilters`.
3. Новый API-вызов → `src/api/<...>/<...>.api.ts`, импортировать через `useAPI`.
4. Новый под-маршрут → править `ReportsRouter.tsx` + `routes.constants.ts`.