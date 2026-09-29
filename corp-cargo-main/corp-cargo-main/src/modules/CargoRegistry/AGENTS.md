# Инструкция для модуля CargoRegistry

## 1. Архитектура модуля

### Обзор

`CargoRegistry` — это специализированный модуль для работы с грузовыми перевозками (заявками). Является частью системы Registry и реализует паттерн **Registry Module** с собственным роутером, хуками и компонентами.

### Структура файлов

```
src/modules/CargoRegistry/
├── CargoRegistry.tsx                    # Базовый компонент (прямой роутинг)
├── CargoRegistryRouter.tsx              # Роутер с ToolbarProvider (используемый)
├── constants.ts                         # VisibleFields, sortFields
├── types.ts                             # Типы данных
├── styles.module.scss                   # Общие стили
├── components/
│   ├── DetailedView.tsx                 # Детальный просмотр заявки
│   ├── ExportXlsButton.tsx              # Кнопка экспорта XLS
│   ├── ListView.tsx                     # Список заявок (основной компонент)
│   └── ListView.module.scss             # Стили ListView
└── hooks/
    ├── useColumns.tsx                   # Генерация колонок таблицы
    ├── useDataSource.ts                 # Загрузка данных с пагинацией
    ├── useDefferedSearch.tsx            # Отложенный поиск (организация/исполнители)
    ├── useFilter.tsx                    # Управление фильтрами
    ├── usePagination.ts                 # Пагинация
    ├── useSorting.ts                    # Сортировка
    ├── useTransformedData.ts            # Трансформация данных
    └── useTransformedData.test.tsx      # Тесты
```

### Маршрутизация

#### CargoRegistry.tsx — прямой роутинг
```typescript
// Пути: /reports/registry/cargo, /reports/registry/cargo/:id
export const CargoRegistry: FC = () => {
  const { id } = useParams<{ id?: string }>();
  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path="/reports/registry/cargo"
          render={() => <ListView pathname={pathname} />}
          exact
        />
        <Route
          path="/reports/registry/cargo/:id"
          component={() => <DetailedView id={id} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};
```

#### CargoRegistryRouter.tsx — интеграция с Registry
```typescript
// Пути: REGISTRY_CARGO_ORDERS, REGISTRY_CARGO_ORDERS_VIEW
export const Router: FC = () => {
  const { id } = useParams<{ id?: string }>();
  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path={routes.REGISTRY_CARGO_ORDERS}
          render={() => <ListView pathname={pathname} />}
          exact
        />
        <Route
          path={routes.REGISTRY_CARGO_ORDERS_VIEW}
          component={() => <DetailedView id={id} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

// Используется в Registry.routes.tsx:
// component: <CargoOrdersRegistryRouter />
```

**Важно:** Используйте `CargoRegistryRouter.tsx` для интеграции с Registry, а не `CargoRegistry.tsx`.

---

## 2. Основные компоненты

### ListView.tsx — основной компонент

**Назначение:** Отображение таблицы заявок с фильтрацией, сортировкой и пагинацией.

**Структура:**
```typescript
export const ListView: FC<ListViewProps> = withErrorBoundary(({ pathname }) => {
  // Хуки
  const { pageSetting, setPageSetting } = usePagination();
  const { sortSetting, setSortSetting, handleDefaultSort } = useSorting();
  const { filterValues, setFilterValues, ...filterProps } = useFilter();
  const { columns, settings: visibilitySettings } = useColumns(pathname, sortSetting);
  const [isVisible, setIsVisible] = useState(false);

  // Контекст
  const { organizationId, isOrganization, executorGroupId } = useOrganizationContext();

  // Поиск
  const { executeSearch, data: responseData, isLoadingOrg, isLoadingExec } = useDeferredSearch(...);

  // Трансформация
  const transformedData = useTransformedData(responseData?.content || []);

  // Основная логика
  const pagination = useMemo(...);
  const onTableChange = useCallback(...);
  const handleSearchId = (id: string) => {...};

  return (
    <div className={styles.listView}>
      <ActionsRegistry ... />
      <TableSettingsContainer ... />
      <Table ... />
    </div>
  );
});
```

