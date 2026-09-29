# Проект: Corp Frontend Microservices "Main"

## Обзор проекта

Это **микросервисное фронтенд-приложение** для корпоративной системы транспортных услуг (SberTransport Corp). Проект является **основным (main) микрофронтендом** в архитектуре Micro Frontends, который объединяет несколько автономных бизнес-модулей в единое приложение.

### Ключевые характеристики

- **Тип**: Корпоративное веб-приложение (Micro Frontend)
- **Базовая технология**: React 16.14.0 + TypeScript 5.2.2
- **Архитектура**: Module Federation + Dependency Injection (Inversify)
- **Управление состоянием**: MobX 5.15.4 с классовыми сторами
- **UI-библиотека**: Ant Design 4.17.2 + `@sber-sbertransport/ui-kit`
- **Стилизация**: Less + Styled Components + Sass
- **Серверный менеджмент запросов**: React Query 2.26.4
- **Сборка**: CRACO (Create React App Configuration Override)
- **Тестирование**: Jest + ts-jest + Testing Library
- **Контейнеризация**: Docker на базе SberNGX (sngx/sbel9)

---

## Архитектура

### Micro Frontend Architecture

Проект использует **Module Federation** через `@sber-sbertransport/mf-core` для разделения на автономные микрофронтенды. Конфигурация хранится в `mf.json`:

**Основные микрофронтенды (remotes):**
- `auth` - Аутентификация
- `platform` - Платформенные сервисы
- `cargo` - Грузовые перевозки
- `passengers` - Пассажирские перевозки
- `fleet` - Автопарк

**Shared зависимости** (синхронизируются между remotes):
- `react`, `react-dom`, `react-router-dom`
- `styled-components`
- `@sber-sbertransport/mf`, `@sber-sbertransport/mf-core`
- `@sber-sbertransport/ui-kit`

### Структура проекта

```
/
├── .babelrc                    # Babel конфигурация (декораторы MobX/Inversify)
├── .editorconfig               # Общие настройки редактора
├── .env                        # Переменные окружения (не в git)
├── .env.development            # Dev-переменные окружения
├── .env.local                  # Локальные переменные разработчика
├── .nvmrc                      # Версия Node.js
├── .yarnrc                     # Конфиг Yarn
├── commitlint.config.js        # Конвенция коммитов
├── devsecops-config.yml        # DevSecOps конфигурация
├── Dockerfile                  # Docker образ (порт 8085)
├── eslint.config.js            # ESLint конфигурация
├── jest.config.ts              # Jest конфигурация
├── lefthook.yml                # Git hooks
├── mf.json                     # Module Federation конфигурация
├── nginx.conf                  # Nginx конфигурация
├── package.json                # Зависимости и скрипты
├── sonar-project.properties    # SonarQube
├── tsconfig.json               # TypeScript конфигурация
├── version.json                # Версия приложения
├── version.sh                  # Скрипт версионирования
├── public/                     # Статические файлы
├── scripts/                    # Вспомогательные скрипты сборки
└── src/
    ├── api/                    # API-сервисы (axios клиенты)
    ├── components/             # Переиспользуемые UI-компоненты
    ├── constants/              # Константы приложения
    ├── context/                # React Context провайдеры
    ├── fonts/                  # Шрифты
    ├── i18n/                   # Интернационализация
    ├── ioc/                    # Dependency Injection (Inversify)
    │   ├── ioc.container.ts    # DI контейнер
    │   ├── ioc.stores.ts       # Инициализация сторов
    │   ├── ioc.types.ts        # Типы DI
    │   ├── ioc.hooks.ts        # Хуки DI
    │   ├── ioc.context.ts      # DI контекст
    │   ├── ioc.errors.ts       # DI ошибки
    │   ├── ioc.storeNames.ts   # Имена сторов
    │   └── types.ts            # Общие типы
    ├── mf/                     # Module Federation runtime
    ├── modules/                # Бизнес-модули
    │   ├── Autopark/
    │   ├── BusinessReports/
    │   ├── Customers/
    │   ├── Home/
    │   ├── ImportReport/
    │   ├── OrderExecution/
    │   ├── Page404/
    │   ├── PlannerSRM/
    │   ├── RedesignHome/
    │   ├── Registry/
    │   ├── Reports/
    │   ├── ServiceSettings/
    │   ├── TariffSettings/
    │   └── TripSettings/
    ├── shared/                 # Общие ресурсы (утилиты, компоненты, стили)
    ├── stores/                 # MobX сторы
    ├── styles/                 # Глобальные стили (Less)
    ├── ui/                     # UI-библиотека компонентов
    ├── utils/                  # Утилиты
    ├── App.tsx                 # Корневой компонент
    ├── AppProvider.tsx         # Провайдер приложения
    ├── AppRouter.tsx           # Основной роутер
    ├── AuthRouter.tsx          # Роутер авторизации
    ├── Main.tsx                # Entry компонент
    ├── bootstrap.tsx           # Bootstrap
    ├── index.ts                # Entry point
    └── setupTests.ts           # Настройка тестов
```

