# Trips

> Модуль пассажирских перевозок («Заявки») диспетчерской платформы: список заявок (таблица + карта), фильтрация, назначение водителя/ТС, редактирование и выгрузка реестра.

## Назначение

Модуль реализует рабочий стол диспетчера по пассажирским поездкам. Позволяет:
- просматривать активные заявки в виде таблицы и/или на карте (2ГИС);
- быстро фильтровать по статусу, периоду, водителю, сервису и поиску по ID заявки;
- назначать водителя и транспортное средство на заявку, назначать заявку водителю;
- редактировать заявку и просматривать адреса (путевые точки);
- выгружать реестр заявок (экспорт);
- смотреть статистику по статусам заявок.

Модуль живёт по адресу `/platform/trips/:type`, где `:type` — `passenger` | `cargo`. Грузовая вкладка — заглушка («Демо»).

## Точка входа

| Что                  | Файл / экспорт                                                            |
|----------------------|---------------------------------------------------------------------------|
| Роутер модуля        | `FakePage/FakePage.TripsRouter.tsx` → `TripsRouter` (default)             |
| Вкладки (passenger/cargo) | `FakePage/FakePage.Trips.tsx` → `FakePage` (default)             |
| Корневой компонент   | `Trips.tsx` → `TripsWithProviders` (default)                              |

## Где монтируется

**Prod (Module Federation):** корневой `Trips.tsx` экспортируется host-приложению как
MF-remote через `scripts/exposes.js`:

```js
'./Trips': './src/modules/Trips/Trips',
```

В шапке/меню хост решает, где рендерить remote. В `prod/AppRouter.tsx` модуль
Trips **не** смонтирован напрямую — там идут trip/staff/schedule/reports/support.

**Dev / самостоятельный запуск:** `src/app/dev/AppRouter.tsx` монтирует обёртку
с вкладками:

```tsx
const Trips = lazy(() => import('modules/Trips/FakePage/FakePage.TripsRouter'));
…
<CustomRoute path={routes.TRIPS} component={Trips} />
```

Маршрут: `src/constants/routes.constants.ts` → `TRIPS = ${APP}/trips`.

Внутри себя `TripsRouter` редиректит `/platform/trips` → `/platform/trips/passenger`
и отдаёт `:type` во вкладки. `FakePage` рендерит `Tabs` на `TripType.Passenger`/`TripType.Cargo`
(`src/constants/app.constants.ts`); пассажирская вкладка лениво грузит `Trips.tsx`
через `lazy(() => import('../Trips'))`, грузовая — заглушка `<div>Демо</div>`.
Оба рендера обёрнуты в `<ErrorBoundary>` + `<Suspense fallback={<SpinWrapped />}>`.

## Структура

```
modules/Trips/
├── Trips.tsx                        # корневой компонент + провайдеры
├── styles.module.scss
├── constants/
│   └── index.ts                     # Columns, staticColumns, TripStatisticStatuses, EXPECTED_TIME
├── context/
│   ├── ActiveTrip.ts                # активная (выбранная) заявка
│   ├── TripsModal.ts               # управление модалками
│   ├── TripsQuery.ts               # query-параметры списка (localStorage)
│   └── TripsSettings.context.ts    # видимость карта/таблица, размер страницы
├── hooks/
│   ├── useFilters.ts               # дефолтные фильтры (статусы, сортировка)
│   └── useMyTrips.ts               # переключатель «Мои» (localStorage)
├── FakePage/                       # роутер + вкладки passenger/cargo
└── components/
    ├── Filters/                    # панель фильтров + модалка фильтров
    ├── TripsTable/                 # таблица заявок (колонки, ячейки)
    ├── Map/                        # карта 2ГИС с водителями и активной заявкой
    ├── Statistics/                 # статистика по статусам
    ├── DurationFilter/             # быстрый фильтр по продолжительности
    ├── SetDriverModal/             # назначение водителя на заявку
    ├── SetTripsModal/              # назначение заявки водителю
    ├── EditModal/                  # редактирование заявки
    ├── Addresses/                  # просмотр адресов/путевых точек
    ├── DownloadModal/              # выгрузка реестра
    ├── VehiclesSelect/             # выбор ТС
    ├── Panel/                      # обёртка-панель
    └── StatusWithConfirm/          # смена статуса с подтверждением (StatusConfirm.tsx)
```

