# Dispatcher frontend microservices "Platform"

[![Node.js](https://img.shields.io/badge/Node.js-%3E=18.0.0-green.svg)](https://nodejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2.2-blue.svg)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-16.14.0-blue.svg)](https://reactjs.org/)
[![License](https://img.shields.io/badge/License-proprietary-red.svg)](LICENSE.txt)

> Микросервис "Platform" — это фронтенд-компонента для диспетчерской системы SberTransport, построенная по принципам Module Federation и микросервисной архитектуры. Включает в себя пассажирскую диспетчерскую, график работы, отчеты и управление персоналом.

---

## 📋 Содержание

- [Из чего состоит проект](#-из-чего-состоит-проект)
- [Быстрый старт](#-быстрый-старт)
- [Окружения и запуск](#-окружения-и-запуск)
- [Структура проекта](#-структура-проекта)
- [Архитектура](#-архитектура)
- [Разработка](#-разработка)
- [Тестирование](#-тестирование)
- [Линтинг и форматирование](#-линтинг-и-форматирование)
- [Сборка и деплой](#-сборка-и-деплой)
- [Module Federation](#-module-federation)
- [Контрибьюторам](#-контрибьюторам)
- [Часто задаваемые вопросы](#-часто-задаваемые-вопросы)

---

## 📦 Из чего состоит проект

Этот проект представляет собой фронтенд-микросервис (Micro Frontend), построенный с использованием:

- **React 16.14.0** — библиотека для построения пользовательских интерфейсов
- **TypeScript 5.2.2** — строгая типизация и улучшенная разработка
- **MobX 5.15.4** — управление состоянием приложения
- **Ant Design 4.17.2** — UI-библиотека компонентов
- **Module Federation** — архитектура для распределённой сборки микросервисов
- **React Router DOM 5.3.0** — клиентская маршрутизация
- **React Query 2.26.4** — управление серверным состоянием и кэшированием
- **styled-components 5.3.3** — CSS-in-JS решение для стилизации

### Основные зависимости
- `@sber-sbertransport/mf-core` — ядро Module Federation
- `@sber-sbertransport/tool-kit` — набор инструментов для микросервисов
- `@sber-sbertransport/ui-kit` — UI-компоненты
- `d3.js 6.7.0` — визуализация данных и графики
- `fp-ts`, `io-ts` — функциональное программирование и валидация
- `axios` — HTTP-клиент

---

## 🚀 Быстрый старт

### Предварительные требования

- Node.js **>= 18.0.0**
- [Yarn](https://sberusersoft.sigma.sbrf.ru/#program/s/6e307c51-3647-40b5-83b3-baa2855ac36f) (pkg manager)
- [nvm](hhttps://sberusersoft.sigma.sbrf.ru/#program/s/3b4e1f10-aeb5-4faa-bc9d-dd5b726a3bb1) (рекомендуется для управления версиями Node.js)

### Установка зависимостей
[Начало работы](https://confluence.sberbank.ru/pages/viewpage.action?pageId=7251173224)
Ставим токен по [инструкции](https://confluence.sberbank.ru/pages/viewpage.action?pageId=8501957813)
Затем выполняем команды:

```bash
# Установка нужной версии Node.js (из .nvmrc)
nvm use

# Установка зависимостей строго по yarn.lock
yarn ci (у некоторых может выдавать ошибку авторизации, тогда пользуйтесь командой ниже)

# или эквивалентно
yarn install --frozen-lockfile
```

---

## 🌍 Окружения и запуск

### Локальная разработка (Remote Mode)

Приложение запускается как Remote-микросервис и подключается к хост-приложению:

```bash
# Базовый запуск (Remote mode)
yarn start
```

Можно запускать как самостоятельное приложение, выбрав окружение и тип авторизации

### SUDIR:

```bash
# Запуск с подключением к dev-окружению autopark
yarn start:dev-autopark

# Запуск с подключением к dev-окружению cargo
yarn start:dev-cargo

# Запуск с подключением к dev-окружению autoservice
yarn start:dev-autoservice

# Запуск с подключением к dev-окружению sbertransport
yarn start:dev-sbertransport

# Запуск с подключением к ST (Автотесты)
yarn start:dev-st

# Запуск с подключением к NT (Стенд нагрузочного тестирования)
yarn start:dev-nt

# Запуск с подключением к IFT (Integration Functional Testing)
yarn start:dev-ift
```

### BASIC:
```bash
# С Basic-авторизацией
yarn start-auth:dev-autopark
yarn start-auth:dev-cargo
yarn start-auth:dev-autoservice
yarn start-auth:dev-sbertransport
yarn start-auth:dev-st
yarn start-auth:dev-nt
yarn start-auth:dev-ift

# С замоканной авторизацией (mocked auth)
yarn start-auth-mocked-login:dev-autopark
...

# С моками по API
yarn start-auth-mocked-api:dev-autopark
...

# С замоканной авторизацией и моками по API
yarn start-auth-mocked-login-api:dev-autopark
```

### Порт разработки

По умолчанию приложение запускается на порту **7005**.

---

## 📁 Структура проекта

```
src/
├── api/                            # API-сервисы (слой взаимодействия с бэкендом)
│   ├── analytics/                  # API аналитики
│       ├── analytics.api.ts        # Хуки API
│       ├── analytics.constants.ts  # Константы API
│       └── analytics.types.ts      # Типы API
│   ├── trips/                      # API поездок
│   └── ...                   
│
├── app/                            # Основа приложения
│   ├── dev/                        # Конфигурация для разработки
│       └── ...                
│   └── prod/                       # Конфигурация для продакшна
│       ├── App.tsx                 # Корневой компонент приложения
│       ├── Main.tsx                # Основная компонента
│       ├── AppRouter.tsx           # Основной роутер приложения
│       └── AppProvider.tsx         # Провайдеры контекстов приложения
│
├── components/                     # UI-компоненты (shared/components)
│   ├── Button/
│   ├── TableStyled/
│   ├── Select/
│   └── ...
│
├── modules/                        # Бизнес-модули приложения
│   ├── Reports/                    # Модуль отчетов
│   ├── Schedule2.0/                # Модуль расписания
│   ├── Staff/                      # Модуль персонала
│   ├── Support/                    # Модуль поддержки
│   ├── Trip/                       # Модуль поездки (одна)
│   └── Trips/                      # Модуль поездок (список)
│   └── ...
│
├── constants/                      # Константы приложения
│   └── routes.constants.ts         # Маршруты приложения
│   └── trips.constants.ts          # Константы поездок
│   └── ...
│
├── context/                        # Глобальные React-контексты
│
├── hooks/                          # Кастомные хуки
│
├── i18n/                           # Интернационализация (i18n)
│
├── ioc/                            # Inversion of Control (DI-контейнер)
│   ├── ioc.container.ts
│   ├── ioc.context.ts
│   ├── ioc.stores.ts
│   └── ioc.types.ts
│
├── mf/                             # Конфигурация Module Federation
│
├── styles/                         # Глобальные стили
│
├── types/                          # Глобальные TypeScript-типы
│   ├── drivers.ts
│   ├── vehicles.ts
│   ├── waypoint.ts
│   └── index.d.ts
│
├── ui/                   # Layout для локального запуска (меню, шапка)
│
└── utils/                # Утилиты и хелперы
```

---

## 🏗️ Архитектура

### Микросервисная архитектура (Module Federation)

Проект построен по принципам **Module Federation**, что позволяет:

- **Независимую разработку** — каждый микросервис может развиваться отдельно
- **Общий чанк** — совместное использование зависимостей (React, MobX, Ant Design и т.д.)
- **Динамическую загрузку** — загрузка модулей по требованию
- **Распределённую сборку** — сборка каждого микросервиса независимо

### Архитектурные принципы

- **Разделение ответственности** — компоненты не содержат бизнес-логику
- **MobX как state manager** — управление состоянием через классовые сторы
- **API-сервисы** — все вызовы API вынесены в отдельные сервисы
- **Типобезопасность** — строгая типизация на всех уровнях

### Паттерны проектирования

- **DI (Dependency Injection)** — через Inversify и контекст
- **Observer** — реактивное обновление UI через MobX
- **Repository** — абстракция над данными
- **Service Layer** — разделение бизнес-логики и API-вызовов

---

## 🛠️ Разработка

[Рекомендации по коду](https://confluence.sberbank.ru/pages/viewpage.action?pageId=15056208286)

### Запуск в режиме разработки

```bash
# Базовый режим c подключением к хост приложению (Remote)
yarn start

# Режим самостоятельного приложения с подключем бэкенда к одному из стендов (лгипассная авторизация)
yarn start-auth:dev-{env}
```

### Основные команды

| Команда | Описание |
|---------|----------|
| `yarn start` | Запуск в режиме Remote |
| `yarn start:dev-{env}` | Запуск с указанным окружением |
| `yarn start-auth:dev-{env}` | Запуск с Basic-авторизацией и указанным окружением |
| `yarn start-auth-mocked-login:dev-{env}` | Запуск с замоканной авторизацией |
| `yarn start-auth-mocked-api:dev-{env}` | Запуск с моками по API |
| `yarn start-auth-mocked-login-api:dev-{env}` | Запуск с замоканной авторизацией и моками по API |

### Переменные окружения

| Переменная | Значение | Описание |
|------------|----------|----------|
| `REACT_APP_NAME` | `platform` | Имя микросервиса |
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

## 🧪 Тестирование

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

- API-сервисы (`src/api/**`)
- Конфигурация приложения (`src/app/**`)
- Контексты (`src/context/**`)
- i18n (`src/i18n/**`)
- DI-контейнер (`src/ioc/**`)
- Module Federation (`src/mf/**`)
- Глобальные стили (`src/styles/**`)

---

## 🔬 Линтинг и форматирование

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

## 📦 Сборка и деплой

[Инструкция Confluence](https://confluence.sberbank.ru/pages/viewpage.action?pageId=9616460508)

### Сборка для продакшна

```bash
# Сборка с версионированием
yarn build

# Отладочная сборка
yarn build:debug

# Сборка для Sigma (по умолчанию)
yarn build:sigma
```

### Docker

Сборка и запуск через Docker:

```bash
# Сборка образа
docker build -t disp-platform .

# Запуск контейнера
docker run -p 8085:8085 disp-platform
```

### Dockerfile

- Базовый образ: `snx/sbel9`
- Порт: `8085`
- Путь к сборке: `/opt/syngx/share`
- Пользователь: `syngx`

---

## 🔄 Module Federation

### Экспортируемые модули

```javascript
// scripts/exposes.js
module.exports = {
  './version': './version.json',
  './App': './src/app/prod/App',
  './AppProvider': './src/app/prod/AppProvider',
  './routes': './src/constants/routes.constants',
  './Trips': './src/modules/Trips/Trips',
};
```

### Подключаемые микросервисы

```javascript
// scripts/remotes.js
module.exports = {
  auth: 'auth@http://front-micro-auth.ift.transport.apps.a6gabrx6.k8s.delta.sbrf.ru/auth.js?v=[Date.now()]',
};
```

### Shared dependencies

```javascript
// scripts/constants.js
const shared = getMfShared(IS_REMOTE);
```

---

## 🤝 Контрибьюторам

### Git Flow
[Описание нашего флоу](https://confluence.sberbank.ru/display/TRANSPORT/Git+Flow)
[Соглашение по работе с git](https://confluence.sberbank.ru/pages/viewpage.action?pageId=8285367085)

### Ветвление и коммиты

Проект использует **Conventional Commits**:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

#### Типы коммитов

- `feat` — новый функционал
- `fix` — исправление бага
- `docs` — изменения в документации
- `style` — форматирование (не влияет на логику)
- `refactor` — рефакторинг кода
- `test` — добавление тестов
- `chore` — обслуживание проекта
- `perf` — оптимизация производительности
- `ci` — изменения CI/CD

#### Примеры

```
feat(Trips): добавить фильтрацию по статусу
fix(Reports): исправить дублирование данных
docs(Readme): обновить инструкцию по установке
refactor(Drivers): вынести логику в сервис
test(Table): добавить тесты сортировки
```

### Pull Request Checklist

Перед созданием PR убедитесь, что:

- [ ] Код проходит линтинг (`yarn lint`)
- [ ] Тесты проходят (`yarn test`)
- [ ] Покрытие кода тестами >= 80% (для новых функций)
- [ ] Добавлены изменения в `CHANGELOG.md` (если применимо)
- [ ] Обновлена документация (при необходимости)
- [ ] Коммиты следуют конвенции conventional commits

### Инструкция для ревьюера

См. `instructions.md` — детальная инструкция для ревьюера кода, включающая проверку по 9 направлениям:

1. Общие принципы
2. TypeScript
3. JavaScript (ES6+)
4. React
5. Производительность
6. Безопасность
7. Архитектура
8. Читаемость
9. Логика

---

## ❓ Часто задаваемые вопросы

### Как запустить микросервис локально?

```bash
yarn ci
# На примере cargo
yarn start-auth:dev-cargo
```

### Как подключить новую API-эндпоинт?

1. Создайте папку `api/new-api-service`
2. Опишите типы в `api/new-api-service/new-api-service.types.ts`
3. Опишите кнстанты в `api/new-api-service/new-api-service.constats.ts`
4. Создайте хуки в `api/new-api-service/new-api-service.api.ts` (если используется react-query)

### Как добавить новый компонент?

1. Создайте папку в `src/components/ComponentName/`
2. Создайте файл `ComponentName.tsx`
4. Экспортируйте компонент из index.ts папки

### Как добавить новый маршрут?

1. Добавьте константу в `src/constants/routes.constants.ts`
2. Добавьте route в `src/app/prod/AppRouter.tsx` (или `src/app/dev/AppRouter.tsx`, если маршрут нужен только при запуске мф как самостоятельного приложения)
3. Создайте компонент в `src/modules/{Module}/`

### Как отладить микросервис на стенде?

```bash
# Собрать с отладочным флагом (соответствующая галочка в дженкинс)
yarn build:debug
```

### Как обновить зависимости?

```bash
# Обновить все зависимости
yarn up

# Или обновить конкретные
yarn up @sber-sbertransport/mf-core @sber-sbertransport/tool-kit @sber-sbertransport/ui-kit
```