### Управление состоянием (MobX + Inversify DI)

Проект использует **Inversify** для инъекции сторов:

```typescript
// Пример стора
class DIHomeStore {
  @observable data: IHomeData[] = [];

  @action.bound
  loadData() { /* ... */ }

  @computed get processedData() { /* ... */ }
}
```

**Основные сторы:**
- `homeStore` - Данные главной страницы
- `employeeStore` - Информация о сотрудниках
- `organizationsStore` - Организации
- `organizationsGroupStore` - Группы организаций
- `paginationStore` - Пагинация
- `transportServiceTypesStore` - Типы транспортных услуг
- `transportTypesStore` - Типы транспорта

### Маршрутизация

Используется `react-router-dom@5.3.0` с кастомным `RouterProvider` через контекст.

---

## Сборка и запуск

### Предварительные требования

- **Node.js**: `>=18.0.0` (использовать `nvm use`)
- **Пакетный менеджер**: Yarn (строго по `yarn.lock`)
- **CRACO**: для override конфигурации CRA

### Установка зависимостей

```bash
# Строгая установка по yarn.lock (рекомендуется для CI)
yarn ci

# Альтернативно
yarn install --frozen-lockfile

# Полная переустановка (CI style)
yarn ci  # эквивалентно rm -rf node_modules && yarn install --frozen-lockfile
```

### Переменные окружения

**Основные переменные (`.env`):**
- `REACT_APP_NAME` - Имя микрофронта (main)
- `REACT_APP_MF_LINK` - Ссылка на микросервис (заполняется автоматически через `yarn create:mf-json`)
- `REACT_APP_DEV_AS_PROD` - Использовать продовые URL в development (`true`/`false`)

**Типы сред (`.env.development`):**
- `REACT_APP_NETWORK_LOOP` - Тип среды: `DEV_AUTOPARK`, `DEV_AUTOSERVICE`, `DEV_CARGO`, `DEV_SBERTRANSPORT`, `ST`, `NT`, `IFT`

**Дополнительные переменные:**
- `REACT_APP_BASIC_AUTH` - Включить Basic-auth (`TRUE`)
- `REACT_APP_MOCKED_AUTH` - Мокать аутентификацию (`TRUE`)
- `REACT_APP_MOCKED_API` - Мокать API (`TRUE`)
- `REACT_APP_DEBUG` - Режим отладки (`TRUE`)

### Запуск в development

```bash
# Базовый запуск
yarn start
yarn start:dev

# Запуск для конкретного направления
yarn start:dev-autopark          # Судир авторизация
yarn start:dev-autoservice
yarn start:dev-cargo
yarn start:dev-sbertransport
yarn start:dev-st                # ST (System Testing)
yarn start:dev-nt                # NT (Negative Testing)
yarn start:dev-ift               # IFT (Integration Testing)

# С Basic-авторизацией
yarn start-auth:dev-cargo

# С моками auth/API
yarn start-auth-mocked-login:dev-cargo        # Мок авторизации
yarn start-auth-mocked-api:dev-cargo          # Мок API
yarn start-auth-mocked-login-api:dev-cargo    # Моки auth + API
```

### Сборка для production

```bash
# Основная сборка
yarn build

# Сборка с Basic-авторизацией
yarn build:basic

# Сборка с отладкой
yarn build:debug
yarn build:basic:debug

# Сборка для Sigma (специальный конфиг)
yarn build:sigma
```

