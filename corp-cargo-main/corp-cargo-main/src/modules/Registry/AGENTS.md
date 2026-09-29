# Инструкция для модуля Registry

## 1. Архитектура модуля Registry

### Обзор архитектуры

Модуль `Registry` — это **общий реестр**, который служит контейнером для всех специализированных реестров (Грузовые перевозки, Маршруты, Компенсации и т.д.). Он реализует шаблон **Unified Registry Pattern**, где каждый модуль интегрируется как вкладка с собственной логикой.

### Основные файлы

```
src/modules/Registry/
├── Registry.tsx                    # Центральный компонент с табами
├── RegistryFilterContext.tsx       # Context для управления фильтрами
├── styles.module.scss              # Общие стили модуля
├── constants/
│   ├── filters.tsx                 # Константы фильтров (безопасно для переиспользования)
│   └── Registry.routes.tsx         # Конфигурация маршрутов и табов
├── components/
│   ├── ActionsRegistry/            # Поиск и кнопки управления
│   ├── Cells/                      # Ячейки таблицы (ссылки, спейсеры)
│   ├── ColumnVisibilitySettings/   # Настройка видимости колонок
│   ├── DefaultSorting/             # Кнопка сортировки по умолчанию
│   ├── DisplayPeriod/              # Отображение периода
│   ├── ExportButton/               # Кнопка экспорта
│   ├── ExportKceButton/            # Экспорт KCE
│   ├── ExportXlsButton/            # Экспорт XLS
│   ├── Filters/                    # Модальное окно фильтров
│   ├── Modals/                     # Дополнительные модалки
│   ├── Table/                      # Обёртка над Ant Design Table
│   ├── TableSettingsButton/        # Кнопка настроек таблицы
│   └── TableSettingsContainer/     # Контейнер для настроек
```

### Registry.tsx — центральный компонент

**Назначение:** Управляет табами и маршрутизацией между модулями.

**Ключевые особенности:**
- Использует `antd/Tabs` для отображения вкладок
- Поддерживает вложенные табы (2 уровня вложенности)
- Использует `React.lazy` для ленивой загрузки модулей
- Контролирует роутинг через `history.push()` и `useLocation`
- Фильтрует табы по ролям пользователя через `allowedRoles`

**Структура табов:**
```typescript
// Registry.routes.tsx
export const tabRoutes = [
  {
    title: 'Грузовые перевозки',
    route: routes.REGISTRY_CARGO,
    tabs: [  // Вложенные табы
      {
        title: 'Заявки',
        route: routes.REGISTRY_CARGO_ORDERS,
        component: <CargoOrdersRegistryRouter />,
      },
      // ...
    ]
  },
];
```

### RegistryFilterContext.tsx — управление фильтрами

**Назначение:** Предоставляет контекст для хранения и синхронизации значений фильтров между компонентами.

**API:**
```typescript
interface RegistryFiltersContext<T> {
  filterValues: T;                    // Текущие значения фильтров
  setFilterValues: Dispatch<SetStateAction<T>>;  // Сеттер для фильтров
}

// Использование
const { filterValues, setFilterValues } = useRegistryFilters<FilterValues>();
```

**Важно:** Каждый модуль может использовать свой тип фильтров, контекст поддерживает generics.

### Маршрутизация

Система поддерживает:
1. **Одноуровневые роуты** — прямые табы (`OneLevelRoute`)
2. **Двухуровневые роуты** — группы табов (`TwoLevelRoutes`)

**Поведение роутера:**
- При переходе на несуществующий роут — редирект на первый доступный
- При переходе в группу без указанного подроута — редирект на первый подтаб
- Ключ активной вкладки = текущий путь (`pathname`)

---

## 2. Основные компоненты

### ActionsRegistry

**Назначение:** Контейнер для поиска и кнопок управления.