**Ключевые особенности:**
- Использует `withErrorBoundary` для обработки ошибок
- Интеграция с `useOrganizationContext` для выбора между organization/executorGroup
- Отложенная загрузка данных через `useDeferredSearch`
- Синхронизация хуков (пагинация сбрасывается при изменении фильтров)

### DetailedView.tsx — детальный просмотр

**Назначение:** Отображение полной информации о заявке с картой.

**Структура:**
```typescript
export const DetailedView: FC<DetailedViewProps> = ({ id }) => {
  // Данные из API
  const { data: mainContent } = useCargoTripInfo(id);
  const { data: { content: [auxContent] } } = useSearchCargoRegistry({ requestHumanId: mainContent.humanReadableId });

  // Трансформация
  const transformedData = useTransformedData([{ ...auxContent, ...mainContent }]);

  // Формирование записей для Descriptions
  const records = useMemo(() => {
    const labels = t.Forms.registryCargoSettings;
    const transformed = transformedData[0] || {};
    return (Object.keys(transformed) as VisibleFields[]).map(key => ({
      key,
      label: labels[key],
      value: transformed[key],
    }));
  }, [transformedData, t]);

  return (
    <div className={styles.tripDetailedView}>
      {/* Карта */}
      <MapComponent
        className={styles.map}
        markers={mainContent.expected?.waypoints}
        polylines={mainContent.expected?.segments}
        dragging
      />
      
      {/* Описание */}
      <Descriptions size="small" column={1}>
        {records.map(({ key, label, value }) => (
          <Descriptions.Item key={key} label={label}>
            <span>{value}</span>
          </Descriptions.Item>
        ))}
      </Descriptions>
    </div>
  );
};
```

**Особенности:**
- Объединяет данные из двух API (основная + дополнительная)
- Использует `MapComponent` для отображения маршрута
- `Descriptions` из Ant Design для отображения полей

### ExportXlsButton.tsx

**Назначение:** Экспорт данных в Excel с настройками фильтров.

**Пропсы:**
```typescript
interface ExportXlsButtonProps {
  disabled?: boolean;
  filterValues?: TransformedFilterValues;
  columnVisibility?: ColumnVisibilitySettings;
  formatType?: 'xls' | 'cse';
}
```

**Логика:**
```typescript
const [isVisible, setIsVisible] = useState(false);
const [withFilters, setWithFilters] = useState(false);

const handleOk = () => {
  downloadRegistryXLSAsync(
    http,
    'cargo',
    XLSX_MIME_TYPE,
    {
      withView,
      withFilters,
      ...(withFilters ? filterValues : {}),
    },
    organizationId,
    logger,
    setIsLoading,
    formatType
  );
};

// Возвращается:
{formatType === 'cse' ? <ExportKceButton /> : <ExportXlsButton />}
<Modal visible={isVisible} onOk={handleOk}>...</Modal>
```

### Контейнеры (из Registry)

#### ActionsRegistry

Контейнер для поиска и кнопок действий (из `modules/Registry/components/ActionsRegistry`).

```tsx
<ActionsRegistry
  isNewDesign
  handleSearchId={handleSearchId}
  isVisible={isVisible}
  setIsVisible={setIsVisible}
  filters={<FilterPanel {...filterProps} />}
  buttons={[
    <ExportXlsButton key="export-cse" formatType="cse" />,
    <ExportXlsButton key="export-xls" />,
  ]}
/>
```

#### TableSettingsContainer

Контейнер для настроек таблицы.

