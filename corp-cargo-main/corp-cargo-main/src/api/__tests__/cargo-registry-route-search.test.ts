import { renderHook } from '@testing-library/react-hooks';
import {
  useSearchCargoRegistry,
  useGetSearchRouteRegistry,
  useCargoTransportTypes,
  useDefaultColumnVisibilitySettings,
  useSaveCargoRoutesUsersAttributes,
  useCargoUserSettings,
} from '../cargo-registry-route-search';
import {
  SearchRequestRoutes,
} from 'stores/CargoRegistry/CargoRegistry.interface';
import {
  CARGO_ROUTE_REPORT,
  GET_CARGO_TRANSPORT_TYPES,
  CARGO_REGISTRY_ROUTES_DEFAULT_ATTRIBUTES,
  REQUESTS_JOURNAL_ROUTE_MULTIPLE,
  GET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES,
  SET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES,
} from 'constants/constants.api';
import { UsersAttributes } from 'api/register-search';

// ========================
//    Моки
// ========================

const mockUseAPI = jest.fn();
const mockUseAPIMutation = jest.fn();
const mockHttp = { get: jest.fn(), post: jest.fn(), put: jest.fn() };

jest.mock('api/index', () => ({
  useAPI: (...args: unknown[]) => mockUseAPI(...args),
  useAPIMutation: (...args: unknown[]) => mockUseAPIMutation(...args),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(() => ({
    http: mockHttp,
    process: mockHttp,
  })),
}));

jest.mock('constants/constants.api', () => ({
  CARGO_ROUTE_REPORT: 'reports-cargo/cargo_routelist',
  GET_CARGO_TRANSPORT_TYPES: 'constants/cargo-transport-types',
  CARGO_REGISTRY_ROUTES_DEFAULT_ATTRIBUTES: 'reports-cargo/routelist/default/attributes',
  REQUESTS_JOURNAL_ROUTE_MULTIPLE: 'route-cargo/journal/:requestId',
  GET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES: 'reports-cargo/routelist/:userId/attributes',
  SET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES: 'reports-cargo/routelist/:userId/attributes/cargo',
}));

jest.mock('io-ts', () => ({
  Type: class Type {
    validate = jest.fn();
    decode = jest.fn();
    encode = jest.fn();
    is = jest.fn();
  },
  type: jest.fn(() => ({ _tag: 'Type' })),
  partial: jest.fn(() => ({ _tag: 'PartialType' })),
  string: { _tag: 'StringType' },
  number: { _tag: 'NumberType' },
  boolean: { _tag: 'BooleanType' },
  array: jest.fn(() => ({ _tag: 'ArrayType' })),
  union: jest.fn(),
  literal: jest.fn(),
}));

jest.mock('utils/io-ts', () => ({
  optional: jest.fn(),
}));

jest.mock('stores/CargoRegistry/CargoRegistry.interface', () => ({
  SearchRequestRoutes: { encode: jest.fn((v) => v) },
  TransportType: { _tag: 'TransportType' },
  SearchResponse: { _tag: 'SearchResponse' },
}));

jest.mock('modules/CargoRoutesRegistry/types', () => ({
  ColumnVisibilitySettings: {},
  MonitorRouteResponseType: { _tag: 'MonitorRouteResponseType' },
  RouteType: { _tag: 'RouteType' },
}));

jest.mock('api/register-search', () => ({
  UsersAttributes: { _tag: 'UsersAttributesType' },
}));

// ========================
//    Типизация моков
// ========================

const mUseAPI = mockUseAPI as jest.Mock;
const mUseAPIMutation = mockUseAPIMutation as jest.Mock;

// Результат по умолчанию для useAPI
const defaultAPIResult = {
  data: null,
  isLoading: false,
  error: null,
  refetch: jest.fn(),
  fetchMore: jest.fn(),
};

// Результат по умолчанию для useAPIMutation
const defaultMutationResult = [
  jest.fn(), // mutate
  { status: 'idle', data: null, error: null, isLoading: false },
] as const;