## Компоненты

| Компонент        | Файл                                     | Пропсы                                                                              | Назначение                                        |
|------------------|------------------------------------------|-------------------------------------------------------------------------------------|---------------------------------------------------|
| `TripsWithProviders` | `Trips.tsx`                          | `—`                                                                                 | Корневой компонент: провайдеры + компоновка        |
| `Trips`          | `Trips.tsx`                              | `—`                                    | Сборка фильтров, карты, таблицы и модалок          |
| `TripsWrapper`   | `Trips.tsx`                              | `children`                                   | Клик-вне → сброс активной заявки                  |
| `Filters`        | `components/Filters/index.tsx`           | `isSelfTripsVisible, setIsSelfTripsVisible, columns, tableSettings, onSaveTableSettings` | Панель поиска/фильтров и переключателей           |
| `TripsTable`     | `components/TripsTable/index.tsx`        | `isSelfTripsVisible, getOptimizeColumns`                            | Таблица заявок с пагинацией/сортировкой           |
| `Map`            | `components/Map/index.tsx`               | `—`                                                    | Карта 2ГИС с водителями и активной заявкой        |
| `FiltersModal`   | `components/Filters/FiltersModal.tsx`    | опирается на `useTripsQuery`                                                        | Расширенная модалка фильтров                       |
| `FakePage`       | `FakePage/FakePage.Trips.tsx`            | `—`                                                                  | Вкладки passenger/cargo с lazy-загрузкой          |
| `TripsRouter`    | `FakePage/FakePage.TripsRouter.tsx`      | `—`                                                                    | Роутер `/trips/:type` + redirect                  |

## Контексты (локальные, `src/modules/Trips/context/`)

Все контексты созданы через `createCallableCtx` из `utils/createCallableContext`.

| Контекст               | API                         | Назначение                                             |
|------------------------|-----------------------------|--------------------------------------------------------|
| `TripsSettingsProvider` / `useTripsSettings` | `settings.{map,table}, setIsMapVisible, setIsTableVisible, setTableSize` | Видимость карты/таблицы, размер страницы; переживает `PASS_TRIPS_SETTINGS` в localStorage |
| `TripsQueryProvider` / `useTripsQuery`       | `query, setQuery, setSort, setPagination`          | Параметры списка (фильтры, сортировка, пагинация); `PASS_FILTERS` в localStorage |
| `TripsModalProvider` / `useTripsModal`       | `modalState, openSetDriver, openSetRequest, openEdit, openAddresses, openFilters, openDownload, closeModal, isOpened` | Управление модальными окнами модуля       |
| `ActiveTripProvider` / `useActiveTrip`       | `activeTrip, setActiveTrip`                         | Активная (выбранная на карте) заявка `PassTrip` |

## API

Хуки из `src/api/**`. Ключевые:

| Хук (react-query)                    | Файл                                  | Назначение                          |
|--------------------------------------|---------------------------------------|-------------------------------------|
| `usePassTrips`                       | `api/trips/trips.api`                 | Список пассажирских заявок          |
| `usePassTripsMutation`               | `api/trips/trips.api`                 | Создание/мутирование заявки         |
| `useSetDriverToRequest`              | `api/trips/trips.api`                 | Назначение водителя на заявку       |
| `useChangeVehicle`                   | `api/trips/trips.api`                 | Смена ТС в заявке                   |
| `useEditTrip`                        | `api/trips/trips.api`                 | Редактирование заявки               |
| `useTripStatistic`                   | `api/trips/trips.api`                 | Статистика по статусам              |
| `useAllAvaliableDrivers` / `useDriversLocationWebsocket` | `api/trips/trips.api` | Водители на карте + websocket координат |
| `useProfile`                         | `api/profile/profile.api`             | Профиль (id, contractorId)          |
| `useCreateShifts`                    | `api/schedule2.0/schedule.api`        | Создание смен                       |
| `useShift`/`useDriver`               | `api/schedule2.0`, `api/drivers`      | Данные смены/водителя для модалок   |
| `useTripTransport`                   | `api/dispatchers/dispatchers.api`     | Доступные ТС для выбора             |

Выгрузка реестра: `DownloadModal` использует GET-загрузку файла через константы
`EXPORT_TRIPS` / `TRIPS_SERVICE` из `api/trips/trips.constants`.