```tsx
<TableSettingsContainer>
  <DefaultSorting onClick={handleDefaultSort} />
  <ColumnVisibilitySettings
    setting={visibilitySettings.columnVisibility}
    saveSettingChange={visibilitySettings.setColumnVisibility}
    defaultColumns={visibilitySettings.defaultVisibility}
    isButtonDisabled={visibilitySettings.isSaving}
    settingsEntries={t.Forms.registryCargoSettings}
  />
</TableSettingsContainer>
```

---

## 3. Хуки

### useColumns

**Назначение:** Генерация колонок таблицы на основе видимости и сортировки.

**API:**
```typescript
export const useColumns = (
  pathname: string,
  sortSetting?: SortSetting
): UseColumnsResult => {
  // Загрузка настроек пользователя
  const { data: defaultColumnsVisibility } = useDefaultCargoColumnVisibilitySettings();
  const { refetch: refetchUserSettings, data: userSettings } = useCargoUserSettings(userId);
  const [saveColumnVisibilitySettings, { isLoading: isSaving }] = useSaveCargoUsersAttributes(userId);

  // Формирование колонок
  const columns = useMemo(
    () => Object.values(VisibleFields)
      .filter(item => columnVisibility[item])
      .map(item => ({
        dataIndex: item,
        title: t.Forms.registryCargoSettings[item],
        sortProperty: sortFields[item],
        sorter: true,
        fixed: item === VisibleFields.humanReadableId ? 'left' : undefined,
      })),
    [pathname, t, sortSetting, columnVisibility]
  );

  return {
    columns,
    settings: {
      columnVisibility,
      setColumnVisibility,
      isSaving,
      defaultVisibility,
    },
  };
};
```

**Важно:**
- Сортировка автоматически применяется к первой колонке (ID заявки)
- Колонки сохраняются в API через `saveCargoUsersAttributes`

### useFilter

**Назначение:** Управление фильтрами и генерация полей формы.

**Поля фильтров:**
```typescript
fields = [
  // Простые поля
  { fieldType: 'TEXT', name: 'requestHumanId' },
  { fieldType: 'SELECT', name: 'cargoTransportType', mode: 'multiple' },
  { fieldType: 'SELECT', name: 'contractorSet', mode: 'multiple' },
  { fieldType: 'TEXT', name: 'authorFIO' },
  { fieldType: 'SELECT', name: 'requestStatusSet', mode: 'multiple' },
  { fieldType: 'SELECT', name: 'deadlineDate' },
  { fieldType: 'SELECT', name: 'requestTypeSet' },
  
  // Даты
  { fieldType: 'CUSTOM', name: 'desiredDate', component: <NewDateInput /> },
  { fieldType: 'CUSTOM', name: 'creationDate', component: <NewDateInput /> },
  { fieldType: 'CUSTOM', name: 'changeDate', component: <NewDateInput /> },
]
```

**Преобразование фильтров:**
```typescript
const transformedFilterValues = useMemo(() => ({
  ...filterValues,
  desiredDate: filterValues?.desiredDate 
    ? toDateRangeISO(filterValues.desiredDate as any) 
    : undefined,
  creationDate: filterValues?.creationDate 
    ? toDateRangeISO(filterValues.creationDate as any) 
    : undefined,
  changeDate: filterValues?.changeDate 
    ? toDateRangeISO(filterValues.changeDate as any) 
    : undefined,
  expectedCost: filterValues?.expectedCost 
    ? transformToRangeObject(filterValues.expectedCost) 
    : undefined,
}), [filterValues]);
```

**Важно:** Стоимость преобразуется в копейки (`* 100`).

### useSorting

**Назначение:** Сортировка через SettingsContext.

```typescript
export const useSorting = (): {
  sortSetting: SortSetting | undefined;
  setSortSetting: Dispatch<SetStateAction<SortSetting | undefined>>;
  handleDefaultSort: () => void;
} => {
  const { [Registry.Cargo]: cargoSettings } = useSettingsContext();
  const [sortSetting, setSortSetting] = useState(cargoSettings.sortSettings());

  const handleDefaultSort = (): void => {
    const defaultSorting = {
      property: SortFields.CREATION_DATE,
      directionAsc: false,
    };
    cargoSettings.saveSortSettings(defaultSorting);
    setSortSetting(defaultSorting);
  };

  return { sortSetting, setSortSetting, handleDefaultSort };
};
```