**Пропсы:**
```typescript
interface Props {
  idValue?: string;                  // Значение поиска по ID
  handleSearchId?: (id: string) => void;  // Обработчик поиска
  handleChangeId?: (id: string) => void;  // Обработчик изменения поля
  buttons?: ReactNode[];             // Кнопки действий (экспорт и др.)
  filters?: ReactNode;               // Компонент фильтров
  isVisible?: boolean;               // Открыт ли фильтр
  setIsVisible?: Dispatch<SetStateAction<boolean>>;  // Сеттер состояния фильтра
  isNewDesign?: boolean;             // Использовать новый дизайн
  placeholder?: string;              // Текст плейсхолдера
}
```

**Пример использования:**
```tsx
<ActionsRegistry
  isNewDesign
  handleSearchId={handleSearchId}
  isVisible={isVisible}
  setIsVisible={setIsVisible}
  filters={<FilterPanel {...filterProps} />}
  buttons={[
    <ExportXlsButton key="export" />,
    <Button key="action">Действие</Button>
  ]}
/>
```

### Table

**Назначение:** Обёртка над Ant Design Table с дополнительной логикой.

**Функционал:**
- Добавляет `Spin` во время загрузки
- Автоматическая пагинация с размерами 10/20/50/100
- Закраска чётных строк в серый цвет
- Поддержка всех `TableProps` из Ant Design

**Пропсы:**
```typescript
type Props<T> = {
  className?: string;
  isFetching?: boolean;  // Показывать ли спиннер
} & TableProps<T>;
```

**Пример использования:**
```tsx
<Table
  isFetching={isLoading}
  dataSource={data}
  columns={columns}
  rowKey="id"
  onChange={onTableChange}
  pagination={pagination}
/>
```

### ColumnVisibilitySettings

**Назначение:** Управление видимостью колонок таблицы.

**Пропсы:**
```typescript
interface Props {
  setting?: PublicUIVisibilityDTO;       // Текущие настройки
  saveSettingChange: (key: Record<string, boolean | undefined>) => void;  // Сохранение
  isButtonDisabled?: boolean;            // Блокировка кнопки
  defaultColumns?: Record<string, boolean>;  // Колонки по умолчанию
  settingsEntries?: Record<string, string>;  // Локализованные названия
  disabled?: boolean;                    // Полная блокировка
}
```

**Пример использования:**
```tsx
<ColumnVisibilitySettings
  setting={columnVisibility}
  saveSettingChange={setColumnVisibility}
  defaultColumns={defaultVisibility}
  settingsEntries={t.Forms.registryCargoSettings}
/>
```

### DefaultSorting

**Назначение:** Кнопка для сброса сортировки к значениям по умолчанию.

**Пропсы:** Все `ButtonProps` из Ant Design + `className`.

**Пример использования:**
```tsx
<DefaultSorting onClick={handleDefaultSort} />
```

### DisplayPeriod

**Назначение:** Отображение выбранного периода дат.

**Пропсы:**
```typescript
interface Props {
  className?: string;
  period: (DateRange | DateRangeISO)[];  // Массив периодов
}
```

**Пример использования:**
```tsx
<DisplayPeriod period={filterValues.desiredDate} />
```

### FiltersRegistryWrapper

**Назначение:** Модальное окно для фильтров.

**Пропсы:**
```typescript
interface Props {
  isVisible?: boolean;
  isNewDesign?: boolean;
  setIsVisible?: Dispatch<SetStateAction<boolean>>;
  children: ReactNode;  // Компонент фильтров
}
```

**Пример использования:**
```tsx
<FiltersRegistryWrapper
  isVisible={isVisible}
  setIsVisible={setIsVisible}
  isNewDesign
>
  <FilterPanel {...filterProps} />
</FiltersRegistryWrapper>
```

### Modal компоненты

**ExportXLSModal** — расширенная модалка для экспорта с настройками:

