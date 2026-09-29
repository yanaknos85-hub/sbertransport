# Trip

> Детальная страница одной пассажирской поездки: маршрут, плановые и фактические данные, водитель, ТС, пассажиры, комментарии и журнал изменений.

## Назначение

Модуль показывает агрегированную информацию по конкретной поездке
(тип `PassTrip`) и позволяет диспетчеру:

- менять статус поездки;
- редактировать фактические данные (пробег, стоимость, время ожидания);
- назначать/переназначать водителя.

Доступ к данным — через реактивные хуки `react-query` с учётом `contractorId`
из профиля текущего пользователя.

## Точка входа

| Что                | Файл / экспорт                          |
|--------------------|-----------------------------------------|
| Корневой компонент | `modules/Trip/Trip.tsx` → `Trip` (default) |
| Public API         | отсутствует (модуль подключается только роутером) |

## Где монтируется

`src/app/prod/AppRouter.tsx`:

```tsx
const Trip = lazy(() => import('modules/Trip/Trip'));
…
<Route path={routes.TRIP} component={Trip} />
```

Маршрут: `src/constants/routes.constants.ts` → `routes.TRIP = ${APP}/trip/:id`
(параметр `id` — `UUID`). `<Suspense fallback={<SpinWrapped />}>` + `<ErrorBoundary>`
оборачивают всё в `AppRouter`.

## Структура

```
modules/Trip/
├── Trip.tsx
├── Trip.module.scss
├── components/
│   ├── Card/                 # Бейдж-обёртка
│   ├── DriverTitle/
│   ├── EditModal/            # Редактирование фактов
│   ├── PersonalCard/
│   ├── Rating/
│   ├── SetDriverModal/       # Назначение водителя
│   ├── TextButton/
│   └── WaypointInfo/
├── constants/passTrip.constants.ts
├── context/TripInfo.context.tsx
├── hooks/
│   ├── useSubtitle.tsx
│   └── useWaypoints.ts
├── sections/
│   ├── AdditionalDataCard/
│   ├── ChangeLogCard/
│   ├── DataCard/
│   ├── PassengersCard/
│   ├── RouteCard/
│   ├── TransferDataCard/
│   ├── CommentCard.tsx
│   ├── DriverCard.tsx
│   └── VehiceCard.tsx
└── types/tripPass.interface.ts
```

## Компоненты

### Секции (`sections/`)

| Компонент | Файл | Пропсы | Назначение |
|-----------|------|--------|------------|
| `RouteCard` | `sections/RouteCard/RouteCard.tsx` | — | Маршрут: таймлайн точек + карта (план/факт) |
| `DataCard` | `sections/DataCard/DataCard.tsx` | — | Плановые и фактические данные, кнопка редактирования |
| `TransferDataCard` | `sections/TransferDataCard/TransferDataCard.tsx` | — | Информация о поездке (трансфер): пассажиры, клиент, рейс, багаж |
| `AdditionalDataCard` | `sections/AdditionalDataCard/AdditionalDataCard.tsx` | — | Доп. параметры (трансфер): детское кресло, животное, багаж, тип ТС |
| `VehicleCard` | `sections/VehiceCard.tsx` | `vehicle` | Автомобиль поездки |
| `DriverCard` | `sections/DriverCard.tsx` | — | Водитель (факт/план) + кнопка «Назначить» |
| `PassengersCard` | `sections/PassengersCard/Passengers.tsx` | — | Список пассажиров с посадкой/высадкой |
| `CommentCard` | `sections/CommentCard.tsx` | `requests`, `comment` | Комментарии к заказу |
| `ChangeLogCard` | `sections/ChangeLogCard/ChangeLogCard.tsx` | — | Журнал изменений (просмотр/изменение статуса) |

Подкомпоненты `RouteCard`: `components/Route/` (timeline точек),
`components/Map/` (карта 2GIS с маршрутами), `components/RouteNullAlert/`
(валert, если фактический маршрут не построен).

### Общие компоненты (`components/`)

| Компонент | Файл | Пропсы | Назначение |
|-----------|------|--------|------------|
| `DriverTitle` | `components/DriverTitle/DriverTitle.tsx` | `isPlanned?: boolean` | Заголовок «Водитель» с подсказкой планового |
| `PersonalCard` | `components/PersonalCard/PersonalCard.tsx` | `title`, `desc1`, `desc2?`, `src?`, `alt?` | Карточка персоны (аватар + фио) |
| `Rating` | `components/Rating/Rating.tsx` | `rating` | Рейтинг со звездой |
| `EditModal` | `components/EditModal/index.tsx` | `trip`, `visible`, `closeModal` | Модалка редактирования фактов |
| `SetDriverModal` | `components/SetDriverModal/SetDriverModal.tsx` | `trip`, `visible`, `closeModal` | Модалка назначения водителя |
| `WaypointInfo` | `components/WaypointInfo/WaypointInfo.tsx` | `waypoint`, `index`, `length`, `passengerCount?`, `passengers?` | Карточка точки маршрута |
| `Card` | `components/Card/Card.tsx` | `HTMLProps` | Бейдж, используется в `WaypointInfo` |
| `TextButton` | `components/TextButton/TextButton.tsx` | `HTMLProps` | Текстовая кнопка (Раскрыть/Скрыть) |

