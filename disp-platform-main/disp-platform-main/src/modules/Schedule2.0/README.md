# Schedule2.0

> Модуль «График работы» диспетчерской платформы SberTransport. Позволяет просматривать календарный график смен и поездок водителей по часам и машинам, управлять сменами (создание/редактирование/удаление), фильтровать период и отслеживать загруженность автопарка.

## Назначение

Модуль решает задачу планирования смен водителей: визуализация календарной сетки (часы × машины), наложенной на неё загруженности по пассажирским и грузовым поездкам, создание/редактирование разовых и повторяющихся (еженедельных и «плавающих») смен, удаление смен и рядов смен, фильтрация по временному периоду, а также отображение обобщённого показателя загруженности автопарка (rainbow-шкала).

## Точка входа

| Что                  | Файл / экспорт                                       |
|----------------------|------------------------------------------------------|
| Корневой компонент   | `modules/Schedule2.0/Schedule.tsx` → `Schedule` (default) |
| Роутер               | `modules/Schedule2.0/ScheduleRouter.tsx` → `Router` (default), `Router` (named) |
| Public API           | `modules/Schedule2.0/index.ts` → `Schedule`, `*` из `Schedule.types` |
| Мост микрофронтов    | `modules/Schedule2.0/ShiftEditLayer.tsx` → `ShiftEditLayer` |

## Где монтируется

`src/app/prod/AppRouter.tsx`:

```tsx
const Schedule = lazy(() => import('modules/Schedule2.0/ScheduleRouter'));
…
<Route path={routes.SCHEDULE} component={Schedule} />
```

Маршрут: `src/constants/routes.constants.ts` → `routes.SCHEDULE = '/platform/schedule'`.

`ShiftEditLayer` монтируется на уровне `src/app/prod/AppProvider.tsx` (между `ShiftEditContext.Provider` и `TripsProvider`), поэтому слой редактирования смены активен на любом экране хоста, включая экраны флота.

## Структура

```
modules/Schedule2.0/
├── Schedule.tsx                ← корневой компонент
├── ScheduleRouter.tsx          ← маршрутизация табов
├── Schedule.lib.ts             ← утилиты позиционирования moment
├── Schedule.types.ts           ← календарные типы
├── ShiftEditLayer.tsx          ← мост микрофронтов в SimpleEditModal
├── Schedule.module.scss
├── components/
│   ├── RainBow/                ← rainbow-шкала загруженности
│   ├── Workload/               ← бейдж обобщённой загруженности
│   └── SelectVehicle.tsx       ← выбор авто (с пагинацией)
├── constants/schedule.constants.ts
├── context/
│   ├── modal.context.ts        ← открытие/закрытие модалок
│   ├── selectedShift.context.ts← выбранная смена/конфликт
│   └── workload.context.ts     ← общее значение загруженности
├── modals/
│   ├── CreateModal/            ← создание/редактирование смены
│   └── SimpleEditModal/        ← упрощённое редактирование (мост)
└── tabs/
    ├── Shifts/                 ← вкладка смен (календарь)
    └── Conflicts/              ← вкладка конфликтов (скрыта)
```

## Роутинг

`ScheduleRouter.tsx`:

- Маршрут верхнего уровня: `/platform/schedule`.
- Подмаршрут таба: `/platform/schedule/:tab/:routeId?`, где `tab` — из `SchedulerTabs` (`shift` | `conflict`).
- Fallback: `Redirect` → `/platform/schedule/shift` (`SchedulerTabs.Shift`).

## Компоненты

| Компонент | Файл | Пропсы | Назначение |
|-----------|------|--------|------------|
| `Schedule` (default) | `Schedule.tsx` | — | Корневой компонент; оборачивает в `WorkloadGeneralProvider` → `SelectedShiftProvider` → `ModalsProvider`, рендерит `Panel`, `Workload`, `Shifts`, `CreateModal` |
| `Router` | `ScheduleRouter.tsx` | — | Переключение табов и fallback-редирект |
| `RainBow` | `components/RainBow/RainBow.tsx` | `{ value?: number; className?: string; size?: 'small' \| 'middle' }` | Подбирает иконку по диапазону загруженности, выводит процент |
| `Workload` | `components/Workload/Workload.tsx` | — | Бейдж «Загруженность» с tooltip и `RainBow`; берёт `workloadGeneral` из контекста |
| `SelectVehicle` | `components/SelectVehicle.tsx` | `{ onChangeVal?; disableIds?; searchValue?; …Select }` | Поиск авто с бесконечной прокруткой (`useSearchAllVehiclesMutation`) |
| `ShiftEditLayer` / `ShiftEditBridge` | `ShiftEditLayer.tsx` | — | Мост: регистрирует обработчик `openShift(shiftId)` в брокере хоста и открывает `SimpleEditModal` |

### Таб Shifts (`tabs/Shifts/`)

