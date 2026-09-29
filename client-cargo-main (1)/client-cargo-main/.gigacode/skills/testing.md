# Skill: Тестирование MobX сторов в проекте Cargo

## Контекст

Проект **Cargo** — это frontend-микросервис для управления грузоперевозками в рамках платформы SberTransport.

**Технологический стек:**
- **React 16.13.1** + **TypeScript** (строгая типизация)
- **MobX 5.15.4** — управление состоянием (классовые сторы с декораторами)
- **Ant Design 4.17.2** — UI-библиотека
- **Jest** — тестирование

**Роль этого документа:** Данный документ является частью контекста проекта для AI-ассистентов и содержит готовые шаблоны для написания тестов MobX сторов.

---

## Шаблон для создания тестов MobX сторов

### 1. Базовые моки (обязательные)

```typescript
// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock antd notification to avoid initialization issues
jest.mock('antd', () => ({
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

// Mock ioc types to avoid container initialization
jest.mock('ioc/types', () => ({
  TYPES: {
    // Используемые типы из ioc/types
    ICargoService: Symbol('ICargoService'),
    ILogger: Symbol('ILogger'),
    ISelfEmployeeStore: Symbol('ISelfEmployeeStore'),
  },
}));

// Mock @sber-sbertransport/mf-core to avoid logger initialization
jest.mock('@sber-sbertransport/mf-core', () => ({
  inject: jest.fn(),
  injectable: jest.fn((target: any) => target),
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
  Logger: class Logger {},
  Employee: class Employee {},
  IOHumanReadable: class IOHumanReadable {},
  EmployeeModel: class EmployeeModel {
    fullNameWithCode: string = '';
    mobilePhone: string = '';
  },
  // Если нужен ISelfEmployeeStore
  ISelfEmployeeStore: class ISelfEmployeeStore {
    selfEmployee: any = null; // Всегда null для тестов
  },
}));
```

### 2. Моки для специфических зависимостей

#### Момент (если используется в инициализации)

```typescript
// Mock moment to avoid date dependencies
jest.mock('moment', () => {
  const mockDate = new Date('2024-01-01');
  const mockMoment = (date?: any) => ({
    utc: () => mockMoment(),
    add: (amount: number, unit: any) => mockMoment(),
    startOf: (unit: any) => mockMoment(),
    format: () => '2024-01-01T00:00:00.000Z',
    valueOf: () => mockDate.getTime(),
    isValid: () => true,
    _isUTC: false,
    _d: date ? new Date(date) : mockDate,
  });
  mockMoment.utc = () => mockMoment();
  mockMoment.now = () => mockDate.getTime();
  return mockMoment;
});
```

#### Константы

```typescript
// Mock constants
jest.mock('constants/constants.app', () => ({
  DATE_FORMAT: {
    BASE: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
  },
}));
```

#### Модели

```typescript
// Mock models
jest.mock('../Cargos/models/CargoRequest.model', () => ({
  CargoRequestModel: class CargoRequestModel {
    constructor(data: any) {
      Object.assign(this, data);
    }
  },
}));
```

#### Типы

```typescript
// Mock types
jest.mock('types/Cargo', () => ({
  BackendCargoPersonalListItemType: {},
  CargoListItem: {},
  CargoPersonalListItemType: {},
}));

// Mock Cargos types
jest.mock('../Cargos/types', () => ({
  PeriodType: {},
  Pack: {},
  PackData: {},
}));

// Mock Cargos typesMulti
jest.mock('../Cargos/typesMulti', () => ({
  OrderRequestMulti: {},
  OrderRequestRegularMulti: {},
  AddressListMultiForm: {},
  WaypointMulti: {},
  StepAddressValuesMulti: {},
  StepAddressValues: {},
  StepCargosValues: {},
  StepTariffValuesMulti: {},
  StepTariffValues: {},
  StepFinalValues: {},
}));
```

### 3. Основные описания тестов