Сборка выполняет:
1. `yarn create:mf-json` - генерация `mf.json` для production
2. `sh version.sh` - обновление `version.json`
3. `craco build` - сборка через CRACO

### Очистка

```bash
# Удаление node_modules
yarn clean

# Очистка кэша (webpack/jest)
yarn clean:cache

# Очистка кэша Jest
yarn test:clear
```

### Обновление зависимостей

```bash
# Обновить основные зависимости микрофронтенда SberTransport
yarn up

# Полная пересборка (CI style)
yarn ci
```

---

## Линтинг и тестирование

### Линтинг ESLint

```bash
# Проверка
yarn lint

# Исправление
yarn lint:fix
```

**Конфигурация**: `eslint.config.js` с расширением из `@sber-sbertransport/tool-kit/tslint`

### Тестирование Jest

```bash
# Запуск тестов
yarn test

# Очистка кэша Jest
yarn test:clear

# CI-тесты с покрытием
yarn test:ci
```

**Конфигурация**: `jest.config.ts`
- **Preset**: `ts-jest`
- **Environment**: `jsdom`
- **Workers**: до 5 параллельно
- **Timeout**: 50 секунд
- **Покрытие**: V8 (отчёты: `text`, `lcov`, `clover`, `cobertura` для SonarQube)
- **Моки файлов**: `__mocks__/fileMock.js`, `styleMock.js`, `svgMock.jsx`

**Исключения из coverage:**
- `src/api/**` - API слой
- `src/app/**` - Корневая конфигурация
- `src/context/**`, `src/Context/**` - Context провайдеры
- `src/i18n/**` - Интернационализация
- `src/ioc/**` - Dependency Injection
- `src/mf/**` - Module Federation
- `src/Services/**` - Сервисы
- `src/styles/**` - Стили
- `**/*.d.ts`, `**/*.config.*`, `**/*.setup.*`
- `**/constants/**`, `**/types/**`, `**/interfaces/**`
- `**/__mocks__/**`, `**/__fixtures__/**`, `**/__tests__/**`
- Тестовые файлы (`*.test.*`, `*.spec.*`, `*.stories.*`)

---

## Git hooks и CI/CD

### Lefthook (pre-commit/pre-push)

Конфигурация: `lefthook.yml` (расширяется из `@sber-sbertransport/tool-kit/lefthook`)

**Хуки:**
- `commit-msg` - Проверка на соответствие Conventional Commits (commitlint)
- `pre-commit` - ESLint проверка staged файлов (`yarn lint`)
- `pre-push` - Проверка `package.json` и версии Node.js (`package-ok.js`, `node-version.js`)

### Conventional Commits

Формат: `type(scope): message`

**Примеры:**
```
feat(auth): добавить поддержку SSO
fix(cargo): исправить расчет времени доставки
refactor(api): рефакторинг клиентов axios
docs: обновить README
```

**Issue префиксы:** `TRANSPORT-` (настраивается в `commitlint.config.js`)

### Docker

**Dockerfile** использует:
- Базовый образ: `sngx/sbel9` (SberNGX)
- Порт: `8085`
- Путь к build: `build/` (создается после `yarn build`)
- Entrypoint: `syngx -g daemon off`

---

## Стили и CSS

### Технологии

- **Less** - основной препроцессор (`craco-less`)
- **Styled Components** 5.3.3 - для динамических стилей компонентов
- **Sass** - дополнительно
- **CSS Modules** - через `typescript-plugin-css-modules`

### Стилевая конвенция

Проект использует **CSS-in-JS** подход с приоритетом:
1. Ant Design компоненты (использовать `className` для переопределения)
2. Styled Components для специфичной стилизации
3. Less классы для глобальных стилей

### БЭМ и нейминг

Проект следует принципам БЭМ с модификаторами через классы Ant Design или `className`.

---

## Работа с API

### Структура API-сервисов

```
src/api/
├── index.ts               # Инициализация клиентов axios
├── profile.ts             # Профиль пользователя
├── tariffs.ts             # Тарифы
├── transport-types.ts     # Типы транспорта
├── executor-group.ts      # Сервисы исполнителей
├── departments/           # Подразделения
└── organizations/         # Организации
```

