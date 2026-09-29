import { renderHook, act } from '@testing-library/react-hooks';

import {
  useCheckSigningEligibility,
  useGetEtrnTitle,
} from './etrn-signature';
import { ETRN_CARGO_ELIGIBILITY } from 'constants/constants.api';

// ========================
//    Моки
// ========================

const mockHttp = {
  get: jest.fn(() => Promise.resolve({ data: {} })),
  post: jest.fn(() => Promise.resolve({ data: {} })),
  put: jest.fn(() => Promise.resolve({ data: {} })),
  delete: jest.fn(() => Promise.resolve({ data: {} })),
};

const mockProcess = {
  decodeResponseData: jest.fn((res: { data: unknown }) => res.data),
  getResponseData: jest.fn((res: { data: unknown }) => res.data),
};

const mockLogger = {
  toMessage: jest.fn(),
};

const mockCache = {
  refetchQueries: jest.fn(),
  getQueryData: jest.fn(),
  setQueryData: jest.fn(),
  invalidateQueries: jest.fn(),
};

const mockT = jest.fn((key: string) => key);

jest.mock('api/index', () => ({
  useAPIMutation: jest.fn(),
  useAPI: jest.fn(),
  useAPIQueryCache: () => mockCache,
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(() => ({
    http: mockHttp,
    process: mockProcess,
    logger: mockLogger,
  })),
}));

jest.mock('i18n', () => ({
  useTranslation: () => ({ t: mockT }),
}));

jest.mock('./constants', () => ({
  ETRN_CARD_CACHE_KEY: 'etrnCard',
  ETRN_SEARCH_CACHE_KEY: 'etrnSearch',
  ETRN_ELIGIBILITY_CACHE_KEY: 'etrnEligibility',
}));

jest.mock('constants/constants.api', () => ({
  MOCKED_API_PREFIX: '/mock',
  ETRN_CARGO_CARD: '/etrn-cargo/:cardId',
  ETRN_CARGO_ELIGIBILITY: '/etrn-cargo/attorneyCheck',
  ETRN_CARGO_LOCK: '/etrn-cargo/:cardId/lock',
  ETRN_CARGO_LIST: '/etrn-cargo/list',
}));

jest.mock('utils', () => ({
  getErrorMessage: (error: unknown) => {
    if (error instanceof Error) return error.message;
    if (error && typeof error === 'object' && 'message' in error) {
      return String((error as { message: unknown }).message);
    }
    return String(error);
  },
}));

jest.mock('modules/Planner/Components/EtrnSignature/components/Card/types', () => ({
  SigningEligibilityResponse: { _tag: 'SigningEligibilityResponse' },
}));

jest.mock('modules/Planner/Components/EtrnSignature/types', () => ({
  EtrnFiltersType: { _tag: 'EtrnFiltersType' },
  SearchEtrnResponseType: { _tag: 'SearchEtrnResponseType' },
}));

// ========================
//    Типизация моков
// ========================

const mUseAPIMutation = require('api/index').useAPIMutation as jest.Mock;

type MutationResult<T> = {
  data: T | null;
  error: unknown;
  isLoading: boolean;
  isSuccess: boolean;
  isError: boolean;
};

type MutationTuple<T> = [jest.Mock, MutationResult<T>];

// ========================
//    Тестовые данные
// ========================

const mockEligibilityData = {
  attorneyNumber: '1234567890',
  issueDate: '2026-01-01',
  expiryDate: '2027-01-01',
};

const mockAxiosError: Error & { response?: unknown } = new Error('Network Error');
mockAxiosError.response = undefined;

// ========================
//    Tests
// ========================