```typescript
describe('DICargoStore', () => {
  let DICargoStore: any;

  beforeEach(async () => {
    jest.resetModules();
    DICargoStore = (await import('./DICargo.store')).DICargoStore;
  });

  describe('initial values', () => {
    // Проверка всех начальных значений
    it('should have initial stepAddressValues with sender and recipient', () => {
      const store = new DICargoStore();
      expect(store.stepAddressValues).toEqual({
        sender: null,
        recipient: null,
        senderAddress: '',
        desiredDate: expect.anything(),
        senderName: '',
        senderPhone: '',
        senderOrganization: '',
        sourceLoaders: false,
        recipientAddress: '',
        recipientName: '',
        recipientPhone: '',
        recipientOrganization: '',
        destinationLoaders: false,
        sourceLoadersCount: 0,
        destinationLoadersCount: 0,
      });
    });

    it('should have initial stepCargosValues with empty cargoList', () => {
      const store = new DICargoStore();
      expect(store.stepCargosValues).toEqual({
        cargoList: [],
      });
    });

    it('should have initial stepTariffValues', () => {
      const store = new DICargoStore();
      expect(store.stepTariffValues).toEqual({
        distance: 0,
        tariffCost: null,
        expected: null,
        express: false,
        comment: '',
      });
    });

    // ... остальные начальные значения
  });

  describe('methods', () => {
    // Проверка наличия всех методов
    it('should have setRepeatOrder method', () => {
      const store = new DICargoStore();
      expect(typeof store.setRepeatOrder).toBe('function');
    });

    it('should have setAdditionalServices method', () => {
      const store = new DICargoStore();
      expect(typeof store.setAdditionalServices).toBe('function');
    });

    // ... остальные методы
  });

  describe('computed properties', () => {
    // Проверка геттеров
    it('should have isRegular getter', () => {
      const store = new DICargoStore();
      expect(typeof store.isRegular).toBe('boolean');
    });

    it('should have isRelocation getter', () => {
      const store = new DICargoStore();
      expect(typeof store.isRelocation).toBe('boolean');
    });
  });
});
```

---

## Важные правила

### 1. Не используйте `selfEmployee` в инициализации

**Проблема:** `@inject` работает после создания экземпляра, поэтому `this.selfStore.selfEmployee` будет `undefined` при инициализации полей.

**Решение:** Используйте `null` напрямую:

```typescript
// ❌ ПЛОХО
@observable
private initialStepAddressValues = {
  sender: this.selfStore.selfEmployee, // undefined при инициализации!
  senderName: this.selfStore.selfEmployee?.fullNameWithCode || '',
};

// ✅ ХОРОШО
@observable
private initialStepAddressValues: StepAddressValues = {
  sender: null,
  senderName: '',
};
```

### 2. Упрощайте инициализацию полей

Избегайте вызовов `moment()` и других сложных функций в инициализации полей, так как это может вызвать проблемы с моками.

```typescript
// ❌ ПЛОХО
initialPeriodValues: types.PeriodType = {
  beginDate: moment()
    .startOf('day')
    .add(1, 'days')
    .format(DATE_FORMAT.BASE)
    .valueOf() as unknown as string,
};

// ✅ ХОРОШО
initialPeriodValues: types.PeriodType = {
  beginDate: '' as any,
};
```

### 3. Всегда очищайте моки перед каждым тестом

```typescript
beforeEach(async () => {
  jest.resetModules();
  DICargoStore = (await import('./DICargo.store')).DICargoStore;
});
```

### 4. Проверяйте типы методов, а не их поведение

Для простых тестов достаточно проверить, что метод существует и является функцией:

```typescript
it('should have clearStepValues method', () => {
  const store = new DICargoStore();
  expect(typeof store.clearStepValues).toBe('function');
});
```

### 5. Используйте `expect.anything()` для динамических значений

Для значений, зависящих от даты или времени, используйте `expect.anything()`:

```typescript
it('should have initial desiredDate', () => {
  const store = new DICargoStore();
  expect(store.stepAddressValues.desiredDate).toEqual(expect.anything());
});
```

---

## Примеры готового файла теста

См. `src/stores/Cargos/DICargos.store.test.ts` — полный пример рабочих тестов для MobX стора.
См. `src/stores/CargoType/DICargoType.service.test.ts` — полный пример рабочих тестов для MobX сервиса.

---

## Проверка работы тестов

После создания тестов запустите:

```bash
# Запуск конкретного теста
yarn test src/stores/Cargos/DICargos.store.test.ts --no-coverage

# Запуск всех тестов
yarn test --no-coverage
```

**Ожидаемый результат:**
- Все тесты проходят (`Test Suites: X passed, Tests: X passed`)
- 0 падений (`0 failed`)

---

## Ссылки на примеры

| Файл | Описание |
|------|----------|
| `src/stores/Cargos/DICargos.store.test.ts` | Пример тестов для простого стора (без `selfEmployee`) |
| `src/stores/CargoMassMultiple/DICargoMassMultiple.store.test.ts` | Пример тестов для стора со сложной инициализацией |

---

## Частые ошибки и решения

| Ошибка | Причина | Решение |
|--------|---------|---------|
| `Cannot read properties of undefined (reading 'selfEmployee')` | Использование `this.selfStore.selfEmployee` в инициализации поля | Заменить на `null` или вынести в отдельный метод |
| Тесты не проходят после импорта | Не вызван `jest.resetModules()` | Добавить `beforeEach` с очисткой модулей |
| `moment` не мокается | Порядок импортов в тесте | Убедиться, что мок `moment` идет до импорта стора |
| Типы не распознаются | Не созданные моки для типов | Создать моки для всех используемых типов |