Корневой `Shifts.tsx`: иерархия `ShiftsQueryProvider` → `SelectedTripProvider` → `TableZoomProvider` → `EstimatedHoursProvider` → `AnalyticsWorkloadProvider`; внутри `Header` и в `ErrorBoundary` + `Suspense` — `Table` и модалки `DeleteModal`, `FiltersModal`, `OrderModal`, `SettingsModal`.

| Компонент | Файл | Пропсы | Назначение |
|-----------|------|--------|------------|
| `Header` | `components/Header/Header.tsx` | — | Поиск по госномеру, `FiltersButton`, `PeriodFilter`, зум, настройки, создание смены |
| `FiltersButton` | `components/Header/FiltersButton/FiltersButton.tsx` | `{ onClick }` | Кнопка фильтров периода |
| `PeriodFilter` | `components/Header/PeriodFilter/PeriodFilter.tsx` | — | Select «Сегодня/Неделя/Месяц» (`PeriodType`), меняет `startDate`/`endDate` |
| `Table` | `components/Table/Table.tsx` | — | Календарная сетка «часы × машины»: смены и поездки, пагинация |
| `Name`, `Shift`, `Trip`, `Vehicle`, `OnlineSwitcher`, `Th`, `CurrentTime` | `components/Table/components/*` | см. файлы | Бейджи ячеек, заголовок часа, онлайн-переключатель, линия текущего времени |
| `DeleteModal` | `modals/DeleteModal/DeleteModal.tsx` | — | Удаление смены / всего последующего ряда (`useDeleteShifts`, `useDeleteRowShifts`) |
| `FiltersModal` | `modals/FiltersModal/FiltersModal.tsx` | — | Период `startDate`/`endDate` через `useShiftsQuery` |
| `OrderModal` | `modals/OrderModal/OrderModal.tsx` | — | Назначение водителя и создание смены из поездки (`useCreateShifts`, `useSetDriverToRequest`) |
| `SettingsModal` | `modals/SettingsModal/SettingsModal.tsx` | — | Длительность рабочего дня (`useEstimatedHours`) и показ загруженности (`useWorkload`) |

Контексты вкладки (папка `context/`):

| Контекст | Хук | Смысл |
|----------|-----|-------|
| `shiftsQuery` | `useShiftsQuery` | Обёртка `useQuery<ScheduleFilters>` (localStorage `SHIFTS_FILTERS`) |
| `selectedTrip` | `useSelectedTrip` | Выбранная поездка `TripWithVehicle` |
| `tableZoom` | `useTableZoom` | Ширина ячейки (`cellWidth`), `zoomIn`/`zoomOut` |
| `estimatedHours` | `useEstimatedHours` | Длительность рабочего дня (localStorage `WORKING_DAY_KEY`) |
| `analyticsWorkload` | `useWorkload` | Загруженность, `vehicleIds`, `visibleVehicleWorkload` |

### Таб Conflicts (`tabs/Conflicts/`)

⚠️ **Компонент реализован и поддерживается, но временно скрыт** — использование в `Schedule.tsx` закомментировано (задача TRANSPORT-43426). Папка `context/` пустая, переиспользуются общие `modal.context` и `selectedShift.context`.

| Компонент | Файл | Назначение |
|-----------|------|------------|
| `Conflicts` | `Conflicts.tsx` | Таблица конфликтов смен, поиск по госномеру, `useShiftConflicts`, пагинация |
| `DeleteConflictModal` | `modals/DeleteConflictModal.tsx` | Удаление конфликтной смены (`useDeleteConflictShift`) |
| `useConflictsColumns` | `hooks/useConflictsColumns.tsx` | Колонки с подсветкой ошибок (`SHIFT_CONFLICT_REASONS_TEXTS`) |

## Типы

`modules/Schedule2.0/Schedule.types.ts`:
- `CalendarData` — `calendarStartTime`, `calendarStartHour`, `calendarEndTime`, `isStartOuter`.
- `CalendarShift` `extends Shift, CalendarData`.
- `CalendarTrip` `extends BusynessTrip, CalendarData`.
- `ShiftWithCar` `extends Shift { vehicle: ScheduleVehicle }`.

Также `constants/schedule.constants.ts`: enum `SchedulerTabs { Shift='shift', Conflict='conflict' }`, enum `PeriodicityTypes { Weekly, Flexible }`, `periodicityTitles`, `flexibleDays`, `SHIFT_DATE_FORMAT`, `CELL_PADDING`, `ONE_HOUR`, `WORKING_DAY_LENGTH`, `WORKING_DAY_KEY`.

## Контексты модуля

- `useModals` / `ModalsProvider` (`context/modal.context.ts`) — состояние открытых модалок: `create`, `delete`, `filters`, `order`, `settings`, `deleteConflict`; хендлеры `handleOpen*`/`handleClose`. `close` чистит `routeId` в URL.
- `useSelectedShift` / `SelectedShiftProvider` (`context/selectedShift.context.ts`) — `selectedShift: ShiftWithCar`, `conflictShift: ShiftConflict`.
- `useWorkloadGeneral` / `WorkloadGeneralProvider` (`context/workload.context.ts`) — `workloadGeneral: number`.

