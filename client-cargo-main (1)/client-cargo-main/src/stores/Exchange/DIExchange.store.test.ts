import { DIExchangeStore } from './DIExchange.store';
import { ListType, SortProperty } from './types';

// Моки для зависимостей
const mockHttp = {
  post: jest.fn(),
  put: jest.fn(),
};

const mockLogger = {
  toMessage: jest.fn(),
};

const mockProcess = {
  getResponseData: jest.fn(),
  processStatus: jest.fn(),
};

describe('DIExchangeStore', () => {
  let store: DIExchangeStore;

  beforeEach(() => {
    jest.clearAllMocks();
    store = new DIExchangeStore();
    // Перезаписываем зависимости на моки
    (store as any).http = mockHttp;
    (store as any).logger = mockLogger;
    (store as any).process = mockProcess;
  });

  describe('pagination', () => {
    it('should have initial pagination values', () => {
      // Then
      expect(store.pagination).toEqual({
        size: 10,
        totalElements: -1,
        totalPages: 0,
        number: 0,
      });
    });
  });

  describe('searchId', () => {
    it('should have null initial searchId', () => {
      // Then
      expect(store.searchId).toBeNull();
    });
  });

  describe('sortingOrders', () => {
    it('should have initial sortingOrders value', () => {
      // Then
      expect(store.sortingOrders).toBe('Сначала новые');
    });
  });

  describe('stateSorting', () => {
    it('should have initial stateSorting value', () => {
      // Then
      expect(store.stateSorting).toEqual({
        cargoRequestList: 'Сначала новые',
      });
    });
  });

  describe('pageSetting', () => {
    it('should have initial pageSetting values', () => {
      // Then
      expect(store.pageSetting).toEqual({
        page: 0,
        size: 10,
      });
    });
  });

  describe('sortSetting', () => {
    it('should have initial sortSetting values', () => {
      // Then
      expect(store.sortSetting).toEqual({
        directionAsc: false,
        property: SortProperty.CREATION_DATE,
      });
    });
  });

  describe('exchangeRequest', () => {
    it('should have empty object initial exchangeRequest', () => {
      // Then
      expect(store.exchangeRequest).toEqual({});
    });
  });

  describe('setPageSetting', () => {
    it('should update pageSetting', () => {
      // Given
      const newSettings = { page: 2, size: 20 };

      // When
      store.setPageSetting(newSettings);

      // Then
      expect(store.pageSetting).toEqual({
        page: 2,
        size: 20,
      });
    });
  });

  describe('setSortSetting', () => {
    it('should update sortSetting with inverted direction', () => {
      // Given
      const directionAsc = true;
      const property = SortProperty.CREATION_DATE;

      // When
      store.setSortSetting(directionAsc, property);

      // Then
      expect(store.sortSetting).toEqual({
        directionAsc: false, // inverted
        property: SortProperty.CREATION_DATE,
      });
    });

    it('should update sortSetting with direction false', () => {
      // Given
      const directionAsc = false;
      const property = SortProperty.CREATION_DATE;

      // When
      store.setSortSetting(directionAsc, property);

      // Then
      expect(store.sortSetting).toEqual({
        directionAsc: true, // inverted
        property: SortProperty.CREATION_DATE,
      });
    });
  });

  describe('resetSettings', () => {
    it('should reset all settings to initial values', () => {
      // Given - change settings
      store.sortingOrders = 'Сначала старые';
      store.pageSetting = { page: 5, size: 50 };
      store.sortSetting = { directionAsc: true, property: SortProperty.CREATION_DATE };

      // When
      store.resetSettings();

      // Then
      expect(store.sortingOrders).toBe('Сначала новые');
      expect(store.pageSetting).toEqual({
        page: 0,
        size: 10,
      });
      expect(store.sortSetting).toEqual({
        directionAsc: false,
        property: SortProperty.CREATION_DATE,
      });
    });
  });

  describe('resetSortingOrders', () => {
    it('should reset sortingOrders to DESC for CREATION_DATE', () => {
      // Given
      store.sortingOrders = 'Сначала старые';

      // When
      store.resetSortingOrders();

      // Then
      expect(store.sortingOrders).toBe('Сначала новые');
    });
  });

  describe('setSortingOrders', () => {
    it('should set ASC order for CREATION_DATE when directionAsc is true', () => {
      // When
      store.setSortingOrders(true, SortProperty.CREATION_DATE);

      // Then
      expect(store.sortingOrders).toBe('Сначала старые');
    });

    it('should set DESC order for CREATION_DATE when directionAsc is false', () => {
      // When
      store.setSortingOrders(false, SortProperty.CREATION_DATE);

      // Then
      expect(store.sortingOrders).toBe('Сначала новые');
    });

    it('should not change sortingOrders for other properties', () => {
      // Given
      store.sortingOrders = 'Сначала новые';

      // When
      store.setSortingOrders(true, SortProperty.CREATION_DATE);
      store.setSortingOrders(false, SortProperty.CREATION_DATE);

      // Then
      expect(store.sortingOrders).toBe('Сначала новые');
    });
  });

  describe('getAvailableListService', () => {
    it('should call http.post with correct parameters', async () => {
      // Given
      const mockResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };

      // When
      await (store as any).getAvailableListService(type, pageSetting, sortSetting);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        { pageSetting, sortSetting },
        { urlParams: { type } }
      );
    });
  });

  describe('denyExchangeService', () => {
    it('should call http.put with correct parameters', async () => {
      // Given
      const mockResponse = { id: '1' };
      mockHttp.put.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const id = 'exchange-id';

      // When
      await (store as any).denyExchangeService(id);

      // Then
      expect(mockHttp.put).toHaveBeenCalledWith(
        expect.stringContaining('/deny'),
        {},
        { urlParams: { id } }
      );
    });
  });

  describe('takeToWorkExchangeService', () => {
    it('should call http.put with correct parameters', async () => {
      // Given
      const mockResponse = { id: '1' };
      mockHttp.put.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const id = 'exchange-id';

      // When
      await (store as any).takeToWorkExchangeService(id);

      // Then
      expect(mockHttp.put).toHaveBeenCalledWith(
        expect.stringContaining('/accept'),
        {},
        { urlParams: { id } }
      );
    });
  });

  describe('getAvailableList', () => {
    it('should set exchangeRequest on success', async () => {
      // Given
      const mockResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };

      // When
      await store.getAvailableList(type, pageSetting, sortSetting);

      // Then
      expect(mockHttp.post).toHaveBeenCalled();
      expect(store.exchangeRequest).toEqual(mockResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка сервера' } } };
      mockHttp.post.mockRejectedValue(mockError);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };

      // When
      await store.getAvailableList(type, pageSetting, sortSetting);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка сервера');
    });
  });

  describe('takeToWorkExchange', () => {
    it('should take exchange to work and update list on success', async () => {
      // Given
      const mockTakeResponse = { id: '1' };
      const mockListResponse = {
        content: [{ id: '1' }], totalPages: 1, totalElements: 1, size: 10, number: 0,
      };
      mockHttp.put.mockResolvedValue(mockTakeResponse);
      mockProcess.getResponseData.mockResolvedValue(mockTakeResponse);
      mockHttp.post.mockResolvedValue(mockListResponse);
      mockProcess.getResponseData.mockResolvedValue(mockListResponse);

      const id = 'exchange-id';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.takeToWorkExchange(id, type);

      // Then
      expect(mockHttp.put).toHaveBeenCalledWith(
        expect.stringContaining('/accept'),
        {},
        { urlParams: { id } }
      );
      expect(mockLogger.toMessage).toHaveBeenCalledWith('success', 'Заявка взята в работу');
      expect(store.exchangeRequest).toEqual(mockListResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка при взятии в работу' } } };
      mockHttp.put.mockRejectedValue(mockError);

      const id = 'exchange-id';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.takeToWorkExchange(id, type);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка при взятии в работу');
    });
  });

  describe('denyExchange', () => {
    it('should deny exchange and update list on success', async () => {
      // Given
      const mockDenyResponse = { id: '1' };
      const mockListResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.put.mockResolvedValue(mockDenyResponse);
      mockProcess.getResponseData.mockResolvedValue(mockDenyResponse);
      mockHttp.post.mockResolvedValue(mockListResponse);
      mockProcess.getResponseData.mockResolvedValue(mockListResponse);

      const id = 'exchange-id';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.denyExchange(id, type);

      // Then
      expect(mockHttp.put).toHaveBeenCalledWith(
        expect.stringContaining('/deny'),
        {},
        { urlParams: { id } }
      );
      expect(mockLogger.toMessage).toHaveBeenCalledWith('success', 'Вы отказались от заявки');
      expect(store.exchangeRequest).toEqual(mockListResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка при отказе' } } };
      mockHttp.put.mockRejectedValue(mockError);

      const id = 'exchange-id';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.denyExchange(id, type);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка при отказе');
    });
  });

  describe('getFilterAvailableList', () => {
    it('should set exchangeRequest on success', async () => {
      // Given
      const mockResponse = {
        content: [{ id: '1' }], totalPages: 1, totalElements: 1, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const addressFrom = 'Москва';
      const addressTo = 'Санкт-Петербург';
      const desiredDateRange = { start: '2024-01-01', end: '2024-01-31' };

      // When
      await store.getFilterAvailableList(type, addressFrom, addressTo, desiredDateRange);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        {
          pageSetting: { page: 0, size: 10 },
          sortSetting: { directionAsc: false, property: SortProperty.CREATION_DATE },
          addressFrom, addressTo, desiredDateRange,
        },
        { urlParams: { type } }
      );
      expect(store.exchangeRequest).toEqual(mockResponse);
    });

    it('should handle empty desiredDateRange', async () => {
      // Given
      const mockResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const addressFrom = 'Москва';
      const addressTo = 'Санкт-Петербург';

      // When
      await store.getFilterAvailableList(type, addressFrom, addressTo);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        {
          pageSetting: { page: 0, size: 10 },
          sortSetting: { directionAsc: false, property: SortProperty.CREATION_DATE },
          addressFrom, addressTo, desiredDateRange: undefined,
        },
        { urlParams: { type } }
      );
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка фильтрации' } } };
      mockHttp.post.mockRejectedValue(mockError);

      const type: ListType = ListType.AVAILABLE;
      const addressFrom = 'Москва';
      const addressTo = 'Санкт-Петербург';

      // When
      await store.getFilterAvailableList(type, addressFrom, addressTo);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        {
          pageSetting: { page: 0, size: 10 },
          sortSetting: { directionAsc: false, property: SortProperty.CREATION_DATE },
          addressFrom, addressTo, desiredDateRange: undefined,
        },
        { urlParams: { type } }
      );
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка фильтрации');
    });
  });

  describe('getRequestById', () => {
    it('should set exchangeRequest on success', async () => {
      // Given
      const mockResponse = {
        content: [{ id: '1' }], totalPages: 1, totalElements: 1, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const id = 'request-123';

      // When
      await store.getRequestById(type, id);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        { humanReadableId: id },
        { urlParams: { type } }
      );
      expect(store.exchangeRequest).toEqual(mockResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка загрузки' } } };
      mockHttp.post.mockRejectedValue(mockError);

      const type: ListType = ListType.AVAILABLE;
      const id = 'request-123';

      // When
      await store.getRequestById(type, id);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка загрузки');
    });
  });

  describe('updateRouteStatusExchange', () => {
    it('should update route status and reset list on success', async () => {
      // Given
      const mockUpdateResponse = { id: '1' };
      const mockListResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockUpdateResponse);
      mockProcess.getResponseData.mockResolvedValue(mockUpdateResponse);
      mockHttp.post.mockResolvedValue(mockListResponse);
      mockProcess.getResponseData.mockResolvedValue(mockListResponse);

      const id = 'exchange-id';
      const status = 'CARGO_ACCEPTED';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.updateRouteStatusExchange(id, status, type);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        expect.stringContaining('/status'),
        {},
        { urlParams: { id, status } }
      );
      expect(store.exchangeRequest).toEqual(mockListResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка обновления статуса' } } };
      mockHttp.post.mockRejectedValue(mockError);

      const id = 'exchange-id';
      const status = 'CARGO_ACCEPTED';
      const type: ListType = ListType.AVAILABLE;

      // When
      await store.updateRouteStatusExchange(id, status, type);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка обновления статуса');
    });
  });

  describe('getAvailableListWithFilters', () => {
    it('should set exchangeRequest on success with all params', async () => {
      // Given
      const mockResponse = {
        content: [{ id: '1' }], totalPages: 1, totalElements: 1, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };
      const addressFrom = 'Москва';
      const addressTo = 'Санкт-Петербург';
      const desiredDateRange = { start: '2024-01-01', end: '2024-01-31' };

      // When
      await store.getAvailableListWithFilters(type, pageSetting, sortSetting, addressFrom, addressTo, desiredDateRange);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        {
          pageSetting, sortSetting, addressFrom, addressTo, desiredDateRange,
        },
        { urlParams: { type } }
      );
      expect(store.exchangeRequest).toEqual(mockResponse);
    });

    it('should set exchangeRequest on success with only addressFrom and addressTo', async () => {
      // Given
      const mockResponse = {
        content: [], totalPages: 0, totalElements: 0, size: 10, number: 0,
      };
      mockHttp.post.mockResolvedValue(mockResponse);
      mockProcess.getResponseData.mockResolvedValue(mockResponse);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };
      const addressFrom = 'Москва';
      const addressTo = 'Санкт-Петербург';

      // When
      await store.getAvailableListWithFilters(type, pageSetting, sortSetting, addressFrom, addressTo);

      // Then
      expect(mockHttp.post).toHaveBeenCalledWith(
        'cargo-exchange/exchange/:type',
        {
          pageSetting, sortSetting, addressFrom, addressTo, desiredDateRange: undefined,
        },
        { urlParams: { type } }
      );
      expect(store.exchangeRequest).toEqual(mockResponse);
    });

    it('should log error on failure', async () => {
      // Given
      const mockError = { response: { data: { message: 'Ошибка фильтрации' } } };
      mockHttp.post.mockRejectedValue(mockError);

      const type: ListType = ListType.AVAILABLE;
      const pageSetting = { page: 0, size: 10 };
      const sortSetting = { directionAsc: false, property: SortProperty.CREATION_DATE };

      // When
      await store.getAvailableListWithFilters(type, pageSetting, sortSetting);

      // Then
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Ошибка фильтрации');
    });
  });
});
