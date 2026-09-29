# GIGACODE.md - Контекст проекта

## Проект overview

**Passengers** — это frontend-микросервис для транспортной платформы СберТранспорт, реализующий функционал для пассажиров (поездки, транспорт, подтверждения, бонусы и т.д.). Проект использует архитектуру Micro Frontends (MFE) и интегрируется с основным приложением как Remote Module.

### Ключевые технологии

| Категория | Технологии |
|-----------|------------|
| **Библиотеки** | React 16.13.1+, TypeScript 5.2.2, Ant Design 4.17.2 |
| **State Management** | MobX 5.15.4, MobX React 6.1.8, mobx-logger 0.7.1 |
| **Фетч дата** | react-query 2.25.2, axios 0.19.2 |
| **Роутинг** | react-router-dom 5.3.0 |
| **Стили** | styled-components 5.3.3, Less, css-modules |
| **Формы** | Ant Design Forms, иммутабельные модели |
| **Утилиты** | io-ts 2.2.4, fp-ts 2.6.3, ramda 0.27.1, lodash 4.17.15 |
| **Карты** | OpenLayers 8.2.0 (ol), Leaflet 1.6.0 + react-leaflet 2.6.3 |
| **Моки API** | MirageJS 0.1.41 |
| **UI Kit** | @sber-sbertransport/ui-kit 2.0.14 |
| **Tool Kit** | @sber-sbertransport/tool-kit 3.2.4 (базовая конфигурация) |

---

## Сборка и запуск

### Требования

- **Node.js**: v14.18.2 (указана в `.nvmrc`)
- **Менеджер пакетов**: yarn (строго по `yarn.lock`)

```bash
nvm use
yarn ci  # или: yarn install --frozen-lockfile
```

### Команды для разработки

#### Базовые команды

| Команда | Описание |
|---------|----------|
| `yarn start` | Запуск как Remote Module (по умолчанию) |
| `yarn build` | Сборка для продакшена |
| `yarn build:debug` | Сборка с отладочными флагами |
| `yarn lint` / `yarn lint:fix` | ESLint проверка / автофикс |
| `yarn test` / `yarn test:ci` | Юнит-тесты с coverage |
| `yarn clean` / `yarn clean:cache` | Очистка node_modules / кэша |

#### Запуск с разными конфигурациями сетевых циклов

Сервер разработки запускается с переменной `REACT_APP_NETWORK_LOOP`:

| Переменная | Окружение |
|------------|-----------|
| `DEV_AUTOPARK` | Девелоперский стенд автопарк |
| `DEV_AUTOSERVICE` | Девелоперский стенд автосервис |
| `DEV_CARGO` | Девелоперский стенд грузы |
| `DEV_SBERTRANSPORT` | Девелоперский стенд СберТранспорт |
| `ST` | Стенд тестирования |
| `NT` | Нагрузочный стенд |
| `IFT` | Интеграционный фид тест |

**Примеры:**
```bash
yarn start:dev-autopark        # Как HOST (Main) на стенде автопарк
yarn start:dev-autoservice     # Как HOST на стенде автосервис
yarn start:dev-cargo           # Как HOST на стенде грузы
yarn start:dev-sbertransport   # Как HOST на стенде СберТранспорт
yarn start:dev-st              # Как HOST на стенде тестирования
```

#### Режимы аутентификации

Комбинации с переменными авторизации:

| Команда | Режим |
|---------|-------|
| `yarn start-auth:dev-autopark` | Basic Auth |
| `yarn start-auth-mocked-login:dev-autopark` | Mocked Auth (имитация входа) |
| `yarn start-auth-mocked-api:dev-autopark` | Basic Auth + моки API |
| `yarn start-auth-mocked-login-api:dev-autopark` | Mocked Auth + моки API |

**Пример полной команды:**
```bash
yarn start-auth-mocked-login-api:dev-autopark
```

### Сборка и деплой

```bash
# Запуск сборки через скрипт
sh build.sh

# Вручную (для.debug)
yarn build
yarn build:debug
```

Сборка пушится в Docker Registry Sigma и деплоится через SyNgx.

---

## Архитектура проекта

### Структура директорий

