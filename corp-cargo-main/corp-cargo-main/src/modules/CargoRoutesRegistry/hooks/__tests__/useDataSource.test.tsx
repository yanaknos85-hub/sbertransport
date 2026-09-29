import { renderHook } from '@testing-library/react-hooks';
import { useDataSource } from '../useDataSource';
import { useOrganizationContext } from 'context/Organization.context';
import { useSearchCargoRegistry } from 'api/cargo-registry-route-search';
import { useTransformedData } from '../useTransformedData';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { TransformedFilterValues } from '../../types';

// ==========================
// МОКИ ЗАВИСИМОСТЕЙ
// ==========================
jest.mock('context/Organization.context', () => ({
  useOrganizationContext: jest.fn(),
}));

jest.mock('api/cargo-registry-route-search', () => ({
  useSearchCargoRegistry: jest.fn(),
}));

// ИСПРАВЛЕНИЕ 4: Путь к моку тоже должен быть относительным от __tests__
jest.mock('../useTransformedData', () => ({
  useTransformedData: jest.fn(),
}));

const mUseOrg = useOrganizationContext as unknown as jest.Mock;
const mUseSearch = useSearchCargoRegistry as unknown as jest.Mock;
const mUseTransform = useTransformedData as unknown as jest.Mock;

describe('useDataSource', () => {
  const mockSetPageSetting = jest.fn();

  const defaultProps = {
    filterValues: { status: 'ACTIVE' } as TransformedFilterValues,
    sortSetting: { property: 'date', directionAsc: false } as SortSetting,
    pageSetting: { page: 0, size: 20 } as PageSetting,
    setPageSetting: mockSetPageSetting,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    mUseOrg.mockReturnValue({
      organizationId: 123,
      executorGroupId: null,
      isOrganization: true,
    });

    mUseSearch.mockReturnValue({
      data: {
        content: [{ id: 1 }, { id: 2 }],
        totalElements: 50,
        pageable: { pageNumber: 0, pageSize: 20 },
      },
      isLoading: false,
    });

    mUseTransform.mockImplementation((content) => content);
  });

  describe('Data Fetching & Transformation', () => {
    it('should return transformed data source', () => {
      const mockTransformed = [{ id: 1, transformed: true }];
      mUseTransform.mockReturnValue(mockTransformed);

      const { result } = renderHook(() => useDataSource(defaultProps));

      expect(result.current.dataSource).toEqual(mockTransformed);
      expect(mUseTransform).toHaveBeenCalledWith([{ id: 1 }, { id: 2 }]);
    });

    it('should return correct metadata from response', () => {
      const { result } = renderHook(() => useDataSource(defaultProps));

      expect(result.current.metadata).toEqual({
        total: 50,
        page: 0,
        pageSize: 20,
      });
    });

    it('should return isLoading state from search hook', () => {
      mUseSearch.mockReturnValue({ data: undefined, isLoading: true });

      const { result } = renderHook(() => useDataSource(defaultProps));

      expect(result.current.isLoading).toBe(true);
    });

    it('should handle undefined data gracefully', () => {
      mUseSearch.mockReturnValue({ data: undefined, isLoading: false });
      mUseTransform.mockReturnValue([]);

      const { result } = renderHook(() => useDataSource(defaultProps));

      expect(result.current.dataSource).toEqual([]);
      expect(result.current.metadata.total).toBeUndefined();
      expect(result.current.metadata.page).toBeUndefined();
      expect(result.current.metadata.pageSize).toBeUndefined();
    });
  });

  describe('Organization Context Logic', () => {
    it('should pass organizationId when isOrganization is true', () => {
      renderHook(() => useDataSource(defaultProps));

      expect(mUseSearch).toHaveBeenCalledWith(
        expect.objectContaining({
          organizationId: 123,
        }),
        {}
      );
      const callArgs = mUseSearch.mock.calls[0][0];
      expect(callArgs).not.toHaveProperty('executorGroupIds');
    });

    it('should pass executorGroupIds when isOrganization is false', () => {
      mUseOrg.mockReturnValue({
        organizationId: null,
        executorGroupId: [5, 6],
        isOrganization: false,
        emptyExecutorGroup: false,
      });

      renderHook(() => useDataSource(defaultProps));

      expect(mUseSearch).toHaveBeenCalledWith(
        expect.objectContaining({
          executorGroupIds: [5, 6],
          emptyExecutorGroup: false,
        }),
        {}
      );
      const callArgs = mUseSearch.mock.calls[0][0];
      expect(callArgs).not.toHaveProperty('organizationId');
    });

    it('should set emptyExecutorGroup to true when executorGroupId is empty array', () => {
      mUseOrg.mockReturnValue({
        organizationId: null,
        executorGroupId: [],
        isOrganization: false,
        emptyExecutorGroup: true,
      });

      renderHook(() => useDataSource(defaultProps));

      expect(mUseSearch).toHaveBeenCalledWith(
        expect.objectContaining({
          emptyExecutorGroup: true,
        }),
        {}
      );
    });

    it('should set emptyExecutorGroup to true when executorGroupId is null', () => {
      mUseOrg.mockReturnValue({
        organizationId: null,
        executorGroupId: null,
        isOrganization: false,
        emptyExecutorGroup: true,
      });

      renderHook(() => useDataSource(defaultProps));

      expect(mUseSearch).toHaveBeenCalledWith(
        expect.objectContaining({
          emptyExecutorGroup: true,
        }),
        {}
      );
    });
  });

  describe('Side Effects (useEffect)', () => {
    it('should reset page to 0 when filterValues change', () => {
      const { rerender } = renderHook(
        ({ props }) => useDataSource(props),
        { initialProps: { props: defaultProps } }
      );

      mockSetPageSetting.mockClear();

      const newFilterValues = { status: 'COMPLETED' } as TransformedFilterValues;
      rerender({
        props: { ...defaultProps, filterValues: newFilterValues },
      });

      expect(mockSetPageSetting).toHaveBeenCalledTimes(1);
      const updaterFn = mockSetPageSetting.mock.calls[0][0];
      expect(updaterFn({ page: 5, size: 20 })).toEqual({ page: 0, size: 20 });
    });

    it('should NOT reset page when only sortSetting changes', () => {
      const { rerender } = renderHook(
        ({ props }) => useDataSource(props),
        { initialProps: { props: defaultProps } }
      );

      mockSetPageSetting.mockClear();

      const newSortSetting = { property: 'name', directionAsc: true } as SortSetting;
      rerender({
        props: { ...defaultProps, sortSetting: newSortSetting },
      });

      expect(mockSetPageSetting).not.toHaveBeenCalled();
    });

    it('should NOT reset page when only pageSetting changes', () => {
      const { rerender } = renderHook(
        ({ props }) => useDataSource(props),
        { initialProps: { props: defaultProps } }
      );

      mockSetPageSetting.mockClear();

      const newPageSetting = { page: 2, size: 20 } as PageSetting;
      rerender({
        props: { ...defaultProps, pageSetting: newPageSetting },
      });

      expect(mockSetPageSetting).not.toHaveBeenCalled();
    });
  });

  describe('Query Parameters Composition', () => {
    it('should merge filterValues, orgQuery, sortSetting and pageSetting into single query object', () => {
      renderHook(() => useDataSource(defaultProps));

      expect(mUseSearch).toHaveBeenCalledWith(
        {
          status: 'ACTIVE',
          organizationId: 123,
          sortSetting: { property: 'date', directionAsc: false },
          pageSetting: { page: 0, size: 20 },
        },
        {}
      );
    });
  });
});

