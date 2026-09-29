global.fetch = jest.fn();

jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/stores/index', () => ({}), { virtual: true });
jest.mock('src/shared/hooks/useAppStoreContext', () => ({}), { virtual: true });
jest.mock('src/api/index', () => ({}), { virtual: true });
jest.mock('src/api/register-search', () => ({}), { virtual: true });
jest.mock('src/stores/PersonalSearch/PersonalSearch.interface', () => ({}), { virtual: true });
jest.mock('src/modules/Planner/types', () => ({}), { virtual: true });
jest.mock('src/stores/Planner/DIPlanner.store', () => ({}), { virtual: true });
jest.mock('src/ioc/ioc.stores', () => ({}), { virtual: true });
jest.mock('src/api/cargo-registry-compensations-search', () => ({}), { virtual: true });
jest.mock('src/api/profile', () => ({}), { virtual: true });
jest.mock('src/stores/CargoRegistry/CargoRegistry.interface', () => ({}), { virtual: true });

// Мокаем useTransformedData - не используем virtual: true, так как мы его будем использовать в тесте
jest.mock('./useTransformedData', () => ({
  useTransformedData: jest.fn(),
}));

// Мок для io-ts - все функции возвращают заглушки
jest.mock('io-ts', () => ({
  Type: class Type {
    validate = jest.fn();
    decode = jest.fn();
    encode = jest.fn();
    is = jest.fn();
  },
  type: jest.fn(),
  number: jest.fn(),
  string: jest.fn(),
  boolean: jest.fn(),
  array: jest.fn(),
  union: jest.fn(),
  literal: jest.fn(),
  readonly: jest.fn(),
  partial: jest.fn(),
  record: jest.fn(),
  unknown: jest.fn(),
  undefined: jest.fn(),
  null: jest.fn(),
  never: jest.fn(),
  intersection: jest.fn(),
  keyof: jest.fn(),
  exact: jest.fn(),
  strict: jest.fn(),
  tuple: jest.fn(),
  recursion: jest.fn(),
  brand: jest.fn(),
  taggedUnion: jest.fn(),
  refinement: jest.fn(),
  clean: jest.fn(),
  success: jest.fn(),
  failure: jest.fn(),
  failures: jest.fn(),
  getValidationError: jest.fn(),
  getDefaultContext: jest.fn(),
  Either: {},
  Context: {},
  Validation: {},
}));

jest.mock('@sber-sbertransport/mf-core', () => ({}), { virtual: true });

// Мок для utils/io-ts
jest.mock('utils/io-ts', () => ({
  optional: jest.fn(),
  withValidate: jest.fn(),
  fallback: jest.fn(),
  numberString: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  money: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  mobilePhone: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  nullable: jest.fn(),
  epochTimestamp: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  time: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  ISODate: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  EpochMS: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  uuid: {
    validate: jest.fn(), decode: jest.fn(), encode: jest.fn(),
  },
  oneOf: jest.fn(),
  DecoderInput: {},
}));

// Мокаем зависимости хука
jest.mock('utils/formatTime', () => ({
  formatBaseDate: jest.fn(),
}));
jest.mock('utils/convertToRubles', () => ({
  convertToRubles: jest.fn(),
}));
jest.mock('constants/constants.app', () => ({
  emptySign: '—',
}));

import { renderHook, act } from '@testing-library/react-hooks';
import { useDataSource } from './useDataSource';
import * as profileModule from 'api/profile';
import * as cargoRegistryCompensationsSearchModule from 'api/cargo-registry-compensations-search';
import { useTransformedData } from './useTransformedData';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';

// Мокаем хуки api
jest.mock('api/profile', () => ({
  useProfile: jest.fn(),
}));

jest.mock('api/cargo-registry-compensations-search', () => ({
  useSearchCargoCompensations: jest.fn(),
}));

