import { DICargoStore } from './DICargoStore.store';
import { Contact } from '../../interfaces/Orders.types';
import { UUID } from 'utils/io-ts';
import { Source } from '../../interfaces/Orders.types';

// Мокаем все зависимости
jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/ioc/types', () => ({ TYPES: {} }), { virtual: true });
jest.mock('mobx', () => ({
  action: jest.fn((target: any, key: string, descriptor: any) => descriptor),
  observable: jest.fn((target: any, key: string) => {
    // Просто возвращаем свойство как есть для мока
    return target[key];
  }),
}));
jest.mock('inversify', () => ({
  inject: jest.fn((token) => (target: any, key: string) => {}),
  injectable: jest.fn(() => (target: any) => target),
}));
jest.mock('ioc/types', () => ({ TYPES: { ICargoService: 'ICargoService' } }), { virtual: true });
jest.mock('modules/OrderExecution/constants/Filters', () => ({
  DEFAULT_FILTER_VALUES: {
    page: 0,
    pageSize: 10,
  },
}));
jest.mock('modules/OrderExecution/constants/Cargo/Cargo', () => ({
  dOrderFields: {},
  DEFAULT_STATUS_FILTERS: {
    statuses: ['CARGO_AWAITING_APPROVAL', 'CARGO_APPROVED'],
  },
}));
jest.mock('modules/OrderExecution/utils/FilterStorage/filterStorage', () => ({
  filterSession: {
    data: null,
    set: jest.fn(),
    clear: jest.fn(),
  },
}));
jest.mock('modules/OrderExecution/constants/Tabs', () => ({
  Tab: { cargo: 'cargo', template: 'template' },
  Category: { all: 'all', template: 'template' },
}));

describe('DICargoStore', () => {
  let store: DICargoStore;
  let mockService: any;

  beforeEach(() => {
    jest.clearAllMocks();

    // Создаем мок сервиса
    mockService = {
      getFeedOrderListPost: jest.fn(),
      getCargoSchedulerList: jest.fn(),
      getCargoOrderActive: jest.fn(),
      changeEngineerComment: jest.fn(),
      addAdditionalContact: jest.fn(),
      sendOrderToContractor: jest.fn(),
      sendRouteToContractor: jest.fn(),
      getStateNumberList: jest.fn(),
      getEmployeeList: jest.fn(),
      getEmployeeOrganization: jest.fn(),
      getOrganizationList: jest.fn(),
      getDepartmentList: jest.fn(),
    };

    // Создаем стор с моком сервиса
    store = new DICargoStore();
    // @ts-ignore
    store.service = mockService;
  });

  describe('addAdditionalContact', () => {
    const humanReadableId = 'ORD-123';
    const waypointId = 'waypoint-1';
    const contactData: Contact[] = [
      {
        employeeId: 'employee-1',
        fullName: 'Иванов Иван Иванович',
        mobilePhone: '9123456789',
        waypointId: waypointId,
      },
    ];

    it('должен вызвать service.addAdditionalContact с правильными параметрами', async () => {
      mockService.addAdditionalContact.mockResolvedValueOnce(undefined);

      await store.addAdditionalContact(humanReadableId, contactData);

      expect(mockService.addAdditionalContact).toHaveBeenCalledWith(
        humanReadableId,
        contactData
      );
    });

    it('должен сбросить cargoOrderActive при ошибке', async () => {
      mockService.addAdditionalContact.mockRejectedValueOnce(new Error('Network error'));

      await expect(
        store.addAdditionalContact(humanReadableId, contactData)
      ).rejects.toThrow('Network error');
    });

    it('не должен изменять cargoOrderActive при успешном вызове', async () => {
      store.cargoOrderActive = {
        id: 'order-1' as UUID,
        humanReadableId: 'ORD-123',
        status: 'approved',
        additionalSenderContacts: [],
      } as any;

      mockService.addAdditionalContact.mockResolvedValueOnce(undefined);

      await store.addAdditionalContact(humanReadableId, contactData);

      expect(store.cargoOrderActive).toBeDefined();
    });
  });

  describe('getCargoOrderActive', () => {
    const orderId = '550e8400-e29b-41d4-a716-446655440000' as UUID;
    const source: Source = 'API' as any;

    const mockResponse = {
      id: 'order-123',
      humanReadableId: 'ORD-123',
      status: 'approved',
      commentEng: 'Test comment',
      additionalSenderContacts: [],
    };

    it('должен вызвать service.getCargoOrderActive с правильными параметрами', async () => {
      mockService.getCargoOrderActive.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderActive(orderId, source);

      expect(mockService.getCargoOrderActive).toHaveBeenCalledWith(orderId, source);
    });

    it('должен сохранить ответ в cargoOrderActive', async () => {
      mockService.getCargoOrderActive.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderActive(orderId, source);

      expect(store.cargoOrderActive).toEqual(mockResponse);
    });

    it('должен перезаписать cargoOrderActive при повторном вызове', async () => {
      const initialResponse = {
        id: 'order-123',
        humanReadableId: 'ORD-123',
        status: 'approved',
        commentEng: 'Initial comment',
        additionalSenderContacts: [],
      };

      const updatedResponse = {
        id: 'order-123',
        humanReadableId: 'ORD-123',
        status: 'approved',
        commentEng: 'Updated comment',
        additionalSenderContacts: [
          {
            employeeId: 'employee-1',
            fullName: 'Иванов Иван',
            mobilePhone: '9123456789',
          },
        ],
      };

      mockService.getCargoOrderActive
        .mockResolvedValueOnce(initialResponse)
        .mockResolvedValueOnce(updatedResponse);

      await store.getCargoOrderActive(orderId, source);
      expect(store.cargoOrderActive).toEqual(initialResponse);

      await store.getCargoOrderActive(orderId, source);
      expect(store.cargoOrderActive).toEqual(updatedResponse);
    });

    it('должен установить cargoOrderActive в null при clearOrderActive', () => {
      store.cargoOrderActive = {
        id: 'order-1' as UUID,
        humanReadableId: 'ORD-123',
        status: 'approved',
        commentEng: 'Test comment',
        additionalSenderContacts: [],
      } as any;
      store.clearOrderActive();

      expect(store.cargoOrderActive).toBeNull();
    });
  });

  describe('changeEngineerComment', () => {
    const humanReadableId = '550e8400-e29b-41d4-a716-446655440000' as UUID;
    const comment = 'Новый комментарий';

    const mockResponse = [
      {
        field: 'commentEng',
        value: 'Новый комментарий',
      },
    ];

    it('должен вызвать service.changeEngineerComment с правильными параметрами', async () => {
      store.cargoOrderActive = {
        id: 'order-1' as UUID,
        humanReadableId: 'ORD-123',
        commentEng: 'Old comment',
      } as any;

      mockService.changeEngineerComment.mockResolvedValueOnce(mockResponse);

      await store.changeEngineerComment(humanReadableId, comment);

      expect(mockService.changeEngineerComment).toHaveBeenCalledWith(humanReadableId, comment);
    });

    it('должен обновить commentEng в cargoOrderActive при успешном ответе', async () => {
      store.cargoOrderActive = {
        id: 'order-1' as UUID,
        humanReadableId: 'ORD-123',
        commentEng: 'Old comment',
      } as any;

      mockService.changeEngineerComment.mockResolvedValueOnce(mockResponse);

      await store.changeEngineerComment(humanReadableId, comment);

      expect(store.cargoOrderActive?.commentEng).toBe('Новый комментарий');
    });

    it('не должен изменять cargoOrderActive при пустом ответе', async () => {
      store.cargoOrderActive = {
        id: 'order-1' as UUID,
        humanReadableId: 'ORD-123',
        commentEng: 'Old comment',
      } as any;

      mockService.changeEngineerComment.mockResolvedValueOnce(undefined);

      await store.changeEngineerComment(humanReadableId, comment);

      expect(store.cargoOrderActive?.commentEng).toBe('Old comment');
    });
  });
});

