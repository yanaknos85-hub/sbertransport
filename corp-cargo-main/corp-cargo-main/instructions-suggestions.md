Не предлагай изменить бизнес логику. Особое внимание удели производительности. Предлагай изменения в формате:

**Рекомендация**: (конкретное предложение по исправлению, с примером кода)
  ```tsx
  // До
  const [user, setUser] = useState<any>({});

  // После
  interface User { name: string; email: string; }
  const [user, setUser] = useState<User | null>(null);
  ```
**Важность**: критическая / высокая / средняя / низкая

---

## Специфические подсказки для ревьюера по модулю Planner

### Производительность

#### 1. Ленивая загрузка компонентов через React.lazy
**Контекст**: Роутер Planner (`PlannerRouter.tsx`) загружает все компоненты через `React.lazy()`. Это хорошо для производительности, но нужно проверить, что:

- Все `lazy`-компоненты обёрнуты в `<Suspense>` с fallback.
- Fallback — это лоадер (Spin из antd или кастомный), а не пустой div.
- Компоненты не загружаются повторно при каждом рендере роутера (стабильный path).

**Пример проверки**:
```tsx
// Должно быть:
const Orders = React.lazy(() => import('./Components/Orders'));
const SomeComponent = React.lazy(() => import('./Components/SomeComponent'));

// В роутере:
<Switch>
  <Route path="/multi-logistics/planner/order/:id">
    <Suspense fallback={<Spin />}>
      <OrderDetailed />
    </Suspense>
  </Route>
</Switch>
```

**Важность**: высокая

#### 2. Подписка на контекст без мемоизации
**Контекст**: Компоненты подписываются на `PlannerContext` через `useContext`. Если контекст обновляется часто (например, при фильтрации), компоненты списка будут рендериться каждый раз.

**Рекомендация**: Использовать `useMemo` для разбивки контекста на частичные подписки:
```tsx
// До
const { isRouteVisible, routeFilters, setRoutePage } = useContext(PlannerContext);

// После
const isRouteVisible = useContextSelect(PlannerContext, ctx => ctx.isRouteVisible);
const routeFilters = useContextSelect(PlannerContext, ctx => ctx.routeFilters);
```

**Важность**: высокая

#### 3. Создание QueryClient при каждом тесте
**Контекст**: В тестах хуков (например, `useOrdersQuery.test.tsx`) создаётся новый `QueryClient` на каждый тест.

**Рекомендация**: Использовать один `QueryClient` для всех тестов в `describe` блоке:
```tsx
// До
it('test1', () => {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  ...
});

it('test2', () => {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  ...
});

// После
const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
// Дальше queryClient.resetQueries() между тестами
```

**Важность**: средняя

#### 4. Fallback для navigator.clipboard
**Контекст**: Копирование ID реализовано через `navigator.clipboard.writeText()` с fallback на `document.execCommand('copy')`. Проверить, что:

- Fallback используется только если API недоступен (не `isSecureContext`).
- Нет лишних уведомлений при успешном копировании (antd notification уже есть).
- Нет попыток копирования пустой строки.

**Рекомендация**: Вынести логику копирования в утилиту:
```tsx
// Лучше
const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    const result = document.execCommand('copy');
    document.body.removeChild(textArea);
    return result;
  }
};
```
**Важность**: средняя

### Структура и читаемость

#### 5. Drag-and-drop на нативных API
**Контекст**: В Planner используется нативная реализация drag-and-drop (HTML5 DnD API), а не библиотека. Это означает:

- Нет библиотек react-dnd или react-beautiful-dnd в пакете.
- Нужно проверять `onDragStart`, `onDragOver`, `onDrop` на компонентах.
- Нет индикации перетаскивания (по умолчанию курсор меняется).

**Рекомендация**: Если список сложный (группировка, вложенные элементы), рассмотреть использование react-dnd:
```tsx
// Если появляются баги с DnD:
// npm install react-dnd react-dnd-html5-backend
// import { DndProvider } from 'react-dnd'
// import { HTML5Backend } from 'react-dnd-html5-backend'

// <DndProvider backend={HTML5Backend}>
//   <OrderListItem draggable />
//   <RouteListItem_New droppable />
// </DndProvider>
```

**Важность**: средняя