```typescript
interface ModalProps<T, P, C> {
  transportType: string;
  filterParams: T;
  mimeType: XLSX_MIME_TYPE | XLS_MIME_TYPE | XLSM_MIME_TYPE;
  widthFields?: boolean;  // Показывать чекбокс "с views"
  columnsVisibility?: C;
  onRequestParamsOnXlsDownload: (params, orgId, purposes) => P;
}
```

---

## 3. Хуки для разработки модулей

Все хуки находятся в `src/modules/[MODULE_NAME]/hooks/`.

### useColumns

**Назначение:** Генерация колонок таблицы на основе `VisibleFields` и настроек пользователя.

**Возвращает:**
```typescript
interface UseColumnsResult {
  columns: Column[];  // Массив колонок для Table
  settings: {
    columnVisibility: ColumnVisibilitySettings;  // Текущая видимость
    setColumnVisibility: (settings) => void;    // Сохранить настройки
    isSaving: boolean;                           // Загрузка сохранения
    defaultVisibility: ColumnVisibilitySettings; // Видимость по умолчанию
  };
}
```

**Как работает:**
1. Загружает настройки пользователя из API
2. Собирает колонки из `VisibleFields`, учитывая видимость
3. Добавляет сортировку, если для поля определён `sortProperty`
4. Сортировка первого столбца фиксируется слева (`fixed: 'left'`)

**Пример:**
```tsx
const { columns, settings } = useColumns(pathname, sortSetting);

// columns готовы к передаче в <Table columns={columns} />
```

### useFilter

**Назначение:** Управление фильтрами, генерация полей формы.

**Возвращает:**
```typescript
interface UseFilterResult {
  setFilterValues: Dispatch<SetStateAction<FilterValues>>;
  isStatusChangeActive: boolean;
  setIsStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  setForm: Dispatch<SetStateAction<FormInstance<any>>>;
  onClearButtonActivator: () => void;
  fields: FormSectionProps[];  // Поля формы фильтров
  filterValues?: TransformedFilterValues;  // Преобразованные значения
  form?: FormInstance<any>;
}
```

**Поля формы формируются автоматически из `ModelFormFieldType`:**
- `TEXT` — Input
- `SELECT` — Select (single/multiple)
- `CUSTOM` — произвольный компонент

**Преобразование значений:**
- `desiredDate` → `DateRangeISO`
- `creationDate` → `DateRangeISO`
- `expectedCost` → `{ start, end }` (в копейках)

**Пример:**
```tsx
const { fields, filterValues, form, setFilterValues } = useFilter();

// Передать в FilterPanel
<FilterPanel
  fields={fields}
  form={form}
  onApplyFilters={setFilterValues}
/>
```

### useSorting

**Назначение:** Управление сортировкой.

**Возвращает:**
```typescript
interface UseSortingResult {
  sortSetting?: SortSetting;  // { property: string, directionAsc: boolean }
  setSortSetting: (setting?: SortSetting) => void;
  handleDefaultSort: () => void;  // Сброс к дефолту
}
```

**Пример:**
```tsx
const { sortSetting, setSortSetting, handleDefaultSort } = useSorting();

// В Table onChange
const onTableChange = (_pagination, _filters, sorter) => {
  const sortField = sortFields[sorter.column?.dataIndex];
  if (sortField) {
    setSortSetting({
      property: sortField,
      directionAsc: sorter.order === 'ascend',
    });
  }
};
```

### usePagination

**Назначение:** Управление пагинацией.

**Возвращает:**
```typescript
interface UsePaginationResult {
  pageSetting: { page: number; size: number };
  setPageSetting: (setting: { page: number; size: number }) => void;
}
```

**Пример:**
```tsx
const { pageSetting, setPageSetting } = usePagination();

const pagination = {
  total: data?.totalElements || 0,
  current: pageSetting.page + 1,
  onChange: (page, size) => setPageSetting({ page: page - 1, size }),
};

<Table pagination={pagination} />
```

### useTransformedData

**Назначение:** Трансформация сырых данных для отображения.