## i18n

Корневой блок ключей: `t.Requests` (пассажирские заявки). Определён в
`src/i18n/ru/index.ts` (примерно стр. 546). Подблоки: `Requests.Filters`,
`Requests.Columns` (заголовки колонок таблицы — значения `t.Requests.Columns.*`).

| Ключ                        | ru                       | en                |
|----------------------------|--------------------------|-------------------|
| `t.Requests.trip`           | «Поездка»                | —                 |
| `t.Requests.assignToMe`     | «Взять в работу»         | —                 |
| `t.Requests.Filters.title`  | «Фильтры»                | —                 |
| `t.Requests.Columns.Vehicle`| «Автомобиль»             | —                 |

В `src/i18n/en/index.ts` блок `Requests` отсутствует (en: —).

## Роутинг

- Верхний уровень: `TRIPS = ${APP}/trips` (`src/constants/routes.constants.ts`).
- Подмаршрут: `/platform/trips/:type` (`TripType.Passenger | TripType.Cargo`).
- Редиректы: `/platform/trips` → `/platform/trips/passenger`; `/` и `/client`
  → `/platform/trips/passenger` (в `dev/AppRouter.tsx`).

## Побочные эффекты / эффекты

- `TripsTable` раз в 60 c (через `setTimeout`, не `setInterval` — примечание SAST)
  перезапрашивает список, если модалка закрыта (`!isOpened`).
- `useQuery`/`TripsQuery` — каждое изменение фильтров пишется в `localStorage`
  (`PASS_FILTERS`), в т.ч. размер таблицы → `setTableSize`.
- `TripsSettings` — видимость карты/таблицы и размер страницы сохраняются
  в `localStorage` (`PASS_TRIPS_SETTINGS`).
- `useMyTrips` — состояние переключателя «Мои» в `localStorage`
  (`IS_SELF_TRIPS_PASS_VISIBLE`); у администратора переключатель disabled.
- **Сортировка `START_TIME` — legacy-трюк:** `getSort` в `TripsQuery` мапит
  сохранённое старое поле на новое (`Columns.StartTime`), чтобы не ловить 400.
- `TripsWrapper` — клик вне контейнера (`useClickOutside`) сбрасывает активную
  заявку, если модалка закрыта.

## Стили

- `styles.module.scss` (корневой) — `.requestWrapper`.
- `FakePage/FakePage.module.scss` — `.container`, `.tripTypeTabs`.
- Подкомпоненты используют собственные `*.module.scss` (импорт
  `import styles from './X.module.scss'`).
- Токены/классы утилит: `classnames` (`cn(...)`) для условных стилей строк таблицы
  (`.active`, `.deadline`, `.booked`, `.new` — в `TripsTable/index.module.scss`).

## Тесты

| Файл                                          | Покрывает                        |
|-----------------------------------------------|----------------------------------|
| `components/Filters/__tests__/FiltersModal.test.tsx` | Логика модалки фильтров          |
| `components/TripsTable/__tests__/useColumns.test.tsx` | Формирование колонок таблицы     |

Запуск: `yarn test src/modules/Trips`. Остальные компоненты (Map, модалки,
контексты, роутер) покрытия не имеют — рекомендуется добавить тесты.

## Известные ограничения / TODO

- Грузовая вкладка (`TripType.Cargo`) — заглушка `<div>Демо</div>`, функционал
  не реализован в этом модуле.
- EN-локализация (`i18n/en`) для блока `Requests` отсутствует.

## Как расширять

1. Новый подкомпонент → `modules/Trips/components/<Name>/<Name>.tsx`.
2. Новый столбец таблицы → добавить значение в `enum Columns` (`constants/index.ts`)
   и заголовок в `Requests.Columns` в `src/i18n/ru/index.ts`.
3. Новый API-вызов → `src/api/<...>/<...>.api.ts`, импортировать через
   `useAPI`/`useQuery`.
4. Новое модальное окно → добавить тип в `TripsModal` (`context/TripsModal.ts`)
   и рендер в `Trips.tsx`.
5. Новый под-маршрут или вкладку (passenger/cargo) → править
   `FakePage/FakePage.TripsRouter.tsx` + `routes.constants.ts`.
6. Изменение public-API remote → обновить экспорт `./Trips` в `scripts/exposes.js`.