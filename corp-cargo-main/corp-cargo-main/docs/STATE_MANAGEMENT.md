# Управление состоянием (MobX)

> **Подробно об устройстве глобального состояния** в проекте.
> **Дата:** 7 августа 2026 г.
> **Библиотека:** MobX `^5.15.4` (классы с декораторами, **НЕ** v6+).

---

## 📑 Указатель

- [Типы сторов](#-типы-сторов)
- [Регистрация в IoC](#-регистрация-в-ioc)
- [Доступ в компонентах](#-доступ-в-компонентах)
- [Каталог всех сторов](#-каталог-всех-сторов)
- [Cross-store коммуникация](#-cross-store-коммуникация)
- [Computed значения](#-computed-значения)
- [Async через runInAction](#-async-через-runinaction)
- [Тестирование сторов](#-тестирование-сторов)

---

## 🎯 Типы сторов

В проекте используются **два типа** сторов:

### 1. DI-сторы (через Inversify)

**Где:**
- Файл: `src/stores/<Domain>/<Domain>.store.ts` (например, `Planner/DIPlanner.store.ts`)
- Регистрация: `src/ioc/ioc.stores.ts`
- Доступ: `useAppStoreContext().<storeName>`

**Особенности:**
- Помечены `@injectable()`
- Получают зависимости через `@inject(TYPES.X)`
- Singleton (один экземпляр на приложение)
- Подходят для **сложной бизнес-логики** с множеством сервисов

**Пример:** `plannerStore`, `cargoStore`

### 2. Прямые сторы (обычные классы)

**Где:**
- Файл: `src/stores/<Domain>/<Domain>.store.ts` (например, `Employee/Employee.store.ts`)
- Импорт напрямую: `import { EmployeeStore } from 'stores/Employee/Employee.store'`

**Особенности:**
- Без `@injectable()` / `@inject()`
- Проще в использовании (без IoC-регистрации)
- Подходят для **доменных данных** (списки сотрудников, фильтры, настройки)

---

## 📝 Регистрация в IoC

### Шаг 1: Определить интерфейс

```ts
// src/stores/Planner/Planner.interface.ts
export interface IPlannerService {
  searchRoutes: (query: RoutesFiltersType) => Promise<RouteResponseType>;
  getRoute: (id: string) => Promise<RouteType>;
}

export interface IPlannerStore {
  // observable
  routeStore: RouteType | undefined;
  routesListStore: RoutesListStoreType;

  // actions
  setRoute: (route: RouteType | undefined) => void;
  fetchRoutes: (filters: RoutesFiltersType) => Promise<void>;
}
```

### Шаг 2: Реализовать стор

```ts
// src/stores/Planner/DIPlanner.store.ts
import { injectable, inject } from 'inversify';
import { action, observable, runInAction } from 'mobx';
import { TYPES } from 'ioc/types';
import type { ILogger } from '@sber-sbertransport/mf-core';
import type { IPlannerService, IPlannerStore } from './Planner.interface';

@injectable()
export class DIPlannerStore implements IPlannerStore {
  @inject(TYPES.IPlannerService)
  private service!: IPlannerService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable routeStore: RouteType | undefined;

  @observable routesListStore: RoutesListStoreType = {
    content: [],
    totalElements: 0,
    totalPages: 0,
  };

  @action
  setRoute(route: RouteType | undefined): void {
    this.routeStore = route;
  }

  @action
  async fetchRoutes(filters: RoutesFiltersType): Promise<void> {
    try {
      const response = await this.service.searchRoutes(filters);
      runInAction(() => {
        this.routesListStore = response;
      });
    } catch (error) {
      this.logger.toNotify('error', 'Не удалось загрузить маршруты', error);
    }
  }
}
```

### Шаг 3: Реализовать сервис

```ts
// src/stores/Planner/DIPlanner.service.ts
import { injectable, inject } from 'inversify';
import type { IHttpService, ILogger } from '@sber-sbertransport/mf-core';
import { TYPES } from 'ioc/types';
import type { IPlannerService } from './Planner.interface';
import { PLANNER_SEARCH_ROUTES_MULTIPLE } from 'constants/constants.api';

@injectable()
export class DIPlannerService implements IPlannerService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  async searchRoutes(query: RoutesFiltersType): Promise<RouteResponseType> {
    const response = await this.http.post(PLANNER_SEARCH_ROUTES_MULTIPLE, query);
    return response.data;
  }

  async getRoute(id: string): Promise<RouteType> {
    const response = await this.http.get(PLANNER_GET_ROUTE_MULTIPLE, {
      urlParams: { id },
    });
    return response.data;
  }
}
```

### Шаг 4: Зарегистрировать в IoC

```ts
// src/ioc/ioc.types.ts
export const TYPES = {
  // ...
  IPlannerStore: Symbol.for('IPlannerStore'),
  IPlannerService: Symbol.for('IPlannerService'),
};
```

```ts
// src/ioc/ioc.stores.ts
import { DIPlannerStore } from 'stores/Planner/DIPlanner.store';
import { DIPlannerService } from 'stores/Planner/DIPlanner.service';
import { IPlannerService, IPlannerStore } from 'stores/Planner/Planner.interface';

export type IAppStore = {
  [StoreNames.plannerStore]: IPlannerStore;
  // ...
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IPlannerStore>(TYPES.IPlannerStore).to(DIPlannerStore);
  container.bind<IPlannerService>(TYPES.IPlannerService).to(DIPlannerService);

  return {
    plannerStore: container.get<IPlannerStore>(TYPES.IPlannerStore),
    // ...
  } as IAppStore;
}
```

### Шаг 5: Добавить в `StoreNames`

```ts
// src/ioc/ioc.storeNames.ts
export enum StoreNames {
  // ...
  plannerStore = 'plannerStore',
}
```

---

## 🎮 Доступ в компонентах

### Через `useAppStoreContext`

```tsx
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

const PlannerScreen: FC = observer(() => {
  const { plannerStore, http, logger } = useAppStoreContext();

  useEffect(() => {
    plannerStore.fetchRoutes({});
  }, []);

  return (
    <div>
      {plannerStore.routesListStore.content.map(route => (
        <div key={route.id}>{route.humanReadableId}</div>
      ))}
    </div>
  );
});
```

### Через `useAppStore`

```tsx
import { useAppStore } from 'ioc/ioc.context';

const SomeHook = () => {
  const stores = useAppStore();
  // ...
};
```

### ⚠️ Важно

- `useAppStoreContext` бросает ошибку, если компонент рендерится **до** инициализации стора (защита от race conditions).
- Компоненты, использующие observable, **должны** быть обёрнуты в `observer()` (`mobx-react`).

---

## 📚 Каталог всех сторов

### DI-сторы (через Inversify)

| Store | Файл | Ответственность |
|---|---|---|
| **`plannerStore`** | `stores/Planner/DIPlanner.store.ts` | Планировщик: маршруты, заявки, drag-and-drop, журнал, фильтры ЭТрН |
| **`cargoStore`** | `modules/OrderExecution/stores/Cargo/DICargoStore.store.ts` | Хранилище грузов в текущей сессии |

### Из `@sber-sbertransport/mf-core` (через `IRootStore`)

| Store | Ответственность |
|---|---|
| `configStore` | Конфигурация приложения |
| `selfStore` | Текущий пользователь |
| `rootStore` | Корневой стор |
| `authStore` | Авторизация |
| `settingsStore` | Настройки |
| `employeeStore` | Сотрудники |
| `employeeExtStore` | Расширенные данные сотрудников |
| `delegatesStore` | Делегаты |

### Доменные сторы (прямой импорт)

| Стор | Файл | Ответственность |
|---|---|---|
| `AddressTypes` | `stores/AddressTypes/` | Типы адресов |
| `Analysis` | `stores/Analysis/` | Аналитика |
| `ApprovalSettings` | `stores/ApprovalSettings/` | Настройки согласований |
| `Auth` | `stores/Auth/` | Авторизация (клиентская) |
| `BusinessReports` | `stores/BusinessReports/` | Бизнес-отчёты |
| `CargoAuto` | `stores/CargoAuto/` | Автомобили |
| `CargoCategoryName` | `stores/CargoCategoryName/` | Категории грузов |
| `CargoDeliveryTimeSettings` | `stores/CargoDeliveryTimeSettings/` | Время доставки |
| `CargoPackage` | `stores/CargoPackage/` | Упаковка |
| `CargoRegistry` | `stores/CargoRegistry/` | Реестр грузов |
| `CargoType` / `CargoTypeName` | `stores/CargoType/` | Типы грузов |
| `CarSharingTrip` | `stores/CarSharingTrip/` | Поездки каршеринга |
| `CompensationRegistry` | `stores/CompensationRegistry/` | Реестр компенсаций |
| `Compensations` | `stores/Compensations/` | Компенсации |
| `ContractorDispatchers` | `stores/ContractorDispatchers/` | Диспетчеры контрагентов |
| `Contractors` | `stores/Contractors/` | Контрагенты |
| `Contracts` | `stores/Contracts/` | Договоры |
| `Corporate` | `stores/Corporate/` | Корпоративные данные |
| `DateTypes` | `stores/DateTypes/` | Типы дат |
| `DeadlineSettings` | `stores/DeadlineSettings/` | Дедлайны |
| `Delegates` | `stores/Delegates/` | Делегаты |
| `Department` | `stores/Department/` | Подразделения |
| `Employee` | `stores/Employee/` | Сотрудники |
| `EmployeesAttribute` | `stores/EmployeesAttribute/` | Атрибуты сотрудников |
| `Engineer` | `stores/Engineer/` | Инженеры |
| `FleetManagment` | `stores/FleetManagment/` | Управление автопарком |
| `Geo` | `stores/Geo/` | Гео-данные |
| `GeoZones` | `stores/GeoZones/` | Гео-зоны |
| `Limits` | `stores/Limits/` | Лимиты |
| `Locations` | `stores/Locations/` | Местоположения |
| `Notifications` | `stores/Notifications/` | Уведомления |
| `Organizations` | `stores/Organizations/` | Организации |
| `OrganizationsGroup` | `stores/OrganizationsGroup/` | Группы организаций |
| `Pagination` | `stores/Pagination/` | Общая пагинация |
| `PersonalSearch` | `stores/PersonalSearch/` | Поиск (личный реестр) |
| `Position` | `stores/Position/` | Должности |
| `PublicRegistry` | `stores/PublicRegistry/` | Публичный реестр |
| `Registry` | `stores/Registry/` | Общий реестр |
| `RegistryTaxi` | `stores/RegistryTaxi/` | Реестр такси |
| `Roles` | `stores/Roles/` | Роли |
| `ServiceTypes` | `stores/ServiceTypes/` | Типы сервисов |
| `SharedRide` | `stores/SharedRide/` | Совместные поездки |
| `StatusTypes` | `stores/StatusTypes/` | Типы статусов |
| `Tariffs` | `stores/Tariffs/` | Тарифы |
| `Tasks` | `stores/Tasks/` | Задачи |
| `TransportServiceTypes` | `stores/TransportServiceTypes/` | Типы транспортных сервисов |
| `TransportTypes` | `stores/TransportTypes/` | Типы транспорта |
| `TransportTypesDelegates` | `stores/TransportTypesDelegates/` | Делегаты транспорта |
| `Trip` | `stores/Trip/` | Поездки |
| `TripPurposes` | `stores/TripPurposes/` | Цели поездок |
| `UserAgent` | `stores/UserAgent/` | User Agent |

### Глобальные контексты

| Файл | Назначение |
|---|---|
| `src/stores/SettingsContext.tsx` | `SettingsContext`, `RegistrySettingsContext` |
| `src/stores/StoreNames.enum.ts` | `enum StoreNames` (строковые ключи для `IAppStore`) |

---

## 🔗 Cross-store коммуникация

**Правило:** сторы **не импортируют** друг друга напрямую. Взаимодействие — через **корневой стор** (`rootStore`) или через **контекст**.

### ❌ Антипаттерн

```ts
// ❌ Циклическая зависимость
import { EmployeeStore } from 'stores/Employee/Employee.store';

@injectable()
export class DIPlannerStore {
  @inject('EmployeeStore')
  private employeeStore: EmployeeStore;  // ← прямой импорт
}
```

### ✅ Правильный паттерн

Через корневой стор (через `useAppStoreContext`):

```tsx
const Component: FC = () => {
  const { plannerStore, employeeStore } = useAppStoreContext();
  // ...
};
```

Через сервис (если нужна синхронная координация):

```ts
@injectable()
export class DIPlannerService {
  @inject(TYPES.IPlannerStore)
  private plannerStore: IPlannerStore;  // ← инжектится через IoC

  // ...
}
```

---

## ⚡ Computed значения

**Для производных данных** — чтобы не пересчитывать при каждом рендере.

```ts
@injectable()
export class DIPlannerStore {
  @observable ordersListStore: OrdersListMinimalStoreType = {
    content: [],
    totalElements: 0,
    totalPages: 0,
  };

  @observable routeStore: RouteType | undefined;

  // Computed — кэшируется и пересчитывается только при изменении зависимостей
  @computed
  get hasOrders(): boolean {
    return this.ordersListStore.content.length > 0;
  }

  @computed
  get totalWeight(): number {
    return this.ordersListStore.content.reduce(
      (sum, order) => sum + order.cargo.weightKg,
      0
    );
  }
}
```

> ⚠️ Computed **не должен** иметь побочных эффектов (никаких вызовов API, mutation, console.log).

---

## ⚙️ Async через `runInAction`

Для обновления observable из `await`-блока используйте `runInAction`:

```ts
@action
async fetchRoutes(filters: RoutesFiltersType): Promise<void> {
  try {
    const response = await this.service.searchRoutes(filters);
    runInAction(() => {
      this.routesListStore = response;  // ← обновление в runInAction
    });
  } catch (error) {
    this.logger.toNotify('error', 'Не удалось загрузить маршруты', error);
  }
}
```

**Почему:** MobX требует, чтобы все мутации observable происходили внутри `action`. `await` разрывает синхронный контекст, поэтому после `await` нужна обёртка в `runInAction`.

---

## 🧪 Тестирование сторов

### Создание стора в тесте

```ts
import { DIPlannerStore } from 'stores/Planner/DIPlanner.store';
import type { IPlannerService } from 'stores/Planner/Planner.interface';

const mockService: jest.Mocked<IPlannerService> = {
  searchRoutes: jest.fn(),
  getRoute: jest.fn(),
};

const store = new DIPlannerStore(mockService, mockLogger);
```

### Установка observable напрямую

```ts
runInAction(() => {
  store.routesListStore = {
    content: [mockRoute1, mockRoute2],
    totalElements: 2,
    totalPages: 1,
  };
});
```

### Проверка computed

```ts
expect(store.hasOrders).toBe(true);
expect(store.totalWeight).toBe(2500);
```

---

## 🚨 Распространённые ошибки

### 1. Забыли `observer`

```tsx
// ❌ Компонент не подписан на observable — не будет ререндериться
const Component: FC = () => {
  const { plannerStore } = useAppStoreContext();
  return <div>{plannerStore.routesListStore.totalElements}</div>;
};

// ✅ С observer
const Component: FC = observer(() => {
  const { plannerStore } = useAppStoreContext();
  return <div>{plannerStore.routesListStore.totalElements}</div>;
});
```

### 2. Мутация без `action` или `runInAction`

```ts
// ❌ Мутация вне action — варнинг от MobX
async fetchRoutes(filters: RoutesFiltersType) {
  const response = await this.service.searchRoutes(filters);
  this.routesListStore = response;  // ← warning
}

// ✅ Через runInAction
async fetchRoutes(filters: RoutesFiltersType) {
  const response = await this.service.searchRoutes(filters);
  runInAction(() => {
    this.routesListStore = response;
  });
}
```

### 3. Использование `makeObservable`

```ts
// ❌ Это для MobX 6+. У нас MobX 5.
constructor() {
  makeObservable(this);
}

// ✅ Декораторы @observable/@action/@computed
@observable
field = 0;
```

---

> **Версия:** 1.0 (7 августа 2026 г.)
> **Источник:** `src/ioc/`, `src/stores/Planner/DIPlanner.store.ts`, `src/modules/Planner/Planner.architecture.md`, реальный код проекта.
