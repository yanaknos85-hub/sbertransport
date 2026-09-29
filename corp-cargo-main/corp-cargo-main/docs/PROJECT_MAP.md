# Карта проекта Cargo

> **Цель:** за <1 секунду найти «куда смотреть» по любому доменному слову проекта.
> **Дата:** 7 августа 2026 г. **Размер проекта:** 40 модулей, 57 API-сервисов, 58 shared-компонентов, 80+ утилит, ~57 доменных моделей в `src/stores/` (в основном io-ts DTO) и 3 настоящих MobX-стора.

---

## 📑 Указатель

- [Модули (`src/modules/`)](#-модули-srcmodules)
- [Сторы (`src/stores/`)](#-сторы-srcstores)
- [API (`src/api/`)](#-api-srcapi)
- [Shared-компоненты (`src/shared/components/`)](#-shared-компоненты-srcsharedcomponents)
- [Shared-хуки (`src/shared/hooks/`)](#-shared-хуки-srcsharedhooks)
- [Утилиты (`src/utils/`)](#-утилиты-srcutils)
- [Глобальные константы (`src/constants/`)](#-глобальные-константы-srcconstants)

---

## 📦 Модули (`src/modules/`)

> Бизнес-модули. Большинство имеют свой `Router`, `types.ts`, `constants.ts`, `context/`, `Components/`.

| Модуль | Назначение | Роут (префикс) | Архитектурный файл |
|---|---|---|---|
| **Approvals** | Согласования поездок | `TRIP_SETTINGS/approvals` | — |
| **BusinessReports** | Бизнес-отчёты (грузовые) | `BUSINESS_REPORTS_CARGO` | — |
| **CargoAuto** | Реестр автомобилей для грузов | `AUTO_DIRECTORY` | — |
| **CargoBusinessReports** | Бизнес-отчёты по грузам | — | — |
| **CargoCompensationsRegistry** | Реестр компенсаций (грузы) | `REGISTRY_CARGO_COMPENSATIONS` | — |
| **CargoCompensationsReportsRegistry** | Отчёты по компенсациям | `REGISTRY_CARGO_COMPENSATIONS_REPORTS` | — |
| **CargoPackage** | Настройки упаковки грузов | — | — |
| **CargoRegistry** | **Реестр грузов** (поиск, фильтрация, отчёты) | `REGISTRY_CARGO`, `REGISTRY_CARGO_VIEW` | `AGENTS.md` (1033 строки) |
| **CargoRoutesRegistry** | Реестр маршрутов грузов | `REGISTRY_CARGO_ROUTES` | — |
| **CargoType** | Типы грузов | — | — |
| **CarSharingRegistry** | Реестр каршеринга | — | — |
| **Contractors** | Контрагенты (юрлица-партнёры) | `CONTRACTORS` | — |
| **Customers** | Клиенты | (внутренний) | — |
| **DeadlineSettings** | Настройки дедлайнов | `DEADLINES` | — |
| **Departments** | Подразделения | `DEPARTMENTS`, `DEPARTMENT/:id` | — |
| **Employees** | Сотрудники | `EMPLOYEES`, `EMPLOYEE/:id` | — |
| **Engineers** | Инженеры | `ENGINEERS` | — |
| **ExecutorGroup** | Исполнительные группы | `EXECUTOR_GROUPS` | — |
| **Geo** | Гео-зоны | (внутренний) | — |
| **Home** | Главная страница | `HOME`, `PAGE` | — |
| **ImportReport** | Импорт отчётов | — | — |
| **Locations** | Местоположения | `LOCATIONS`, `LOCATION/:id` | — |
| **NewTariffs** | Новые тарифы | — | — |
| **OrderExecution** | Исполнение заявок | `ORDER_EXECUTION`, `ORDER_EXECUTION_CARGO` | — |
| **Organizations** | Организации (multi-tenancy) | `ORGANIZATIONS`, `ORGANIZATION/:id` | — |
| **Page404** | 404-я страница | `PAGE_404` | — |
| **PersonalRegistry** | Личный реестр | `PERSONAL_REGISTRY` | — |
| **Planner** | **Планировщик маршрутов + Журнал + Подписание ЭТрН** (самый сложный) | `MULTI_LOGISTICS`, `PLANNER`, `PLANNER_*` | `Planner.architecture.md` (873 строки), `AGENTS.md` (248 строк) |
| **Positions** | Должности | `EMPLOYEE_POSITIONS` | — |
| **PublicRegistry** | Публичный реестр | `PUBLIC_REGISTRY` | — |
| **Registry** | **Общий реестр** (поиск) | `REGISTRY` | `AGENTS.md` (1139 строк) |
| **Roles** | Роли и разрешения | `ROLES`, `ROLE/:id` | — |
| **ServiceMetrics** | Метрики сервисов | — | — |
| **ServiceSettings** | Настройки сервисов | `SERVICE_SETTINGS/*` | — |
| **SharedRides** | Совместные поездки | `SHARED_RIDES` | — |
| **TariffSettings** | Настройки тарифов | `TARIFF_SETTINGS/tariffs` | — |
| **TaxiRegistry** | Реестр такси | — | — |
| **TripDetailed** | Детализация поездки | — | — |
| **TripPurposes** | Цели поездок | `PURPOSES` | — |
| **TripSettings** | Настройки поездок | `TRIP_SETTINGS/*` | — |
| **UploadButton** | Кнопка загрузки файлов | — | — |
| **WorkingGroups** | Рабочие группы | — | — |

**Где искать конкретную фичу:**
- Грузоперевозки и маршруты → `Planner`
- ЭТрН → `Planner/Components/EtrnSignature`
- Поиск/реестр → `CargoRegistry`, `Registry`, `PersonalRegistry`, `PublicRegistry`, `TaxiRegistry`
- Справочники (сотрудники, оргструктура) → `Employees`, `Departments`, `Organizations`, `Roles`, `Positions`
- Настройки → `TariffSettings`, `TripSettings`, `ServiceSettings`, `DeadlineSettings`, `SharedRides`, `NewTariffs`
- Аналитика и отчёты → `BusinessReports`, `ServiceMetrics`, `Registry`

---

## 🗄 Сторы (`src/stores/`)

> MobX-сторы (классы с `@observable`/`@action`/`@computed`). Доступ через `useAppStoreContext().<store>` (для DI-сторов) или прямой импорт.

### DI-сторы (кастомные, через Inversify)

Зарегистрированы в `src/ioc/ioc.stores.ts`:

| Store | Файл | Ответственность |
|---|---|---|
| **`cargoStore`** | `modules/OrderExecution/stores/Cargo/DICargoStore.store.ts` | Хранилище грузов в текущей сессии |
| **`plannerStore`** | `stores/Planner/DIPlanner.store.ts` | Планировщик: маршруты, заявки, drag-and-drop, журнал |

> **Примечание:** интерфейс `IAppStore` (тип) сейчас содержит только эти два стора, но из `@sber-sbertransport/mf-core` через `IRootStore` расширяется `configStore`, `selfStore`, `authStore`, `settingsStore`, `employeeStore`, `employeeExtStore`, `delegatesStore`.

### Доменные модели (io-ts DTO) в `src/stores/`

> ⚠️ **Важно:** большинство папок в `src/stores/<Domain>/` — это **io-ts DTO-модели** (`*.interface.ts`: типы и кодеки данных), а **не** MobX-сторы. Настоящих MobX-сторов в проекте всего **3**: `src/stores/Planner/DIPlanner.store.ts` (`plannerStore`), `src/modules/OrderExecution/stores/Cargo/DICargoStore.store.ts` (`cargoStore`), `src/modules/CargoBusinessReports/Filter.store.ts` (`FilterStore`, не используется). В IoC зарегистрированы `plannerStore` и `cargoStore`.

Расположены в `src/stores/<Domain>/` (в основном `*.interface.ts`):

| Домен | Файл | Назначение |
|---|---|---|
| `AddressTypes` | — | Типы адресов |
| `Analysis` | — | Аналитические данные |
| `ApprovalSettings` | — | Настройки согласований |
| `Auth` | — | Авторизация |
| `BusinessReports` | — | Бизнес-отчёты |
| `CargoAuto` | — | Реестр автомобилей |
| `CargoCategoryName` | — | Категории грузов |
| `CargoDeliveryTimeSettings` | — | Настройки времени доставки |
| `CargoPackage` | — | Упаковка |
| `CargoRegistry` | — | Реестр грузов |
| `CargoType` / `CargoTypeName` | — | Типы грузов |
| `CarSharingTrip` | — | Поездки каршеринга |
| `CompensationRegistry` / `Compensations` | — | Компенсации |
| `ContractorDispatchers` / `Contractors` / `Contracts` | — | Контрагенты и договоры |
| `Corporate` | — | Корпоративные данные |
| `DateTypes` | — | Типы дат |
| `DeadlineSettings` | — | Дедлайны |
| `Delegates` | — | Делегаты |
| `Department` | — | Подразделения |
| `Employee` / `EmployeesAttribute` | — | Сотрудники |
| `Engineer` | — | Инженеры |
| `FleetManagment` | — | Управление автопарком |
| `Geo` / `GeoZones` | — | Гео-данные |
| `Limits` | — | Лимиты |
| `Locations` | — | Местоположения |
| `Notifications` | — | Уведомления |
| `Organizations` / `OrganizationsGroup` | — | Организации |
| `Pagination` | — | Общая пагинация |
| `PersonalSearch` | — | Поиск по личному реестру |
| `Position` | — | Должности |
| `PublicRegistry` | — | Публичный реестр |
| `Registry` | — | Общий реестр |
| `RegistryTaxi` | — | Реестр такси |
| `Roles` | — | Роли |
| `ServiceTypes` | — | Типы сервисов |
| `SharedRide` | — | Совместные поездки |
| `StatusTypes` | — | Типы статусов |
| `Tariffs` | — | Тарифы |
| `Tasks` | — | Задачи |
| `TransportServiceTypes` / `TransportTypes` / `TransportTypesDelegates` | — | Типы транспорта |
| `Trip` | — | Поездки |
| `TripPurposes` | — | Цели поездок |
| `UserAgent` | — | User Agent (для API) |

**Глобальные файлы:**
- `src/stores/index.ts` — переэкспорт `useAppStoreContext`, `TYPES`, `initAppStore`, `AppStoreContext`, `useAppStore`, `StoreNames`, `hooks`
- `src/stores/SettingsContext.tsx` — контекст настроек
- `src/stores/StoreNames.enum.ts` — `enum StoreNames` (используется как ключ в `IAppStore`)

Подробнее об устройстве MobX — `docs/STATE_MANAGEMENT.md`.

---

## 🌐 API (`src/api/`)

> HTTP-сервисы (axios + react-query v2). Хелперы `useAPI`/`useAPIMutation` экспортируются из `src/api/index.ts`.

### Простые сервисы (один файл)

| Файл | Эндпоинты (примерно) |
|---|---|
| `approval-settings.ts` | Согласования |
| `business-reports.ts` | Бизнес-отчёты |
| `car-sharing-report.ts` | Отчёты каршеринга |
| `cargo-auto.ts` | Реестр автомобилей |
| `cargo-category-names.ts` | Категории грузов |
| `cargo-delivery-time-settings.ts` | Время доставки |
| `cargo-package-settings.ts` | Упаковка |
| `cargo-registry-compensations-search.ts` | Поиск компенсаций |
| `cargo-registry-route-search.ts` | Поиск маршрутов |
| `cargo-registry-search.ts` | Поиск грузов |
| `cargo-type-names.ts` / `cargo-types.ts` | Типы грузов |
| `compensations.ts` | Компенсации |
| `contracts.ts` | Договоры |
| `deadline-settings.ts` | Дедлайны |
| `delegates.ts` | Делегаты |
| `employee-attributes.ts` | Атрибуты сотрудников |
| `executor-group.ts` | Исполнительные группы |
| `geo-zones.ts` / `geo.ts` | Гео-зоны |
| `locations.ts` | Местоположения |
| `notifications.ts` | Уведомления |
| `order-execution.ts` | Исполнение заявок |
| `personal-search.ts` | Поиск (личный реестр) |
| **`planner.ts`** | **Planner: ~20 эндпоинтов (routes, orders, monitor, contractors, journal)** |
| `positions.ts` | Должности |
| `profile.ts` | Профиль пользователя |
| `public-register-search.ts` | Поиск (публичный реестр) |
| `purposes.ts` | Цели |
| `register-search.ts` | Поиск (общий реестр) |
| `registry.ts` | Общий реестр |
| `reports.ts` | Отчёты |
| `reset-pass.ts` | Сброс пароля |
| `roles.ts` | Роли |
| `service-types.ts` | Типы сервисов |
| `shared-ride-settings.ts` / `shared-rides.ts` | Совместные поездки |
| `statusCancellationCodes.ts` | Коды отмены |
| `tariffs-cargo.ts` / `tariffs.ts` | Тарифы |
| `transport-types.ts` | Типы транспорта |
| `travel-status.ts` | Статусы поездки |
| `trip-detailed-view.ts` | Детализация поездки |
| `upload.ts` | Загрузка файлов |
| `user-agent.ts` | User Agent |

### Сложные сервисы (папка: `.api.ts` + `.types.ts` + `.constants.ts`)

| Сервис | Файл | Эндпоинты |
|---|---|---|
| `contractors/` | `.api.ts` | Контрагенты (расширенный API) |
| `departments/` | `.api.ts` | Подразделения (расширенный) |
| `employee/` | `.api.ts` | Сотрудники (расширенный) |
| `engineer/` | `.api.ts` | Инженеры |
| **`etrn-signature/`** | `.api.ts`, `.constants.ts` | **ЭТрН: lock/unlock, card, search** (см. `openspec/specs/etrn-signing/spec.md`) |
| `geo/` | `.api.ts` | Гео (расширенный) |
| `limits/` | `.api.ts` | Лимиты |
| `organizations/` | `.api.ts` | Организации |

### Глобальные файлы

- `src/api/index.ts` — `useAPI`, `useAPIMutation`, `useAPIQueryCache`, `updateQueryCache`, `mapQuery`, `APIQueryResult`, `APIMutationConfig`. **Это критический файл — в нём определена обёртка над react-query.**
- `src/api/type-helpers.ts` — утилита `Prefixes<>` для типизации ключей кэша.
- `src/api/__tests__/` — тесты API-сервисов.

Подробнее — `docs/API_LAYER.md`.

---

## 🧩 Shared-компоненты (`src/shared/components/`)

> Переиспользуемые компоненты. Стилизация — `*.module.scss` или `*.styles.ts`.

| Категория | Компоненты |
|---|---|
| **Доступ** | `AccessControl` |
| **Адреса** | `AddressAutoComplete`, `SelectAddressType` |
| **Кнопки** | `Button`, `ButtonLoad`, `ButtonLoadForVsp`, `DownloadButton`, `DownloadButtonVsp`, `IconButton`, `TableEditButtons` |
| **Хлебные крошки** | `Breadcrumbs` (с `CustomRoutes` для роутера) |
| **Дата/время** | `DateInput`, `DateInputWithAvailableFuture`, `DatePicker` |
| **Ошибки** | `ErrorBoundary` |
| **Пусто** | `EmptyView` |
| **Фильтры** | `FilterPanel`, `FilterPanelBusinessReports`, `AnalyticsFilter` |
| **Формы** | `FormItem`, `Field`, `FormField`, `HelperTooltip`, `RestrictedInput`, `Inputs`, `PhoneMask`, `NumericRange` |
| **Иконки** | `Icon` (обёртка над SVG) |
| **Карты** | `Map` |
| **Модалки** | `Modal` (базовый контейнер с шапкой/телом/футером) |
| **Экспорт** | `ModalsExportXLSBusinessReports` |
| **Оргструктура** | `Organization`, `SelectOrganization`, `SelectDepartment`, `SelectDepartments`, `SelectExecutorGroup`, `SelectContractor` |
| **Страницы** | `PageContent`, `PageLayout` |
| **Пагинация** | `Pagination`, `PaginationWithPageSelect` |
| **Селекты** | `Select`, `SelectStatus`, `SelectRole`, `SelectTariff`, `SelectTransportType`, `SelectDateTimeType`, `SelectFlag` |
| **Загрузка** | `SpinWrapped` |
| **Степперы** | `Stepper`, `Steps` |
| **Таблицы** | `Table`, `TableEditButtons`, `Tables`, `TableShadow`, `TableStyled`, `EditableTable` |
| **Вкладки** | `Tab`, `Tabs` |
| **Текст** | `TextField`, `ColoredSlider` |

**Глобальные файлы:**
- `src/shared/columnsPropsFactory.ts` — фабрика колонок для antd `Table`
- `src/shared/fieldValidationRules.ts` — правила валидации для `Form.Item`

---

## 🪝 Shared-хуки (`src/shared/hooks/`)

| Хук | Назначение |
|---|---|
| `useAppStoreContext` | Доступ к `IAppStore` (MobX-сторы через DI) |
| `useClientType` | Тип клиента (мобильный/десктоп) |
| `useDebounce` | Дебаунс значений |
| `useEmpContext` | Контекст сотрудника |
| `useGeoPosition` | Геопозиция |
| `useImageUrl` | URL картинки (с подписью) |
| `useMapType` | Тип карты |
| `useModal` | Управление видимостью модалки |
| `usePagination` | Универсальная пагинация |
| `useQuery` | Обёртка над react-query |
| `useSelectablePublicCompensations` | Выбор публичных компенсаций |
| `useStep` | Текущий шаг в степпере |
| `useTableConfig` | Конфигурация таблицы |
| `useUrl` | Параметры URL |

---

## 🛠 Утилиты (`src/utils/`)

> 80+ утилит. Именование: `formatX.ts`, `getX.ts`, `buildX.ts`, `parseX.ts`, `convertX.ts`.

**Глобальные:**
- `index.js` — реэкспорт всего
- `Misc.ts`, `Types.ts`, `uid.ts`, `uuid.ts`
- `isTestMode.ts`, `isTestStand.ts`
- `createCallableContext.tsx` — **критический хелпер для UI-контекста модулей** (см. `docs/PATTERNS.md`)
- `ioTypeFromEnum.ts` — конвертация TS-enum → io-ts Type
- `useRole.ts`, `useTitle.ts` — хуки для ролей и заголовка страницы
- `treeUtils.ts` — работа с деревьями
- `reportsUtils.tsx` — утилиты для отчётов

**Форматы:**
- `formatAddress.ts`, `formatPhoneNumber.ts`, `formatPassengerName.ts`, `formatName.ts`, `formatDriverRating.ts`
- `formatRubles.ts`, `serializeRubles.ts`, `convertToRubles.ts`
- `formatTime.ts`, `formatDistance.ts`, `formatVolume.ts`
- `formatFilterRequest.ts` — формирование query-параметров фильтра

**Дата:**
- `calendar.ts`, `datetime.ts`, `transformDate.ts`

**IO:**
- `arrayHelpers.ts`, `indexById.ts`, `sorting.ts`, `searchSymbol.ts`
- `between.ts`, `getErrorCode.ts`, `isJSONString.ts`
- `purposesOptions.ts`, `textTransform.ts`
- `setIntoUrl.ts`, `removeIdFromUrl.ts`, `keygen.ts`
- `downloads.ts`, `throwAxiosErrorMessage.ts`, `getAddressLetter.ts`, `getRoundedParams.ts`, `getMaskPhone.ts`, `buildAddressString.ts`
- `declOfNum/` — склонение числительных

**io-ts:**
- `io-ts/` — кастомные типы (UUID и др.)

---

## 🌐 Глобальные константы (`src/constants/`)

| Файл | Содержание |
|---|---|
| `constants.app.ts` | Enum-ы (`EmployeeStatus`, `TripRequestStatuses` — 450+ строк), форматы дат |
| `constants.api.ts` | **Все эндпоинты** как константы (`ETRN_CARGO_LIST`, `PLANNER_*`) |
| `constants.env.ts` | `APP_NAME`, `IS_DEV`, переменные окружения |
| `constants.geo.ts` | Константы карт (центры, зум) |
| `constants.routes.ts` | **Все маршруты** как константы (`PLANNER`, `CONTRACTORS`, `MULTI_LOGISTICS`) |

> ⚠️ Эти файлы исключены из coverage (см. `docs/PITFALLS.md`). Тесты на них не пишутся.

---

## 🗺 Где искать что — шпаргалка

| Задача | Куда смотреть |
|---|---|
| Изменить текст/формат поля | `src/utils/formatX.ts` или `src/shared/components/Field/Field` |
| Добавить новый список (таблицу) | `src/shared/components/Table/` + `useTableConfig` |
| Добавить новый фильтр | `src/shared/components/FilterPanel/` |
| Добавить новую модалку | `src/shared/components/Modal/` (Container) |
| Добавить новый роут | `src/constants/constants.routes.ts` → `src/app/prod/AppRouter.tsx` |
| Изменить статус | `src/constants/constants.app.ts` или локальный `constants.ts` модуля |
| Добавить новый API-эндпоинт | Сложный: `src/api/<X>/<X>.{api,types,constants}.ts`. Простой: `src/api/<X>.ts` |
| Изменить DI-стор | `src/ioc/ioc.stores.ts` + `src/ioc/ioc.types.ts` |
| Изменить UI-контекст модуля | `src/modules/<X>/context/<X>Context.tsx` |
| Найти «где живёт формат даты» | `src/constants/constants.app.ts: DATE_FORMAT` |
| Найти «где живёт ЭТрН» | `src/modules/Planner/Components/EtrnSignature/` + `openspec/specs/etrn-signing/spec.md` |
| Найти «где живёт drag-and-drop» | `src/modules/Planner/Components/Planner/Planner.tsx` (нативный HTML5) |
| Найти «где живёт карта» | `src/shared/components/Map/` (OpenLayers/Leaflet) |

---

> **Версия карты:** 1.0 (7 августа 2026 г.)
> **Источник:** `find src/ -type d` + `ls src/modules/`, `src/stores/`, `src/api/`, `src/shared/components/`.
> **Что НЕ включено:** внутренние файлы модулей (`*.test.tsx`, `*.styles.ts`) — для них есть `PROJECT_MAP.md` уровня модуля (см. `Planner.architecture.md` как образец).
