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
jest.mock('src/api/cargo-registry-search', () => ({}), { virtual: true });
jest.mock('src/api/profile', () => ({}), { virtual: true });
jest.mock('src/stores/CargoRegistry/CargoRegistry.interface', () => ({}), { virtual: true });

// Мокаем useTransformedData - не используем virtual: true, так как мы его будем использовать в тесте
jest.mock('./useTransformedData', () => ({
  useTransformedData: jest.fn(),
}));

// Мок для io-ts
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
import * as cargoRegistrySearchModule from 'api/cargo-registry-search';
import { useTransformedData } from './useTransformedData';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';

// Мокаем хуки api
jest.mock('api/profile', () => ({
  useProfile: jest.fn(),
}));

jest.mock('api/cargo-registry-search', () => ({
  useSearchCargoRegistry: jest.fn(),
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
          humanReadableId: 'TR-001',
          status: 'PENDING',
          desiredDate: '2023-12-25',
          expected: { cost: 50000, distance: 100, waypointsCount: 3 },
          weight: 100,
          volume: 5,
          contractor: { id: 'c1', name: 'Контрагент 1' },
          author: { fio: 'Иван Иванов', mobilePhone: '123', personnelNumber: 'P1' },
          sender: { fio: 'Петр Петров', mobilePhone: '456' },
          recipient: { fio: 'Сидор Сидоров', mobilePhone: '789' },
          senderAddress: 'Москва',
          recipientAddress: 'Санкт-Петербург',
          creationTime: 1703491200000,
          source: 'web',
          cargoTripId: 'TR-001',
          templateNumber: 'TMPL-1',
          evaluation: { rating: 5 },
          costCenter: 'МВЗ-001',
          plannedDeliveryDate: 1703577600000,
          economy: 10000,
          shipmentTime: 1703664000000,
          transferTime: 1703577600000,
          deadlineDate: '2023-12-30',
          actualCost: 55000,
          actualDistance: 110,
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
        requestIdVisible: 'TR-001',
        requestStatusVisible: '-',
        cargoTransportTypeVisible: '-',
        authorVisible: 'Иван Иванов',
        authorPhoneVisible: '123',
        authorPersonnelNumberVisible: 'P1',
        costCenterVisible: 'МВЗ-001',
        desiredDateVisible: '—',
        carrierVisible: 'Контрагент 1',
        plannedPriceVisible: '500,00',
        actualCostVisible: '550,00',
        plannedRangeVisible: '100,0',
        actualDistanceVisible: '110,0',
        plannedDeliveryDateVisible: '—',
        transferTimeVisible: '—',
        shipmentTimeVisible: '—',
        deadlineDateVisible: '2023-12-30',
        senderVisible: 'Петр Петров',
        senderPhoneVisible: '456',
        waypointFromVisible: 'Москва',
        senderOrganizationVisible: '—',
        recipientVisible: 'Сидор Сидоров',
        recipientPhoneVisible: '789',
        waypointToVisible: 'Санкт-Петербург',
        recipientOrganizationVisible: '—',
        waypointsCountVisible: '3',
        weightVisible: '100',
        volumeVisible: '5',
        creationDateVisible: '—',
        creationTimeVisible: '—',
        sourceVisible: 'web',
        routeNumberVisible: 'TR-001',
        templateNumberVisible: 'TMPL-1',
        evaluationVisible: '—',
        economyVisible: '100',
      },
    ];

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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

    rerender({ filterValues: { requestHumanId: 'TR-001' } });

    expect(mockRefetch).toHaveBeenCalledTimes(2);
  });

  it('вызывает refetch при изменении pageSetting', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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

  it('правильно обрабатывает отсутствующий organizationId', async () => {
    const mockRefetch = jest.fn();

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: {} });
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
      data: undefined,
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

    // При отсутствии organizationId метаданные имеют значения по умолчанию
    expect(result.current.metadata).toEqual({
      total: undefined,
      page: undefined,
      pageSize: undefined,
    });
  });

  it('правильно обрабатывает metadata при пустых данных', async () => {
    const mockOrganizationId = 'test-org-123';

    (profileModule.useProfile as jest.Mock).mockReturnValue({ data: { organizationId: mockOrganizationId } });
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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
      total: undefined,
      page: undefined,
      pageSize: undefined,
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
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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

  it('правильно вызывает useTransformedData с данными из useSearchCargoRegistry', async () => {
    const mockOrganizationId = 'test-org-123';
    const mockData = {
      content: [
        {
          id: '1',
          humanReadableId: 'TR-001',
          status: 'PENDING',
          desiredDate: '2023-12-25',
          expected: { cost: 50000, distance: 100, waypointsCount: 3 },
          weight: 100,
          volume: 5,
          contractor: { id: 'c1', name: 'Контрагент 1' },
          author: { fio: 'Иван Иванов', mobilePhone: '123', personnelNumber: 'P1' },
          sender: { fio: 'Петр Петров', mobilePhone: '456' },
          recipient: { fio: 'Сидор Сидоров', mobilePhone: '789' },
          senderAddress: 'Москва',
          recipientAddress: 'Санкт-Петербург',
          creationTime: 1703491200000,
          source: 'web',
          cargoTripId: 'TR-001',
          templateNumber: 'TMPL-1',
          evaluation: { rating: 5 },
          costCenter: 'МВЗ-001',
          plannedDeliveryDate: 1703577600000,
          economy: 10000,
          shipmentTime: 1703664000000,
          transferTime: 1703577600000,
          deadlineDate: '2023-12-30',
          actualCost: 55000,
          actualDistance: 110,
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
    (cargoRegistrySearchModule.useSearchCargoRegistry as jest.Mock).mockReturnValue({
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