**Важно:** Настройки сохраняются в `SettingsContext` (не в API как в useColumns).

### usePagination

**Назначение:** Управление пагинацией.

```typescript
const defaultPageSettings: PageSetting = { page: 0, size: 10 };

export const usePagination = (
  initialPageSettings = defaultPageSettings
): { pageSetting: PageSetting; setPageSetting } => {
  const [pageSetting, setPageSetting] = useState<PageSetting>(initialPageSettings);
  return { pageSetting, setPageSetting };
};
```

**Сброс при фильтрации:**
```typescript
// Автоматически сбрасывает на первую страницу при изменении фильтров
useEffect(() => {
  setPageSetting(setting => ({ page: 0, size: setting.size }));
}, [setPageSetting, filterValues]);
```

### useTransformedData

**Назначение:** Трансформация сырых данных для отображения.

```typescript
export const useTransformedData = (data: TripInfo[]): Row[] => {
  const { data: statuses } = useCargoTripStatuses();
  const { data: transportTypes } = useCargoTransportTypes();

  return useMemo(
    () => (data || []).map(item => ({
      id: item.id,
      [VisibleFields.humanReadableId]: item.humanReadableId,
      [VisibleFields.status]: getTripStatus(statuses, item.status, '-'),
      [VisibleFields.transportType]: transportTypes.find(s => s.name === item.transportType)?.rusName,
      [VisibleFields.desiredDate]: formatTimeDate(item.desiredDate, '-'),
      [VisibleFields.contractor]: item.contractor?.name || '-',
      [VisibleFields.expectedCost]: (item.expected.cost / 100).toFixed(2).replace('.', ','),
      [VisibleFields.economy]: item.economy 
        ? convertToRubles(item.economy).replace('.', ',') 
        : '-',
      // ... остальные поля
    })),
    [data, statuses, transportTypes]
  );
};
```

**Форматирование:**
- Даты: `formatTimeDate`, `formatBaseDate`, `formatBaseTime`
- Суммы: `convertToRubles`, деление на 100
- Телефоны: `formatPhoneNumber`
- Объем: `formatVolume`
- Десятичные: замена точки на запятую

### useDeferredSearch

**Назначение:** Отложенный поиск с поддержкой двух типов запросов.

```typescript
export const useDeferredSearch = <TQuery, TResponse>({
  orgHook,           // useRegistryJournalDataDeffered
  executorHook,      // useRegistryJournalDataExecutorDeffered
  organizationId,
  executorGroupId,
  isOrganization,
  initialData,
}): UseDeferredSearchResult => {
  const [searchOrg, { isLoading: isLoadingOrg }] = orgHook(organizationId);
  const [searchExec, { isLoading: isLoadingExec }] = executorHook(executorGroupId);

  const executeSearch = useCallback(async (query: TQuery) => {
    if (isOrganization && organizationId) {
      const result = await searchOrg(query);
      setData(result?.responseData);
    } else if (executorGroupId?.length) {
      const result = await searchExec(query);
      setData(result?.responseData);
    }
  }, [isOrganization, organizationId, executorGroupId, searchOrg, searchExec]);

  return { executeSearch, data, isLoadingOrg, isLoadingExec };
};
```

**Логика:**
- Если `isOrganization = true` — использует `orgHook`
- Иначе — использует `executorHook` (для исполнителей)

### useDataSource (альтернативный хук)

**Назначение:** Прямая загрузка данных с автоматическим refetch.

