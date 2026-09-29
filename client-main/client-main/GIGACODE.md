# GigaCode Context — front_client (main)

## Project Overview

**Client frontend microservice "main"** — это хост-фронтенд-компонента для системы SberTransport, построенная по принципам **Module Federation** и микросервисной архитектуры. Является точкой входа (разводкой) для микрофронтендов `passengers`, `cargo`, `fleet`. Включает в себя управление заявками на поездки, лимитами, адресами, профилем и данными.

### Основные технологии

| Технология | Версия | Назначение |
|-----------|--------|-----------|
| React | 16.13.1 | Библиотека для построения UI |
| TypeScript | 5.2.2 | Строгая типизация |
| MobX | 5.15.4 | Управление состоянием |
| Ant Design | 4.17.2 | UI-библиотека компонентов |
| React Query | 2.25.2 | Управление серверным состоянием |
| styled-components | 5.3.3 | CSS-in-JS решение |
| Module Federation | 2.0.22 | Архитектура микросервисов |
| React Router | 5.3.0 | Роутинг |
| Axios | 0.19.2 | HTTP-клиент |
| Inversify | 5.0.1 | DI-контейнер (IoC) |
| fp-ts | 2.6.3 | Функциональное программирование |
| io-ts | 2.2.4 | Валидация данных |
| Leaflet | 1.6.0 | Карты |

### Ключевые зависимости

- `@sber-sbertransport/mf-core` — ядро Module Federation
- `@sber-sbertransport/tool-kit` — набор инструментов для микросервисов
- `@sber-sbertransport/ui-kit` — UI-компоненты
- `@sber-sbertransport/ai-agent` — AI-агент (ассистент)
- `fp-ts`, `io-ts` — функциональное программирование и валидация
- `axios` — HTTP-клиент

---

## Building and Running

### Предварительные требования

- Node.js **>= 18.18.0** (см. `.nvmrc`)
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

**Примечание:** По умолчанию приложение запускается на порту **7000** (стандартный порт для хост-приложений).

### Сборка и деплой

| Команда | Описание |
|---------|----------|
| `yarn build` | Сборка с версионированием |
| `yarn build:debug` | Отладочная сборка |
| `yarn build:basic` | Сборка с Basic-авторизацией |
| `yarn build:sigma` | Сборка для Sigma |

### Docker

