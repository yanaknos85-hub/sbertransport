# Словарь предметной области Cargo

> **Глоссарий терминов** для AI-агентов, работающих с проектом.
> Домен: **грузовые перевозки + электронный документооборот (ЭТрН) + оргструктура**.
> **Дата:** 7 августа 2026 г.

---

## 📑 Указатель

- [Грузоперевозки](#-грузоперевозки)
- [ЭТрН (электронная транспортная накладная)](#-этрн-электронная-транспортная-накладная)
- [Оргструктура и пользователи](#-оргструктура-и-пользователи)
- [Картография](#-картография)
- [UI-термины](#-ui-термины)

---

## 🚚 Грузоперевозки

### Груз (Cargo)

**Что это:** Единица перевозки с параметрами (вес, объём, описание, габариты).

**Поля:**
- `id` — UUID груза
- `description` — описание («Оборудование, 5 мест»)
- `places` — количество мест (грузовых единиц)
- `weightKg` — вес в килограммах
- `volume` — объём в м³
- `length`, `width`, `height` — габариты (опционально)

**Где в коде:**
- API: `src/api/cargo-*.ts`, `src/api/planner.ts`
- Сторы: `src/stores/CargoRegistry/`, `src/modules/OrderExecution/stores/Cargo/`
- Модуль: `CargoRegistry`, `CargoType`

---

### Маршрут (Route)

**Что это:** Упорядоченная последовательность точек (погрузка/разгрузка) с привязанными грузами. Создаётся в Планировщике.

**Поля:**
- `id` — UUID маршрута
- `humanReadableId` — человекочитаемый ID (`CT-0001-0002931`)
- `applicationNumber` — номер заявки (`OT-0001-0002931`)
- `routeNumber` — номер маршрута
- `status` — статус маршрута (см. [Статусы маршрута](#статусы-маршрута))
- `createdAt` — дата создания
- `points` — массив точек маршрута (погрузка/разгрузка)
- `orders` — массив привязанных заявок

**Где в коде:**
- API: `src/api/planner.ts` (PLANNER_*)
- Стор: `src/stores/Planner/DIPlanner.store.ts`
- Модуль: `src/modules/Planner/Components/Routes/`

---

### Заявка (Order)

**Что это:** Запрос на перевозку груза по определённому маршруту.

**Поля:**
- `id` — UUID заявки
- `humanReadableId` — человекочитаемый ID
- `status` — статус заявки
- `cargo` — параметры груза
- `desiredDateRange` — желаемый диапазон дат
- `senderAddress` / `receiverAddress` — адреса

**Где в коде:**
- API: `src/api/planner.ts`, `src/api/cargo-registry-search.ts`
- Модуль: `src/modules/Planner/Components/Orders/`

---

### Точка маршрута (Waypoint / Point)

**Что это:** Адрес на маршруте с типом (погрузка/разгрузка/транзит).

**Типы:**
- `SOURCE` — точка погрузки
- `TARGET` — точка разгрузки
- `TRANSIT` — транзитная точка

**Где в коде:**
- Тип: `src/types/waypoint.ts`

---

### Статусы маршрута

Из `openspec/specs/etrn-signing/spec.md` и `src/modules/Planner/Components/Monitor/constants.ts`:

| Статус | Описание |
|---|---|
| `CREATED` | Маршрут создан |
| `ASSIGNED` | Назначен водитель |
| `IN_PROGRESS` | В процессе выполнения |
| `FINISHED` | Завершён |
| `CANCELED` | Отменён |
| `CARGO_DELIVERY_CONFIRMATION_FINISHED` | Доставка подтверждена |
| `READY_TO_SIGN` | Готов к подписанию ЭТрН |

**Где:** `src/modules/Planner/Components/Monitor/constants.ts: Statuses`

---

### Компенсация (Compensation)

**Что это:** Выплата за грузоперевозку (для самозанятых/ИП).

**Модули:** `Compensations`, `CompensationRegistry`, `CargoCompensationsRegistry`

---

### Статусы заявки (`TripRequestStatuses`)

Из `src/constants/constants.app.ts`:

```ts
export enum TripRequestStatuses {
  DRAFT = 'DRAFT',                       // Черновик
  REGISTERED = 'REGISTERED',             // Зарегистрирована
  AWAITING_APPROVAL = 'AWAITING_APPROVAL', // На согласовании
  APPROVED = 'APPROVED',                 // Согласована
  DECLINED = 'DECLINED',                 // Отклонена
  AWAITING_SEARCH = 'AWAITING_SEARCH',   // Ожидает поиска водителя
  DRIVER_SEARCH = 'DRIVER_SEARCH',       // Поиск водителя
  DRIVER_FOUND = 'DRIVER_FOUND',         // Водитель найден
  DRIVER_ARRIVED = 'DRIVER_ARRIVED',     // Водитель прибыл
  TRIP_IN_PROGRESS = 'TRIP_IN_PROGRESS', // Поездка в процессе
  TRIP_FINISHED = 'TRIP_FINISHED',       // Поездка завершена
  CANCELLED = 'CANCELLED',               // Отменена
  PAYMENT_AWAITING = 'PAYMENT_AWAITING', // Ожидает оплаты
}
```

---

## 📄 ЭТрН (электронная транспортная накладная)

**Etrn** (Электронная транспортная накладная) — замена бумажной ТТН в электронном виде. Подписывается УКЭП (усиленная квалифицированная электронная подпись).

### Титулы (T1–T4)

> **Важно:** сравнивать через `enum TitleType` (см. `docs/PATTERNS.md`), не через магические строки.

| Титул | Кто подписывает | Когда |
|---|---|---|
| **T1** | Грузоотправитель | Перед погрузкой |
| **T2** | Перевозчик (водитель) | В начале поездки |
| **T3** | Грузополучатель | При приёмке |
| **T4** | Перевозчик (приёмка) | После доставки |

**Где в коде:**
- Определение enum: `src/modules/Planner/Components/EtrnSignature/constants.ts: TitleType`
- API: `src/api/etrn-signature/`
- Тип DTO: `t.union([t.null, t.string])` (значение может быть `null`)
- UI-сравнение: `card?.currentTitle === TitleType.T2`

**Спека:** `openspec/specs/etrn-signing/spec.md`

---

### Статусы ЭТрН

Из `src/modules/Planner/Components/EtrnSignature/constants.ts: EtrnStatus`:

| Статус | Описание |
|---|---|
| `IDENTIFIED` | ЭТрН создана |
| `WAIT_KORUS_DATA` | Ожидание данных из Korus |
| `READY_TO_SIGN` | Готова к подписанию |
| `SIGNED` | Подписана |
| `REJECTED` | Отклонена |
| `EXPIRED` | Истёк срок |

---

### Lock-паттерн (блокировка карточки)

**Зачем:** чтобы два пользователя не подписали ЭТрН одновременно.

**Как работает:**
1. При открытии карточки → `PUT /etrn-cargo/{cardId}/lock` (5 минут)
2. При закрытии → `DELETE /etrn-cargo/{cardId}/lock`
3. Через 5 минут бездействия — блокировка автоматически снимается на бэкенде
4. При конфликте (409) — карточка открывается в read-only режиме

**Хуки:** `useAcquireLock`, `useReleaseLock` (из `src/api/etrn-signature/`)

**Где в коде:**
- API: `src/api/etrn-signature/etrn-signature.ts`
- UI: `src/modules/Planner/Components/EtrnSignature/`

**Спека:** `openspec/specs/etrn-signing/spec.md` → раздел «Card Lock»

---

### УКЭП / КриптоПро

**Что это:** Усиленная квалифицированная электронная подпись через КриптоПро CSP (плагин `crypto-pro-actual-cades-plugin`).

**В проекте (реализовано по состоянию на 2026-08):**
- Интерфейс: `src/modules/Planner/Components/EtrnSignature/cryptoPro.interface.ts` — `CertificateExtended extends Certificate` (поле `active: boolean`).
- Хук инициализации CSP: `src/modules/Planner/Components/EtrnSignature/hooks/useCryptoPro.ts` — получает системную информацию (`getSystemInfo`) и активные сертификаты (`getCertificates`); состояния `loading | ready | error` через enum `Messages` (`NoCspProvider`, `NoCadesPlugin`, `NoCertificates`, `GetCertificatesError`, `GetSystemInfoError`, `SigningError`).
- Модалка подписания: `src/modules/Planner/Components/EtrnSignature/components/Sign/SignT3Modal.tsx` — шаги `init → выбор сертификата → подпись → отправка`. Экраны: `Loading/`, `Error/`, `Certificates/`.
- Кнопка «Подписать УКЭП» отображается в `EtrnModal` только при `currentTitle === TitleType.T2 && lockStatus === 'active'`. Клик открывает `SignT3Modal`.
- Мутация отправки подписанного Т3: `useSendEtrn` в `src/api/etrn-signature/etrn-signature.ts` — `POST /etrn-cargo/:etrnId/title/send` с телом `{ fileName, file, signature, creationTime }`. `file` — исходный base64 из GET-ответа (не декодированный). Реализована в TRANSPORT-45412 (Часть 2).
- На успехе `EtrnModal` закрывается, `releaseLock` (в `onSettled`), `onSigned` → `refetch` списка + `message.success`.

**Алгоритм подписания:**
1. Инициализация CSP и получение активных сертификатов (`useCryptoPro`).
2. Пользователь выбирает сертификат из списка (`thumbprint`).
3. `signFileByCertificate(thumbprint, requestKey)` → возвращает подпись (`string`).
4. `sendEtrn({ cardId, titleType: TitleType.T3, lockToken, requestKey, signature })` → backend (пока заглушка).

**Подробнее:** `openspec/specs/etrn-signing/spec.md` → «T3 UK Sign», `Planner.architecture.md` → «КриптоПро (УКЭП)».

---

### Цепочка подписания (Title Chain)

**Что это:** Массив из 4 объектов (T1, T2, T3, T4) с информацией о подписании каждого титула:

```ts
{
  title: 'T1',
  signedAt: '2026-07-21T10:00:00',
  signedBy: 'uuid-сотрудника',
}
```

**Где в коде:** `EtrnCardDto.titleChain`

**UI:** Блок «Цепочка титулов» в `EtrnModal` (компонент `TitleChain`)

---

### MRPА (место разгрузки/погрузки адреса)

**Что это:** Адрес места погрузки/разгрузки в ЭТрН.

**Поля:**
- `address` — строковый адрес
- `mrpaExpiresAt` — срок действия

---

## 🏢 Оргструктура и пользователи

### Организация (Organization)

**Что это:** Корневая сущность для всех данных (multi-tenancy). Все данные привязаны к организации через `organizationId`.

**Модули:** `Organizations`, `OrganizationsGroup`

---

### Подразделение (Department)

**Что это:** Структурная единица организации (отдел, департамент, филиал).

**Модули:** `Departments`

**Статусы:** `ACTIVE`, `INACTIVE`

---

### Исполнительная группа (Executor Group)

**Что это:** Группа сотрудников, ответственных за обработку определённого типа заявок.

**Модули:** `ExecutorGroup`, `WorkingGroups`

**Где используется:** фильтры в Planner (заявки фильтруются по `executorGroupIds`)

---

### Контрагент (Contractor)

**Что это:** Юрлицо-партнёр (грузоотправитель, грузополучатель, перевозчик).

**Модули:** `Contractors`, `Contracts`

---

### Сотрудник (Employee)

**Что это:** Пользователь системы.

**Поля:**
- `id` — UUID
- `fullName` — ФИО
- `email`, `phone` — контакты
- `status` — `ACTIVE` / `INACTIVE`
- `gender` — `MALE` / `FEMALE`
- `positionIds` — должности
- `departmentIds` — подразделения

**Модули:** `Employees`

**Статусы:** `EmployeeStatus.ACTIVE`, `EmployeeStatus.INACTIVE`

---

### Должность (Position)

**Модули:** `Positions`

---

### Роль (Role)

**Что это:** Набор разрешений для сотрудника.

**Модули:** `Roles`

**Связь:** Сотрудник → Роли → Разрешения (Permissions)

---

### Делегат (Delegate)

**Что это:** Сотрудник, который временно может действовать от имени другого сотрудника.

**Модули:** `Delegates`

---

## 🗺 Картография

### OpenLayers (`ol` + `rlayers`)

**Где используется:** Planner (карта маршрутов с маркерами и полилиниями).

**Зачем:** для отображения сложных маршрутов с множеством точек.

**Где в коде:** `src/modules/Planner/Components/Planner/Planner.tsx` (импорт `Map` из `shared/components/Map`)

---

### Leaflet (`leaflet` + `react-leaflet`)

**Где используется:** для простых карт (выбор адреса, отображение точки).

**Где в коде:** `AddressAutoComplete`, `SelectAddressType`

---

### Map2GIS

**Что это:** Альтернативный провайдер карт от 2ГИС.

**Где в коде:** `Map2GISContext`, `use2GIS` в `src/app/prod/AppProvider.tsx`

---

## 🖥 UI-термины

### MRPА (в контексте адреса)

В UI адрес часто называют «MRPА» (место разгрузки/погрузки адреса). См. раздел ЭТрН.

---

### SLA (контрольный срок)

**Что это:** Время, за которое нужно обработать ЭТрН/заявку.

**Где в коде:** `EtrnCardDto.sla: '00:42'` (формат `HH:mm`)

---

### УКЭП / УК-sign (UK Sign)

**Что это:** Усиленная квалифицированная электронная подпись (см. выше).

**В UI:** Кнопка «Подписать УКЭП» в `EtrnModal`.

---

### Хлебные крошки (Breadcrumbs)

**Что это:** Навигационная цепочка в верхней части страницы.

**Где в коде:** `src/shared/components/Breadcrumbs/`, `CustomRoutes.tsx`

---

### Лента (Feed) vs Детальный просмотр (Detailed)

**Лента:** список записей (страница по умолчанию).
**Детальный просмотр:** страница конкретной записи (`/:id`).

В Planner:
- Лента маршрутов: `/multi-logistics/planner`
- Детальный просмотр маршрута: `/multi-logistics/planner/route-detailed/:id`
- Детальный просмотр заявки: `/multi-logistics/planner/order-detailed/:id`

---

### Modals vs Drawers

**Modal (модалка):** всплывающее окно поверх страницы.
**Drawer (сайдбар):** выезжающая панель сбоку.

В проекте используются **преимущественно модалки** через `Container` из `shared/components/Modal/`.

---

### Журнал (Monitor / Journal)

**Что это:** Список завершённых/текущих маршрутов с возможностью мониторинга.

**Где в коде:** `src/modules/Planner/Components/Monitor/`

**Роут:** `MULTI_LOGISTICS/journal`

---

## 🔤 Сокращения и аббревиатуры

| Сокращение | Расшифровка |
|---|---|
| `MF` | Module Federation |
| `IoC` / `DI` | Inversion of Control / Dependency Injection |
| `DnD` | Drag-and-Drop |
| `SPA` | Single Page Application |
| `UI-Kit` | UI-компоненты от `@sber-sbertransport/ui-kit` |
| `ETRN` / `ЭТрН` | Электронная транспортная накладная |
| `УКЭП` | Усиленная квалифицированная электронная подпись |
| `MRPА` | Место разгрузки/погрузки адреса |
| `SLA` | Контрольный срок обработки |
| `DTO` | Data Transfer Object |
| `IoC` | Inversion of Control |
| `CPA` | Contract Price Agreement (договор с контрагентом) |
| `VSP` | ? (используется в `ButtonLoadForVsp`, `DownloadButtonVsp`) |

---

> **Версия:** 1.0 (7 августа 2026 г.)
> **Источник:** `openspec/specs/etrn-signing/spec.md`, `src/modules/Planner/Components/EtrnSignature/constants.ts`, `src/constants/constants.app.ts`, `src/modules/Planner/Planner.architecture.md`.