```typescript
export const useDataSource = ({
  filterValues,
  sortSetting,
  pageSetting,
  setPageSetting,
}): UseDataSourceResult => {
  const { organizationId } = useProfile().data;
  const { data, refetch, isLoading } = useSearchCargoRegistry(
    { ...filterValues, sortSetting, pageSetting },
    { enabled: false },  // Важно: enabled: false для ручного вызова
    organizationId
  );

  const dataSource = useTransformedData(data?.content);
  const metadata = useMemo(() => ({
    total: data?.totalElements,
    page: data?.pageable.pageNumber,
    pageSize: data?.pageable.pageSize,
  }), [data]);

  // Сброс страницы при изменении фильтров
  useEffect(() => {
    setPageSetting(setting => ({ page: 0, size: setting.size }));
  }, [setPageSetting, filterValues]);

  // Автоматический refetch при изменении параметров
  useEffect(() => {
    refetch();
  }, [refetch, filterValues, organizationId, pageSetting, sortSetting]);

  return { dataSource, isLoading, metadata };
};
```

**Важно:** Используется `{ enabled: false }` в хуке запроса для ручного вызова `refetch()`.

---

## 4. Типы и константы

### VisibleFields (enum)

```typescript
export enum VisibleFields {
  // Основные поля
  humanReadableId = 'requestIdVisible',        // Номер заявки
  status = 'requestStatusVisible',             // Статус
  transportType = 'cargoTransportTypeVisible', // Тип тарифа
  author = 'authorVisible',                    // ФИО заявителя
  authorPhone = 'authorPhoneVisible',          // Телефон
  authorPersonnelNumber = 'authorPersonnelNumberVisible',
  costCenter = 'costCenterVisible',            // МВЗ
  desiredDate = 'desiredDateVisible',          // Плановая дата
  
  // Перевозчик
  contractor = 'carrierVisible',
  
  // Стоимость
  expectedCost = 'plannedPriceVisible',
  actualCost = 'actualCostVisible',
  economy = 'economyVisible',
  
  // Дальность
  expectedDistance = 'plannedRangeVisible',
  actualDistance = 'actualDistanceVisible',
  
  // Даты
  plannedDeliveryDate = 'plannedDeliveryDateVisible',
  transferTime = 'transferTimeVisible',        // Фактическая дата сбора
  shipmentTime = 'shipmentTimeVisible',        // Фактическая дата доставки
  deadlineDate = 'deadlineDateVisible',        // Контрольный срок
  
  // Адреса
  sender = 'senderVisible',
  senderPhone = 'senderPhoneVisible',
  senderAddress = 'waypointFromVisible',
  senderOrganization = 'senderOrganizationVisible',
  recipient = 'recipientVisible',
  recipientPhone = 'recipientPhoneVisible',
  recipientAddress = 'waypointToVisible',
  recipientOrganization = 'recipientOrganizationVisible',
  
  // Другое
  waypointsCount = 'waypointsCountVisible',
  weight = 'weightVisible',
  volume = 'volumeVisible',
  creationDate = 'creationDateVisible',
  creationTime = 'creationTimeVisible',
  source = 'sourceVisible',
  routeNumber = 'routeNumberVisible',
  templateNumber = 'templateNumberVisible',
  evaluation = 'evaluationVisible',
}
```

**Важно:** Значения — это ключи i18n для локализации заголовков колонок.

### sortFields (маппинг)

```typescript
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.desiredDate]: 'DESIRED_DATE',
};
```

**Важно:** Не все поля поддерживают сортировку — только явно указанные в маппинге.

### Типы данных

```typescript
// Тип строки таблицы
export type Row = {
  id: string;
} & { [key in VisibleFields]?: string };

// Тип колонки
export type Column = Omit<ColumnType<Row>, 'dataIndex'> & {
  dataIndex: VisibleFields;
  sortProperty?: string;
};

// Тип фильтров
export interface FilterValues {
  requestHumanId: string;
  desiredDate: DateFormValue;
  creationDate: DateFormValue;
  changeDate: DateFormValue;
  expectedCost: NumberRange;
  contractorSet: string[];
  authorFIO: string;
  requestStatusSet: string[];
  cargoTransportType?: string[];
}

// Преобразованные фильтры
export type TransformedFilterValues = Record<
  string,
  string | string[] | DateRangeISO | RangeNumber | undefined
>;

// Настройки видимости
export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

// Параметры запроса для API
export type RequestBodyCargoParams = {
  cargoUIVisibilityDTO?: ColumnVisibilitySettings;
} & SearchRequest;
```

