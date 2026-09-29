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
  MOCKED_API_PREFIX: '',
}));

jest.mock('constants/constants.api', () => ({
  MOCKED_API_PREFIX: '',
  CARGO_TYPE_SEARCH: '/tariffs/:organizationId/cargo/type/available',
  CARGO_TYPE_POST: '/organizations/:organizationId/cargo/type',
}));

describe('DICargoTypeService', () => {
  let DICargoTypeService: any;
  let service: any;

  beforeEach(async () => {
    jest.resetModules();
    DICargoTypeService = (await import('./DICargoType.service')).DICargoTypeService;

    // Создаем моки для зависимостей
    const mockHttp = {
      get: jest.fn(),
      post: jest.fn(),
    };
    const mockProcess = {
      getResponseData: jest.fn((response: any) => response.data),
    };

    service = new DICargoTypeService();
    // Перезаписываем зависимости на моки
    (service as any).http = mockHttp;
    (service as any).process = mockProcess;
  });

  describe('searchCargoType', () => {
    it('should call http.get with correct URL and params', async () => {
      // Given
      const mockResponse = {
        data: [
          {
            id: '1', name: 'Test Cargo 1', type: 'OTHER', category: 'REGULAR', length: 10, width: 10, height: 10, weight: 10, volume: 10,
          },
          {
            id: '2', name: 'Test Cargo 2', type: 'TECHNIQUE', category: 'REGULAR', length: 20, width: 20, height: 20, weight: 20, volume: 20,
          },
        ],
      };
      (service as any).http.get.mockResolvedValue(mockResponse);

      const text = 'test';
      const organizationId = 'org123';

      // When
      const result = await service.searchCargoType(text, organizationId);

      // Then
      expect((service as any).http.get).toHaveBeenCalledWith(
        `${(await import('constants/constants.api')).MOCKED_API_PREFIX}/tariffs/:organizationId/cargo/type/available?text=${text}`,
        { urlParams: { organizationId } }
      );
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse);
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle empty response', async () => {
      // Given
      const mockResponse = { data: [] };
      (service as any).http.get.mockResolvedValue(mockResponse);

      const text = 'no results';
      const organizationId = 'org456';

      // When
      const result = await service.searchCargoType(text, organizationId);

      // Then
      expect(result).toEqual([]);
    });
  });

  describe('postCargoType', () => {
    it('should call http.post with correct URL, data and params', async () => {
      // Given
      const mockResponse = { data: { id: 'new-id' } };
      (service as any).http.post.mockResolvedValue(mockResponse);

      const data = {
        name: 'New Cargo',
        type: 'OTHER',
        category: 'REGULAR',
        length: 10,
        width: 5,
        height: 3,
        weight: 100,
        volume: 150,
      };
      const organizationId = 'org123';

      // When
      const result = await service.postCargoType(data, organizationId);

      // Then
      expect((service as any).http.post).toHaveBeenCalledWith(
        '/organizations/:organizationId/cargo/type',
        data,
        { urlParams: { organizationId } }
      );
      expect((service as any).process.getResponseData).toHaveBeenCalledWith(mockResponse);
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle POST with minimal data', async () => {
      // Given
      const mockResponse = { data: { id: 'minimal-id' } };
      (service as any).http.post.mockResolvedValue(mockResponse);

      const data = {
        name: 'Minimal Cargo',
        type: 'DOCUMENT',
        category: 'CORRESPONDENCE',
        length: 1,
        width: 1,
        height: 1,
        weight: 1,
        volume: 1,
      };
      const organizationId = 'org-minimal';

      // When
      const result = await service.postCargoType(data, organizationId);

      // Then
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle POST without MOCKED_API_PREFIX', async () => {
      // Given
      const mockResponse = { data: { id: 'no-prefix-id' } };
      (service as any).http.post.mockResolvedValue(mockResponse);

      const data = {
        name: 'Test Cargo',
        type: 'FURNITURE',
        category: 'REGULAR',
        length: 100,
        width: 50,
        height: 30,
        weight: 500,
        volume: 150000,
      };
      const organizationId = 'org-no-prefix';

      // When
      const result = await service.postCargoType(data, organizationId);

      // Then
      expect((service as any).http.post).toHaveBeenCalledWith(
        '/organizations/:organizationId/cargo/type',
        data,
        { urlParams: { organizationId } }
      );
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('integration scenarios', () => {
    it('should handle search and post sequence', async () => {
      // Given
      const searchResponse = {
        data: [{
          id: '1', name: 'Test Cargo', type: 'OTHER', category: 'REGULAR', length: 10, width: 10, height: 10, weight: 10, volume: 10,
        }],
      };
      const postResponse = { data: { id: 'created-id' } };

      (service as any).http.get.mockResolvedValue(searchResponse);
      (service as any).http.post.mockResolvedValue(postResponse);

      const organizationId = 'org-integration';

      // When - search first
      const searchResult = await service.searchCargoType('test', organizationId);

      // Then - search results
      expect(searchResult).toEqual(searchResponse.data);

      // When - post after
      const postData = {
        name: 'Created Cargo',
        type: 'OTHER',
        category: 'REGULAR',
        length: 20,
        width: 10,
        height: 5,
        weight: 200,
        volume: 300000,
      };
      const postResult = await service.postCargoType(postData, organizationId);

      // Then - post results
      expect(postResult).toEqual(postResponse.data);
    });
  });
});