### Использование

**Данные из API поступают в:**
1. **MobX сторы** - для состояния приложения
2. **React Query** - для кэширования и управления запросами

```typescript
// В сторе
class SomeStore {
  @observable data: DataType[] = [];

  @action
  fetchData = async () => {
    try {
      const response = await apiService.getData();
      this.data = response.data;
    } catch (error) {
      // Обработка ошибок
    }
  }
}
```

---

## Internationalization (i18n)

Проект имеет папку `src/i18n/` для многоязычной поддержки. Текущая локаль по умолчанию - `ru` (через `moment/locale/ru`).

---

## Инструменты и библиотеки

### Основные зависимости

| Библиотека | Версия | Назначение |
|-----------|--------|-----------|
| `react` | 16.14.0 | Основная библиотека |
| `react-dom` | 16.14.0 | React DOM рендеринг |
| `mobx` | ^5.15.4 | Управление состоянием |
| `mobx-react` | ^6.1.8 | Интеграция MobX с React |
| `react-router-dom` | 5.3.0 | Маршрутизация |
| `antd` | 4.17.2 | UI-библиотека |
| `axios` | ^0.19.2 | HTTP-клиент |
| `react-query` | 2.26.4 | Управление запросами |
| `ramda` | ^0.27.1 | Функциональные утилиты |
| `fp-ts` | ^2.6.3 | Функциональное программирование |
| `io-ts` | ^2.2.4 | Валидация типов |
| `io-ts-reporters` | ^1.2.0 | Отчёты ошибок io-ts |
| `inversify` | ^5.0.1 | DI-контейнер |
| `reflect-metadata` | ^0.2.1 | Метаданные для декораторов |
| `d3` | ^6.7.0 | Визуализация данных |
| `classnames` | ^2.2.6 | Условные CSS классы |
| `sass` | ^1.71.0 | CSS препроцессор |
| `styled-components` | 5.3.3 | CSS-in-JS |
| `express` | ^4.17.3 | SSR/прокси |
| `react-scripts` | 5.0.1 | CRA под капотом |
| `react-fontawesome` | ^0.1.9 | Иконки |
| `eslint` | 8.56.0 | Линтер |

### Библиотеки SberTransport

| Библиотека | Версия | Назначение |
|-----------|--------|-----------|
| `@sber-sbertransport/mf-core` | ^2.0.22 | Micro Frontend core |
| `@sber-sbertransport/ui-kit` | ^2.0.14 | UI-компоненты |
| `@sber-sbertransport/tool-kit` | ^3.2.4 | Утилиты, конфиги, lefthook |
| `@sber-sbertransport/ai-agent` | ^0.8.0 | AI-интеграции |

### Инструменты разработки (devDependencies)

| Библиотека | Назначение |
|-----------|-----------|
| `typescript` 5.2.2 | TypeScript компилятор |
| `@craco/craco` 6.4.3 | Override CRA конфигурации |
| `craco-less` 2.0.0 | Less поддержка в CRACO |
| `jest` 29.7.0 | Тест-раннер |
| `ts-jest` 29.4.0 | Jest + TypeScript |
| `@testing-library/react` 11.2.7 | React тесты |
| `less` / `less-loader` | Less компиляция |
| `babel-plugin-styled-components` | SSR/дебаг styled-components |
| `@evilmartians/lefthook` | Git hooks |
| `cross-env` | Кросс-платформенные env переменные |

### Resolutions (фиксация версий)

```json
{
  "styled-components": "^5.3.0",
  "webpack-dev-server": "4.15.1"
}
```

---

## TypeScript конфигурация

`tsconfig.json`:
- `baseUrl: ./src` - базовый путь для импортов
- `target: es5`, `module: esnext`
- `jsx: react`
- `strict: true`
- `experimentalDecorators: true` + `emitDecoratorMetadata: true` - для MobX/Inversify
- `esModuleInterop: true`
- `moduleResolution: node`
- `isolatedModules: true`
- `noEmit: true` (используется только для проверки типов)

---

## Краткая справка по командам

