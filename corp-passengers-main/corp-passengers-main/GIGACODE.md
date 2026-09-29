# GIGACODE.md — Контекст проекта

## 📋 Проект Overview

**Corp frontend microservice "Passengers"** — это фронтенд-микросервис для корпоративной системы управления пассажирскими перевозками, входящий в экосистему Sber SberTransport. Проект построен как микроприложение (micro-frontend) на базе архитектуры, совместимой с `@sber-sbertransport/mf-core`.

**Основной стек:** React 16.14, TypeScript, MobX, Ant Design v4, React Router v5, react-query v2, Styled Components.

**Среда выполнения:** Node.js >= 18, сборка через CRA + CRACO, деплой через Nginx.

---

## 🏗️ Архитектура и структура

### Архитектурные принципы
- **Микроприложение (Micro-Frontend):** Упаковывается как независимое SPA для интеграции в платформу через `mf-core`
- **Разделение окружений:** Отдельные конфигурации для `dev` (модульный роутинг) и `prod` (стандартная маршрутизация)
- **DI-контейнер:** Использование `inversify` и `reflect-metadata` для управления зависимостями
- **MobX** — управление состоянием (классовые сторы с `makeObservable`/`@action`/`@computed`)
- **react-query v2** — кэширование и синхронизация данных с сервером

### Структура исходного кода (`src/`)

```
src/
├── api/              # Слой API — конфигурация запросов, кастомные хуки useAPI/useAPIMutation
├── app/              # Точки входа (dev, prod)
│   ├── dev/         # Режим разработки с расширенным роутингом
│   └── prod/        # Режим продакшена
├── components/       # UI компоненты
├── constants/        # Константы (роуты, эндпоинты, конфиги)
├── context/          # React Context провайдеры
├── i18n/             # Международная локализация
├── ioc/              # Inversion of Control — контейнер зависимостей и сторы
├── mf/               # Конфигурация micro-frontend (remote entry)
├── modules/          # Бизнес-модули (по функциональности):
│   ├── Registry, OrderExecution, BusinessReports
│   ├── Planner, FraudMonitoring, ExecutorGroup и др.
├── shared/           # Общие утилиты, хуки, формы, чарты
├── stores/           # Сторы MobX (альтернативный путь)
├── ui/               # UI Kit компоненты (Ant Design надстройки)
├── utils/            # Вспомогательные функции (fp-ts, ramda)
├── styles/           # Глобальные стили, темы
└── fonts/            # Шрифты
```

---

## 🛠️ Сборка и запуск

### Установка зависимостей

Строго по `yarn.lock` (заблокирована конфликтующая зависимость `styled-components@5.3.3`):

```bash
yarn ci          # или yarn install --frozen-lockfile
```

### Сценарии разработки

Серия скриптов для запуска с разными конфигурациями окружения (`REACT_APP_NETWORK_LOOP`) и типами авторизации:

| Скрипт | Описание |
|--------|----------|
| `yarn start` | REMOTE режим (как микросервис) |
| `yarn start:dev` | HOST режим (локальный сервер, без NETWORK_LOOP) |
| `yarn start:dev-autopark` | Для подключения к dev-стенду autopark |
| `yarn start:dev-autoservice` | Для подключения к dev-стенду autoservice |
| `yarn start:dev-cargo` | Для подключения к dev-стенду cargo |
| `yarn start:dev-sbertransport` | Для подключения к dev-стенду sbertransport |
| `yarn start:dev-st` | Для подключения к ST (System Test) |
| `yarn start:dev-nt` | Для подключения к NT (New Test) |
| `yarn start:dev-ift` | Для подключения к IFT (Integration Test) |

### Конфигурации авторизации (кombинации с `start-auth*`)

| Префикс | Логика |
|---------|--------|
| `start-auth:*` | Basic Auth (через заголовки) |
| `start-auth-mocked-login:*` | Моканая авторизация (имитация входа) |
| `start-auth-mocked-api:*` | Basic Auth + моки для API-запросов |
| `start-auth-mocked-login-api:*` | Моканая авторизация + моки для API |

Примеры:
```bash
yarn start-auth:dev-autopark
yarn start-auth-mocked-login-api:dev-autoservice
```

### Сборка продакшена

```bash
yarn build              # Сборка для REMOTE режима
yarn build:debug        # Сборка с режимом отладки
yarn build:sigma        # Сборка в режиме sigma (стандарт)
```

Версия автоматически генерируется через `version.sh`.

### Деплой

Docker-контейнер на базе Nginx (порт 8085):

```bash
docker build -t passengers .
docker run -p 8085:8085 passengers
```

---

## 🧪 Тестирование

```bash
yarn test           # Запуск Jest
yarn test:clear     # Очистка кэша Jest
yarn test:ci        # Запуск с coverage (CI режим)
```

Конфигурация в `jest.config.ts` — JS DOM окружение + ts-jest.

---

## 🎨 Разработка и стилистика

### TypeScript

- **`strict: true`** — включены строгие проверки
- **`noImplicitAny: false`** — временно выключено (необходимо включить постепенно)
- **Декораторы** — включены `experimentalDecorators` и `emitDecoratorMetadata`
- **ES5 target** — для совместимости со старыми браузерами

> **Важно:** Код должен использовать TypeScript с явной типизацией. Избегать `any`, использовать `unknown` или проверку типов.

### Стили

