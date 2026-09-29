import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';

// ==========================
// 1. ГЛОБАЛЬНЫЕ ПОЛИФИЛЫ И ПОДАВЛЕНИЕ ШУМА
// ==========================
if (typeof global.fetch === 'undefined') {
  global.fetch = jest.fn(() => Promise.resolve({ json: () => Promise.resolve({}) })) as any;
}

beforeAll(() => {
  jest.spyOn(console, 'error').mockImplementation(() => {});
  jest.spyOn(console, 'warn').mockImplementation(() => {});
});

afterAll(() => {
  jest.restoreAllMocks();
});

// ==========================
// 2. МОКИ ЗАВИСИМОСТЕЙ И ХУКОВ
// ==========================
jest.mock('api/cargo-registry-search', () => ({
  useRegistryJournalDataDeffered: jest.fn(),
  useRegistryJournalDataExecutorDeffered: jest.fn(),
}));
jest.mock('api/register-search', () => ({}));

jest.mock('context/Organization.context', () => ({ useOrganizationContext: jest.fn() }));
jest.mock('shared/hooks/usePagination', () => ({ usePagination: jest.fn() }));
jest.mock('../hooks/useSorting', () => ({ useSorting: jest.fn() }));
jest.mock('../hooks/useFilter', () => ({ useFilter: jest.fn() }));
jest.mock('../hooks/useColumns', () => ({ useColumns: jest.fn() }));
jest.mock('../hooks/useDefferedSearch', () => ({ useDeferredSearch: jest.fn() }));
jest.mock('../hooks/useTransformedData', () => ({ useTransformedData: jest.fn() }));

jest.mock('../constants', () => ({
  sortFields: { status: 'statusField' },
  VisibleFields: { humanReadableId: 'humanReadableId' },
}));
jest.mock('./utils', () => ({ hasAnyActiveFilter: jest.fn() }));

// ИСПРАВЛЕННЫЙ МОК i18n
jest.mock('i18n', () => {
  const t: any = (key: string) => key;
  t.Forms = {
    registryCargoSettings: 'mocked-registry-cargo-settings',
  };
  return { useTranslation: () => ({ t }) };
});

jest.mock('shared/decorators/withErrorBoundary', () => (c: any) => c);
jest.mock('./ListView.module.scss', () => ({
  listView: 'listView',
  placeholder: 'placeholder',
  placeholderText: 'placeholderText',
}));

// ==========================
// 3. МОКИ ДОЧЕРНИХ КОМПОНЕНТОВ (ВОЗВРАЩАЕМ ОБЪЕКТЫ С КЛЮЧАМИ!)
// ==========================
jest.mock('modules/Registry/components/ActionsRegistry/ActionsRegistry', () => ({
  ActionsRegistry: (props: any) => (
    <div data-testid="actions">
      <button data-testid="search-empty" onClick={() => props.handleSearchId('')}>Empty</button>
      <button data-testid="search-short" onClick={() => props.handleSearchId('12')}>Short</button>
      <button data-testid="search-valid" onClick={() => props.handleSearchId('12345')}>Valid</button>
      <button data-testid="search-long" onClick={() => props.handleSearchId('123456789012345678901')}>Long</button>

      {/* Рендерим фильтры */}
      <div data-testid="filters-slot">{props.filters}</div>

      {/* ВАЖНО: Рендерим кнопки, переданные через пропс buttons */}
      <div data-testid="actions-buttons">
        {props.buttons}
      </div>
    </div>
  )
}));

jest.mock('./FilterPanel', () => ({
  FilterPanel: (props: any) => (
    <div data-testid="filter-panel" data-disabled={props.disabledSubmit}>
      <button data-testid="apply-valid" onClick={() => props.onApplyFilters({ someFilter: 'value' })}>Apply Valid</button>
      <button data-testid="apply-empty" onClick={() => props.onApplyFilters({})}>Apply Empty</button>
      <button data-testid="apply-empty-transport" onClick={() => props.onApplyFilters({ cargoTransportType: [] })}>Empty Transport</button>
    </div>
  )
}));

jest.mock('modules/Registry/components/Table/Table', () => ({
  Table: (props: any) => (
    <div data-testid="table" data-fetching={props.isFetching}>
      <button data-testid="change-page" onClick={() => props.pagination.onChange(2, 10)}>Page</button>
      <button data-testid="change-sort" onClick={() => props.onChange({}, {}, { column: { dataIndex: 'status' }, order: 'ascend' })}>Sort</button>
      <button data-testid="change-sort-array" onClick={() => props.onChange({}, {}, [{ column: { dataIndex: 'status' }, order: 'descend' }])}>Sort Arr</button>
      <button data-testid="change-sort-unknown" onClick={() => props.onChange({}, {}, { column: { dataIndex: 'unknown' }, order: 'ascend' })}>Sort Unk</button>
    </div>
  )
}));

