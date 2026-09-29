import { renderHook, act } from '@testing-library/react-hooks';
import { useDeferredSearch } from './useDefferedSearch';

// Мокаем SearchResponse
jest.mock('stores/CargoRegistry/CargoRegistry.interface', () => ({}), { virtual: true });

describe('useDeferredSearch', () => {
  const mockSearchOrg = jest.fn();
  const mockSearchExec = jest.fn();

  const mockOrgHook = jest.fn();
  const mockExecutorHook = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockSearchOrg.mockClear();
    mockSearchExec.mockClear();

    mockOrgHook.mockImplementation((orgId) => [mockSearchOrg, { isLoading: false }]);
    mockExecutorHook.mockImplementation((execId, empty) => [mockSearchExec, { isLoading: false }]);
  });

  describe('Тесты инициализации', () => {
    it('возвращает начальные значения при отсутствии данных', () => {
      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: false,
        })
      );

      expect(result.current.data).toBeUndefined();
      expect(result.current.isLoadingOrg).toBe(false);
      expect(result.current.isLoadingExec).toBe(false);
      expect(typeof result.current.executeSearch).toBe('function');
    });

    it('устанавливает initialData при его передаче', () => {
      const initialData = { totalElements: 0, totalPages: 0, size: 10, number: 0, numberOfElements: 0, first: true, last: true, empty: true, sort: { sorted: false, unsorted: true, empty: true }, pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: { sorted: false, unsorted: true, empty: true } }, content: [] };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: false,
          initialData,
        })
      );

      expect(result.current.data).toBe(initialData);
    });

    it('вызывает orgHook с organizationId при инициализации', () => {
      const mockOrganizationId = 'test-org-123';

      renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: mockOrganizationId,
          executorGroupId: undefined,
          isOrganization: true,
        })
      );

      expect(mockOrgHook).toHaveBeenCalledWith(mockOrganizationId);
    });

    it('вызывает executorHook с executorGroupId при инициализации', () => {
      const mockExecutorGroupId = ['exec-1', 'exec-2'];

      renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: mockExecutorGroupId,
          isOrganization: false,
        })
      );

      expect(mockExecutorHook).toHaveBeenCalledWith(mockExecutorGroupId, undefined);
    });
  });

  describe('Тесты поиска по организации', () => {
    it('выполняет поиск по организации при isOrganization=true и наличии organizationId', async () => {
      const mockOrganizationId = 'test-org-123';
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockResponse = {
        responseData: { totalElements: 1, totalPages: 1, size: 10, number: 0, numberOfElements: 1, first: true, last: true, empty: false, sort: { sorted: true, unsorted: false, empty: false }, pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: { sorted: true, unsorted: false, empty: false } }, content: [] },
      };

      mockSearchOrg.mockResolvedValue(mockResponse);

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: mockOrganizationId,
          executorGroupId: undefined,
          isOrganization: true,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).toHaveBeenCalledWith(mockQuery);
      expect(result.current.data).toBe(mockResponse.responseData);
    });

    it('не выполняет поиск по организации при isOrganization=false', async () => {
      const mockOrganizationId = 'test-org-123';
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: mockOrganizationId,
          executorGroupId: undefined,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).not.toHaveBeenCalled();
    });

    it('не выполняет поиск по организации при organizationId=null', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: true,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).not.toHaveBeenCalled();
    });

    it('не выполняет поиск по организации при organizationId=undefined', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: undefined,
          executorGroupId: undefined,
          isOrganization: true,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).not.toHaveBeenCalled();
    });
  });

  describe('Тесты поиска по исполнителям', () => {
    it('выполняет поиск по исполнителям при наличии executorGroupId', async () => {
      const mockExecutorGroupId = ['exec-1', 'exec-2'];
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockResponse = {
        responseData: { totalElements: 1, totalPages: 1, size: 10, number: 0, numberOfElements: 1, first: true, last: true, empty: false, sort: { sorted: true, unsorted: false, empty: false }, pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: { sorted: true, unsorted: false, empty: false } }, content: [] },
      };

      mockSearchExec.mockResolvedValue(mockResponse);

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: mockExecutorGroupId,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchExec).toHaveBeenCalledWith(mockQuery);
      expect(result.current.data).toBe(mockResponse.responseData);
    });

    it('не выполняет поиск по исполнителям при executorGroupId пустом массиве', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: [],
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchExec).not.toHaveBeenCalled();
    });

    it('не выполняет поиск по исполнителям при executorGroupId=undefined', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchExec).not.toHaveBeenCalled();
    });

    it('предпочтительно выполняет поиск по организации приоритетно поиску по исполнителям', async () => {
      const mockOrganizationId = 'test-org-123';
      const mockExecutorGroupId = ['exec-1'];
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockResponse = {
        responseData: { totalElements: 1, totalPages: 1, size: 10, number: 0, numberOfElements: 1, first: true, last: true, empty: false, sort: { sorted: true, unsorted: false, empty: false }, pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort: { sorted: true, unsorted: false, empty: false } }, content: [] },
      };

      mockSearchOrg.mockResolvedValue(mockResponse);

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: mockOrganizationId,
          executorGroupId: mockExecutorGroupId,
          isOrganization: true,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).toHaveBeenCalledWith(mockQuery);
      expect(mockSearchExec).not.toHaveBeenCalled();
    });
  });

  describe('Тесты установки данных в undefined', () => {
    it('устанавливает data=undefined при отсутствии organizationId и executorGroupId', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(result.current.data).toBeUndefined();
    });

    it('устанавливает data=undefined при isOrganization=false и executorGroupId пустой', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: 'org-123',
          executorGroupId: [],
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(result.current.data).toBeUndefined();
    });
  });

  describe('Test special flags emptyExecutorGroup / allExecutorGroups', () => {
    it('выполняет поиск по исполнителям при emptyExecutorGroup=true даже без executorGroupId', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockResponse = {
        responseData: {
          totalElements: 0, totalPages: 0, size: 10, number: 0, numberOfElements: 0,
          first: true, last: true, empty: true,
          sort: { sorted: false, unsorted: true, empty: true },
          pageable: {
            offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false,
            sort: { sorted: false, unsorted: true, empty: true },
          },
          content: [],
        },
      };

      mockSearchExec.mockResolvedValue(mockResponse);

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          emptyExecutorGroup: true,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchExec).toHaveBeenCalledWith(mockQuery);
      expect(mockExecutorHook).toHaveBeenCalledWith(undefined, true);
      expect(result.current.data).toBe(mockResponse.responseData);
    });

    it('выполняет поиск по исполнителям при allExecutorGroups=true даже без executorGroupId', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockResponse = {
        responseData: {
          totalElements: 0, totalPages: 0, size: 10, number: 0, numberOfElements: 0,
          first: true, last: true, empty: true,
          sort: { sorted: false, unsorted: true, empty: true },
          pageable: {
            offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false,
            sort: { sorted: false, unsorted: true, empty: true },
          },
          content: [],
        },
      };

      mockSearchExec.mockResolvedValue(mockResponse);

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          allExecutorGroups: true,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchExec).toHaveBeenCalledWith(mockQuery);
      expect(result.current.data).toBe(mockResponse.responseData);
    });

    it('не вызывает ни orgHook, ни execHook при отсутствии флагов и executorGroupId', async () => {
      const mockQuery = { requestHumanId: 'TR-001' };

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: null,
          executorGroupId: undefined,
          isOrganization: false,
        })
      );

      await act(async () => {
        await result.current.executeSearch(mockQuery);
      });

      expect(mockSearchOrg).not.toHaveBeenCalled();
      expect(mockSearchExec).not.toHaveBeenCalled();
      expect(result.current.data).toBeUndefined();
    });
  });

  describe('Тесты обработки ошибок', () => {
    it('бросает ошибку при неудачном поиске', async () => {
      const mockOrganizationId = 'test-org-123';
      const mockQuery = { requestHumanId: 'TR-001' };
      const mockError = new Error('Search failed');

      mockSearchOrg.mockRejectedValue(mockError);

      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      const { result } = renderHook(() =>
        useDeferredSearch({
          orgHook: mockOrgHook,
          executorHook: mockExecutorHook,
          organizationId: mockOrganizationId,
          executorGroupId: undefined,
          isOrganization: true,
        })
      );

      await act(async () => {
        await expect(result.current.executeSearch(mockQuery)).rejects.toThrow('Search failed');
      });

      consoleErrorSpy.mockRestore();
    });
  });
});