Внешние контексты: `useHistory`/`useShiftEditApi` (mf-core, `@sber-sbertransport/mf-core`), `useTripsContext`, `useAPIQueryCache`, `useProfile`, `useAppStore` (ioc).

## API

Хуки основаны на `react-query` (react-query 2.26):

| Хук | Метод / эндпоинт | Где определён |
|-----|------------------|---------------|
| `useSchedule`, `useShift`, `useCreateShifts`, `useUpdateShift`, `useDeleteShifts`, `useDeleteRowShifts` | смены | `api/schedule2.0/schedule.api` |
| `useAnalyticsWorkload` | загруженность | `api/schedule2.0/schedule.api` |
| `useDriverStatuses` | статусы водителей | `api/schedule2.0/schedule.api` |
| `usePassVehicleBusyness`, `useCargoVehicleBusyness` | занятость по поездкам | `api/schedule2.0/schedule.api` |
| `useShiftConflicts`, `useDeleteConflictShift` | конфликты смен | `api/shift-conflicts/shift-conflicts.api` |
| `useProfile` | профиль/автопарк | `api/profile/profile.api` |
| `useSearchAllVehiclesMutation` | поиск авто | `api/vehicles/vehicles.api` |
| `useSearchDriversMutation` | поиск водителей | `api/drivers/drivers.api` |
| `useUpdateDriverOnline` | онлайн водителя | `api/trips/trips.api` |
| `useUpdateCargoDriverOnline` | онлайн грузового водителя | `api/trips-cargo/trips-cargo.api` |
| `useSetDriverToRequest` | назначение водителя | `api/trips/trips.api` |

Кэш-refetch занятости в `useBusynessData` производится через `useAPIQueryCache().refetchQueries([ScheduleKeys.PassBusyness])` при `useTripsContext().lastMessage`.

## Module Federation (remote-модули)

Модуль не содержит `React.lazy`-импортов MF-remote. Единственная связь с микрофронтами — `ShiftEditLayer`, который через брокер хоста (`context/ShiftEdit.context.tsx`) открывает `SimpleEditModal` по `shiftId` из флота (контракт `register` / `openShift`).

## i18n

Корневой блок ключей: `t.Shifts.*` (в `src/i18n/ru/index.ts:711–741`).

| Ключ (примеры)          | ru                     | en   |
|-------------------------|------------------------|------|
| `t.Shifts.title`        | «График»               | —    |
| `t.Shifts.shiftTitle`   | заглавие вкладки смен   | —    |
| `t.Shifts.conflictTitle`| заглавие вкладки конфликтов | — |
| `t.Shifts.workloadTitle`| «Загруженность»        | —    |
| `t.Shifts.createShift` / `editShift` / `removeShit` | создание/редактирование/удаление смены | — |

Блок `Shifts` в английской локали отсутствует (`src/i18n/en/index.ts` — пустая заглушка, ru используется как fallback).

## Стили

- Главный файл: `Schedule.module.scss`.
- Используемые CSS-токены: `--light-blue`, `--light-zinc`, `--white`, `--dark-grey`, `--silver`, `--solitude`, `--light-red`, `--pale-grey`.
- CSS-modules → классы через `styles.<className>`.

## Тесты

Тесты в модуле отсутствуют, рекомендуется добавить. Кандидаты на покрытие:
- `Schedule.lib.ts` — `getCalendarStartHour/StartTime/EndTime` (позиционирование на календаре).
- `components/RainBow/RainBow.tsx` — подбор иконки по диапазону `value`.
- `constants/schedule.constants.ts` — `periodicityTitles`, `flexibleDays`.
- `modals/CreateModal/utils.ts` — `buildShifts` (разовые/повторяющиеся смены).
- Контексты `modal.context.ts`, `selectedShift.context.ts`, `workload.context.ts`.

Запуск: `yarn test src/modules/Schedule2.0`.

## Известные ограничения / TODO

- Вкладка конфликтов (`tabs/Conflicts`) временно скрыта в `Schedule.tsx` (задача TRANSPORT-43426) — код поддерживается, но таб не рендерится.
- Английская локализация (`en/index.ts`) отсутствует — используется русский fallback.
- Модуль использует `moment` и устаревший блок `Tab`/`TabPane` из antd (в закомментированном коде Schedule.tsx).

## Как расширять

1. Новый подкомпонент → `modules/Schedule2.0/components/<Name>/<Name>.tsx`.
2. Новый блок в i18n → добавить в `src/i18n/ru/index.ts` (блок `Shifts:`).
3. Новый API-вызов → `src/api/<...>/<...>.api.ts`, импортировать через `useAPI`/`useQuery`.
4. Новый под-маршрут таба → править `ScheduleRouter.tsx` + `SchedulerTabs` в `schedule.constants.ts`.
5. Новая модалка → `modals/<Name>/` и регистрация флага/хендлера в `context/modal.context.ts`.