**Пример:**
```tsx
const transformedData = useTransformedData(rawData);

// Использовать в Table dataSource
<Table dataSource={transformedData} />
```

### useDeferredSearch

**Назначение:** Отложенный поиск с дебаунсом и предотвращением лишних запросов.

**Возвращает:**
```typescript
interface UseDeferredSearchResult {
  executeSearch: (query: Query) => Promise<void>;
  data: Response | undefined;
  isLoadingOrg: boolean;   // Загрузка для organization
  isLoadingExec: boolean;  // Загрузка для executorGroup
}
```

**Логика:**
- Сравнивает предыдущие и текущие параметры
- Не делает запрос при отсутствии изменений
- Использует один из хуков: `orgHook` или `executorHook`

**Пример:**
```tsx
const { executeSearch, data, isLoadingOrg, isLoadingExec } = useDeferredSearch<
  RegisterSearchQuery,
  SearchResponse
>({
  orgHook: useRegistryJournalDataDeffered,
  executorHook: useRegistryJournalDataExecutorDeffered,
  organizationId,
  executorGroupId,
  isOrganization,
  initialData: undefined,
});

// Выполнить поиск
executeSearch({ ...filterValues, pageSetting, sortSetting });
```

---

## 4. Типы и константы

### VisibleFields

**Назначение:** Enum всех полей, которые могут быть показаны в таблице.

**Правила:**
- Значения — это ключи i18n для локализации заголовков
- Следует использовать при определении колонок

**Пример из CargoRegistry:**
```typescript
export enum VisibleFields {
  humanReadableId = 'requestIdVisible',
  status = 'requestStatusVisible',
  transportType = 'cargoTransportTypeVisible',
  // ...
}
```

### sortFields

**Назначение:** Маппинг полей в параметры сортировки для бэкенда.

```typescript
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.desiredDate]: 'DESIRED_DATE',
};
```

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

// Тип фильтров (базовый)
export type FilterValues = {
  requestHumanId: string;
  desiredDate: DateFormValue;
  creationDate: DateFormValue;
  // ...
};

// Преобразованные фильтры для API
export type TransformedFilterValues = Record<
  string,
  string | string[] | DateRangeISO | RangeNumber | undefined
>;

// Настройки видимости колонок
export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

// Параметры запроса для API
export type RequestBodyCargoParams = {
  cargoUIVisibilityDTO?: ColumnVisibilitySettings;
} & SearchRequest;
```

---

## 5. Примеры структуры модуля

### Структура нового модуля реестра

```
src/modules/MyRegistry/
├── MyRegistry.tsx              # Основной компонент (если нужен отдельный роутер)
├── MyRegistryRouter.tsx        # Роутер с ListView и DetailedView
├── constants.ts                # VisibleFields, sortFields
├── types.ts                    # Типы данных
├── components/
│   ├── ListView.tsx            # Список записей
│   ├── DetailedView.tsx        # Детальный просмотр
│   ├── ExportXlsButton.tsx     # Экспорт
│   └── Filters/                # Фильтры
├── hooks/
│   ├── useColumns.tsx
│   ├── useFilter.tsx
│   ├── useSorting.ts
│   ├── usePagination.ts
│   ├── useTransformedData.ts
│   └── useDeferredSearch.tsx
├── api/                        # API вызовы (если отдельные)
├── stores/                     # MobX сторы (если отдельные)
└── styles.module.scss          # Стили
```

### RegistryRouter.tsx

```typescript
import { ToolbarProvider } from 'components/Toolbar';
import React, { FC } from 'react';
import { Route, Switch, useLocation, useParams } from 'react-router-dom';
import { DetailedView } from './components/DetailedView';
import { ListView } from './components/ListView';
import * as routes from 'constants/constants.routes';