#### 6. `catch(() => null)` — антипаттерн
**Контекст**: В асинхронных вызовах (модалки создания маршрута) встречается `catch(() => null)`. Это подавляет ошибки, и пользователь не видит, что пошло не так.

**Рекомендация**: Логировать ошибку, а не возвращать `null`:
```tsx
// До
useMutation({
  mutationFn: () => createRoute(data),
  onError: () => null,
});

// После
useMutation({
  mutationFn: () => createRoute(data),
  onError: (error) => {
    logger.toNotify('error', 'Не удалось создать маршрут', error);
    notification.error({ message: 'Ошибка', description: error.message });
  },
});
```

**Важность**: высокая

#### 7. Дублирующиеся системы (legacy vs new)
**Контекст**: В модуле Planner есть дублирующиеся компоненты:
- `AddressBlockDetailed` vs `AddressBlockDetailed_New`
- `RouteParams` vs `RouteParams_New`
- `Tools` vs `Tools_New`

**Рекомендация**: При ревью проверять, что:
- Старая версия не используется в новом коде (если используется — отметить как проблему).
- Есть комментарий `@deprecated` на старом компоненте.
- Новые компоненты не дублируют логику старых.

**Важность**: средняя

### Типизация и логика

#### 8. io-ts типы
**Контекст**: В `types.ts` используются `io-ts` типы с `t.TypeOf<typeof Type>`. Проверить:
- Не перепутаны ли `t.Type` (статический тип) и `t.TypeOf` (то же самое).
- Не используется ли `t.decode` без проверки через `t.is` / `fold`.
- Есть ли валидация данных из API через io-ts (не просто через any).

**Рекомендация**: Если данные из API не проходят через io-ts decode — это потеря типизации:
```tsx
// Плохо: любой ответ от API без валидации
const data = await api.get('/routes') as any;

// Хорошо: валидация через io-ts
const response = RouteResponse.decode(await api.get('/routes'));
if (E.isLeft(response)) {
  throw new Error('Invalid API response format');
}
return response.right;
```

**Важность**: высокая

#### 9. Обновление кэша после мутации
**Контекст**: После `useMutation` (например, `useUpdateRouteStatus`) не всегда явно указано, что делать с кэшем.

**Рекомендация**: Убедиться, что после мутации вызывается `queryClient.invalidateQueries`:
```tsx
useMutation({
  mutationFn: (data) => updateRouteStatus(data),
  onSuccess: () => {
    // ВАЖНО: инвалидация кэша
    queryClient.invalidateQueries({ queryKey: ['searchRoutes'] });
    // Или обновление стейта
    queryClient.setQueryData(['searchRoutes'], (old) => [...old, newItem]);
  },
});
```

**Важность**: высокая

#### 10. Работа с датами (moment.js)
**Контекст**: В модуле используется `moment.js` для конвертации дат в формат ISO. Это legacy-библиотека.

**Рекомендация**: При ревью проверять:
- Moment не используется в новых файлах (если да — предложить dayjs или date-fns).
- Конвертация делает `startOf('day')` (не сдвигает время).
- Даты в фильтрах корректно конвертируются в UTC.

**Важность**: средняя

#### 11. Сброс пагинации при изменении фильтров
**Контекст**: При изменении фильтров вызывается `setRoutePage(START_PAGE)`.

**Рекомендация**: Проверить, что:
- `START_PAGE = 1` (не 0).
- При изменении сортировки тоже сбрасывается страница.
- При изменении размера страницы (pageSize) — да (иначе теряются данные).

**Пример**:
```tsx
// При изменении любого фильтра:
<FilterMainButton onClick={() => {
  setRoutePage(START_PAGE);
  setRoutePageSize(PAGE_SIZE);
  // Вызов API с новыми параметрами
  refetch();
}} />
```

**Важность**: средняя

---

### Примечания для AI
- При проверке модуля Planner обращай особое внимание на те антипаттерны, которые перечислены выше (дублирование, catch-обработка, устаревшая типизация).
- Модуль Planner — один из самых крупных и сложных в проекте. Основные риски: производительность (большие списки), дублирование компонентов, недостаточная типизация через io-ts.
- Для каждого замечания указывай конкретный файл и строку, с примером до/после.
- Не предлагай изменения бизнес-логики (например, логики расчёта маршрутов) — только код-качество и производительность.