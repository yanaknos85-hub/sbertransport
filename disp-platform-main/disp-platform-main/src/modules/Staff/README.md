# Staff (Персонал)

Модуль управления персоналом: просмотр, создание, редактирование и удаление
водителей и диспетчеров. Реализует справочник сотрудников в виде двух вкладок
— «Водители» и «Диспетчеры».

## Назначение

Модуль решает задачу ведения кадрового справочника для диспетчерской системы
SberTransport: поиск сотрудников, просмотр детальной карточки выбранного
сотрудника, добавление/редактирование/удаление записей (недоступно для
внутренних автопарков `isInternal`), а также сброс транспортного пароля
сотрудника.

## Точка входа

| Что                | Файл / экспорт                                      |
|--------------------|-----------------------------------------------------|
| Корневой компонент | `modules/Staff/Staff.tsx` → `Staff` (default)        |
| Роутер             | `modules/Staff/StaffRouter.tsx` → `StaffRouter`      |
| Public API         | отсутствует — модуль монтируется только через роутер |

## Где монтируется

`src/app/prod/AppRouter.tsx`:

```tsx
const Staff = lazy(() => import('modules/Staff/StaffRouter'));
…
<Route path={routes.STAFF} component={Staff} />
```

Маршрут: см. `src/constants/routes.constants.ts` → `STAFF` (`/platform/staff`).

## Структура

```
modules/Staff/
├── Staff.tsx               ← вкладки (Tabs) + lazy-загрузка подмодулей
├── StaffRouter.tsx         ← роутинг по `:role` + редирект на drivers
├── styles.module.scss      ← стили вкладок Staff
├── Drivers/                ← подмодуль «Водители»
│   ├── index.tsx           ← Providers + Список + Детали + Модалка
│   ├── components/         ← DriverList, DriversDetailed, ModalFormDriver
│   ├── constants/statuses.ts
│   ├── context/            ← ActiveDriver, ModalForm, EditDeleteContext
│   └── utils/              ← трансформации данных водителя
└── Dispatchers/            ← подмодуль «Диспетчеры»
    ├── index.tsx           ← Providers + Список + Детали + Модалка
    ├── components/         ← DispatcherList, DispatcherDetailed, ModalFormDispatcher
    ├── constants/statuses.ts
    └── context/            ← ActiveDispatcher, ModalForm, EditDeleteContext
```

## Компоненты

| Компонент                | Файл                                                | Пропсы | Назначение                                            |
|--------------------------|-----------------------------------------------------|--------|-------------------------------------------------------|
| `Staff`                  | `Staff.tsx`                                          | —      | Корневой компонент: antd `Tabs` по ролям, lazy-загрузка подмодулей |
| `StaffRouter`            | `StaffRouter.tsx`                                    | —      | `Route path=${STAFF}/:role`, редирект на `/drivers`    |
| `Drivers`                | `Drivers/index.tsx`                                  | —      | Обёртка Providers + список водителей + детали + модалка |
| `DriverList`             | `Drivers/components/DriverList/DriverList.tsx`       | —      | Список водителей (`List`), фильтры, выбор активной строки |
| `DriversDetailed`        | `Drivers/components/DriversDetailed/DriversDetailed.tsx` | — | Детальная карточка выбранного водителя (`ListDetailed`) |
| `ModalFormDriver`        | `Drivers/components/ModalFormDriver/ModalFormDriver.tsx` | — | Модалка создания/редактирования водителя |
| `Dispatchers`            | `Dispatchers/index.tsx`                              | —      | Обёртка Providers + список диспетчеров + детали + модалка |
| `DispatcherList`         | `Dispatchers/components/DispatcherList/DispatcherList.tsx` | — | Список диспетчеров, фильтры по ФИО/статусу, удаление |
| `DispatcherDetailed`     | `Dispatchers/components/DispatcherDetailed/DispatcherDetailed.tsx` | — | Детальная карточка выбранного диспетчера |
| `ModalFormDispatcher`    | `Dispatchers/components/ModalFormDispatcher/ModalFormDispatcher.tsx` | — | Модалка создания/редактирования диспетчера |
| `formJSX`                | `…/{DriverList,DispatcherList}/formJSX/formJSX.tsx` | — | Формы фильтров для `List` (вкладка поиска) |

## Контексты

Контексты созданы через `createCallableCtx` (`utils/createCallableContext`)
и предоставляют хуки + Provider:

- **Drivers** (`Drivers/context/`):
  - `useActiveDriver` / `ActiveDriverProvider` — выбранный водитель и его toggle.
  - `useModalForm` / `ModalFormProvider` — состояние модалки (add/edit) водителя.
  - `useEditDelete` / `EditDeleteProvider` — открытие редактирования водителя.
- **Dispatchers** (`Dispatchers/context/`):
  - `useActiveDispatcher` / `ActiveDispatcherProvider` — выбранный диспетчер.
  - `useModalForm` / `ModalFormProvider` — состояние модалки диспетчера.
  - `useEditDelete` / `EditDeleteProvider` — редактирование и удаление диспетчера (`useDeleteDispatcher`).

