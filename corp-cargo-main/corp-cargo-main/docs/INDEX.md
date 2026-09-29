# 🗂 INDEX — Индекс документации проекта Cargo

> **⛔ ОБЯЗАТЕЛЬНО К ПРОЧТЕНИЮ AI-АГЕНТОМ ПЕРЕД ЛЮБОЙ ЗАДАЧЕЙ.**
>
> Ты (AI-агент) читаешь это первым. Затем по карте ниже открываешь релевантные файлы через `read_file`.
> Не работай по памяти — контекст проекта объёмный и специфичный. Если задача не покрыта картой ниже — начни с `docs/PROJECT_MAP.md` и `GIGACODE.md`.

---

## 🎯 Когда что читать

| Задача / вопрос | Первый файл | Затем (если нужно) |
|---|---|---|
| **Любая задача в проекте (обязательный минимум)** | [`GIGACODE.md`](../GIGACODE.md) | Этот файл уже у тебя в контексте, если хост его подхватил. Если нет — `read_file` |
| «Как устроен проект в целом?» / Module Federation / роутинг / сборка | [`ARCHITECTURE.md`](./ARCHITECTURE.md) | [`PROJECT_MAP.md`](./PROJECT_MAP.md) |
| «Где какой модуль / стор / API / компонент?» | [`PROJECT_MAP.md`](./PROJECT_MAP.md) | Конкретный `src/modules/<X>/<X>.architecture.md` |
| Работа в конкретном модуле (Planner, Contractors и т.д.) | `src/modules/<X>/<X>.architecture.md` | [`PROJECT_MAP.md`](./PROJECT_MAP.md) — раздел про этот модуль |
| **MobX-стор / DI / `@inject` / IoC** | [`STATE_MANAGEMENT.md`](./STATE_MANAGEMENT.md) | [`PATTERNS.md`](./PATTERNS.md) → раздел «MobX-стор» |
| **API / react-query / io-ts / кэш / ошибки** | [`API_LAYER.md`](./API_LAYER.md) | [`PATTERNS.md`](./PATTERNS.md) → раздел «useAPI / io-ts» |
| **Как писать код** (конвенции, шаблоны) | [`PATTERNS.md`](./PATTERNS.md) | [`PITFALLS.md`](./PITFALLS.md) |
| **Что НЕ делать / подводные камни** | [`PITFALLS.md`](./PITFALLS.md) | [`GIGACODE.md`](../GIGACODE.md) → раздел «Известные расхождения» |
| **Доменная область** (грузоперевозки, ЭТрН, оргструктура) | [`DOMAIN_GLOSSARY.md`](./DOMAIN_GLOSSARY.md) | `openspec/specs/<X>/spec.md` |
| **ЭТрН / подпись / lock-паттерн / УКЭП** | [`../openspec/specs/etrn-signing/spec.md`](../openspec/specs/etrn-signing/spec.md) | `skill: etrn-flow`, [`Planner.architecture.md`](../src/modules/Planner/Planner.architecture.md) |
| Написать тесты / увеличить покрытие | `skill: test` | [`PATTERNS.md`](./PATTERNS.md) → раздел «Тесты», `jest.config.ts` |
| Сделать commit | `skill: commit` | `commitlint.config.js` |
| Создать ветку для задачи | `skill: merge-branch` | — |

---

## 📚 Оглавление `docs/`

### [`ARCHITECTURE.md`](./ARCHITECTURE.md) — Архитектура (418 строк)

- Module Federation (`shell`, `cargo` remote, exposed-компоненты)
- Поток данных: UI → store → API → backend → cache → стор
- Слои приложения (`app/`, `modules/`, `stores/`, `api/`, `shared/`, `ioc/`, `constants/`, `utils/`)
- DI через Inversify (`src/ioc/ioc.container.ts`)
- Auth-flow (токены, редиректы, SberID)
- Роутинг (`react-router-dom@5.3.0`, `AppRouter`, `PlannerRouter`)
- Сборка (webpack 5, Module Federation plugin, `host: 'auto'`)

### [`PROJECT_MAP.md`](./PROJECT_MAP.md) — Карта проекта (354 строки)

- **40 модулей** (`src/modules/`) с описанием и статусом линта
- **55+ сторов** (`src/stores/`) с владельцами
- **57 API-сервисов** (`src/api/`) с эндпоинтами
- **58 shared-компонентов** (`src/shared/components/`)
- **80+ утилит** (`src/utils/`)
- Каталог OpenSpec-фич (`openspec/specs/`)

### [`PATTERNS.md`](./PATTERNS.md) — Паттерны кода (682 строки)

13 типовых паттернов с примерами из реального кода:

1. MobX-стор (декораторы + `makeObservable`)
2. `createCallableCtx` — сервис-синглтон через context
3. `useAPI` / `useAPIMutation` — react-query v2 обёртки
4. io-ts: декодирование ответов, валидация форм
5. Нативный HTML5 DnD (`@dnd-kit` НЕ используется!)
6. Модалки через `antd` `Modal`
7. Стилизация: CSS Modules + `styles/`
8. Импорты: absolute paths через `baseUrl: ./src`
9. Хлебные крошки через `antd` `Breadcrumb`
10. Clipboard через `navigator.clipboard.writeText`
11. Error handling: `logger.toNotify` / `logger.toMessage`
12. Даты: `moment.js` (НЕ `dayjs` / `date-fns`!)
13. Тесты: Jest + React Testing Library

