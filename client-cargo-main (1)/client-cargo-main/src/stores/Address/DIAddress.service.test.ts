// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock antd notification to avoid initialization issues
jest.mock('antd', () => ({
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

// Mock ioc types to avoid container initialization
jest.mock('ioc/types', () => ({
  TYPES: {
    IHttpService: Symbol('IHttpService'),
    IResponseService: Symbol('IResponseService'),
  },
}));

// Mock @sber-sbertransport/mf-core to avoid logger initialization
jest.mock('@sber-sbertransport/mf-core', () => ({
  inject: jest.fn(),
  injectable: jest.fn((target: any) => target),
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
  Logger: class Logger {},
  Employee: class Employee {},
  IOHumanReadable: class IOHumanReadable {},
  EmployeeModel: class EmployeeModel {
    fullNameWithCode = '';
    mobilePhone = '';
  },
}));

// Mock constants
jest.mock('constants/constants.env', () => ({
  ORGANIZATIONS: 'organizations',
  SELF: 'self',
  ADDRESSES: 'addresses',
  FAVORITE: 'favorite',
  SELF_ADDRESSES: '/organizations/self/addresses',
  SELF_FREQUENT_ADDRESSES: '/organizations/self/addresses/frequently',
  SELF_FREQUENT_ADDRESSES_PARAMS: '/organizations/self/addresses/frequently/:addressId',
  SELF_FAVORITE_ADDRESSES: '/organizations/self/addresses/favorite',
  SELF_FAVORITE_ADDRESSES_PARAMS: '/organizations/self/addresses/favorite/:addressId',
}));

describe('DIAddressService', () => {
  let DIAddressService: any;
  let service: any;

  beforeEach(async () => {
    jest.resetModules();
    DIAddressService = (await import('./DIAddress.service')).DIAddressService;

    // Создаем моки для зависимостей
    const mockHttp = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
    };
    const mockProcess = {
      getResponseData: jest.fn((response: any, codec: any) => response.data),
      getResponseStatus: jest.fn((response: any) => response.status),
    };

    service = new DIAddressService();
    // Перезаписываем зависимости на моки
    (service as any).http = mockHttp;
    (service as any).process = mockProcess;
  });

  describe('getAddressList', () => {
    it('should call http.get with correct URL', async () => {
      // Given
      const mockResponse = {
        data: [
          {
            id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
          },
          {
            id: '2', label: 'Работа', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
          },
        ],
      };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getAddressList();

      // Then
      expect((service as any).http.get).toHaveBeenCalledWith('/organizations/self/addresses');
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse, expect.anything());
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle empty response', async () => {
      // Given
      const mockResponse = { data: [] };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getAddressList();

      // Then
      expect(result).toEqual([]);
    });
  });

  describe('getFrequentList', () => {
    it('should call http.get with correct URL', async () => {
      // Given
      const mockResponse = {
        data: [
          {
            id: '1', label: 'Частый адрес', country: 'Россия', city: 'Санкт-Петербург', street: 'Невский', house: '3',
          },
        ],
      };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getFrequentList();

      // Then
      expect((service as any).http.get).toHaveBeenCalledWith('/organizations/self/addresses/frequently');
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse, expect.anything());
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle empty response', async () => {
      // Given
      const mockResponse = { data: [] };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getFrequentList();

      // Then
      expect(result).toEqual([]);
    });
  });

  describe('getFavoriteList', () => {
    it('should call http.get with correct URL', async () => {
      // Given
      const mockResponse = {
        data: [
          {
            id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
          },
        ],
      };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getFavoriteList();

      // Then
      expect((service as any).http.get).toHaveBeenCalledWith('/organizations/self/addresses/favorite');
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse, expect.anything());
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle empty response', async () => {
      // Given
      const mockResponse = { data: [] };
      (service as any).http.get.mockResolvedValue(mockResponse);

      // When
      const result = await service.getFavoriteList();

      // Then
      expect(result).toEqual([]);
    });
  });

  describe('createFavoriteAddress', () => {
    it('should call http.post with correct URL and data', async () => {
      // Given
      const mockResponse = {
        data: {
          id: 'new-id', label: 'Новый адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
        },
      };
      (service as any).http.post.mockResolvedValue(mockResponse);

      const address = {
        label: 'Новый адрес',
        country: 'Россия',
        city: 'Москва',
        street: 'Тверская',
        house: '5',
      };

      // When
      const result = await service.createFavoriteAddress(address);

      // Then
      expect((service as any).http.post).toHaveBeenCalledWith(
        '/organizations/self/addresses/favorite',
        { ...address }
      );
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse, expect.anything());
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getFavoriteAddress', () => {
    it('should call http.get with correct URL and urlParams', async () => {
      // Given
      const mockResponse = {
        data: {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        },
      };
      (service as any).http.get.mockResolvedValue(mockResponse);

      const addressId = '1';

      // When
      const result = await service.getFavoriteAddress(addressId);

      // Then
      expect((service as any).http.get).toHaveBeenCalledWith(
        '/organizations/self/addresses/favorite/:addressId',
        { urlParams: { addressId } }
      );
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse, expect.anything());
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('updateFavoriteAddress', () => {
    it('should call http.put with correct URL, data and urlParams', async () => {
      // Given
      const mockResponse = {
        status: 200,
        data: [{
          id: '1', label: 'Обновленный адрес', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        }],
      };
      (service as any).http.put.mockResolvedValue(mockResponse);

      const address = {
        id: '1',
        label: 'Обновленный адрес',
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
      };

      // When
      const result = await service.updateFavoriteAddress(address);

      // Then
      expect((service as any).http.put).toHaveBeenCalledWith(
        '/organizations/self/addresses/favorite/:addressId',
        { ...address },
        { urlParams: { addressId: address.id } }
      );
      expect((service as any).process.getResponseStatus).toHaveBeenCalledWith(mockResponse);
      expect(result).toEqual(mockResponse.status);
    });
  });

  describe('deleteFavoriteAddress', () => {
    it('should call http.delete with correct URL and urlParams', async () => {
      // Given
      const mockResponse = {
        status: 200,
        data: [],
      };
      (service as any).http.delete.mockResolvedValue(mockResponse);

      const addressId = '1';

      // When
      const result = await service.deleteFavoriteAddress(addressId);

      // Then
      expect((service as any).http.delete).toHaveBeenCalledWith(
        '/organizations/self/addresses/favorite/:addressId',
        { urlParams: { addressId } }
      );
      expect((service as any).process.getResponseStatus).toHaveBeenCalledWith(mockResponse);
      expect(result).toEqual(mockResponse.status);
    });
  });

  describe('deleteFrequentAddress', () => {
    it('should call http.delete with correct URL and urlParams', async () => {
      // Given
      const mockResponse = {
        status: 200,
        data: [],
      };
      (service as any).http.delete.mockResolvedValue(mockResponse);

      const addressId = '1';

      // When
      const result = await service.deleteFrequentAddress(addressId);

      // Then
      expect((service as any).http.delete).toHaveBeenCalledWith(
        '/organizations/self/addresses/frequently/:addressId',
        { urlParams: { addressId } }
      );
      expect((service as any).process.getResponseStatus).toHaveBeenCalledWith(mockResponse);
      expect(result).toEqual(mockResponse.status);
    });
  });

  describe('integration scenarios', () => {
    it('should handle sequence of operations: load all lists', async () => {
      // Given
      const addressListResponse = {
        data: [{
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        }],
      };
      const frequentListResponse = {
        data: [{
          id: '2', label: 'Частый', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        }],
      };
      const favoriteListResponse = {
        data: [{
          id: '3', label: 'Избранный', country: 'Россия', city: 'Москва', street: 'Тверская', house: '3',
        }],
      };

      (service as any).http.get.mockResolvedValueOnce(addressListResponse);
      (service as any).http.get.mockResolvedValueOnce(frequentListResponse);
      (service as any).http.get.mockResolvedValueOnce(favoriteListResponse);

      // When
      const addressList = await service.getAddressList();
      const frequentList = await service.getFrequentList();
      const favoriteList = await service.getFavoriteList();

      // Then
      expect(addressList).toEqual(addressListResponse.data);
      expect(frequentList).toEqual(frequentListResponse.data);
      expect(favoriteList).toEqual(favoriteListResponse.data);
    });

    it('should handle create and update sequence', async () => {
      // Given
      const createResponse = {
        data: {
          id: 'new-id', label: 'Новый адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
        },
      };
      const updateResponse = {
        status: 200,
        data: [{
          id: 'new-id', label: 'Обновленный адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
        }],
      };

      (service as any).http.post.mockResolvedValue(createResponse);
      (service as any).http.put.mockResolvedValue(updateResponse);

      const newAddress = {
        label: 'Новый адрес',
        country: 'Россия',
        city: 'Москва',
        street: 'Тверская',
        house: '5',
      };

      const updatedAddress = {
        id: 'new-id',
        label: 'Обновленный адрес',
        country: 'Россия',
        city: 'Москва',
        street: 'Тверская',
        house: '5',
      };

      // When
      const created = await service.createFavoriteAddress(newAddress);
      const updatedStatus = await service.updateFavoriteAddress(updatedAddress);

      // Then
      expect(created).toEqual(createResponse.data);
      expect(updatedStatus).toEqual(updateResponse.status);
    });
  });
});