describe('cargo-registry-route-search', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mUseAPI.mockReturnValue(defaultAPIResult);
    mUseAPIMutation.mockReturnValue(defaultMutationResult);
  });

  // ==============================
  //  useSearchCargoRegistry
  // ==============================
  describe('useSearchCargoRegistry', () => {
    const query: SearchRequestRoutes = { organizationId: 'org-123' };
    const config = {};

    it('вызывает useAPI с правильными аргументами', () => {
      renderHook(() => useSearchCargoRegistry(query, config));

      expect(mUseAPI).toHaveBeenCalledTimes(1);

      // Первый аргумент — ключ кэша
      const callArgs = mUseAPI.mock.calls[0];
      expect(callArgs[0]).toEqual(['cargoRegistryRoutes', query]);

      // Второй аргумент — функция queryFn (проверяем только тип)
      expect(typeof callArgs[1]).toBe('function');

      // Третий аргумент — config
      expect(callArgs[2]).toBe(config);
    });

    it('передаёт результат useAPI', () => {
      const testData = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, numberOfElements: 0, first: true, last: true, empty: true, sort: { sorted: false, unsorted: true, empty: true }, pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: { sorted: false, unsorted: true, empty: true } } };
      mUseAPI.mockReturnValue({ ...defaultAPIResult, data: testData });

      const { result } = renderHook(() => useSearchCargoRegistry(query, config));

      expect(result.current.data).toEqual(testData);
    });
  });

  // ==============================
  //  useGetSearchRouteRegistry
  // ==============================
  describe('useGetSearchRouteRegistry', () => {
    const requestId = 'route-456';

    it('вызывает useAPI с правильным ключом', () => {
      renderHook(() => useGetSearchRouteRegistry(requestId));

      expect(mUseAPI).toHaveBeenCalledWith(
        ['cargoRegistryRoute', requestId],
        expect.any(Function),
      );
    });

    it('возвращает данные маршрута', () => {
      const testRoute = { id: 'route-456', humanReadableId: 'R-001' };
      mUseAPI.mockReturnValue({ ...defaultAPIResult, data: testRoute });

      const { result } = renderHook(() => useGetSearchRouteRegistry(requestId));

      expect(result.current.data).toEqual(testRoute);
    });
  });

  // ==============================
  //  useCargoTransportTypes
  // ==============================
  describe('useCargoTransportTypes', () => {
    it('вызывает useAPI с правильным ключом', () => {
      renderHook(() => useCargoTransportTypes());

      expect(mUseAPI).toHaveBeenCalledWith(
        ['cargoTransportTypes'],
        expect.any(Function),
      );
    });

    it('возвращает типы транспорта', () => {
      const testTypes = [{ name: 'plane', rusName: 'Самолёт' }];
      mUseAPI.mockReturnValue({ ...defaultAPIResult, data: testTypes });

      const { result } = renderHook(() => useCargoTransportTypes());

      expect(result.current.data).toEqual(testTypes);
    });
  });

  // ==============================
  //  useDefaultColumnVisibilitySettings
  // ==============================
  describe('useDefaultColumnVisibilitySettings', () => {
    const userId = 'user-789';

    it('вызывает useAPI с правильным ключом', () => {
      renderHook(() => useDefaultColumnVisibilitySettings(userId));

      expect(mUseAPI).toHaveBeenCalledWith(
        ['defaultCargoRouteColumns'],
        expect.any(Function),
      );
    });

    it('возвращает настройки видимости', () => {
      const testSettings = { id: 'user-789' };
      mUseAPI.mockReturnValue({ ...defaultAPIResult, data: testSettings });

      const { result } = renderHook(() => useDefaultColumnVisibilitySettings(userId));

      expect(result.current.data).toEqual(testSettings);
    });
  });

  // ==============================
  //  useCargoUserSettings
  // ==============================
  describe('useCargoUserSettings', () => {
    const userId = 'user-789';

    it('вызывает useAPI с правильным ключом', () => {
      renderHook(() => useCargoUserSettings(userId));

      expect(mUseAPI).toHaveBeenCalledWith(
        ['userCargoRouteSettings', userId],
        expect.any(Function),
      );
    });

    it('возвращает настройки пользователя', () => {
      const testSettings = { id: 'user-789', cargoUIVisibility: { show: true } };
      mUseAPI.mockReturnValue({ ...defaultAPIResult, data: testSettings });

      const { result } = renderHook(() => useCargoUserSettings(userId));

      expect(result.current.data).toEqual(testSettings);
    });
  });

  // ==============================
  //  useSaveCargoRoutesUsersAttributes
  // ==============================
  describe('useSaveCargoRoutesUsersAttributes', () => {
    const userId = 'user-789';

    it('вызывает useAPIMutation с правильным коллбэком', () => {
      renderHook(() => useSaveCargoRoutesUsersAttributes(userId));

      expect(mUseAPIMutation).toHaveBeenCalledTimes(1);

      const callArgs = mUseAPIMutation.mock.calls[0];
      // Первый аргумент — функция мутации
      expect(typeof callArgs[0]).toBe('function');

      // Второй аргумент — конфиг с onSuccess
      expect(callArgs[1]).toHaveProperty('onSuccess');
      expect(typeof callArgs[1].onSuccess).toBe('function');
    });

    it('возвращает мутацию и результат', () => {
      const mockMutate = jest.fn();
      const mockResult = { status: 'success', data: { id: 'user-789' }, error: null, isLoading: false };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useSaveCargoRoutesUsersAttributes(userId));

      const [mutate, state] = result.current;
      expect(mutate).toBe(mockMutate);
      expect(state).toEqual(mockResult);
    });

    it('вызывает cache.refetchQueries при onSuccess', () => {
      const mockRefetchQueries = jest.fn();
      const mockCache = { refetchQueries: mockRefetchQueries };

      renderHook(() => useSaveCargoRoutesUsersAttributes(userId));

      const callArgs = mUseAPIMutation.mock.calls[0];
      const config = callArgs[1];

      // Вызываем onSuccess с мок-аргументами
      config.onSuccess({ cache: mockCache, result: {}, variables: {}, process: {}, t: jest.fn(), logger: {} });

      expect(mockRefetchQueries).toHaveBeenCalledWith(['settingsAttributes']);
    });
  });
});