```
src/
├── api/              # API сервисы (axios запросы)
├── app/              # Конфигурации для dev/prod окружений
│   ├── dev/          # Local development entry
│   └── prod/         # Production entry (Remote Module)
├── constants/        # Константы (env, routes, enums)
├── context/          # React Context
├── ioc/              # Inversify DI контейнер
├── libs/             # Библиотеки/утилиты
├── mf/               # Micro Frontends конфигурация
├── modules/          # Модули приложения
│   └── EmployeeApp/  # Основной модуль
├── shared/           # Общие компоненты и утилиты
│   ├── components/   # UI компоненты
│   ├── decorators/   # Декораторы
│   ├── form/         # Формы и валидация
│   ├── hooks/        # Кастомные хуки
│   ├── models/       # Модели данных
│   └── ui/           # UI primitives
├── stores/           # MobX сторы состояния
│   ├── Address/
│   ├── Approvals/
│   ├── Corporate/
│   ├── Delegates/
│   ├── EmployeeExt/
│   ├── Files/
│   ├── Fraud/
│   ├── Geo/
│   ├── Limits/
│   ├── Mapped/
│   ├── Purpose/
│   ├── Request/
│   ├── Response/
│   ├── Settings/
│   ├── SRMMassMultiple/
│   ├── TransportTypes/
│   └── Trip/
└── utils/            # Утилиты (format, geo, trips, и т.д.)
```

### Основные модули

| Модуль | Описание |
|--------|----------|
| `EmployeeApp` | Основной модуль с страницами: поездки, транспорт, подтверждения, бонусы, избранные адреса, личные машины |
| `Approvals` | Страница подтверждений заявок |
| `Trips` | Список и создание поездок |
| `Bonuses` | Страница бонусов |
| `Vehicles` | Управление транспортом (все машины + личные) |
| `FavoriteAddressPage` | Избранные адреса |

---

## Разработка

### Стили и форматирование

- **TypeScript**: строгая типизация (`strict: true`)
- **ESLint**: настроен через `@sber-sbertransport/tool-kit`
- **Стили**: смешанный подход — styled-components + Less + CSS Modules
- **Декораторы**: включены через Babel (legacy decorators)

### Управление состоянием (MobX)

- Классовые сторы с декораторами `@observable`, `@action`, `@computed`
- Использование `runInAction` для асинхронных операций
- `mobx-logger` для логирования изменений

### API взаимодействие

- Сервисы в `src/api/` инкапсулируют axios запросы
- Интеграция с `@sber-sbertransport/mf-core` для микросервисной архитектуры
- Поддержка моков через MirageJS и кастомный router `/api/mock`

### Тестирование

- **Фреймворк**: Jest + ts-jest
- **Типы тестов**: unit тесты для компонентов, утилит, мемоизированных функций
- **Покрытие**: CI покрывает все `src/**/*.{js,ts,tsx}` (исключая api, constants, types, mocks)
- **Конфиг**: `jest.config.ts` с coverage в sonar-report.xml

### Коммиты и Git Hooks

- **Конвенция коммитов**: conventional commits (через `@sber-sbertransport/tool-kit`)
- **Префикс issue**: `TRANSPORT-`
- **Hooks**: Lefthook с проверкой коммитов и линтингом
- **Commitlint config**: `commitlint.config.js`

---

## Дополнительные инструменты

### Docker

- Базовый образ: `sngx/sbel9` (SyNgx)
- Сборка: `sh build.sh` → push в Sigma Registry
- DEPLOY_MESSAGE формируется автоматически с Git branch и последними коммитами

### Интеграции

- **SonarQube**: `sonar-project.properties` для анализа кода
- **DevSecOps**: `devsecops-config.yml`
- **Versioning**: `version.sh` и `version.json`

---

## Важные константы

| Константа | Описание |
|-----------|----------|
| `IS_REMOTE` | Режим работы (Remote Module vs HOST) |
| `REACT_APP_NAME` | Имя микросервиса (`passengers`) |
| `REACT_APP_BASIC_AUTH` | Включить Basic Auth |
| `REACT_APP_MOCKED_AUTH` | Включить моки авторизации |
| `REACT_APP_MOCKED_API` | Включить моки API |

---

## Ключевые паттерны проекта

1. **Слоистая архитектура**: Компоненты → Stores → Services → API
2. **Инверсия зависимостей**: Inversify DI в `src/ioc/`
3. **Функциональный подход**: io-ts для валидации, ramda/fp-ts для обработки данных
4. **Модульность**: Изоляция модулей через `src/modules/`
5. **Контекстное рендеринг**: Breadcrumbs, навигация через `react-router-dom`
6. **Виртуализация**: react-window для длинных списков (Table)

---

## Примечания для AI-ассистента

- Весь UI строится на **Ant Design v4** и `@sber-sbertransport/ui-kit`
- Сторы — классовые MobX с декораторами (не UseState!)
- API вызовы только через сервисы в `src/api/`
- Код должен быть строго типизирован (всегда явно указывайте типы)
- Используйте `ts-jest` для тестов, `craco` для конфигурации Webpack
- Правила ESLint: `@sber-sbertransport/tool-kit` + кастомные оффы в `eslint.config.js`
- Работа с библиотекой: `@sber-sbertransport/mf-core` для MFE коммуникации