---

## 5. Стили

### ListView.module.scss

```scss
.listView {
  .buttons {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding: 12px 0;
  }
}
```

### CargoRegistry.module.scss

```scss
.eyeIcon {
  color: var(--jade) !important;
  transform: scale(1.2);
  &:hover {
    transform: scale(1.5);
    transition: transform 0.5s;
  }
}
```

### Стили из Registry

Используются стили из `modules/Registry/components/`:

- `Table.module.scss` — таблица с закрашиванием строк
- `Filters.module.scss` — стили для модального окна фильтров
- `DisplayPeriod.module.scss` — отображение периода

---

## 6. Паттерны и лучшие практики

### 6.1. Интеграция с Registry

**Правило:** Используйте `CargoRegistryRouter.tsx`, а не `CargoRegistry.tsx`.

```typescript
// Registry.routes.tsx
import { Router as CargoOrdersRegistryRouter } from 'modules/CargoRegistry/CargoRegistryRouter';

export const tabRoutes = [
  {
    title: 'Грузовые перевозки',
    route: routes.REGISTRY_CARGO,
    tabs: [
      {
        title: 'Заявки',
        route: routes.REGISTRY_CARGO_ORDERS,
        component: <CargoOrdersRegistryRouter />,  // ← Вот так
      },
    ]
  },
];
```

### 6.2. Синхронизация хуков

**Правило:** Хуки должны передавать свои значения через `useMemo` в `executeSearch`.

```typescript
const searchParams = useMemo(() => ({
  filterValues,
  pageSetting,
  sortSetting,
  organizationId,
  isOrganization,
  executorGroupId,
}), [filterValues, pageSetting, sortSetting, organizationId, isOrganization, executorGroupId]);

useEffect(() => {
  if (!organizationId) return;
  if (!filterValues) return;
  executeSearch({ ...filterValues, pageSetting, sortSetting } as RegisterSearchQuery);
}, [searchParams, executeSearch, filterValues, organizationId]);
```

### 6.3. Сброс страницы при фильтрации

**Правило:** Сбрасывайте пагинацию на первую страницу при изменении фильтров.

```typescript
useEffect(() => {
  setPageSetting(setting => ({ page: 0, size: setting.size }));
}, [setPageSetting, filterValues]);
```

### 6.4. Обработка пустых значений в фильтрах

**Правило:** Удаляйте поля с пустыми значениями из фильтров.

```typescript
const setFiltersWithAdditionalCheck = (values: FilterValues) => {
  const copiedValues = { ...values };
  if (Array.isArray(values.cargoTransportType) && values.cargoTransportType.length === 0) {
    delete copiedValues.cargoTransportType;
    filterProps.form?.setFieldsValue({ cargoTransportType: undefined });
  }
  setFilterValues(copiedValues);
};
```

### 6.5. Валидация поиска по ID

**Правило:** Проверяйте длину ID (3-20 символов).

```typescript
const handleSearchId = (id: string) => {
  if (id.length === 0) {
    filterProps.form?.setFieldsValue({ requestHumanId: undefined });
    setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: undefined });
  }
  if (id.length < 3 || id.length > 20) return;
  filterProps.form?.setFieldsValue({ requestHumanId: id });
  setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: id });
};
```

### 6.6. Сортировка колонок

**Правило:** Используйте `sortFields` для преобразования в бэкенд-параметры.