- **Primary:** Styled Components (v5.3.3)
- **Fallback:** Less / Ant Design theme
- Плагин `craco-less` для интеграции Less в CRA
- CSS-модули через `typescript-plugin-css-modules`

### Линтинг и форматирование

- **ESLint 8.56** — настройки импортированы из `@sber-sbertransport/tool-kit`
- **Conventional Commits** — через `commitlint` и `lefthook`
- **Хуки Git:**
  - `pre-commit`: ESLint для staged файлов
  - `commit-msg`: проверка формата коммита
  - `pre-push`: валидация package и версии Node

Команды:
```bash
yarn lint      # ESLint src/
yarn lint:fix  # ESLint + auto-fix
```

---

## 📦 Зависимости

### Core (production)

| Пакет | Версия / Примечание |
|-------|---------------------|
| React | 16.14.0 |
| React DOM | 16.14.0 |
| TypeScript | 5.2.2 |
| MobX | 5.15.4 + mobx-react@6.1.8 |
| Ant Design | 4.17.2 |
| Styled Components | 5.3.3 (заблокирован в resolutions) |
| react-router-dom | 5.3.0 |
| react-query | 2.26.4 (устаревшая версия!) |
| @sber-sbertransport/mf-core | ^2.0.22 |
| @sber-sbertransport/ui-kit | ^2.0.14 |
| @sber-sbertransport/tool-kit | ^3.2.4 |

### Utility libraries

- **fp-ts**, **io-ts**, **ramda**, **polished** — функциональное программирование и утилиты
- **axios**, **ol** (OpenLayers), **leaflet** + react-leaflet — карты
- **moment-timezone** — работа со временем
- **d3**, **recharts** — графики и визуализация

> **Обратите внимание:** Проект использует `react-query v2` — API отличается от v3/v4 (другие хуки и параметры кэширования).

---

## 🔐 Безопасность и авторизация

### Поддерживаемые режимы

1. **Basic Auth** — передача credentials через HTTP заголовки
2. **Mocked Auth** — имитация входа (для разработки без backend)
3. **Mocked API** — перехват запросов к API локальными моками

### Работа с API

Сервисы предоставляются через DI-контейнер:
- `http` — экземпляр HTTP-сервиса (`IHttpService`)
- `process` — response service (`ResponseService`)
- `logger` — логгер (`ILogger`)

Кастомные хуки для API:
- `useAPI(key, queryFn)` — GET запросы с кэшированием
- `useAPIMutation(mutationFn, config)` — POST/PUT/DELETE

---

## 🧩 Микросервисная архитектура

Проект работает как **remote** micro-frontend. Ключевые файлы:

- `src/mf/` — конфигурация для микросервиса
- `scripts/` — скрипты сборки (CRACO конфиг)

Совместимость с платформой обеспечивается через `@sber-sbertransport/mf-core`.

---

## 📝 Ревью и качество кода

См. файл `instructions.md` для детального гайда по проверке кода AI-ревьюером.

Ключевые критерии:

| Категория | Требование |
|-----------|------------|
| **Типизация** | Строгий TypeScript, нет `any` без оправдания |
| **State** | MobX (классы с `observable`/`action`/`computed`) |
| **UI** | Ant Design v4 / @sber-sbertransport/ui-kit |
| **API** | react-query или MobX сторы (не напрямую в компонентах) |
| **Архитектура** | Четкое разделение: компоненты → сторы → сервисы API |
| **Производительность** | Мемоизация (`React.memo`, `useMemo`, `useCallback`), виртуализация списков |

---

## 🚀 Быстрый старт

```bash
# 1. Установка Node.js через nvm
nvm use

# 2. Установка зависимостей
yarn ci

# 3. Запуск для разработки (mocked login + API)
yarn start-auth-mocked-login-api:dev-autopark

# 4. Ревью кода
yarn lint

# 5. Тестирование
yarn test
```

---

## 📚 Ключевые концепции

### DI-контейнер (`src/ioc/`)

```typescript
// Доступ к сервисам
const { http, process, logger } = useAppStoreContext();

// Сторы через DI
container.get<SomeStore>(TYPES.SomeStore);
```

### API-хуки

```typescript
// Запрос данных
const { data, isLoading, error } = useAPI(
  'USERS_LIST',
  ({ http }) => http.get('/api/users')
);

// Мутация
const [mutate, { loading }] = useAPIMutation(
  ({ http }, payload) => http.post('/api/user', payload),
  {
    onSuccess: ({ cache, result }) => {
      cache.setQueryData('USERS_LIST', result);
    }
  }
);
```

### Маппинг роутов

Базовые роуты определены в `src/constants/constants.routes`. Для разработки используется `AppRouter` в `src/app/dev/AppRouter.tsx`, который перекрывает и расширяет `ProdRouter`.

---

## 🛑 Известные особенности

- `noImplicitAny: false` — временное отключение, требует миграции
- `react-query v2` — устаревшая версия, переход на v3 требует переписывания хуков
- Styled Components версия жестко заблокирована на `5.3.3`
- Проект не использует `eslintrc.json`, правила передаются через JS API `@sber-sbertransport/tool-kit`

---

## 📄 Лицензия

См. `LICENSE.txt` (не включен в репозиторий)

---

*Этот файл сгенерирован автоматически для предоставления контекста AI-ассистенту. Обновляйте его при существенных изменениях архитектуры.*