export const MyRegistryRouter: FC = () => {
  const { id } = useParams<{ id?: string }>();
  const { pathname } = useLocation();

  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path={routes.REGISTRY_MY_ITEMS}
          render={() => <ListView pathname={pathname} />}
          exact
        />
        <Route
          path={routes.REGISTRY_MY_ITEMS_VIEW}
          component={() => <DetailedView id={id as string} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

export default MyRegistryRouter;
```

### ListView.tsx — базовая реализация

```typescript
import React, {
  FC, useCallback, useMemo, useState, useRef, useEffect
} from 'react';
import { Key, SorterResult, TablePaginationConfig } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import { useOrganizationContext } from 'context/Organization.context';
import { RegisterSearchQuery } from "api/register-search";
import { useRegistryJournalDataDeffered } from 'api/my-registry-search';
import { SearchResponse } from "stores/MyRegistry/MyRegistry.interface";
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { FilterPanel } from 'shared/components/FilterPanel';
import { sortFields, VisibleFields } from '../constants';
import { useFilter } from '../hooks/useFilter';
import { useColumns } from '../hooks/useColumns';
import { useSorting } from '../hooks/useSorting';
import { usePagination } from '../hooks/usePagination';
import { useTransformedData } from "../hooks/useTransformedData";
import { useDeferredSearch } from '../hooks/useDefferedSearch';
import { FilterValues, Row } from '../types';
import { ExportXlsButton } from './ExportXlsButton';

export const ListView: FC<ListViewProps> = ({ pathname }) => {
  const { pageSetting, setPageSetting } = usePagination();
  const { sortSetting, setSortSetting, handleDefaultSort } = useSorting();
  const { filterValues, setFilterValues, ...filterProps } = useFilter();
  const { columns, settings: visibilitySettings } = useColumns(pathname, sortSetting);
  const [isVisible, setIsVisible] = useState(false);

  const { organizationId, isOrganization, executorGroupId } = useOrganizationContext();

  const { executeSearch, data: responseData, isLoadingOrg } = useDeferredSearch<
    RegisterSearchQuery,
    SearchResponse
  >({
    orgHook: useRegistryJournalDataDeffered,
    executorHook: useRegistryJournalDataExecutorDeffered,
    organizationId,
    executorGroupId,
    isOrganization: isOrganization ?? false,
    initialData: undefined,
  });

  const transformedData = useTransformedData(responseData?.content || []);

  const prevParamsRef = useRef<any>(null);
  const hasSearchedRef = useRef(false);

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

    const hasValidFilters = filterValues !== undefined && filterValues !== null;
    if (!hasValidFilters) return;

    const paramsChanged = JSON.stringify(prevParamsRef.current) !== JSON.stringify(searchParams);
    const shouldSearch = !hasSearchedRef.current || paramsChanged;

    if (shouldSearch) {
      executeSearch({ ...filterValues, pageSetting, sortSetting } as RegisterSearchQuery)
        .catch((error) => console.error('Search failed:', error));
      prevParamsRef.current = searchParams;
      hasSearchedRef.current = true;
    }
  }, [searchParams, executeSearch, filterValues, organizationId]);

  const pagination = useMemo(
    () => ({
      total: responseData?.totalElements || 0,
      current: pageSetting.page + 1,
      showSizeChanger: true,
      pageSize: pageSetting.size,
      onChange: (page, size) => setPageSetting({ page: page - 1, size: size || pageSetting.size }),
    }),
    [responseData?.totalElements, pageSetting, setPageSetting]
  );

  const onTableChange = useCallback(
    <RecordType extends Row>(
      _pagination: TablePaginationConfig,
      _filters: Record<string, (Key | boolean)[] | null>,
      sorter: SorterResult<RecordType>
    ) => {
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
    },
    [setSortSetting]
  );

  const handleSearchId = (id: string) => {
    if (id.length === 0) {
      filterProps.form?.setFieldsValue({ requestHumanId: undefined });
      setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: undefined });
    }
    if (id.length < 3 || id.length > 20) return;
    filterProps.form?.setFieldsValue({ requestHumanId: id });
    setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: id });
  };

  return (
    <div>
      <ActionsRegistry
        isNewDesign
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={<FilterPanel {...filterProps} isNewDesign onApplyFilters={setFilterValues} />}
        buttons={[
          <ExportXlsButton
            key="export"
            disabled={!responseData?.content?.length}
            filterValues={filterValues}
            columnVisibility={visibilitySettings.columnVisibility}
          />,
        ]}
      />

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} />
        <ColumnVisibilitySettings
          setting={visibilitySettings.columnVisibility}
          saveSettingChange={visibilitySettings.setColumnVisibility}
          defaultColumns={visibilitySettings.defaultVisibility}
          isButtonDisabled={visibilitySettings.isSaving}
          settingsEntries={t.Forms.registryMySettings}
        />
      </TableSettingsContainer>

      <Table
        isFetching={isOrganization ? isLoadingOrg : isLoadingExec}
        dataSource={transformedData || []}
        columns={columns}
        bordered
        size="small"
        rowKey={VisibleFields.humanReadableId}
        onChange={onTableChange}
        pagination={pagination}
        scroll={{ x: true }}
      />
    </div>
  );
};
```

### DetailedView.tsx — базовая реализация

```typescript
import React, { FC, Suspense, useMemo } from 'react';
import { Descriptions } from 'antd';
import { useTranslation } from 'i18n';
import { useMyTripInfo } from 'api/my-registry-search';
import { VisibleFields } from '../constants';
import { useTransformedData } from '../hooks/useTransformedData';

