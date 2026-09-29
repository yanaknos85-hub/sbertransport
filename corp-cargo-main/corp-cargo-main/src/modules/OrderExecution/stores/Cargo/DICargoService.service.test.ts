/* eslint-disable @typescript-eslint/no-explicit-any */
import { REQUESTS_OTO_FEED_CARGO_POST } from 'constants/constants.api';

// Мокаем все зависимости до импорта
jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/ioc/types', () => ({ TYPES: {} }), { virtual: true });
jest.mock('axios', () => ({
  AxiosError: class AxiosError extends Error {
    constructor(
      public message: string,
      public code?: string,
      public response?: any,
      public request?: any,
    ) {
      super(message);
    }
  },
}));
jest.mock('qs', () => ({
  stringify: jest.fn((obj, options) => {
    const parts: string[] = [];
    Object.keys(obj).forEach((key) => {
      if (Array.isArray(obj[key])) {
        obj[key].forEach((val: any) => {
          parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(val)}`);
        });
      } else {
        parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(obj[key])}`);
      }
    });
    return parts.join('&');
  }),
}));

// Мокаем inversify до импорта DICargoService
jest.mock('inversify', () => ({
  inject: jest.fn((token) => (target: any, key: string) => {}),
  injectable: jest.fn(() => (target: any) => target),
}));
jest.mock('inversify/lib/annotation/decorator_utils', () => ({
  _tagParameterOrProperty: jest.fn(),
  tagParameterOrProperty: jest.fn(),
  tagProperty: jest.fn(),
  tagType: jest.fn(),
}));
jest.mock('inversify/lib/annotation/inject', () => ({
  __decorate: jest.fn((decorators, target, key, desc) => desc),
}));

// Мокаем logger
jest.mock('@sber-sbertransport/mf-core', () => ({
  ResponseService: jest.fn(),
  IHttpService: jest.fn(),
  ILogger: jest.fn(),
}));

// Импортируем после всех моков
import { DICargoService } from './DICargoService.service';

describe('DICargoService - POST методы для useDeferredSearch', () => {
  let service: DICargoService;

  // Моки для зависимостей
  const mockHttp = {
    post: jest.fn(),
  } as any;

  const mockResponseService = {
    getResponseData: jest.fn((response) => response),
  } as any;

  const mockLogger = {
    toMessage: jest.fn(),
    toError: jest.fn(),
  } as any;

  beforeEach(() => {
    jest.clearAllMocks();

    service = new DICargoService();
    // Меняем приватные свойства через set
    Object.defineProperty(service, 'http', { value: mockHttp });
    Object.defineProperty(service, 'process', { value: mockResponseService });
    Object.defineProperty(service, 'logger', { value: mockLogger });
  });

  describe('getFeedOrderListPost', () => {
    const mockOrgId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22';
    const mockQuery = {
      page: 0,
      pageSize: 10,
      statuses: ['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED'],
      requestType: ['Cargo'],
      transportTypeEnum: 'COURIER',
      sortSetting: { property: 'createdAt', directionAsc: false },
    };
    const mockExecutorGroupIds = [
      '1abd2895-b816-4d43-8234-2eb88368b2c4',
      '85086e70-04be-4403-967c-094d8f337846',
    ];

    const mockResponse = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 0,
      number: 0,
      numberOfElements: 0,
      first: true,
      last: true,
      empty: true,
      sort: { sorted: false, unsorted: true, empty: true },
      pageable: { offset: 0, pageNumber: 0, pageSize: 0, paged: false, unpaged: true, sort: { sorted: false, unsorted: true, empty: true } },
    };

    it('должен вызвать POST с правильным URL и телом при наличии orgId и executorGroupIds', async () => {
      mockHttp.post.mockResolvedValueOnce({ config: { data: mockResponse } });
      mockResponseService.getResponseData.mockResolvedValueOnce(mockResponse);

      const result = await service.getFeedOrderListPost(mockOrgId, mockQuery, mockExecutorGroupIds);

      // Проверяем URL
      expect(mockHttp.post).toHaveBeenCalledWith(
        REQUESTS_OTO_FEED_CARGO_POST,
        expect.anything(),
        expect.objectContaining({
          urlParams: {},
        })
      );

      // Проверяем тело запроса - organizationId только если передан
      const requestBody = mockHttp.post.mock.calls[0][1];
      expect(requestBody).toEqual({
        page: 0,
        pageSize: 10,
        transportTypeEnum: 'COURIER',
        sortSetting: { property: 'createdAt', directionAsc: false },
        organizationId: mockOrgId, // organizationId добавляется, так как передан
        statuses: ['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED'],
        requestTypes: ['Cargo'],
        executorGroupIds: mockExecutorGroupIds,
        emptyExecutorGroup: false,
      });

      // statuses и requestType удаляются из тела запроса
      expect(requestBody.statuses).toEqual(['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED']);
      expect(requestBody.requestType).toBeUndefined();

      expect(result).toEqual(mockResponse);
    });

    it('должен вызвать POST с пустым executorGroupIds при организации и executorGroupIds:[]', async () => {
      mockHttp.post.mockResolvedValueOnce({ config: { data: mockResponse } });
      mockResponseService.getResponseData.mockResolvedValueOnce(mockResponse);

      const result = await service.getFeedOrderListPost(mockOrgId, mockQuery, []);

      const requestBody = mockHttp.post.mock.calls[0][1];
      expect(requestBody).toEqual({
        page: 0,
        pageSize: 10,
        transportTypeEnum: 'COURIER',
        sortSetting: { property: 'createdAt', directionAsc: false },
        organizationId: mockOrgId,
        statuses: ['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED'],
        requestTypes: ['Cargo'],
        executorGroupIds: [],
        emptyExecutorGroup: false,
      });
    });

    it('должен вызвать POST без organizationId при undefined orgId и с executorGroupIds', async () => {
      mockHttp.post.mockResolvedValueOnce({ config: { data: mockResponse } });
      mockResponseService.getResponseData.mockResolvedValueOnce(mockResponse);

      const result = await service.getFeedOrderListPost(undefined, mockQuery, mockExecutorGroupIds);

      const requestBody = mockHttp.post.mock.calls[0][1];
      // organizationId НЕ должен быть в теле, так как передан undefined
      expect(requestBody.organizationId).toBeUndefined();
      expect(requestBody.executorGroupIds).toEqual(mockExecutorGroupIds);
      // statuses и requestTypes должны быть в теле
      expect(requestBody.statuses).toEqual(['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED']);
      expect(requestBody.requestTypes).toEqual(['Cargo']);
    });
  });
});