## API

| Хук (react-query)               | Эндпоинт / метод                 | Где определён                                  |
|---------------------------------|----------------------------------|------------------------------------------------|
| `useSearchDrivers`              | GET `contractor/…/drivers`       | `api/drivers/drivers.api.ts`                   |
| `useCreateDriver`               | POST `contractor/…/drivers`      | `api/drivers/drivers.api.ts`                   |
| `useUpdateDriver`               | PUT `contractor/…/drivers/:id`   | `api/drivers/drivers.api.ts`                   |
| `useGetDriverLicenses`          | категории прав (константа)       | `api/drivers/drivers.api.ts`                   |
| `useAllDispatchers`             | GET `contractor/…/dispatchers`   | `api/dispatchers/dispatchers.api.ts`           |
| `useCreateDispatcher`           | POST диспетчер                  | `api/dispatchers/dispatchers.api.ts`           |
| `useEditDispatcher`             | PUT диспетчер                    | `api/dispatchers/dispatchers.api.ts`           |
| `useDeleteDispatcher`           | DELETE диспетчер                 | `api/dispatchers/dispatchers.api.ts`           |
| `useResetUserPass`              | сброс пароля пользователя       | `api/user/user.api.ts`                         |
| `useProfile`                    | профиль (contractorId/autoparkId)| `api/profile/profile.api.ts`                   |
| `useSelfAutopark`               | флаг `isInternal` (собственный)  | `api/contractors/contractors.api.ts`           |

⚠️ Когда `useSelfAutopark().data.isInternal === true`, операции создания,
редактирования и удаления скрываются/отключаются (`onEdit`/`onDelete`/`onAddClick`
передаются как `undefined`).

## i18n

Используются блоки из `src/i18n/ru/index.ts`:
`t.Drivers.*`, `t.DispatcherForm.*`, `t.Registry.Modal.*`, `t.Registry.Labels.*`,
`t.global.*` и `t.Requests.List.searchPanel`.

| Ключ                        | ru                                  | en |
|-----------------------------|-------------------------------------|----|
| `t.Drivers.Labels.rating`   | «Рейтинг»                           | —  |
| `t.Drivers.resetDriverPass` | «Сбросить пароль водителя? …»       | —  |
| `t.DispatcherForm.resetPass`| «Сбросить пароль диспетчера? …»     | —  |
| `t.Registry.Modal.add`      | «Добавить»                          | —  |
| `t.Registry.Labels.phone`   | «Номер телефона»                    | —  |

Блок `en` отсутствует — перевод только на русском.

## Роутинг

- Маршрут верхнего уровня: `routes.STAFF` (`/platform/staff`).
- Внутренние подмаршруты: `/platform/staff/:role`, где `role ∈ {drivers, dispatchers}`
  (`StaffRoles` из `constants/app.constants.ts`).
- Редирект: `<Redirect path={STAFF} to={`${STAFF}/${StaffRoles.Drivers}`} />`
  — при заходе на `/staff` открывается вкладка «Водители».

## Логика вкладок

`Staff.tsx` использует `useParams<{role: StaffRoles}>`, `useRouteMatch` и
`useHistory().replace`. Переключение вкладки `Tabs` обновляет URL
`path.replace(':role', value)`. Каждая вкладка загружается `lazy(() => import('./Drivers'))`
и `lazy(() => import('./Dispatchers'))`, оборачивается в `ErrorBoundary` +
`Suspense fallback={<SpinWrapped />}`.

## Стили

- `modules/Staff/styles.module.scss` — `.container`, `.staffTabs`; используются
  CSS-токены `var(--base-input-border-radius)`.
- Подмодули имеют собственные `*.module.scss`: `ModalFormDriver.module.scss`,
  `ModalFormDispatcher.module.scss`, `{DriverList,DispatcherList}/formJSX/formJSX.module.scss`.
- Стили подключаются через CSS-modules (`import styles from './….module.scss'`).

## Тесты

Тесты отсутствуют, рекомендуется добавить покрытие для:
- `StaffRouter` (редирект `/staff` → `/staff/drivers`, рендер по `:role`);
- логики контекстов `ActiveDriver`/`ActiveDispatcher`/`ModalForm`;
- трансформаций из `Drivers/utils/index.ts`
  (`getDriverData`, `getDriverActivity`, `getDriverLicenses`, `getCurrentErrors`).

Запуск: `yarn test src/modules/Staff`.

## Как расширять

1. Новый под-раздел персонала → копия паттерна `Drivers/` под новой ролью
   (`StaffRoles` + вкладка в `Staff.tsx`).
2. Новое поле формы → править `ModalFormDriver.tsx` / `ModalFormDispatcher.tsx`
   (+ ключи в i18n-блоках).
3. Новый API-вызов → `src/api/<…>/<…>.api.ts`, подключение через `useAPI`/`useAPIMutation`.
4. Новая роль в роутинге → `StaffRouter.tsx` + `StaffRoles` в `app.constants.ts`.