jest.mock('./ExportXlsButton', () => ({
  ExportXlsButton: (props: any) => (
    <button data-testid={`export-${props.formatType || 'xls'}`} disabled={props.disabled}>Export</button>
  )
}));

jest.mock('modules/Registry/components/TableSettingsContainer/TableSettingsContainer', () => ({
  TableSettingsContainer: 'div'
}));
jest.mock('modules/Registry/components/DefaultSorting/DefaultSorting', () => ({
  DefaultSorting: 'div'
}));
jest.mock('modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings', () => ({
  ColumnVisibilitySettings: 'div'
}));

// ==========================
// 4. ИМПОРТ КОМПОНЕНТА И ХУКОВ
// ==========================
import { ListView } from './ListView';

import { useOrganizationContext } from 'context/Organization.context';
import { usePagination } from 'shared/hooks/usePagination';
import { useSorting } from '../hooks/useSorting';
import { useFilter } from '../hooks/useFilter';
import { useColumns } from '../hooks/useColumns';
import { useDeferredSearch } from '../hooks/useDefferedSearch';
import { useTransformedData } from '../hooks/useTransformedData';
import { hasAnyActiveFilter } from './utils';

// ==========================
// 5. ТЕСТЫ
// ==========================
describe('ListView', () => {
  const mockSetPageSetting = jest.fn();
  const mockSetSortSetting = jest.fn();
  const mockSetFilterValues = jest.fn();
  const mockSetFieldsValue = jest.fn();
  const mockExecuteSearch = jest.fn().mockResolvedValue({});

  const mockForm = {
    getFieldsValue: jest.fn(() => ({ existing: 'value' })),
    setFieldsValue: mockSetFieldsValue,
  };

  const mUseOrg = useOrganizationContext as unknown as jest.Mock;
  const mUsePag = usePagination as unknown as jest.Mock;
  const mUseSort = useSorting as unknown as jest.Mock;
  const mUseFilter = useFilter as unknown as jest.Mock;
  const mUseCols = useColumns as unknown as jest.Mock;
  const mUseSearch = useDeferredSearch as unknown as jest.Mock;
  const mUseTrans = useTransformedData as unknown as jest.Mock;
  const mHasFilter = hasAnyActiveFilter as unknown as jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();

    mUseOrg.mockReturnValue({ organizationId: 1, isOrganization: true, executorGroupId: null });
    mUsePag.mockReturnValue({ pageSetting: { page: 0, size: 10 }, setPageSetting: mockSetPageSetting });
    mUseSort.mockReturnValue({ sortSetting: undefined, setSortSetting: mockSetSortSetting, handleDefaultSort: jest.fn() });
    mUseFilter.mockReturnValue({ filterValues: { status: 'ACTIVE' }, setFilterValues: mockSetFilterValues, form: mockForm });
    mUseCols.mockReturnValue({
      columns: [],
      settings: { columnVisibility: {}, setColumnVisibility: jest.fn(), defaultVisibility: {}, isSaving: false },
    });
    mUseSearch.mockReturnValue({
      executeSearch: mockExecuteSearch,
      data: { content: [], totalElements: 0 },
      isLoadingOrg: false,
      isLoadingExec: false,
    });
    mUseTrans.mockReturnValue([]);
    mHasFilter.mockImplementation((v: any) => Object.keys(v || {}).length > 0);
  });

  describe('Initial Rendering & Placeholders', () => {
    it('renders placeholder when organizationId is not loaded yet', async () => {
      mUseOrg.mockReturnValue({ organizationId: null, isOrganization: false, executorGroupId: null });

      await act(async () => {
        render(<ListView pathname="/registry" />);
      });

      expect(screen.getByText(/Для получения данных по заявкам задайте параметры фильтрации/i)).toBeInTheDocument();
      expect(screen.queryByTestId('table')).not.toBeInTheDocument();
    });

    it('renders table when organizationId is loaded and search is executed', async () => {
      await act(async () => {
        render(<ListView pathname="/registry" />);
      });

      await waitFor(() => {
        expect(screen.getByTestId('table')).toBeInTheDocument();
      });
    });
  });

  describe('Search Execution (useEffect)', () => {
    it('executes search on initial render when parameters are valid', async () => {
      await act(async () => {
        render(<ListView pathname="/registry" />);
      });

      await waitFor(() => {
        expect(mockExecuteSearch).toHaveBeenCalledTimes(1);
        expect(mockExecuteSearch).toHaveBeenCalledWith(
          expect.objectContaining({
            status: 'ACTIVE',
            pageSetting: expect.any(Object),
          })
        );
      });
    });

    it('does not execute search if organizationId is missing', async () => {
      mUseOrg.mockReturnValue({ organizationId: null, isOrganization: false, executorGroupId: null });

      await act(async () => {
        render(<ListView pathname="/registry" />);
      });

      expect(mockExecuteSearch).not.toHaveBeenCalled();
    });
  });

  describe('Filters Application', () => {
    it('does not apply filters if hasAnyActiveFilter returns false', async () => {
      mHasFilter.mockReturnValue(false);
      await act(async () => { render(<ListView pathname="/registry" />); });

      fireEvent.click(screen.getByTestId('apply-empty'));
      expect(mockSetFilterValues).not.toHaveBeenCalled();
    });

    it('applies filters and shows table if active filters exist', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });

      fireEvent.click(screen.getByTestId('apply-valid'));
      expect(mockSetFilterValues).toHaveBeenCalledWith({ someFilter: 'value' });
    });

    it('removes cargoTransportType from filter values if empty array is provided', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });

      fireEvent.click(screen.getByTestId('apply-empty-transport'));

      expect(mockSetFilterValues).toHaveBeenCalledWith(
        expect.not.objectContaining({ cargoTransportType: [] })
      );
      expect(mockSetFieldsValue).toHaveBeenCalledWith({ cargoTransportType: undefined });
    });

    it('disables submit button when no active filters are selected', async () => {
      mHasFilter.mockReturnValue(false);
      await act(async () => { render(<ListView pathname="/registry" />); });

      const filterPanel = screen.getByTestId('filter-panel');
      expect(filterPanel).toHaveAttribute('data-disabled', 'true');
    });
  });

  describe('Table Interactions (Pagination & Sorting)', () => {
    it('updates page settings on pagination change', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      fireEvent.click(screen.getByTestId('change-page'));
      expect(mockSetPageSetting).toHaveBeenCalledWith({ page: 1, size: 10 });
    });

    it('updates sort settings correctly on table sort change', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      fireEvent.click(screen.getByTestId('change-sort'));
      expect(mockSetSortSetting).toHaveBeenCalledWith({
        property: 'statusField',
        directionAsc: true,
      });
    });

    it('handles array of sorters correctly', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      fireEvent.click(screen.getByTestId('change-sort-array'));
      expect(mockSetSortSetting).toHaveBeenCalledWith({
        property: 'statusField',
        directionAsc: false,
      });
    });

    it('resets sort settings if sortProperty is not found in constants', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      fireEvent.click(screen.getByTestId('change-sort-unknown'));
      expect(mockSetSortSetting).toHaveBeenCalledWith(undefined);
    });
  });

  describe('Search by ID (handleSearchId)', () => {
    it('resets requestHumanId when empty string is provided', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      fireEvent.click(screen.getByTestId('search-empty'));

      expect(mockSetFieldsValue).toHaveBeenCalledWith({ requestHumanId: undefined });
    });

    it('does not set value when ID length is less than 3', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      fireEvent.click(screen.getByTestId('search-short'));

      expect(mockSetFieldsValue).not.toHaveBeenCalled();
    });

    it('does not set value when ID length is greater than 20', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      fireEvent.click(screen.getByTestId('search-long'));

      expect(mockSetFieldsValue).not.toHaveBeenCalled();
    });

    it('sets requestHumanId when ID is valid', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      fireEvent.click(screen.getByTestId('search-valid'));

      expect(mockSetFieldsValue).toHaveBeenCalledWith({ requestHumanId: '12345' });
    });
  });

  describe('Export Buttons', () => {
    it('disables export buttons when there is no data in response', async () => {
      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('export-xls')).toBeInTheDocument());

      expect(screen.getByTestId('export-cse')).toBeDisabled();
      expect(screen.getByTestId('export-xls')).toBeDisabled();
    });

    it('enables export buttons when data is present', async () => {
      mUseSearch.mockReturnValue({
        executeSearch: mockExecuteSearch,
        data: { content: [{ id: 1 }], totalElements: 1 },
        isLoadingOrg: false,
        isLoadingExec: false,
      });

      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('export-xls')).toBeInTheDocument());

      expect(screen.getByTestId('export-cse')).not.toBeDisabled();
    });
  });

  describe('Loading States', () => {
    it('passes organization loading state to Table when isOrganization is true', async () => {
      mUseSearch.mockReturnValue({
        executeSearch: mockExecuteSearch,
        data: { content: [], totalElements: 0 },
        isLoadingOrg: true,
        isLoadingExec: false,
      });

      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      expect(screen.getByTestId('table')).toHaveAttribute('data-fetching', 'true');
    });

    it('passes executor loading state to Table when isOrganization is false', async () => {
      mUseSearch.mockReturnValue({
        executeSearch: mockExecuteSearch,
        data: { content: [], totalElements: 0 },
        isLoadingOrg: false,
        isLoadingExec: true,
      });

      mUseOrg.mockReturnValue({
        organizationId: 1,
        isOrganization: false,
        executorGroupId: 5,
      });

      await act(async () => { render(<ListView pathname="/registry" />); });
      await waitFor(() => expect(screen.getByTestId('table')).toBeInTheDocument());

      expect(screen.getByTestId('table')).toHaveAttribute('data-fetching', 'true');
    });
  });
});