```typescript
const onTableChange = (_pagination, _filters, sorter) => {
  const sorterElement = Array.isArray(sorter) ? sorter[0] : sorter;
  const sortProperty = sortFields[sorterElement?.column?.dataIndex as VisibleFields];
  if (sortProperty) {
    setSortSetting({
      property: sortProperty,
      directionAsc: sorterElement.order === 'ascend',
    });
  } else {
    setSortSetting(undefined);
  }
};
```

### 6.7. Форматирование дат

**Правило:** Используйте `toDateRangeISO` для преобразования дат.

```typescript
import { toDateRangeISO } from 'shared/components/DateInputWithAvailableFuture/utils';

const transformedFilterValues = useMemo(() => ({
  ...filterValues,
  desiredDate: filterValues?.desiredDate 
    ? toDateRangeISO(filterValues.desiredDate as any) 
    : undefined,
}), [filterValues]);
```

### 6.8. Форматирование чисел

**Правило:** Делите стоимость на 100 (конвертация из копеек).

```typescript
[VisibleFields.expectedCost]: (item.expected.cost / 100).toFixed(2).replace('.', ','),
```

### 6.9. Детальный просмотр

**Правило:** Используйте `useCargoTripInfo` + `useSearchCargoRegistry` для объединения данных.

```typescript
const { data: mainContent } = useCargoTripInfo(id);
const { data: { content: [auxContent] } } = useSearchCargoRegistry(
  { requestHumanId: mainContent.humanReadableId },
  {},
  organizationId
);
const transformedData = useTransformedData([{ ...auxContent, ...mainContent }]);
```

### 6.10. Экспорт данных

**Правило:** Используйте `ExportXlsButton` с правильными параметрами.

```typescript
<ExportXlsButton
  key="export-xls"
  disabled={!responseData?.content?.length}
  filterValues={filterValues}
  columnVisibility={visibilitySettings.columnVisibility}
/>
```

---

## 7. API вызовы

### cargo-registry-search.ts

```typescript
// Поиск по заявкам (организация)
export const useRegistryJournalDataDeffered = (orgId: string | null | undefined) => {
  const http = useHttp();
  return useAsyncFn(async (query: RegisterSearchQuery) => {
    const response = await http.post<SearchResponse>('/cargo-registry/journal', query);
    return response.data;
  }, [http, orgId]);
};

// Поиск по заявкам (исполнители)
export const useRegistryJournalDataExecutorDeffered = (execId?: string[]) => {
  const http = useHttp();
  return useAsyncFn(async (query: RegisterSearchQuery) => {
    const response = await http.post<SearchResponse>('/cargo-registry/journal-executor', query);
    return response.data;
  }, [http, execId]);
};

// Детальная информация о заявке
export const useCargoTripInfo = (id: string) => {
  const http = useHttp();
  return useAsyncFn(async () => {
    const response = await http.get<TripInfo>(`/cargo-registry/trip-info/${id}`);
    return response.data;
  }, [http, id]);
};
```

### reports.ts

```typescript
// Экспорт в Excel
export const downloadRegistryXLSAsync = async (
  http: HttpClient,
  transportType: string,
  mimeType: string,
  params: any,
  organizationId: string,
  logger: any,
  setIsLoading: (loading: boolean) => void,
  formatType: string = 'xls'
) => {
  setIsLoading(true);
  try {
    const response = await http.post(
      `/reports/${transportType}/registry/${formatType}`,
      params,
      { responseType: 'blob' }
    );
    downloadBlob(response, `cargo-registry-${formatType}-${organizationId}.xlsx`);
  } finally {
    setIsLoading(false);
  }
};
```

---

## 8. Особенности CargoRegistry

### 8.1. Выбор между organization и executorGroup

```typescript
const { organizationId, isOrganization, executorGroupId } = useOrganizationContext();

// isOrganization = true → orgHook
// isOrganization = false → executorHook
```

### 8.2. Две колонки сортировки

В `CargoRegistry` доступна сортировка только по двум полям:
- `REQUEST_HUMAN_ID` (номер заявки)
- `DESIRED_DATE` (плановая дата)