describe('DICargoService', () => {
  let service: DICargoService;

  const mockHttp = {
    post: jest.fn(),
  } as any;

  const mockResponseService = {
    getResponseData: jest.fn((response) => response),
  } as any;

  const mockLogger = {
    toMessage: jest.fn(),
    toError: jest.fn(),
  } as any;

  beforeEach(() => {
    jest.clearAllMocks();

    service = new DICargoService();
    Object.defineProperty(service, 'http', { value: mockHttp });
    Object.defineProperty(service, 'process', { value: mockResponseService });
    Object.defineProperty(service, 'logger', { value: mockLogger });
  });

  it('передает emptyExecutorGroup=true в body при явном true', async () => {
    mockHttp.post.mockResolvedValueOnce({
      config: { data: { emptyExecutorGroup: true, executorGroupIds: [] } },
    });

    await service.getFeedOrderListPost(
      'org-123',
      { statuses: ['CREATED'] } as any,
      undefined,
      true
    );

    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining(REQUESTS_OTO_FEED_CARGO_POST),
      expect.objectContaining({
        emptyExecutorGroup: true,
        executorGroupIds: [],
      }),
      expect.any(Object)
    );
  });

  it('передает emptyExecutorGroup=false в body по умолчанию (undefined → false)', async () => {
    mockHttp.post.mockResolvedValueOnce({
      config: { data: { emptyExecutorGroup: false, executorGroupIds: ['exec-1'] } },
    });

    await service.getFeedOrderListPost(
      'org-123',
      { statuses: ['CREATED'] } as any,
      ['exec-1'],
      undefined
    );

    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining(REQUESTS_OTO_FEED_CARGO_POST),
      expect.objectContaining({
        emptyExecutorGroup: false,
        executorGroupIds: ['exec-1'],
      }),
      expect.any(Object)
    );
  });

  it('передает emptyExecutorGroup=false явно', async () => {
    mockHttp.post.mockResolvedValueOnce({
      config: { data: { emptyExecutorGroup: false, executorGroupIds: ['exec-1'] } },
    });

    await service.getFeedOrderListPost(
      'org-123',
      { statuses: ['CREATED'] } as any,
      ['exec-1'],
      false
    );

    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining(REQUESTS_OTO_FEED_CARGO_POST),
      expect.objectContaining({
        emptyExecutorGroup: false,
        executorGroupIds: ['exec-1'],
      }),
      expect.any(Object)
    );
  });
});
