# GigaCode Context - disp-fleet

## Project Overview

**Dispatcher frontend microservice "Fleet"** — это фронтенд-компонента для диспетчерской системы SberTransport, построенная по принципам **Module Federation** и микросервисной архитектуры. Включает в себя управление автомобилями, автопарками, путевыми листами и телематикой.

### Основные технологии

| Технология | Версия | Назначение |
|-----------|--------|-----------|
| React | 16.14.0 | Библиотека для построения UI |
| TypeScript | 5.2.2 | Строгая типизация |
| MobX | 5.15.4 | Управление состоянием |
| Ant Design | 4.17.2 | UI-библиотека компонентов |
| React Query | 2.26.4 | Управление серверным состоянием |
| styled-components | 5.3.3 | CSS-in-JS решение |
| Module Federation | 2.0.22 | Архитектура микросервисов |

### Ключевые зависимости

- `@sber-sbertransport/mf-core` — ядро Module Federation
- `@sber-sbertransport/tool-kit` — набор инструментов для микросервисов
- `@sber-sbertransport/ui-kit` — UI-компоненты
- `fp-ts`, `io-ts` — функциональное программирование и валидация
- `axios` — HTTP-клиент

---

## Building and Running

### Предварительные требования

- Node.js **>= 18.0.0**
- Yarn (pkg manager)
- nvm (рекомендуется для управления версиями Node.js)

### Установка зависимостей

```bash
# Установка нужной версии Node.js (из .nvmrc)
nvm use

# Установка зависимостей строго по yarn.lock
yarn ci

# или эквивалентно
yarn install --frozen-lockfile
```

**Примечание:** Если `yarn ci` выдает ошибку авторизации, используйте `yarn install --frozen-lockfile`.

### Команды для разработки

| Команда | Описание |
|---------|----------|
| `yarn start` | Базовый режим c подключением к хост приложению (Remote) |
| `yarn start:dev-{env}` | Режим самостоятельного приложения с указанным окружением |
| `yarn start-auth:dev-{env}` | Запуск с Basic-авторизацией и указанным окружением |
| `yarn start-auth-mocked-login:dev-{env}` | Запуск с замоканной авторизацией |
| `yarn start-auth-mocked-api:dev-{env}` | Запуск с моками по API |
| `yarn start-auth-mocked-login-api:dev-{env}` | Запуск с замоканной авторизацией и моками по API |

**Примечание:** По умолчанию приложение запускается на порту **7004**.

### Сборка и деплой

| Команда | Описание |
|---------|----------|
| `yarn build` | Сборка с версионированием |
| `yarn build:debug` | Отладочная сборка |
| `yarn build:sigma` | Сборка для Sigma |

### Docker

```bash
# Сборка образа
docker build -t disp-fleet .

# Запуск контейнера
docker run -p 8085:8085 disp-fleet
```

---

## Testing

### Запуск тестов

```bash
# Запуск всех тестов
yarn test

# Очистка кэша Jest
yarn test:clear

# Запуск тестов с покрытием (CI режим)
yarn test:ci
```

### Конфигурация Jest

- Конфигурация в `jest.config.ts`
- Снапшоты и моки в `__mocks__/`
- Setup-файл: `src/setupTests.ts`
- Покрытие: V8 (код-coverage)

### Исключения из покрытия

Тесты исключают из покрытия:
- API-сервисы (`src/api/**`)
- Конфигурация приложения (`src/app/**`)
- Контексты (`src/context/**`)
- DI-контейнер (`src/ioc/**`)
- Module Federation (`src/mf/**`)
- Глобальные стили (`src/styles/**`)

---

## Linting and Formatting

### ESLint

```bash
# Проверка кода
yarn lint

# Исправление автоматически исправляемых ошибок
yarn lint:fix
```

### Конфигурация ESLint

- Конфигурация в `eslint.config.js`
- Правила на основе `@sber-sbertransport/tool-kit`

### Git Hooks (Lefthook)

