# Support

> Модуль «Поддержка и помощь» — справочно-информационный раздел платформы: контактные карточки службы поддержки, пошаговые инструкции (сторис) по работе с сервисом и (в планах) блок популярных вопросов.

## Назначение

Даёт пользователю справку по работе с продуктом: показывает контакты поддержки (телефон, чат, почта) и набор инструкций в виде интерактивных «сторис» или ссылок на PDF. Модуль не обращается к API и не использует server-state — вся информация статична (i18n + локальный JSON + константы).

## Точка входа

| Что                  | Файл / экспорт                                   |
|----------------------|--------------------------------------------------|
| Корневой компонент   | `modules/Support/Support.tsx` → `Support` (default) |

## Где монтируется

`src/app/prod/AppRouter.tsx`:

```tsx
const Support = lazy(() => import('modules/Support/Support'));
…
<Route path={routes.SUPPORT} component={Support} />
```

Маршрут: `src/constants/routes.constants.ts` → `routes.SUPPORT` (`${APP}/support`).

## Структура

```
modules/Support/
├── Support.tsx
├── Support.module.scss
├── components/
│   ├── CardsInfo/
│   │   ├── CardsInfo.tsx
│   │   ├── CardsInfo.module.scss
│   │   └── items.tsx            ← статичные карточки контактов
│   ├── Instructions/
│   │   ├── Instructions.tsx
│   │   ├── Instructions.module.scss
│   │   ├── instructions.json    ← данные инструкций (табы)
│   │   └── components/
│   │       ├── InstructionsItems/
│   │       │   ├── InstructionsItems.tsx
│   │       │   └── InstructionsItemsStyled.ts
│   │       └── Story/
│   │           ├── Story.tsx
│   │           └── StoryStyled.ts
│   └── Questions/
│       ├── Questions.tsx        ← закомментирован в корневом компоненте
│       └── Questions.module.scss
└── README.md
```

## Компоненты

| Компонент | Файл | Пропсы | Назначение |
|-----------|------|--------|------------|
| `<Support>` | `Support.tsx` | `—` | Корневой компонент: рендерит `CardsInfo` + `Instructions` внутри `ErrorBoundary` |
| `<CardsInfo>` | `components/CardsInfo/CardsInfo.tsx` | `isSDO: boolean` | Карточки контактов поддержки, фильтруются по режиму SDO |
| `<Instructions>` | `components/Instructions/Instructions.tsx` | `—` | Табы инструкций (Автопарк / Трансфер / Скачать) из `instructions.json` |
| `<InstructionsItems>` | `components/Instructions/.../InstructionsItems.tsx` | `instructions: IInstruction[]` | Сетка карточек инструкций; открывает Story или ссылку на PDF |
| `<Story>` | `components/Instructions/.../Story/Story.tsx` | `onCancel(): void`, `instruction: IInstruction` | Полноэкранный просмотр «сторис» с прогресс-баром и автоигранием |
| `<Questions>` | `components/Questions/Questions.tsx` | `isSDO: boolean` | Частотные вопросы (Accordion). **Закомментирован** в корневом компоненте |

## Типы

`src/types/Instructions/Instructions.interface.ts`:
- `interface IInstruction` — `{ name, coverImage, title, pages: TPage[] , link?: string }`
- `interface IInstructions` — `{ title, instructions: IInstruction[] }`
- `interface TPage` — `{ image, title, text }`

## Контексты / Store

- `useAppStore()` (`ioc`) — DI-контейнер.
  - `configStore.env.IS_SDO` — флаг SDO-окружения, управляет фильтрацией карточек и контактов (`CardsInfo`, `Questions`).

## API

Модуль **не использует** API / react-query запросов. Все данные статичны:
- контакты — `constants/app.constants` → `enum Contacts`, `enum SDOContacts`;
- инструкции — `modules/Support/.../instructions.json`;
- тексты — i18n (`t.Support`).

## i18n

Корневой блок ключей: `t.Support` (только `src/i18n/ru/index.ts`; блок `Support` в `en/index.ts` отсутствует).

| Ключ | ru |
|------|----|
| `t.Support.cards.insideTel` | «Внутренний» |
| `t.Support.cards.outTel` | «Внешний» |
| `t.Support.cards.supportMail` | «Для жалоб и поддержки» |
| `t.Support.titles.support` | «Поддержка и помощь» |
| `t.Support.titles.instructions` | «Инструкции» |
| `t.Support.titles.questions` | «Часто задаваемые вопросы» |
| `t.Support.popularQuestions` | массив `{ key, header, description }` для `Questions` |

## Роутинг

- Маршрут верхнего уровня: `SUPPORT` (`${APP}/support`, см. `routes.constants`).
- Внутренних подмаршрутов нет.

## Стили

- Главный файл: `Support.module.scss` (класс `.container`, токены `var(--white)`).
- `CardsInfo.module.scss` — `.container`, `.title`, `.cardList`, `.card`, `.contacts`.
- `Instructions.module.scss` — стили табов antd (`.title`, `:global .ant-tabs-*`).
- `InstructionsItemsStyled.ts` / `StoryStyled.ts` — `styled-components` (карточки инструкций, полноэкранный сторис, прогресс). Изображения подгружаются `require('assets/images/instructions/…')`.

## Тесты

| Файл | Покрывает |
|------|-----------|
| —    | тесты отсутствуют, рекомендуется добавить |

Запуск: `yarn test src/modules/Support`.

## Известные ограничения / TODO

- Компонент `Questions` реализован, но **закомментирован** в `Support.tsx` — будет включён в следующих релизах.
- Инструкции и контакты — статические данные (JSON / константы), поддерживаются вручную.

## Как расширять

1. Новая карточка контактов → `components/CardsInfo/items.tsx` + блок в `t.Support.cards`.
2. Новая инструкция → `components/Instructions/instructions.json` (id) + ассеты в `assets/images/instructions/`.
3. Новый вопрос → массив `t.Support.popularQuestions` в `src/i18n/ru/index.ts`.
4. Активировать `Questions` → раскомментировать `<Questions isSDO=... />` в `Support.tsx`.