### [`STATE_MANAGEMENT.md`](./STATE_MANAGEMENT.md) — Сторы и DI (516 строк)

- 4 типа сторов: feature-сторы (`plannerStore`), domain-сторы, UI-сторы, storeNames enum
- Регистрация в IoC (`ioc.stores.ts`)
- Доступ через `useAppStoreContext().<store>`
- Каталог всех 55+ сторов
- Cross-store коммуникация через `rootStore`
- Computed values, `runInAction`, реакции на изменения (`reaction`)

### [`API_LAYER.md`](./API_LAYER.md) — API-слой (491 строка)

- Архитектура: `axios` + react-query v2 + io-ts
- Каталог сервисов с эндпоинтами
- Конвенция файлов (`constants.ts` + `index.ts` + `<name>.ts`)
- `useAPI` / `useAPIMutation` сигнатуры
- io-ts: декодирование, enums, union types
- Auth: bearer token, refresh, SberID
- Error handling: `tryDecodeError`, `logger.toNotify`
- Кэш: `invalidateQueries` / `refetchQueries` / `setQueryData`
- Тесты API

### [`PITFALLS.md`](./PITFALLS.md) — Подводные камни (462 строки)

14 разделов с реальными примерами из кода:

1. Зафиксированные версии библиотек (НЕ апгрейдить!)
2. ESLint ignores (Planner, Contractors, TariffSettings — не линтуются)
3. Jest coverage excludes (`api/`, `ioc/`, `constants/`, `types/` — не тестируются)
4. Legacy `*_New.tsx` / `*_new/` — дубликаты компонентов, не трогать без задачи
5. Магические строки (i18n ключи, статус-коды) — выносить в `constants/`
6. `catch (null)` / потеря stack trace — запрещено
7. Прямые API-вызовы в сторах — запрещены (только через `api/`)
8. `moment.js` — единственная библиотека дат
9. MobX v5: `makeObservable`, нет `makeAutoObservable`
10. Router v5: `useHistory` / `Switch`, нет `useNavigate` / `Routes`
11. react-query v2: `queryCache` / `cache`, нет `queryClient`
12. io-ts v2: `t.type`, `TypeOf<typeof X>`
13. Миграция с MobX v4 → v5, Router v4 → v5 (исторический контекст)
14. Кастомные файлы GigaCode: не путать с системными

### [`DOMAIN_GLOSSARY.md`](./DOMAIN_GLOSSARY.md) — Глоссарий (436 строк)

- Грузоперевозки: заказ, рейс, путевой лист, ТТН, ТЭО
- ЭТрН: титулы T1–T4, статусы, УКЭП, КриптоПро
- Оргструктура: организация → подразделение → сотрудник → роль
- Картография: геообъекты, полигоны, маршруты, зоны
- UI-термины: карточка, список, фильтр, сайдбар, хедер
- Аббревиатуры (ЭТрН, УКЭП, ГЛОНАСС, ПВЗ, ИНН, КПП, ОГРН)

---

## 🔗 Как всё связано

```
GIGACODE.md (корень, "первая страница")
   │
   ├──► AGENTS.md (корень, краткие правила)
   │
   └──► docs/INDEX.md ← ВЫ ЗДЕСЬ
            │
            ├──► ARCHITECTURE.md      ─┐
            ├──► PROJECT_MAP.md        ├─ общая картина
            ├──► DOMAIN_GLOSSARY.md   ─┘
            │
            ├──► STATE_MANAGEMENT.md  ─┐
            ├──► API_LAYER.md          ├─ слои
            └──► PATTERNS.md          ─┘
                  │
                  └──► PITFALLS.md     ← "что НЕ делать"

openspec/specs/<X>/spec.md  ──── для фич с формальной спецификацией (EARS/REQ/SCN)
src/modules/<X>/<X>.architecture.md  ── для сложных модулей (Planner и т.д.)
```

---

## 🚨 Если карта выше не покрывает задачу

1. Открой [`PROJECT_MAP.md`](./PROJECT_MAP.md) — там каталог всего.
2. Открой [`GIGACODE.md`](../GIGACODE.md) — там decision log и словарь.
3. Используй `grep_search` / `glob` по `src/` — найдёшь конкретный код.
4. Если всё равно непонятно — **спроси пользователя** перед тем, как делать предположения.

---

## 📊 Метаданные

- **Файлов в `docs/`:** 8 (включая этот)
- **Общий объём:** ~3700 строк
- **Версия:** 1.0 (7 августа 2026 г.)
- **Поддерживается:** при изменении архитектуры/паттернов — обновлять **этот файл первым**, он индекс.

---

> 💡 **Напоминание хосту:** чтобы AI-агент гарантированно видел этот индекс в каждой сессии, добавьте `docs/INDEX.md` в «always-include» (если GigaCode это поддерживает). Иначе — рассчитывайте на явное упоминание `@docs/INDEX.md` пользователем или на триггер из `AGENTS.md`.