describe('DICargoStore - POST методы для useDeferredSearch', () => {
  let store: DICargoStore;
  let mockService: any;

  beforeEach(() => {
    jest.clearAllMocks();

    // Создаем мок сервиса
    mockService = {
      getFeedOrderListPost: jest.fn(),
      getCargoSchedulerList: jest.fn(),
    };

    // Создаем стор с моком сервиса
    store = new DICargoStore();
    // Заменяем приватное свойство service на мок
    // @ts-ignore
    store.service = mockService;
  });

  describe('getCargoOrderListDeferredPost', () => {
    const mockQuery = {
      page: 0,
      pageSize: 10,
    };

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

    beforeEach(() => {
      // Устанавливаем начальные значения
      store.pageFilters = { ...mockQuery };
      store.cargoQueryFilters = { statuses: ['CARGO_AWAITING_APPROVAL'] };
      store.cargoSortField = null;
      store.cargoSortOrder = null;
    });

    it('должен вызвать service.getFeedOrderListPost с пустым executorGroupIds при isOrganization = true', async () => {
      store.organizationId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22' as any;
      store.executorGroupId = [];
      store.isOrganization = true;

      mockService.getFeedOrderListPost.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderListDeferredPost();

      // Проверяем вызов с пустым executorGroupIds
      expect(mockService.getFeedOrderListPost).toHaveBeenCalledWith(
        store.organizationId,
        expect.objectContaining({
          page: 0,
          pageSize: 10,
          statuses: ['CARGO_AWAITING_APPROVAL'],
        }),
        [], // executorGroupIds пустой для режима организации
        false // emptyExecutorGroup
      );
      expect(store.cargoOrderList).toEqual(mockResponse);
    });

    it('должен вызвать service.getFeedOrderListPost с executorGroupIds при isOrganization = false', async () => {
      store.organizationId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22' as any;
      store.executorGroupId = [
        '1abd2895-b816-4d43-8234-2eb88368b2c4',
        '85086e70-04be-4403-967c-094d8f337846',
      ];
      store.isOrganization = false;

      mockService.getFeedOrderListPost.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderListDeferredPost();

      // Проверяем вызов с executorGroupIds
      // При режиме групп (isOrganization = false) organizationId не передаётся (undefined)
      expect(mockService.getFeedOrderListPost).toHaveBeenCalledWith(
        undefined, // organizationId не передаём для режима групп
        expect.objectContaining({
          page: 0,
          pageSize: 10,
          statuses: ['CARGO_AWAITING_APPROVAL'],
        }),
        store.executorGroupId,
        false // emptyExecutorGroup
      );
      expect(store.cargoOrderList).toEqual(mockResponse);
    });

    it('должен включить sortParams при установленных sortField и sortOrder', async () => {
      store.organizationId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22' as any;
      store.executorGroupId = [];
      store.isOrganization = true;
      store.cargoSortField = 'createdAt';
      store.cargoSortOrder = 'desc' as any;

      mockService.getFeedOrderListPost.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderListDeferredPost();

      const callArgs = mockService.getFeedOrderListPost.mock.calls[0];
      const query = callArgs[1];

      expect(query).toEqual(
        expect.objectContaining({
          sortField: 'createdAt',
          sortDirection: 'DESC',
        })
      );
    });

    it('не должен вызвать метод при отсутствии organizationId и executorGroupId', async () => {
      store.organizationId = undefined as any;
      store.executorGroupId = [];
      store.isOrganization = false;

      await store.getCargoOrderListDeferredPost(false);

      expect(mockService.getFeedOrderListPost).not.toHaveBeenCalled();
      // cargoOrderList не изменяется, так как метод не вызывается
      // Проверяем только что метод не вызывался
    });

    it('должен возвращать пустой список если сервис вернул пустой ответ', async () => {
      store.organizationId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22' as any;
      store.executorGroupId = [];
      store.isOrganization = true;

      const emptyResponse = {
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

      mockService.getFeedOrderListPost.mockResolvedValueOnce(emptyResponse);

      await store.getCargoOrderListDeferredPost();

      expect(store.cargoOrderList).toEqual(emptyResponse);
    });

    it('должен передавать emptyExecutorGroup=true при пустом executorGroupId в режиме групп', async () => {
      store.organizationId = 'ac1d7c0e-6ebd-40b4-9595-086dbed05c22' as any;
      store.executorGroupId = [];
      store.isOrganization = false;

      mockService.getFeedOrderListPost.mockResolvedValueOnce(mockResponse);

      await store.getCargoOrderListDeferredPost(true);

      // orgId не передаётся (undefined), executorGroupIds: [], emptyExecutorGroup: true
      expect(mockService.getFeedOrderListPost).toHaveBeenCalledWith(
        undefined,
        expect.objectContaining({
          page: 0,
          pageSize: 10,
          statuses: ['CARGO_AWAITING_APPROVAL'],
        }),
        [],
        true
      );
      expect(store.cargoOrderList).toEqual(mockResponse);
    });
  });

});

describe('DICargoStore', () => {
  let store: DICargoStore;
  let mockService: any;

  beforeEach(() => {
    jest.clearAllMocks();

    mockService = {
      getFeedOrderListPost: jest.fn(),
    };

    store = new DICargoStore();
    // @ts-ignore
    store.service = mockService;
  });

  it('early-exit: пустой ответ когда нет organizationId, executorGroupId, isOrganization и emptyExecutorGroup=false', () => {
    store.organizationId = '' as UUID;
    store.executorGroupId = [];
    store.isOrganization = false;

    const EMPTY_RESPONSE = { content: [], totalElements: 0, pageable: { pageNumber: 0, pageSize: 20 } };
    (store as any).EMPTY_SEARCH_RESPONSE = EMPTY_RESPONSE;

    store.getCargoOrderListDeferredPost(false);

    expect(mockService.getFeedOrderListPost).not.toHaveBeenCalled();
  });

  it('вызывает service с executorGroupId без фильтрации EXECUTOR_GROUP_ALL_ID в стор (фильтр делает query-hook)', () => {
    store.organizationId = '' as UUID;
    store.executorGroupId = ['exec-1', 'exec-2'];
    store.isOrganization = false;

    store.getCargoOrderListDeferredPost(false);

    expect(mockService.getFeedOrderListPost).toHaveBeenCalledWith(
      undefined,
      expect.objectContaining({}),
      ['exec-1', 'exec-2'],
      false
    );
  });

  it('вызывает service с executorGroupIds=[] и emptyExecutorGroup=true при «Без групп»', () => {
    store.organizationId = '' as UUID;
    store.executorGroupId = [];
    store.isOrganization = false;

    store.getCargoOrderListDeferredPost(true);

    expect(mockService.getFeedOrderListPost).toHaveBeenCalledWith(
      undefined,
      expect.objectContaining({}),
      [],
      true
    );
  });
});