Служебные хуки: `useValues` (модалка редактирования), `waypointInfo.utils.ts` /
`waypointInfo.constants.ts` (тексты и цвета типов точек), `useTransferData.ts`,
`useData.tsx`, `useAdditionalParams.tsx`.

## Типы

`modules/Trip/types/tripPass.interface.ts`:

```ts
interface ITripInfo {
  trip: PassTrip;
  checkinInfo: CheckinInfo;
  waypoints: WaypointWithType[];
}
```

- `PassTrip`, `CheckinInfo` — из `api/trips/trips.types`.
- `WaypointWithType` — из `types/waypoint` (в исходную `waypoint` добавляется
  поле `type` — см. `hooks/useWaypoints.ts`).

## Контексты

`modules/Trip/context/TripInfo.context.tsx`:

- `TripInfoProvider` — подтягивает и кэшит данные поездки (`trip`, `checkinInfo`, `waypoints`);
- `useTripInfo()` — хук доступа к контексту (используется по всем секциям);
- `useTrip(tripId)` — внутренний хук загрузки данных.

## Хуки

| Хук | Файл | Назначение |
|-----|------|------------|
| `useWaypoints` | `hooks/useWaypoints.ts` | Вычисляет тип каждой точки маршрута по чекинам/статусу |
| `useSubtitle` | `hooks/useSubtitle.tsx` | Подзаголовок с датой создания заявки инициатором |
| `useSegments` | `sections/RouteCard/hooks/useSegments.ts` | Плановый/фактический маршрут и расстояния |

## API

| Хук (react-query) | Эндпоинт / метод | Где определён |
|-------------------|------------------|---------------|
| `usePassTrip` | Детали пассажирской поездки | `api/trips/trips.api` |
| `useCheckinInfoPass` | Информация о чекинах | `api/trips/trips.api` |
| `useEditTrip` | Редактирование полей поездки | `api/trips/trips.api` |
| `useSetDriverToRequest` | Назначение водителя | `api/trips/trips.api` |
| `useProfile` | Профиль (для `contractorId`) | `api/profile/profile.api` |
| `usePlanRoute` / `useFactRoute` | Плановый/фактический маршрут | `api/track/track.api` |

## i18n

Модуль использует существующие блоки словарей без собственных ключей:

- `t.Requests.Modal.EditTrip` / `t.Requests.Modal.SetDriver` — модалки;
- `t.Drivers.driverPlanned` — тултип планового водителя;
- `t.global.save` / `t.global.cancel`.

Файлы словарей: `src/i18n/ru/index.ts`; EN-блок отсутствует (значения не переводятся).

## Роутинг

- Верхнего уровня: `routes.TRIP = /platform/trip/:id` (см. `routes.constants`).
- Внутренних подмаршрутов нет; `id` читается через `useParams<{id: UUID}>`.

## Побочные эффекты / эффекты

- `Trip.Page.changeStatus` — смена статуса через `useEditTrip` с уведомлением
  `logger.toMessage('success', …)` из `useAppStore` (IoC).
- Контрольный срок в `DataCard` считается как `expectedStartTime + 15 мин`.
- Маршрут отображается только для завершённых поездок (`useFactRoute.enabled`).

## Стили

- Корневой: `Trip.module.scss` (только `.statusSelect`).
- Модульные стили секций/компонентов: `*.module.scss` CSS-modules.
- `Map` использует `styled-components`, `WaypointInfo` — `styled-components` + `classnames`.

## Тесты

Тесты отсутствуют (`notest`), для модуля рекомендуется добавить покрытие:
- `useWaypoints` / `useData` / `useTransferData` (логика типов точек и рендера данных);
- рендер секций `DataCard`, `DriverCard`, `RouteCard`.

Запуск тестов: `yarn test`.

## Известные ограничения / TODO

- Модуль не имеет собственного `index.ts` (public API) — подключается только роутером.
- Отображение маршрутов зависит от `sourceType: SourceTypes.TWO_GIS` (2GIS).
- Опечатки в исходниках (`VehiceCard`, `dataSource`) сохранены как есть.

## Как расширять

1. Новый подкомпонент → `modules/Trip/components/<Name>/<Name>.tsx`.
2. Новый блок секции → `modules/Trip/sections/<Name>/<Name>.tsx`.
3. Новый API-вызов → `src/api/<...>/<...>.api.ts`, импортировать через `useAPI`/`useQuery`.
4. Изменение маршрута → править `src/constants/routes.constants.ts`.