interface DetailedViewProps {
  id: string;
}

export const DetailedView: FC<DetailedViewProps> = ({ id }) => {
  const { data: mainContent } = useMyTripInfo(id);
  const transformedData = useTransformedData([{ ...mainContent }]);
  const { t } = useTranslation();

  const records = useMemo(() => {
    const labels = t.Forms.registryMySettings;
    const transformed = transformedData[0] || {};
    return (Object.keys(transformed) as VisibleFields[]).map(key => ({
      key,
      label: labels[key],
      value: transformed[key],
    }));
  }, [transformedData, t]);

  return mainContent ? (
    <div className={styles.tripDetailedView}>
      {/* Карты, если нужно */}
      <div className={styles.tableWrapper}>
        <Descriptions size="small" column={1}>
          {records.map(({ key, label, value }) => (
            <Descriptions.Item key={key} label={label}>
              <span>{value}</span>
            </Descriptions.Item>
          ))}
        </Descriptions>
      </div>
    </div>
  ) : null;
};
```

### constants.ts — определение полей

```typescript
export enum VisibleFields {
  humanReadableId = 'requestIdVisible',
  status = 'requestStatusVisible',
  // Добавить свои поля
}

export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.desiredDate]: 'DESIRED_DATE',
};
```

### types.ts — типы данных

```typescript
import { ColumnType } from 'antd/lib/table/interface';
import { NumberRange, SearchRequest } from 'stores/MyRegistry/MyRegistry.interface';
import { DateRangeISO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { RangeNumber } from 'api/register-search';
import { VisibleFields } from './constants';

export type Row = {
  id: string;
} & { [key in VisibleFields]?: string };

export type Column = Omit<ColumnType<Row>, 'dataIndex'> & {
  dataIndex: VisibleFields;
  sortProperty?: string;
};

export interface FilterValues {
  requestHumanId: string;
  desiredDate: DateFormValue;
  statusSet: string[];
  // Добавить свои поля
}

export type TransformedFilterValues = Record<string, string | string[] | DateRangeISO | RangeNumber | undefined>;

export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

export type RequestBodyParams = { myUIVisibilityDTO?: ColumnVisibilitySettings } & SearchRequest;
```

---

## 6. Стили и CSS модули

### Общие стили (styles.module.scss)

```scss
.container {
  position: relative;
  padding: 24px;
  background-color: #fff;
  height: 100%;
  font-family: "SB Sans Text", sans-serif;
  border-radius: 12px;
  width: 100%;

  :global(.ant-tabs-ink-bar) {
    height: 4px !important;
    border-radius: 4px;
  }

  :global(.ant-tabs-content-holder) {
    :global(.ant-tabs-nav-wrap) {
      max-width: calc(100% - 280px);
      overflow: auto !important;
    }
  }
}

.tabMargins {
  :global(.ant-tabs-tab) {
    margin: 0 0 0 14px;
  }
}
```

### Стили таблицы (Table.module.scss)

```scss
.table {
  min-height: 100px;
  padding: 0 !important;
  overflow: inherit !important;
  border-top: 1px solid #f2f2f2;

  :global(.ant-table-sticky-scroll) {
    display: none;
  }

  :global(.ant-table-footer) {
    display: flex;
    justify-content: center;
  }

  .cell {
    font-family: "SB Sans Text", sans-serif;
    font-size: 14px;
    line-height: 22px;
    font-weight: 500;
    white-space: nowrap;
    letter-spacing: -0.3px;
  }

  :global(thead.ant-table-thead) {
    tr {
      th {
        background: #fff;
        border: none;
        &:not(:last-child)::before {
          display: none;
        }
      }
    }
    :global(.ant-table-cell) {
      color: var(--inactive-tab);
      @extend .cell;
    }
  }

  :global(.ant-table-tbody) {
    tr {
      td {
        border: none;
      }
    }
    :global(.ant-table-cell) {
      white-space: nowrap;
      color: var(--dark-grey);
      @extend .cell;
    }
  }

  .link {
    color: var(--dodger-blue);
    &:hover { color: #0080e0; }
    &:active { color: #006ec2; }
  }
}

.rowDark {
  background-color: #f6f7f9;
  :global(.ant-table-cell) { background-color: #f6f7f9; }
}
```

### Стили фильтров (Filters.module.scss)

```scss
.modal {
  width: 75% !important;
}

.newModal {
  width: 90% !important;
  :global(.ant-modal) {
    max-width: 1136px;
    &-content {
      box-shadow: 0px 4px 8px -4px rgba(0, 0, 0, 0.1);
      border-radius: 12px;
    }
    &-header {
      border-radius: 12px 12px 0 0;
      padding: 24px 24px 0;
      border: none;
    }
  }
}

.textField {
  border-radius: 8px;
  &:hover { border-color: var(--silver); }
  &:focus { border-color: var(--pistachio); }
}

.selectField {
  :global(.ant-select-selector) {
    min-height: 48px;
    border-radius: 8px;
  }
}
```

---

## 7. Паттерны и лучшие практики

### 7.1. Работа с context

**Правило:** Используйте `RegistryFiltersContext` для передачи фильтров между компонентами.

**Пример:**
```tsx
// Provider в родителе
<RegistryFiltersProvider<FilterValues> defaultFilters={defaultFilters}>
  <MyComponent />
</RegistryFiltersProvider>

// Внутри компонента
const { filterValues, setFilterValues } = useRegistryFilters<FilterValues>();
```

### 7.2. Организация экспорта

**Правило:** Используйте `ExportXLSModal` для всех экспортов.

```tsx
const exportParams = {
  transportType: 'cargo',
  filterParams,
  mimeType: XLSX_MIME_TYPE,
  columnsVisibility: visibilitySettings.columnVisibility,
  onRequestParamsOnXlsDownload: (params, orgId, purposes) => ({
    ...params,
    organizationId: orgId,
    purposes,
  }),
};

<ExportXLSModal {...exportParams} />
```

### 7.3. Настройка видимости колонок

**Правило:** Сохраняйте настройки через `useColumns().settings.setColumnVisibility`.

```tsx
const { settings } = useColumns(pathname, sortSetting);

// В ColumnVisibilitySettings
<ColumnVisibilitySettings
  setting={settings.columnVisibility}
  saveSettingChange={settings.setColumnVisibility}
  defaultColumns={settings.defaultVisibility}
  isButtonDisabled={settings.isSaving}
  settingsEntries={t.Forms.registryMySettings}
/>
```

### 7.4. Оптимизация производительности

**Правило:** Используйте `useDeferredSearch` для предотвращения лишних запросов.

**Почему:**
- Сравнивает предыдущие и текущие параметры
- Избегает дублирующих запросов при быстром изменении фильтров
- Автоматически задерживает запрос (debounce)

### 7.5. Использование трансформации данных

**Правило:** Всегда используйте `useTransformedData` для приведения данных к формату таблицы.

**Почему:**
- Единообразное форматирование значений
- Консистентное отображение дат и чисел
- Централизованная логика преобразований

### 7.6. Сортировка

**Правило:** Используйте `sortFields` маппинг для бэкенд-параметров.

```typescript
// constants.ts
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.desiredDate]: 'DESIRED_DATE',
};

// useSorting
const onTableChange = (_pagination, _filters, sorter) => {
  const sortField = sortFields[sorter.column?.dataIndex as VisibleFields];
  if (sortField) {
    setSortSetting({
      property: sortField,
      directionAsc: sorter.order === 'ascend',
    });
  }
};
```

### 7.7. Валидация фильтров

**Правило:** Проверяйте длину строковых фильтров (3-20 символов для ID).

```tsx
const handleSearchId = (id: string) => {
  if (id.length < 3 || id.length > 20) return;
  setFilterValues({ ...filterValues, requestHumanId: id });
};
```

### 7.8. Форматирование дат

**Правило:** Используйте `toDateRangeISO` для преобразования дат перед отправкой.

```tsx
import { toDateRangeISO } from 'shared/components/DateInputWithAvailableFuture/utils';

const transformedFilterValues = useMemo(() => ({
  ...filterValues,
  desiredDate: filterValues?.desiredDate 
    ? toDateRangeISO(filterValues.desiredDate as any) 
    : undefined,
}), [filterValues]);
```

### 7.9. Обработка пустых значений

**Правило:** Удаляйте поля с пустыми значениями из фильтров.

```tsx
const setFiltersWithAdditionalCheck = (values: FilterValues) => {
  const copiedValues = { ...values };
  if (Array.isArray(values.cargoTransportType) && values.cargoTransportType.length === 0) {
    delete copiedValues.cargoTransportType;
  }
  setFilterValues(copiedValues);
};
```

### 7.10. Путь к ячейке с детальным просмотром

**Правило:** Раскомментируйте код для рендеринга ссылки в колонке ID.

```tsx
// В useColumns
if (item === VisibleFields.humanReadableId) {
  column.render = (value: string, record: Row) => (
    <CellLink to={`${pathname}/${record.id}`}>{value}</CellLink>
  );
  column.fixed = 'left';
}
```

---

## 8. Чеклист при добавлении нового модуля

- [ ] Создана структура папок (components, hooks, api, stores)
- [ ] Определены `VisibleFields` и `sortFields` в `constants.ts`
- [ ] Определены типы `Row`, `Column`, `FilterValues` в `types.ts`
- [ ] Создан `XXXRouter.tsx` с ListView и DetailedView
- [ ] Реализованы хуки (useColumns, useFilter, useSorting, usePagination)
- [ ] Настроена интеграция с Registry через `Registry.routes.tsx`
- [ ] Добавлены API вызовы для получения данных
- [ ] Реализован экспорт через `ExportXLSModal`
- [ ] Настроена видимость колонок через `ColumnVisibilitySettings`
- [ ] Добавлены стили в `styles.module.scss`
- [ ] Протестирована сортировка, фильтрация, пагинация
- [ ] Проверена интеграция с `useDeferredSearch`

---

**Примечание:** Всегда следуйте существующим паттернам в `CargoRegistry` — это эталонная реализация.