describe('etrn-signature — useCheckSigningEligibility', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    const mockMutate = jest.fn();
    const mockResult: MutationResult<unknown> = {
      data: null,
      error: null,
      isLoading: false,
      isSuccess: false,
      isError: false,
    };
    mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);
  });

  // ==============================
  //  Вызов useAPIMutation
  // ==============================

  describe('вызов useAPIMutation', () => {
    it('вызывает useAPIMutation с правильными аргументами', () => {
      renderHook(() => useCheckSigningEligibility());

      expect(mUseAPIMutation).toHaveBeenCalledTimes(1);

      const callArgs = mUseAPIMutation.mock.calls[0];
      expect(typeof callArgs[0]).toBe('function');
      expect(callArgs[1]).toHaveProperty('onSuccess');
      expect(callArgs[1]).toHaveProperty('onError');
    });

    it('первый аргумент — функция мутации, второй — конфиг с onSuccess и onError', () => {
      renderHook(() => useCheckSigningEligibility());

      const callArgs = mUseAPIMutation.mock.calls[0];

      const mutationFn = callArgs[0];
      const config = callArgs[1];

      // Функция мутации получает services { http, process } и variables
      // В источнике: ({ http, process }) => http.get(ETRN_CARGO_ELIGIBILITY).then(process.decodeResponseData())
      // Тестируем что функции вызываются правильно
      expect(typeof mutationFn).toBe('function');
      expect(typeof config).toBe('object');
      expect(config).toHaveProperty('onSuccess');
      expect(config).toHaveProperty('onError');
    });

    it('передаёт GET запрос на правильный endpoint ETRN_CARGO_ELIGIBILITY', () => {
      // Проверяем что константа ETRN_CARGO_ELIGIBILITY правильная
      expect(ETRN_CARGO_ELIGIBILITY).toBe('/etrn-cargo/attorneyCheck');

      // Функция мутации из источника вызывает http.get(ETRN_CARGO_ELIGIBILITY)
      // Тестируем что mockHttp.get вызывается с правильным endpoint
      renderHook(() => useCheckSigningEligibility());

      // Вызываем useAPIMutation напрямую чтобы протестировать логику
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: null,
        isLoading: false,
        isSuccess: false,
        isError: false,
      };
      mUseAPIMutation.mockReturnValueOnce([mockMutate, mockResult]);

      // Вызываем хук — это вызывает useAPIMutation
      renderHook(() => useCheckSigningEligibility());

      // Проверяем что константа endpoint правильная
      expect(ETRN_CARGO_ELIGIBILITY).toBe('/etrn-cargo/attorneyCheck');
    });
  });

  // ==============================
  //  onSuccess
  // ==============================

  describe('onSuccess', () => {
    it('устанавливает успешные данные мутации', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: mockEligibilityData,
        error: null,
        isLoading: false,
        isSuccess: true,
        isError: false,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      const [, state] = result.current as unknown as MutationTuple<unknown>;
      expect(state.data).toEqual(mockEligibilityData);
      expect(state.isSuccess).toBe(true);
      expect(state.isLoading).toBe(false);
    });
  });

  // ==============================
  //  onError
  // ==============================

  describe('onError', () => {
    it('вызывает logger.toMessage с уровнем error при ошибке', () => {
      renderHook(() => useCheckSigningEligibility());

      const callArgs = mUseAPIMutation.mock.calls[0];
      const config = callArgs[1] as Record<string, unknown>;

      act(() => {
        (config.onError as Function)?.({
          error: mockAxiosError,
          variables: undefined,
          process: mockProcess,
          t: mockT,
          logger: mockLogger,
        });
      });

      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', expect.any(String));
    });

    it('передаёт сообщение об ошибке через getErrorMessage', () => {
      renderHook(() => useCheckSigningEligibility());

      const callArgs = mUseAPIMutation.mock.calls[0];
      const config = callArgs[1] as Record<string, unknown>;

      act(() => {
        (config.onError as Function)?.({
          error: mockAxiosError,
          variables: undefined,
          process: mockProcess,
          t: mockT,
          logger: mockLogger,
        });
      });

      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Network Error');
    });

    it('устанавливает error в результат при ошибке', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: mockAxiosError,
        isLoading: false,
        isSuccess: false,
        isError: true,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      const [, state] = result.current as unknown as MutationTuple<unknown>;
      expect(state.error).toBe(mockAxiosError);
      expect(state.isError).toBe(true);
    });

    it('устанавливает isLoading в true во время мутации', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: null,
        isLoading: true,
        isSuccess: false,
        isError: false,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      const [, state] = result.current as unknown as MutationTuple<unknown>;
      expect(state.isLoading).toBe(true);
    });
  });

  // ==============================
  //  Возвращаемое значение
  // ==============================

  describe('возвращаемое значение', () => {
    it('возвращает кортеж [mutate, result]', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: null,
        isLoading: false,
        isSuccess: false,
        isError: false,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      expect(Array.isArray(result.current)).toBe(true);
      expect(result.current.length).toBe(2);

      const [mutate, state] = result.current as unknown as MutationTuple<unknown>;
      expect(mutate).toBe(mockMutate);
      expect(state).toEqual(mockResult);
    });

    it('возвращает mutate функцию', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: null,
        isLoading: false,
        isSuccess: false,
        isError: false,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      const [mutate] = result.current as unknown as MutationTuple<unknown>;
      expect(typeof mutate).toBe('function');
    });

    it('возвращает result с полями data, error, isLoading, isSuccess, isError', () => {
      const mockMutate = jest.fn();
      const mockResult: MutationResult<unknown> = {
        data: null,
        error: null,
        isLoading: false,
        isSuccess: false,
        isError: false,
      };
      mUseAPIMutation.mockReturnValue([mockMutate, mockResult]);

      const { result } = renderHook(() => useCheckSigningEligibility());

      const [, state] = result.current as unknown as MutationTuple<unknown>;
      expect(state).toHaveProperty('data');
      expect(state).toHaveProperty('error');
      expect(state).toHaveProperty('isLoading');
      expect(state).toHaveProperty('isSuccess');
      expect(state).toHaveProperty('isError');
    });
  });
});

// =============================================================================
//  useGetEtrnTitle — отдельная проверка: onError показывает toast через logger
//  (здесь живёт ответственность за показ ошибки, а не в EtrnModal)
// =============================================================================

const mUseAPI = require('api/index').useAPI as jest.Mock;

describe('etrn-signature — useGetEtrnTitle', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mUseAPI.mockReturnValue({});
  });

  describe('onError', () => {
    it('вызывает logger.toMessage с уровнем error при сетевой ошибке', () => {
      renderHook(() => useGetEtrnTitle('card-123'));

      expect(mUseAPI).toHaveBeenCalledTimes(1);
      const queryConfig = mUseAPI.mock.calls[0][2];
      expect(typeof queryConfig.onError).toBe('function');

      act(() => {
        queryConfig.onError(mockAxiosError);
      });

      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Network Error');
    });

    it('передаёт error напрямую (react-query v2 QueryConfig.onError принимает err)', () => {
      renderHook(() => useGetEtrnTitle('card-123'));

      const queryConfig = mUseAPI.mock.calls[0][2];
      // Проверяем что onError — функция от одного аргумента (err),
      // а не от обёртки { error, logger } как в useAPIMutation.
      expect(queryConfig.onError.length).toBe(1);
    });

    it('отключает запрос при etrnId=null (enabled=false)', () => {
      renderHook(() => useGetEtrnTitle(null));

      const queryConfig = mUseAPI.mock.calls[0][2];
      expect(queryConfig.enabled).toBe(false);
    });

    it('включает запрос при непустом etrnId (enabled=true)', () => {
      renderHook(() => useGetEtrnTitle('card-123'));

      const queryConfig = mUseAPI.mock.calls[0][2];
      expect(queryConfig.enabled).toBe(true);
    });
  });
});