const mockSetPageSetting = jest.fn();

describe('useDataSource', () => {
  const defaultProps = {
    filterValues: { status: 'ACTIVE' } as TransformedFilterValues,
    sortSetting: { property: 'date', directionAsc: false } as SortSetting,
    pageSetting: { page: 0, size: 20 } as PageSetting,
    setPageSetting: mockSetPageSetting,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    mUseOrg.mockReturnValue({
      organizationId: 123,
      executorGroupId: null,
      isOrganization: true,
    });

    mUseSearch.mockReturnValue({
      data: { content: [], totalElements: 0, pageable: { pageNumber: 0, pageSize: 20 } },
      isLoading: false,
    });

    mUseTransform.mockImplementation((content) => content);
  });

  it('должен исключать EXECUTOR_GROUP_ALL_ID из executorGroupIds при isOrganization=false', () => {
    mUseOrg.mockReturnValue({
      organizationId: null,
      executorGroupId: ['allGroups', 'group-1', 'group-2'],
      isOrganization: false,
      emptyExecutorGroup: false,
    });

    renderHook(() => useDataSource(defaultProps));

    expect(mUseSearch).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: ['group-1', 'group-2'],
        emptyExecutorGroup: false,
      }),
      {}
    );
  });

  it('должен передавать пустой массив и emptyExecutorGroup=true при выборе «Без групп»', () => {
    mUseOrg.mockReturnValue({
      organizationId: null,
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });

    renderHook(() => useDataSource(defaultProps));

    expect(mUseSearch).toHaveBeenCalledWith(
      expect.objectContaining({
        executorGroupIds: [],
        emptyExecutorGroup: true,
      }),
      {}
    );
    const callArgs = mUseSearch.mock.calls[0][0];
    expect(callArgs).not.toHaveProperty('organizationId');
  });
});