Проект использует [Lefthook](https://github.com/evilmartians/lefthook) для автоматической проверки:

- `commit-msg` — проверка формата commit-сообщения (conventional commits)
- `pre-commit` — проверка TypeScript/ESLint
- `pre-push` — проверка версии Node.js и консистентности пакетов

---

## Development Conventions

### Архитектура проекта

```
src/
├── api/                            # API-сервисы (слой взаимодействия с бэкендом)
│   ├── autopark/                   # API автопарков
│   │   ├── autopark.api.ts        # Хуки API
│   │   ├── autopark.constants.ts  # Константы API
│   │   └── autopark.types.ts      # Типы API
│   ├── branches/                   # API филиалов
│   ├── contractors/                # API подрядчиков
│   ├── directories/                # API справочников
│   ├── profile/                    # API профиля пользователя
│   ├── telemechanicDispatchers/    # API телемеханика диспетчеров
│   ├── telemechanicDriver/         # API телемеханика водителей
│   ├── telemechanicTransport/      # API телемеханика транспорта
│   ├── transport/                  # API транспорта
│   ├── upload/                     # API загрузки файлов
│   ├── vehicles/                   # API автомобилей
│   └── waybill/                    # API путевых листов
│
├── app/                            # Основа приложения
│   ├── dev/                        # Конфигурация для разработки
│   │   ├── index.dev.ts           # Точка входа для dev режима
│   │   └── ...
│   └── prod/                       # Конфигурация для продакшна
│       ├── App.tsx                 # Корневой компонент приложения
│       ├── Main.tsx                # Основная компонента
│       ├── AppRouter.tsx           # Основной роутер приложения
│       └── AppProvider.tsx         # Провайдеры контекстов приложения
│
├── components/                     # UI-компоненты (shared/components)
│   ├── Arrow/                      # Стрелка
│   ├── AutoparkSelect/             # Выбор автопарка
│   ├── BranchSelect/               # Выбор филиала
│   ├── Breadcrumbs/                # Хлебные крошки
│   ├── Button/                     # Кнопка
│   ├── DatePicker/                 # Календарь
│   ├── EmptyView/                  # Пустое состояние
│   ├── ErrorBoundary/              # Граница ошибок
│   ├── Flex/                       # Flex-контейнер
│   ├── Icon/                       # Иконка
│   ├── List/                       # Список
│   ├── ListDetailed/               # Подробный список
│   ├── Masks/                      # Маски ввода
│   ├── ModelFormField/             # Поле формы модели
│   ├── PageLayout/                 # Макет страницы
│   ├── Panel/                      # Панель
│   ├── Pagination/                 # Пагинация
│   ├── RestrictedInput/            # Ограниченное поле ввода
│   ├── SearchPanel/                # Панель поиска
│   ├── Select/                     # Выбор
│   ├── SelectStateNumberMask/      # Выбор с маской госномера
│   ├── SpinWrapped/                # Спин-обертка
│   ├── StateNumberMask/            # Маска госномера
│   ├── Stepper/                    # Шаговый компонент
│   ├── TableStyled/                # Стилизованная таблица
│   ├── DownloadButton.tsx          # Кнопка загрузки
│   └── SelectEndlessScroll.tsx     # Бесконечный выбор
│
├── modules/                        # Бизнес-модули приложения
│   ├── AutoParks/                  # Модуль управления автопарками
│   ├── ReleaseOnLine/              # Модуль выпуска на линию
│   ├── Telematics/                 # Модуль телематики
│   ├── Vehicles/                   # Модуль управления транспортом
│   └── Waybill/                    # Модуль путевых листов
│
├── constants/                      # Константы приложения
│   ├── api.constants.ts            # Константы API
│   ├── app.constants.ts            # Константы приложения
│   ├── cryptoPro.ts                # Константы CryptoPro
│   ├── env.constants.ts            # Константы окружения
│   └── routes.constants.ts         # Маршруты приложения
│
├── context/                        # Глобальные React-контексты
│
├── hooks/                          # Кастомные хуки
│   ├── useCryptoPro.ts             # Хук для CryptoPro
│   ├── useAutoparkFilters.ts       # Фильтры автопарка
│   ├── useClickOutside.ts          # Событие клика вне
│   ├── useClientType.ts            # Тип клиента
│   ├── useDebounce.ts              # Дебаунс
│   ├── useParamsSerializer.ts      # Сериализация параметров
│   ├── useQuery.ts                 # Запрос
│   ├── useRole.ts                  # Роль
│   ├── useRoleMap.ts               # Карта ролей
│   ├── useTestMode.ts              # Тестовый режим
│   ├── useTitle.ts                 # Заголовок
│   ├── useToken.ts                 # Токен
│   └── useUrl.ts                   # URL
│
├── i18n/                           # Интернационализация (i18n)
│
├── ioc/                            # Inversion of Control (DI-контейнер)
│   ├── ioc.container.ts            # Контейнер зависимостей
│   ├── ioc.context.ts              # Контекст DI
│   ├── ioc.stores.ts               # Сторы
│   └── ioc.types.ts                # Типы
│
├── mf/                             # Конфигурация Module Federation
│
├── styles/                         # Глобальные стили
│
├── types/                          # Глобальные TypeScript-типы
│   ├── cryptoPro.interface.ts      # Интерфейс CryptoPro
│   ├── index.d.ts                  # Глобальные типы
│   ├── Types.ts                    # Общие типы
│   └── vehicles.ts                 # Типы транспорта
│
├── ui/                             # Layout для локального запуска (меню, шапка)
│
└── utils/                          # Утилиты и хелперы
```

### Маршруты приложения

| Маршрут | Описание | Модуль |
|---------|----------|--------|
| `/oauth` | Маршрут аутентификации | - |
| `/fleet/vehicles` | Управление транспортом | Vehicles |
| `/fleet/autopark-settings` | Настройки автопарка | AutoParks |
| `/fleet/release-on-line` | Выпуск на линию | ReleaseOnLine |
| `/fleet/waybill` | Путевые листы | Waybill |
| `/fleet/telematics` | Телематика | Telematics |
| `/fleet/404` | Страница 404 | - |

### Бизнес-модули

| Модуль | Описание | Основные API-сервисы |
|--------|----------|---------------------|
| **AutoParks** | Управление автопарками | autopark, branches, contractors |
| **ReleaseOnLine** | Выпуск транспорта на линию | vehicles, autopark, waybill |
| **Telematics** | Телематика и мониторинг | telemechanicTransport, telemechanicDriver |
| **Vehicles** | Управление транспортом | vehicles, transport, upload, directories |
| **Waybill** | Путевые листы | waybill, vehicles, profile |

### Утилиты (utils/)

Модуль `src/utils/` содержит набор вспомогательных функций для общих задач:

#### Форматирование и преобразование
- `formatFullName(firstName, lastName, middleName)` — Форматирует ФИО как "Фамилия И. О."
- `withNumberSpaces(x)` — Форматирует число с пробелами (`1 000`)
- `jwtDecode(token)` — Декодирует JWT токен
- `b64EncodeUnicode(str)` / `b64DecodeUnicode(str)` — Base64 кодирование/декодирование (UTF-8)
- `clearSymbols(value)` — Очистка строки от символов (оставляет кириллицу, латиницу, цифры)

#### Работа с данными
- `indexById(items)` — Создает объект-индекс по id из массива (использует Ramda)
- `deepMerge(source, target)` — Глубокое слияние двух объектов
- `isEmptyArray(value)` — Проверка пустого массива
- `isEqualArrays(arr1, arr2)` — Сравнение массивов
- `includesByLowerCaseAndSpaces(first, second)` — Вхождение подстроки без учета регистра

#### Работа с API и URL
- `getErrorMessage(error)` — Получает сообщение об ошибке из ответа Axios
- `handlePlug(logger, type, description)` — Обработчик логирования
- `setIntoUrl(key, value)` — Установка параметра в URL
- `_env(name)` — Получение значения переменной окружения

#### Валидация и проверки
- `isObject(value)` — Проверка типа значения
- `isFilledObject(value)` — Проверка непустого объекта
- `isEmptyObject(value)` — Проверка пустого объекта
- `isJSONString(str)` — Проверка, является ли строка JSON
- `preventDefault(event)` — Предотвращает событие Enter в input

#### io-ts типы (валидация)

Файлы в `src/utils/io-ts/`:
- `index.ts` — Основные экспорты
- `pagination.ts` — Типы пагинации
- `wrapped.ts` — Обернутые типы

Утилиты:
- `ioTypeFromEnum(enumName, theEnum)` — Создание типа из enum

#### Правила валидации (Ant Design)

Правила находятся в `src/utils/fieldValidationRules/`:

**validationPatterns:**
- `stateNumberRegExp` — Регулярное выражение для госномера
- `vinRegExp` — Регулярное выражение для VIN
- `driverLicenseRegexp` — Регулярное выражение для прав
- `driverPassportRegexp` — Регулярное выражение для паспорта
- `phoneNumberRegExp` — Регулярное выражение для телефона

**ValidationRules.general:**
- `required` — Обязательное поле
- `email` — Валидация email
- `checkStateNumber` — Формат госномера
- `checkVin` — Формат VIN
- `checkPhoneNumber` — Формат телефона
- `maxInt(name?)` — Максимальное значение (авто для разных полей)
- `minInt(name?)` — Минимальное значение
- `minManufactureYear(n)` — Минимальный год
- `validatorStartDate(form, endDateKey)` — Валидация даты начала
- `validatorEndDate(form, startDateKey)` — Валидация даты окончания

### Глобальные типы (types/)

#### types/Types.ts
- `RequiredPick<T, K>` — Pick полей и сделать их обязательными
- `RequiredKeys<T>` — Выбрать только обязательные поля
- `OptionalKeys<T>` — Выбрать только необязательные поля
- `TRangePickerArg` — Тип аргумента Antd RangePicker
- `LabeledValue<T>` — Интерфейс для значений с лейблами

#### types/vehicles.ts
- `Vehicle` — Интерфейс транспортного средства
- `VehicleModel` — Модель транспорта
- `VehiclesSearchRequest` — Запрос поиска транспорта
- `VehiclesSearchResponse` — Ответ поиска транспорта
- `AutoPark`, `AutoParks`, `CacheParks` — Типы автопарков

#### types/cryptoPro.interface.ts
- Интерфейс для работы с CryptoPro plugin

### Архитектурные принципы

- **Разделение ответственности** — компоненты не содержат бизнес-логику
- **MobX как state manager** — управление состоянием через классовые сторы
- **API-сервисы** — все вызовы API вынесены в отдельные сервисы
- **Типобезопасность** — строгая типизация на всех уровнях
- **DI (Dependency Injection)** — через Inversify и контекст

### Паттерны проектирования

- **Repository** — абстракция над данными
- **Service Layer** — разделение бизнес-логики и API-вызовов
- **Observer** — реактивное обновление UI через MobX

### Module Federation

Проект построен по принципам **Module Federation**, что позволяет:

- **Независимую разработку** — каждый микросервис может развиваться отдельно
- **Общий чанк** — совместное использование зависимостей (React, MobX, Ant Design и т.д.)
- **Динамическую загрузку** — загрузка модулей по требованию
- **Распределённую сборку** — сборка каждого микросервиса независимо

### Переменные окружения

| Переменная | Значение | Описание |
|------------|----------|----------|
| `REACT_APP_NAME` | `fleet` | Имя микросервиса |
| `REACT_APP_REMOTE` | `TRUE` / `FALSE` | Режим работы (Remote/Host) |
| `REACT_APP_DEBUG` | `TRUE` | Включение debug-режима |
| `REACT_APP_NETWORK_LOOP` | `DEV_AUTOPARK`, `DEV_CARGO`, `DEV_AUTOSERVICE`, `DEV_SBERTRANSPORT`, `ST`, `NT`, `IFT` | Окружение для подключения API |
| `REACT_APP_BASIC_AUTH` | `TRUE` | Включение Basic-авторизации |
| `REACT_APP_MOCKED_AUTH` | `TRUE` | Включение замоканной авторизации |
| `REACT_APP_MOCKED_API` | `TRUE` | Включение моков по API |

### Конфигурация

Конфигурация находится в:
- `scripts/index.js` — основной конфиг Craco
- `scripts/constants.js` — константы окружения
- `scripts/remotes.js` — подключенные микросервисы
- `scripts/exposes.js` — экспортируемые модули MF

---

## Commit Convention

Проект использует **Conventional Commits**:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Типы коммитов

- `feat` — новый функционал
- `fix` — исправление бага
- `docs` — изменения в документации
- `style` — форматирование (не влияет на логику)
- `refactor` — рефакторинг кода
- `test` — добавление тестов
- `chore` — обслуживание проекта
- `perf` — оптимизация производительности
- `ci` — изменения CI/CD

### Примеры

```
feat(Vehicles): добавить фильтрацию по статусу
fix(Waybill): исправить дублирование данных
docs(Readme): обновить инструкцию по установке
refactor(AutoParks): вынести логику в сервис
test(Table): добавить тесты сортировки
```

---

## Reviewer Checklist

См. `instructions.md` — детальная инструкция для ревьюера кода, включающая проверку по 9 направлениям:

1. **Общие принципы** — единообразие стиля, отсутствие дублирования, обработка ошибок
2. **TypeScript** — типизация, строгие проверки, интерфейсы и типы
3. **JavaScript (ES6+)** — современный синтаксис, асинхронность, иммутабельность
4. **React** — функциональные компоненты, хуки, оптимизация ререндеров
5. **Производительность** — мемоизация, виртуализация списков, ленивая загрузка
6. **Безопасность** — XSS, инъекции, хранение данных
7. **Архитектура** — структура проекта, MobX сторы, разделение ответственности
8. **Читаемость** — имена переменных, размер функций, форматирование
9. **Логика** — обработка состояний, асинхронные операции, валидация

**Дополнительно:** [Рекомендации по коду](https://confluence.sberbank.ru/pages/viewpage.action?pageId=15056208286)

---

## Common Tasks

### Как запустить микросервис локально?

```bash
yarn ci
# На примере cargo
yarn start-auth:dev-cargo
```

**Дополнительные ресурсы:**
- [Рекомендации по коду](https://confluence.sberbank.ru/pages/viewpage.action?pageId=15056208286)
- [Git Flow](https://confluence.sberbank.ru/display/TRANSPORT/Git+Flow)

### Как подключить новую API-эндпоинт?

1. Создайте папку `src/api/new-api-service`
2. Опишите типы в `src/api/new-api-service/new-api-service.types.ts`
3. Опишите константы в `src/api/new-api-service/new-api-service.constants.ts`
4. Создайте хуки в `src/api/new-api-service/new-api-service.api.ts` (если используется react-query)

### Как добавить новый компонент?

1. Создайте папку в `src/components/ComponentName/`
2. Создайте файл `ComponentName.tsx`
3. Экспортируйте компонент из `index.ts` папки

### Как добавить новый маршрут?

1. Добавьте константу в `src/constants/routes.constants.ts`
2. Добавьте route в `src/app/prod/AppRouter.tsx`
3. Создайте компонент в `src/modules/{Module}/`

### Как использовать утилиты для форматирования данных?

```typescript
// Форматирование ФИО
import { formatFullName } from 'utils/formatFullName';

const fullName = formatFullName('Иван', 'Петров', 'Сидорович');
// Результат: "Петров И. С."

// Форматирование числа с пробелами
import { numberWithSpaces } from 'utils/utils';

const formatted = numberWithSpaces(100000);
// Результат: "100 000"

// Декодирование JWT токена
import { jwtDecode } from 'utils/utils';

const payload = jwtDecode(token);
```

### Как использовать правила валидации для Ant Design Form?

```typescript
import { ValidationRules } from 'utils/fieldValidationRules';

<Form.Item
  name="email"
  label="Email"
  rules={[ValidationRules.general.required, ValidationRules.general.email]}
>
  <Input />
</Form.Item>

<Form.Item
  name="phoneNumber"
  label="Телефон"
  rules={[ValidationRules.general.required, ValidationRules.general.checkPhoneNumber]}
>
  <Input />
</Form.Item>

<Form.Item
  name="manufactureYear"
  label="Год выпуска"
  rules={[
    ValidationRules.general.required,
    ValidationRules.general.minManufactureYear(1900),
    ValidationRules.general.max(new Date().getFullYear())
  ]}
>
  <Input />
</Form.Item>

<Form.Item
  name="stateNumber"
  label="Госномер"
  rules={[ValidationRules.general.required, ValidationRules.general.checkStateNumber]}
>
  <Input />
</Form.Item>

<Form.Item
  name="vin"
  label="VIN"
  rules={[ValidationRules.general.required, ValidationRules.general.checkVin]}
>
  <Input />
</Form.Item>
```

### Как использовать io-ts типы для валидации?

```typescript
import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

// Создание типа из enum
enum Status { ACTIVE = 'active', INACTIVE = 'inactive' }
const StatusType = ioTypeFromEnum('Status', Status);
```

---

## Notes for GigaCode

- Для запуска тестов используй `yarn test`
- Для управления npm пакетами используй yarn
- При работе с кодом соблюдай TypeScript strict mode
- Все компоненты должны быть функциональными (не классами)
- MobX сторы должны использовать `observable`, `action`, `computed`
- API-сервисы вынесены в отдельные модули, не смешивать с логикой компонентов
- При ревью кода используй проверку по 9 направлениям из `instructions.md`
- После изменения кода проверяй, проходит ли проверка линтера `yarn lint`
- Маршруты начинаются с `/fleet` (не `/platform` как в других микросервисах)
- Порт разработки по умолчанию: **7004** (не 7005)