### 8.3. Скрытые поля

Некоторые поля закомментированы (из-за задачи TRANSPORT 11293):
```typescript
// expectedTime
// express
// purpose
```

### 8.4. Карты в DetailedView

Используется `MapComponent` с `markers` и `polylines`:
```typescript
<MapComponent
  className={styles.map}
  markers={mainContent.expected?.waypoints}
  polylines={mainContent.expected?.segments}
  dragging
/>
```

### 8.5. Две кнопки экспорта

`ExportXlsButton` поддерживает два формата:
- `formatType="xls"` — стандартный Excel
- `formatType="cse"` — специальный формат KCE

---

### 8.6. Логика загрузки данных (отложенная инициализация)

**Состояния отображения:**
1. **При первом рендере** — отображается заглушка вместо таблицы
2. **После применения фильтров** — устанавливается `showTable = true`, затем отправляется запрос к API
3. **При получении данных** — отображается таблица с пагинацией

**Реализация в ListView.tsx:**
```typescript
const [showTable, setShowTable] = useState(false);

// Логика отправки запроса
useEffect(() => {
  const hasValidFilters = filterValues !== undefined && filterValues !== null;
  if (!hasValidFilters) return;

  // Запрос отправляем только если showTable === true
  const shouldSearch = showTable && (!hasSearchedRef.current || paramsChanged);

  if (shouldSearch) {
    executeSearch({ ...filterValues, pageSetting, sortSetting });
    hasSearchedRef.current = true;
  }
}, [searchParams, executeSearch, filterValues, organizationId, showTable]);

// Установка showTable при применении фильтров
const setFiltersWithAdditionalCheck = (values: FilterValues) => {
  // ... обработка значений
  setFilterValues(copiedValues);
  setShowTable(true);  // ← После применения фильтров
};

// Условный рендеринг
{showTable ? (
  <Table ... />
) : (
  <div className={styles.placeholder}>
    <h3>Для получения данные по реестрам заявок нужно выбрать фильтры</h3>
  </div>
)}
```

**Стили заглушки (ListView.module.scss):**
```scss
.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  padding: 40px 20px;

  h3 {
    color: var(--dark-grey);
    font-family: "SB Sans Text", sans-serif;
    font-size: 18px;
    font-weight: 500;
    line-height: 26px;
    text-align: center;
  }
}
```

**Почему это важно:**
- Снижает нагрузку на бэкенд — запросы отправляются только при наличии фильтров
- Улучшает UX — пользователь видит, что нужно выбрать фильтры перед получением данных
- Избегает пустых запросов при клике на таб

---

## 9. Чеклист при разработке

- [ ] Создан `XXXRouter.tsx` с `ToolbarProvider`
- [ ] Определены `VisibleFields` и `sortFields` в `constants.ts`
- [ ] Определены типы `Row`, `Column`, `FilterValues` в `types.ts`
- [ ] Реализованы хуки: `useColumns`, `useFilter`, `useSorting`, `usePagination`, `useTransformedData`
- [ ] Реализован `useDeferredSearch` или `useDataSource`
- [ ] Создан `ListView.tsx` с интеграцией всех хуков
- [ ] Создан `DetailedView.tsx` с картой и описанием
- [ ] Реализован экспорт через `ExportXlsButton`
- [ ] Настроена видимость колонок через `ColumnVisibilitySettings`
- [ ] Добавлены стили в `styles.module.scss`
- [ ] Протестирована сортировка, фильтрация, пагинация
- [ ] Проверена интеграция с `useDeferredSearch` для organization/executorGroup
- [ ] Протестирован экспорт с фильтрами
- [ ] Проверено форматирование дат и чисел
- [ ] Реализована логика `showTable` для отложенной загрузки данных
- [ ] Добавлены переводы в i18n

---

**Примечание:** Всегда следуйте существующим паттернам в `CargoRegistry` — это эталонная реализация для всех модулей реестра.