| Команда | Описание |
|---------|----------|
| `nvm use` | Переключить версию Node.js |
| `yarn ci` | Установка по lock-файлу |
| `yarn start:dev-cargo` | Запуск dev для Cargo |
| `yarn start-auth:dev-cargo` | Запуск с Basic auth |
| `yarn start-auth-mocked-login-api:dev-cargo` | Полные моки |
| `yarn build` | Production сборка |
| `yarn build:debug` | Сборка с отладкой |
| `yarn lint` | Проверка ESLint |
| `yarn lint:fix` | Исправление ESLint |
| `yarn test` | Запуск Jest |
| `yarn test:ci` | CI-тесты с покрытием |
| `yarn up` | Обновить SberTransport пакеты |
| `yarn clean` | Удалить node_modules |
| `yarn clean:cache` | Очистить кэш webpack |
| `yarn create:mf-json` | Перегенерировать mf.json |

---

## Ревью кода

Смотрите подробные критерии в `instructions.md` и `reviewer-config.yml`. Основные требования:

1. **TypeScript** - строгая типизация (`strict: true`)
2. **MobX** - классовые сторы с `@observable`, `@action`, `@computed`
3. **React** - функциональные компоненты, хуки
4. **Ant Design** - использовать компоненты библиотеки
5. **Безопасность** - XSS, CSRF, инъекции
6. **Производительность** - мемоизация, оптимизация ререндеров
7. **Читаемость** - осмысленные имена, маленькие функции

---

## Частые проблемы и решения

### Проблемы с версиями

Всегда используйте `nvm use` для переключения версии Node.js (`.nvmrc`).

### Проблемы с зависимостями

```bash
# Если сломались зависимости
yarn clean
yarn ci

# Пересборка mf.json (Micro Frontend config)
yarn create:mf-json
```

### Проблемы с линтингом

```bash
# Очистка кэша ESLint
rm -rf node_modules/.cache
```

### Проблемы с Module Federation

`mf.json` генерируется автоматически через `yarn create:mf-json`. Если remotes не подгружаются - проверить `mf.json` и доступность URL.

---

## Контакты и документация

- **Конфигурация**: `package.json`, `mf.json`, `scripts/`
- **Логика**: `src/` (см. структуру выше)
- **Инструкция для ревьюера**: `instructions.md`, `reviewer-config.yml`
- **Конфиги линтинга**: `commitlint.config.js`, `eslint.config.js`, `lefthook.yml`
- **Docker**: `Dockerfile`, `nginx.conf`
- **CI/CD**: `devsecops-config.yml`, `sonar-project.properties`

---

## GigaCode Added Memories

- В проекте `/Users/20715715/Desktop/Projects/delta/front_corp`:
  1. **Архитектура**: Micro Frontend (Module Federation) приложение `main` объединяет remotes: `auth`, `platform`, `cargo`, `passengers`, `fleet`.
  2. **Стек**: React 16.14.0 + TypeScript 5.2.2 + MobX 5.15.4 + Inversify DI + Ant Design 4.17.2 + Styled Components.
  3. **Бизнес-модули** (`src/modules/`): Autopark, BusinessReports, Customers, Home, ImportReport, OrderExecution, Page404, PlannerSRM, RedesignHome, Registry, Reports, ServiceSettings, TariffSettings, TripSettings.
  4. **DI/IoC** (`src/ioc/`): контейнер, сторы, типы, хуки, контекст - всё через Inversify.
  5. **Сборка**: CRACO + `yarn create:mf-json` для генерации mf.json; продакшен на SberNGX (порт 8085).
  6. **Тесты**: Jest + ts-jest + jsdom, coverage V8 с исключениями api/ioc/context/mf/styles.
  7. **Git hooks**: lefthook (commitlint conventional commits + eslint staged + package/node версии).
  8. **Environments**: `REACT_APP_NETWORK_LOOP` ∈ {DEV_AUTOPARK, DEV_AUTOSERVICE, DEV_CARGO, DEV_SBERTRANSPORT, ST, NT, IFT}, плюс `REACT_APP_BASIC_AUTH`, `REACT_APP_MOCKED_AUTH`, `REACT_APP_MOCKED_API`, `REACT_APP_DEBUG`.