```bash
# Сборка образа
docker build -t sbt-client-front .

# Запуск контейнера
docker run -p 7000:7000 sbt-client-front
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
- Test runner: Jest с jsdom-окружением
- Таймаут тестов: 50 000 ms
- Setup-файл: `src/setupTests.ts`
- Покрытие: V8 (clover)

### Исключения из покрытия

Тесты исключают из покрытия:
- API-сервисы (`src/api/**`)
- Конфигурация приложения (`src/app/**`)
- Контексты (`src/context/**`)
- DI-контейнер (`src/ioc/**`)
- Module Federation (`src/mf/**`)
- Глобальные стили (`src/styles/**`)
- i18n-файлы (`src/i18n/**`)
- Константы (`src/constants/**`)
- Точки входа (`src/index.ts`, `src/index.tsx`, `src/bootstrap.tsx`)

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
│   ├── approvals.ts               # Согласования
│   ├── check-in.ts                # Чек-ин
│   ├── compensations.ts           # Компенсации
│   ├── confirmation.ts            # Подтверждения
│   ├── delegates.ts               # Делегаты
│   ├── feedback.ts               # Отзывы
│   ├── geo.ts                     # Гео-данные
│   ├── index.ts                   # Главный экспорт
│   ├── limits.ts                  # Лимиты
│   ├── notifications.ts          # Уведомления
│   ├── personalCars.ts           # Личные автомобили
│   ├── purposes.ts                # Назначения
│   ├── telemechanic.ts           # Телемеханика
│   ├── trip-requests.ts          # Запросы поездок
│   ├── vehicles.ts               # Транспортные средства
│   ├── type-helpers.ts           # Вспомогательные типы
│   ├── update-pass.ts            # Обновление пароля
│   └── yandexTaxi/               # Яндекс Такси
│       ├── yandex-taxi.api.ts
│       ├── yandex-taxi.constants.ts
│       └── yandex-taxi.types.ts
│
├── components/                     # (зарезервировано)
├── constants/                      # Константы приложения
│   ├── constants.ts               # Константы (QueryCache)
│   ├── constants.app.ts           # Системные константы, enum-ы
│   ├── constants.env.js           # Переменные окружения, endpoint-ы
│   ├── constants.routes.ts        # Маршруты приложения
│   └── calendar.constants.ts      # Календарные константы
│
├── context/                        # React-контексты
│   └── Router.context.tsx         # Контекст роутера
│
├── ioc/                           # DI-контейнер (Inversify)
│   ├── ioc.container.ts          # Контейнер Inversify
│   ├── ioc.context.ts            # Контекст DI
│   ├── ioc.errors.ts            # Ошибки DI
│   ├── ioc.hooks.ts             # Хуки DI
│   ├── ioc.stores.ts            # Сторы DI
│   ├── ioc.storeNames.ts       # Имена сторов
│   ├── ioc.types.ts             # Типы DI
│   └── types.ts                  # Доп. типы
│
├── libs/                          # Внешние библиотеки
├── mf/                            # Конфигурация Module Federation
│   ├── MFDataLoader.ts           # Загрузчик данных MF
│   ├── MFLinksLoader.ts          # Загрузчик ссылок MF
│   ├── MFLoader.tsx              # Загрузчик модулей MF
│   ├── constants.ts              # Константы MF
│   └── debug.ts                  # Дебаг MF
│
├── modules/                       # Бизнес-модули приложения
│   ├── ApprovalPage/             # Страница согласований
│   ├── Bonuses/                  # Бонусы
│   ├── DashboardCards/           # Дашборд-карточки
│   ├── FavoriteAddressPage/      # Избранные адреса
│   ├── Home/                     # Домашний модуль
│   ├── LimitsPage/               # Страница лимитов
│   ├── OnTheLineSwitch/         # Переключатель "На линии"
│   ├── Page404/                  # Страница 404
│   ├── PassengersMemo/          # Памятка пассажиров
│   ├── ProfilePage/              # Профиль
│   └── SupportPage/              # Поддержка
│
├── shared/                        # Общие/переиспользуемые компоненты
│   ├── components/               # UI-компоненты
│   │   ├── AccessControl/        # Контроль доступа
│   │   ├── ActionButton/         # Кнопка действия
│   │   ├── AddressAutoComplete/  # Автодополнение адреса
│   │   ├── Breadcrumbs/          # Хлебные крошки
│   │   ├── BuildInfo/            # Информация о сборке
│   │   ├── Button/               # Кнопка
│   │   ├── Cargo/                # Груз
│   │   ├── DelayedRedirect/      # Отложенный редирект
│   │   ├── EmployeeAutoComplete/ # Автокомплит сотрудника
│   │   ├── EmptyFactory/         # Пустая фабрика
│   │   ├── ErrorBoundary/        # Граница ошибок
│   │   ├── Header/               # Хэдер
│   │   ├── Images/               # Изображения
│   │   ├── InstallPwa/           # PWA-установка
│   │   ├── LimitBar/             # Панель лимита
│   │   ├── LoginWrapper/         # Обёртка логина
│   │   ├── Map/                  # Карта
│   │   ├── NumericInput/         # Числовой ввод
│   │   ├── PageContent/          # Контент страницы
│   │   ├── PageLayout/           # Лэйаут страницы
│   │   ├── Pagination/           # Пагинация
│   │   ├── ReasonModal/          # Модалка причин
│   │   ├── RequestFilter/        # Фильтр запросов
│   │   ├── Scheduler/            # Планировщик
│   │   ├── SpinWrapped/          # Спин-обёртка
│   │   ├── SvgIcons/             # SVG-иконки
│   │   ├── TableEditButtons.tsx  # Кнопки редактирования
│   │   └── TabsNav/              # Навигация по табам
│   ├── form/                     # Формы
│   │   ├── Address/              # Форма адреса
│   │   ├── Button/               # Кнопка формы
│   │   ├── CargoType/            # Тип груза
│   │   ├── Checkbox/             # Чекбокс
│   │   ├── DatePicker/           # Выбор даты
│   │   ├── Employee/            # Сотрудник
│   │   ├── Field/               # Поле
│   │   ├── FormField/           # Поле формы
│   │   ├── Input/               # Ввод
│   │   ├── InputNumber/         # Числовой ввод
│   │   ├── MaskedInput/         # Маскированный ввод
│   │   ├── Radio/               # Радио
│   │   ├── Rate/                # Рейтинг
│   │   ├── Select/              # Селект
│   │   └── Textarea/            # Текстовая область
│   ├── hooks/                    # Хуки
│   │   ├── geo/                 # Гео-хуки
│   │   ├── limit/               # Хуки лимитов
│   │   └── trip/                # Хуки поездок
│   │
│   ├── ui/                      # UI-компоненты
│   │   ├── Button/
│   │   ├── Form/
│   │   ├── Modal/
│   │   ├── Panel/
│   │   └── Toolbar/
│
├── stores/                        # Сторы (MobX)
│   ├── Address/                  # Стор адресов
│   ├── CargoList/               # Список грузов
│   ├── CargoTariff/             # Тарифы грузов
│   ├── CargoType/               # Типы грузов
│   ├── Confirmation/            # Подтверждения
│   ├── Corporates/              # Корпоративные
│   ├── Delegates/               # Делегаты
│   ├── Files/                   # Файлы
│   ├── Geo/                     # Гео-данные
│   ├── Limits/                  # Лимиты
│   ├── Mapped/                  # Карты
│   ├── Pagination/             # Пагинация
│   ├── Request/                 # Запросы
│   ├── Settings/                # Настройки
│   ├── TransportTypes/          # Типы транспорта
│   ├── Trip/                    # Поездки
│   └── StoreNames.enum.ts      # Имена сторов
│
├── styles/                        # Глобальные стили
├── types/                         # Глобальные TypeScript-типы
├── ui/                            # Layout (меню, шапка)
│   ├── Header/                   # Шапка
│   ├── Layout/                   # Лэйаут
│   └── SideMenu/                 # Боковое меню
│
└── utils/                         # Утилиты и хелперы
```

### Маршруты приложения

| Маршрут | Описание | Компонент |
|---------|----------|-----------|
| `/client` | Разводная точка (хост) | Main (роутер модулей) |
| `/client/home` | Дашборд | DashboardCards |
| `/client/approvement` | Согласования | ApprovalRouter |
| `/client/limits` | Лимиты | LimitsPageRouter |
| `/client/favorite` | Избранные адреса | FavoriteAddressPage |
| `/client/support` | Поддержка | SupportPage |
| `/client/profile` | Профиль | ProfilePage |
| `/client/bonuses` | Бонусы | BonusesPageRouter |
| `/client/404` | Страница 404 | Page404 |
| `/mobile-app` | Мобильное приложение | MobileAppRef |
| `/oauth` | Маршрут аутентификации | — |

### Micro Frontends (Module Federation)

| Модуль | Маршрут | Источник |
|--------|----------|----------|
| **passengers** | `/client/passengers` | `front-micro-passenger` |
| **cargo** | `/client/cargo` | `front-micro-cargo` |
| **fleet** | `/client/fleet` | `front-micro-fleet` |

### Утилиты (utils/)

Модуль `src/utils/` содержит набор вспомогательных функций:

#### Форматирование и преобразование
- `formatFullName(firstName, lastName, middleName)` — Форматирует ФИО как "Фамилия И. О."
- `formatName(firstName, lastName, middleName)` — Форматирование имени
- `formatPhoneNumber(phone)` — Форматирование телефона
- `Misc.ts` — Разные утилиты
- `MoneyUtils.ts` — Денежные утилиты
- `parseNumber.ts` — Парсинг числа
- `token.ts` — Работа с токеном

#### Работа с данными
- `storage.ts` — Хранилище данных
- `fileUtils/` — Работа с файлами
- `geo/` — Гео-утилиты
- `uniqId.ts` — Генерация уникальных ID
- `uuid.ts` — UUID

#### Работа с API и URL
- `getErrorCode(error)` — Получение кода ошибки
- `isTestMode.ts` / `isTestStand.ts` — Определение тестового режима
- `transport.ts` — Транспортные утилиты
- `waypoint/` — Точки маршрута

#### Валидация и проверки
- `declOfNum/` — Склонение числительных
- `Types.ts` — Утилиты типов
- `datetime.ts` — Дата/время

#### io-ts типы (валидация)

Файлы в `src/utils/io-ts/`:
- `index.ts` — Основные экспорты
- `pagination.ts` — Типы пагинации
- `wrapped.ts` — Обернутые типы

### Глобальные типы (types/)

- `Cargo.ts` — Типы грузов
- `index.d.ts` — Глобальные типы

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
| `REACT_APP_NAME` | `main` | Имя микросервиса |
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
feat(Dashboard): добавить виджет погоды
fix(Approvals): исправить дублирование запросов
docs(Readme): обновить инструкцию по установке
refactor(Limits): вынести логику в сервис
test(Map): добавить тесты карты
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

1. Создайте файл в `src/api/new-api-service`
2. Опишите типы в `src/api/new-api-service/new-api-service.types.ts`
3. Опишите константы в `src/api/new-api-service/new-api-service.constants.ts`
4. Создайте хуки в `src/api/new-api-service/new-api-service.api.ts` (если используется react-query)

### Как добавить новый компонент?

1. Создайте папку в `src/shared/components/ComponentName/`
2. Создайте файл `ComponentName.tsx`
3. Экспортируйте компонент из `index.ts` папки

### Как добавить новый маршрут?

1. Добавьте константу в `src/constants/constants.routes.ts`
2. Добавьте route в `src/AppRouter.tsx`
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
- Маршруты начинаются с `/client` (не `/fleet`)
- Порт разработки по умолчанию: **7000** (не 7004)