describe('useDataSource', () => {
  const mockSetPageSetting = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockSetPageSetting.mockClear();
  });

  it('возвращает данные после успешной загрузки', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockData = {
      content: [
        {
          id: '1',
          humanReadableId: 'COMP-001',
          routeNumber: 'RTE-001',
          courier: 'Иван Иванов',
          department: 'Отдел доставки',
          mvz: 'МВЗ-001',
          cost: 50000,
          status: 'Одобрено',
          deadline: '2023-12-25',
          approvedBy: 'Петр Петров',
          approvalDate: '2023-12-20',
          formationDate: '2023-12-15',
        },
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      numberOfElements: 1,
      first: true,
      last: true,
      empty: false,
      sort: {
        sorted: true, unsorted: false, empty: false,
      },
      pageable: {
        offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
          sorted: true, unsorted: false, empty: false,
        },
      },
    };

    const mockTransformedData = [
      {
        id: '1',
        humanReadableId: 'COMP-001',
        routeNumber: 'RTE-001',
        courier: 'Иван Иванов',
        department: 'Отдел доставки',
        mvz: 'МВЗ-001',
        cost: 500,
        status: 'Одобрено',
        deadline: '2023-12-25',
        approvedBy: 'Петр Петров',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-15',
      },
    ];

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: mockData,
      refetch: jest.fn(),
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue(mockTransformedData);

    const { result } = renderHook(() => useDataSource({
      filterValues: {},
      sortSetting: { property: 'creationDate', directionAsc: false } as SortSetting,
      pageSetting: { page: 0, size: 10 } as PageSetting,
      setPageSetting: mockSetPageSetting,
    })
    );

    expect(result.current.dataSource).toEqual(mockTransformedData);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.metadata).toEqual({
      total: 1,
      page: 0,
      pageSize: 10,
    });
  });

  it('вызывает refetch при изменении фильтров', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: {
        content: [], totalElements: 0, totalPages: 0, size: 10, number: 0,
        numberOfElements: 0, first: true, last: true, empty: true, sort: {
          sorted: false, unsorted: true, empty: true,
        }, pageable: {
          offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
            sorted: false, unsorted: true, empty: true,
          },
        },
      },
      refetch: mockRefetch,
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { rerender } = renderHook(
      ({ filterValues }) => useDataSource({
        filterValues,
        sortSetting: undefined,
        pageSetting: { page: 0, size: 10 },
        setPageSetting: mockSetPageSetting,
      }),
      { initialProps: { filterValues: {} } }
    );

    expect(mockRefetch).toHaveBeenCalledTimes(1);

    rerender({ filterValues: { humanReadableId: 'COMP-001' } });

    expect(mockRefetch).toHaveBeenCalledTimes(2);
  });

  it('вызывает refetch при изменении pageSetting', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: {
        content: [], totalElements: 0, totalPages: 0, size: 10, number: 0,
        numberOfElements: 0, first: true, last: true, empty: true, sort: {
          sorted: false, unsorted: true, empty: true,
        }, pageable: {
          offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
            sorted: false, unsorted: true, empty: true,
          },
        },
      },
      refetch: mockRefetch,
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { rerender } = renderHook(
      ({ pageSetting }) => useDataSource({
        filterValues: {},
        sortSetting: undefined,
        pageSetting,
        setPageSetting: mockSetPageSetting,
      }),
      { initialProps: { pageSetting: { page: 0, size: 10 } } }
    );

    expect(mockRefetch).toHaveBeenCalledTimes(1);

    rerender({ pageSetting: { page: 1, size: 10 } });

    expect(mockRefetch).toHaveBeenCalledTimes(2);
  });

  it('вызывает refetch при изменении sortSetting', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: {
        content: [], totalElements: 0, totalPages: 0, size: 10, number: 0,
        numberOfElements: 0, first: true, last: true, empty: true, sort: {
          sorted: false, unsorted: true, empty: true,
        }, pageable: {
          offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
            sorted: false, unsorted: true, empty: true,
          },
        },
      },
      refetch: mockRefetch,
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { rerender } = renderHook(
      ({ sortSetting }) => useDataSource({
        filterValues: {},
        sortSetting,
        pageSetting: { page: 0, size: 10 },
        setPageSetting: mockSetPageSetting,
      }),
      { initialProps: { sortSetting: { property: 'creationDate', directionAsc: false } as SortSetting } }
    );

    expect(mockRefetch).toHaveBeenCalledTimes(1);

    rerender({ sortSetting: { property: 'creationDate', directionAsc: true } as SortSetting });

    expect(mockRefetch).toHaveBeenCalledTimes(2);
  });

  it('не вызывает refetch если organizationId отсутствует', async () => {
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: {} });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: undefined,
      refetch: mockRefetch,
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { rerender } = renderHook(
      ({ filterValues }) => useDataSource({
        filterValues,
        sortSetting: undefined,
        pageSetting: { page: 0, size: 10 },
        setPageSetting: mockSetPageSetting,
      }),
      { initialProps: { filterValues: {} } }
    );

    expect(mockRefetch).not.toHaveBeenCalled();

    rerender({ filterValues: { humanReadableId: 'COMP-001' } });

    expect(mockRefetch).not.toHaveBeenCalled();
  });

  it('правильно обрабатывает metadata при пустых данных', async () => {
    const mockOrganizationId = 'test-org-123';

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: undefined,
      refetch: jest.fn(),
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { result } = renderHook(() => useDataSource({
      filterValues: {},
      sortSetting: undefined,
      pageSetting: { page: 0, size: 10 },
      setPageSetting: mockSetPageSetting,
    })
    );

    expect(result.current.metadata).toEqual({
      total: 0, page: 0, pageSize: undefined,
    });
  });

  it('правильно обрабатывает metadata с частичными данными', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockData = {
      content: [],
      totalElements: 42,
      totalPages: 5,
      size: 10,
      number: 2,
      numberOfElements: 2,
      first: false,
      last: false,
      empty: false,
      sort: {
        sorted: true, unsorted: false, empty: false,
      },
      pageable: {
        offset: 20, pageNumber: 2, pageSize: 10, paged: true, unpaged: false, sort: {
          sorted: true, unsorted: false, empty: false,
        },
      },
    };

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: mockData,
      refetch: jest.fn(),
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { result } = renderHook(() => useDataSource({
      filterValues: {},
      sortSetting: undefined,
      pageSetting: { page: 0, size: 10 },
      setPageSetting: mockSetPageSetting,
    })
    );

    expect(result.current.metadata).toEqual({
      total: 42,
      page: 2,
      pageSize: 10,
    });
  });

  it('возвращает refetch функцию для обновления данных', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: {
        content: [], totalElements: 0, totalPages: 0, size: 10, number: 0,
        numberOfElements: 0, first: true, last: true, empty: true, sort: {
          sorted: false, unsorted: true, empty: true,
        }, pageable: {
          offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
            sorted: false, unsorted: true, empty: true,
          },
        },
      },
      refetch: mockRefetch,
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    const { result } = renderHook(() => useDataSource({
      filterValues: {},
      sortSetting: undefined,
      pageSetting: { page: 0, size: 10 },
      setPageSetting: mockSetPageSetting,
    })
    );

    expect(typeof result.current.refetch).toBe('function');

    act(() => {
      result.current.refetch();
    });
    expect(mockRefetch).toHaveBeenCalled();
  });

  it('правильно вызывает useTransformedData с данными из useSearchCargoCompensations', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockData = {
      content: [
        {
          id: '1',
          humanReadableId: 'COMP-001',
          routeNumber: 'RTE-001',
          courier: 'Иван Иванов',
          department: 'Отдел доставки',
          mvz: 'МВЗ-001',
          cost: 50000,
          status: 'Одобрено',
          deadline: '2023-12-25',
          approvedBy: 'Петр Петров',
          approvalDate: '2023-12-20',
          formationDate: '2023-12-15',
        },
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      numberOfElements: 1,
      first: true,
      last: true,
      empty: false,
      sort: {
        sorted: true, unsorted: false, empty: false,
      },
      pageable: {
        offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: {
          sorted: true, unsorted: false, empty: false,
        },
      },
    };

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistryCompensationsSearchModule.useSearchCargoCompensations as jest.Mock).mockReturnValue({
      data: mockData,
      refetch: jest.fn(),
      isLoading: false,
    });
    (useTransformedData as jest.Mock).mockReturnValue([]);

    renderHook(() => useDataSource({
      filterValues: {},
      sortSetting: undefined,
      pageSetting: { page: 0, size: 10 },
      setPageSetting: mockSetPageSetting,
    })
    );

    expect(useTransformedData).toHaveBeenCalledWith(mockData.content);
  });
});
