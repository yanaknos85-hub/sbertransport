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
    ICargosService: Symbol('ICargosService'),
    ILogger: Symbol('ILogger'),
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
}));

// Mock utils
jest.mock('utils', () => ({
  plainToNew: jest.fn((type: any, data: any) => data || {}),
  declension: jest.fn((count: number, words: string[]) => words[0]),
}));

// Mock utils/io-ts
jest.mock('utils/io-ts', () => ({
  UUID: jest.fn(),
  uuid: jest.fn(() => ({})),
}));

// Mock constants
jest.mock('constants/constants.api', () => ({
  MOCKED_API_PREFIX: '',
}));

jest.mock('constants/CargoRequestStatuses.constants', () => ({
  CargoRequestStatuses: {
    CARGO_AWAITING_TRANSFER: 'CARGO_AWAITING_TRANSFER',
    CARGO_ACCEPTED: 'CARGO_ACCEPTED',
    CARGO_REJECTED: 'CARGO_REJECTED',
  },
  CargoRequestStatusesType: {
    CARGO_AWAITING_TRANSFER: 'CARGO_AWAITING_TRANSFER',
    CARGO_ACCEPTED: 'CARGO_ACCEPTED',
    CARGO_REJECTED: 'CARGO_REJECTED',
  },
}));

// Mock shared models
jest.mock('shared/models/geo/types', () => ({
  IOWaypoint: class IOWaypoint {},
  RequestRoute: class RequestRoute {
    cost = 0;
    time = 0;
    distance = 0;
  },
  Segment: class Segment {},
}));

jest.mock('shared/models/types', () => ({
  ApprovalStateStatuses: {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED',
  },
  RequestType: {
    REGULAR: 'REGULAR',
    ONCE: 'ONCE',
  },
}));

// Mock types/Cargo
jest.mock('types/Cargo', () => ({
  cargoListItem: jest.fn(),
  totalSizes: jest.fn(),
  TransportTypeEnum: {
    DEDICATED: 'DEDICATED',
    COURIER: 'COURIER',
    INTERREGIONAL: 'INTERREGIONAL',
  },
}));

// Mock modules/CargosMultiple
jest.mock('modules/CargosMultiple/CargoTabFilters/types', () => ({
  KeyFilterState: {
    once: 'once',
    regular: 'regular',
  },
  KeyFilterStateMain: {
    once: 'once',
    regular: 'regular',
  },
  KeyFilterStateMainApproval: {
    delivery: 'delivery',
    compensation: 'compensation',
  },
  activeTabFilter: {
    all: 'all',
    authorIdRegular: 'authorIdRegular',
  },
}));

// Mock utils/Misc
jest.mock('utils/Misc', () => ({
  declension: jest.fn((count: number, words: string[]) => words[0]),
}));

// Mock models
jest.mock('./models/CargoRequest.model', () => ({
  CargoRequestModel: class CargoRequestModel {
    constructor(data: any) {
      Object.assign(this, data);
    }
  },
}));

jest.mock('./models/CargoRegularRequest.model', () => ({
  CargoRegularRequestModel: class CargoRegularRequestModel {
    constructor(data: any) {
      Object.assign(this, data);
    }
  },
}));

// Mock Trip models
jest.mock('../Trip/models/TripPagination.model', () => ({
  TripPaginationModel: class TripPaginationModel {
    size = 10;
    totalElements = -1;
    totalPages = 0;
    number = 0;
  },
}));

// Mock constants for shared constants
jest.mock('shared/constants/constants', () => ({
  SOMETHING_WRONG_TITLE: 'Что-то пошло не так',
  CHANGE_STATUS_DESCRIPTION: 'Статус изменен',
  CONFIRMATION_RECEIVING_CARGO: 'Подтверждение получения груза',
  PLACES_DECLENSION_DESCRIPTION: ['место', 'места', 'мест'],
}));

describe('DICargosStore', () => {
  let DICargosStore: any;

  beforeEach(async () => {
    jest.resetModules();
    DICargosStore = (await import('./DICargos.store')).DICargosStore;
  });

  describe('initial values', () => {
    it('should have initial pagination', () => {
      const store = new DICargosStore();
      expect(store.pagination).toEqual({
        size: 10,
        totalElements: -1,
        totalPages: 0,
        number: 0,
      });
    });

    it('should have initial paginationRegular', () => {
      const store = new DICargosStore();
      expect(store.paginationRegular).toEqual({
        size: 10,
        totalElements: -1,
        totalPages: 0,
        number: 0,
      });
    });

    it('should have empty cargoRequestList', () => {
      const store = new DICargosStore();
      expect(store.cargoRequestList).toEqual([]);
    });

    it('should have empty cargoApprovalList', () => {
      const store = new DICargosStore();
      expect(store.cargoApprovalList).toEqual([]);
    });

    it('should have empty cargoMultipleApprovalList', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleApprovalList).toEqual([]);
    });

    it('should have empty cargoRegularRequestList', () => {
      const store = new DICargosStore();
      expect(store.cargoRegularRequestList).toEqual([]);
    });

    it('should have empty cargoRegularApprovalList', () => {
      const store = new DICargosStore();
      expect(store.cargoRegularApprovalList).toEqual([]);
    });

    it('should have empty cargoMultipleRegularApprovalList', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleRegularApprovalList).toEqual([]);
    });

    it('should have null nonTerminalTotalElements', () => {
      const store = new DICargosStore();
      expect(store.nonTerminalTotalElements).toBeNull();
    });

    it('should have null nonTerminalRegularTotalElements', () => {
      const store = new DICargosStore();
      expect(store.nonTerminalRegularTotalElements).toBeNull();
    });

    it('should have null nonTerminalMultipleTotalElements', () => {
      const store = new DICargosStore();
      expect(store.nonTerminalMultipleTotalElements).toBeNull();
    });

    it('should have null nonTerminalMultipleRegularTotalElements', () => {
      const store = new DICargosStore();
      expect(store.nonTerminalMultipleRegularTotalElements).toBeNull();
    });

    it('should have undefined cargoRequest', () => {
      const store = new DICargosStore();
      expect(store.cargoRequest).toBeUndefined();
    });

    it('should have undefined cargoMultipleRequest', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleRequest).toBeUndefined();
    });

    it('should have undefined cargoRegularRequest', () => {
      const store = new DICargosStore();
      expect(store.cargoRegularRequest).toBeUndefined();
    });

    it('should have undefined cargoMultipleRegularRequest', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleRegularRequest).toBeUndefined();
    });

    it('should have empty cargoHistory', () => {
      const store = new DICargosStore();
      expect(store.cargoHistory).toEqual([]);
    });

    it('should have empty cargoMultipleHistory', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleHistory).toEqual([]);
    });

    it('should have empty cargoRegularHistory', () => {
      const store = new DICargosStore();
      expect(store.cargoRegularHistory).toEqual([]);
    });

    it('should have empty cargoMultipleRegularHistory', () => {
      const store = new DICargosStore();
      expect(store.cargoMultipleRegularHistory).toEqual([]);
    });

    it('should have empty externalSender', () => {
      const store = new DICargosStore();
      expect(store.externalSender).toBe('');
    });

    it('should have empty externalRecipient', () => {
      const store = new DICargosStore();
      expect(store.externalRecipient).toBe('');
    });

    it('should have initial keyFilterState', () => {
      const store = new DICargosStore();
      expect(store.keyFilterState).toBe('once');
    });

    it('should have initial keyFilterStateMain', () => {
      const store = new DICargosStore();
      expect(store.keyFilterStateMain).toBe('regular');
    });

    it('should have initial keyFilterStateMainApproval', () => {
      const store = new DICargosStore();
      expect(store.keyFilterStateMainApproval).toBe('delivery');
    });

    it('should have empty checkedItem', () => {
      const store = new DICargosStore();
      expect(store.checkedItem).toBe('');
    });

    it('should have empty checkedListApproval', () => {
      const store = new DICargosStore();
      expect(store.checkedListApproval).toEqual([]);
    });

    it('should have isCheckAll false', () => {
      const store = new DICargosStore();
      expect(store.isCheckAll).toBe(false);
    });

    it('should have isIndeterminate false', () => {
      const store = new DICargosStore();
      expect(store.isIndeterminate).toBe(false);
    });

    it('should have initial listPathName', () => {
      const store = new DICargosStore();
      expect(store.listPathName).toBe('');
    });

    it('should have initial sortingOrders', () => {
      const store = new DICargosStore();
      expect(store.sortingOrders).toBe('По времени создания: По убыванию');
    });

    it('should have initial nameList', () => {
      const store = new DICargosStore();
      expect(store.nameList).toBe('');
    });

    it('should have initial tabFilterName', () => {
      const store = new DICargosStore();
      expect(store.tabFilterName).toEqual({
        once: '',
        regular: 'authorIdRegular',
      });
    });

    it('should have initial tabFilterNameMain', () => {
      const store = new DICargosStore();
      expect(store.tabFilterNameMain).toEqual({
        once: '',
        regular: '',
      });
    });

    it('should have initial activeTabFilter', () => {
      const store = new DICargosStore();
      expect(store.activeTabFilter).toEqual({
        once: 'all',
        regular: 'authorIdRegular',
      });
    });

    it('should have initial activeTabFilterMain', () => {
      const store = new DICargosStore();
      expect(store.activeTabFilterMain).toEqual({
        once: 'once',
        regular: 'regular',
      });
    });

    it('should have initial initialTabFilterName', () => {
      const store = new DICargosStore();
      expect(store.initialTabFilterName).toBe('all');
    });

    it('should have initial initialPageSetting', () => {
      const store = new DICargosStore();
      expect(store.initialPageSetting).toEqual({
        page: 0,
        size: 10,
      });
    });

    it('should have initial initialSortSetting', () => {
      const store = new DICargosStore();
      expect(store.initialSortSetting).toEqual({
        directionAsc: false,
        property: 'CREATION_DATE',
        sortingOrders: 'По времени создания: По убыванию',
      });
    });

    it('should have initial stateSorting', () => {
      const store = new DICargosStore();
      expect(store.stateSorting).toEqual({
        cargoRequestList: 'По времени создания: По убыванию',
        cargoApprovalList: 'По времени создания: По убыванию',
        cargoRegularRequestList: 'По времени создания: По убыванию',
        cargoRegularApprovalList: 'По времени создания: По убыванию',
        cargoMultipleApprovalList: 'По времени создания: По убыванию',
      });
    });

    it('should have initial pageSetting', () => {
      const store = new DICargosStore();
      expect(store.pageSetting).toEqual({
        page: 0,
        size: 10,
      });
    });

    it('should have initial sortMapping', () => {
      const store = new DICargosStore();
      expect(store.sortMapping).toEqual({
        REQUEST_HUMAN_ID: {
          true: 'По порядковому номеру заявки: По возрастанию',
          false: 'По порядковому номеру заявки: По убыванию',
        },
        CREATION_DATE: {
          true: 'По времени создания: По возрастанию',
          false: 'По времени создания: По убыванию',
        },
        DESIRED_DATE: {
          true: 'По сроку доставки: По возрастанию',
          false: 'По сроку доставки: По убыванию',
        },
        EXPECTED_COST: {
          true: 'По цене: По возрастанию',
          false: 'По цене: По убыванию',
        },
      });
    });

    it('should have initial desiredDateRange', () => {
      const store = new DICargosStore();
      expect(store.desiredDateRange).toEqual([null, null]);
    });

    it('should have initial settings', () => {
      const store = new DICargosStore();
      expect(store.settings).toBeDefined();
    });

    it('should have initial filterSettings', () => {
      const store = new DICargosStore();
      expect(store.filterSettings).toEqual({});
    });
  });

  describe('methods', () => {
    it('should have setDesiredDateRange method', () => {
      const store = new DICargosStore();
      expect(typeof store.setDesiredDateRange).toBe('function');
    });

    it('should have createSettings method', () => {
      const store = new DICargosStore();
      expect(typeof store.createSettings).toBe('function');
    });

    it('should have setFilterSettings method', () => {
      const store = new DICargosStore();
      expect(typeof store.setFilterSettings).toBe('function');
    });

    it('should have resetFilterSettings method', () => {
      const store = new DICargosStore();
      expect(typeof store.resetFilterSettings).toBe('function');
    });

    it('should have setActiveSettings method', () => {
      const store = new DICargosStore();
      expect(typeof store.setActiveSettings).toBe('function');
    });

    it('should have setCheckedItem method', () => {
      const store = new DICargosStore();
      expect(typeof store.setCheckedItem).toBe('function');
    });

    it('should have setCheckedListApproval method', () => {
      const store = new DICargosStore();
      expect(typeof store.setCheckedListApproval).toBe('function');
    });

    it('should have setIsCheckAll method', () => {
      const store = new DICargosStore();
      expect(typeof store.setIsCheckAll).toBe('function');
    });

    it('should have setIsIndeterminate method', () => {
      const store = new DICargosStore();
      expect(typeof store.setIsIndeterminate).toBe('function');
    });

    it('should have setCheckedListRoutesById method', () => {
      const store = new DICargosStore();
      expect(typeof store.setCheckedListRoutesById).toBe('function');
    });

    it('should have setPageSetting method', () => {
      const store = new DICargosStore();
      expect(typeof store.setPageSetting).toBe('function');
    });

    it('should have resetSettings method', () => {
      const store = new DICargosStore();
      expect(typeof store.resetSettings).toBe('function');
    });

    it('should have resetListPathName method', () => {
      const store = new DICargosStore();
      expect(typeof store.resetListPathName).toBe('function');
    });

    it('should have setListPathName method', () => {
      const store = new DICargosStore();
      expect(typeof store.setListPathName).toBe('function');
    });

    it('should have resetSortingOrders method', () => {
      const store = new DICargosStore();
      expect(typeof store.resetSortingOrders).toBe('function');
    });

    it('should have setNameList method', () => {
      const store = new DICargosStore();
      expect(typeof store.setNameList).toBe('function');
    });

    it('should have setSortingOrders method', () => {
      const store = new DICargosStore();
      expect(typeof store.setSortingOrders).toBe('function');
    });

    it('should have setTabFilterNameMain method', () => {
      const store = new DICargosStore();
      expect(typeof store.setTabFilterNameMain).toBe('function');
    });

    it('should have setKeyFilterState method', () => {
      const store = new DICargosStore();
      expect(typeof store.setKeyFilterState).toBe('function');
    });

    it('should have setKeyFilterStateMain method', () => {
      const store = new DICargosStore();
      expect(typeof store.setKeyFilterStateMain).toBe('function');
    });

    it('should have setKeyFilterStateMainApproval method', () => {
      const store = new DICargosStore();
      expect(typeof store.setKeyFilterStateMainApproval).toBe('function');
    });

    it('should have setActiveTabFilterMain method', () => {
      const store = new DICargosStore();
      expect(typeof store.setActiveTabFilterMain).toBe('function');
    });

    it('should have resetApprovalJournalFilters method', () => {
      const store = new DICargosStore();
      expect(typeof store.resetApprovalJournalFilters).toBe('function');
    });

    it('should have getMultipleRequestListTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRequestListTerminal).toBe('function');
    });

    it('should have getMultipleRequestListNonTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRequestListNonTerminal).toBe('function');
    });

    it('should have getMultipleApproveListTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleApproveListTerminal).toBe('function');
    });

    it('should have getMultipleRegularApproveListTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularApproveListTerminal).toBe('function');
    });

    it('should have getMultipleApproveListNonTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleApproveListNonTerminal).toBe('function');
    });

    it('should have getMultipleRegularApproveListNonTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularApproveListNonTerminal).toBe('function');
    });

    it('should have getMultipleRegularRequestListTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularRequestListTerminal).toBe('function');
    });

    it('should have getMultipleRegularRequestListNonTerminal method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularRequestListNonTerminal).toBe('function');
    });

    it('should have getMultipleRequestById method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRequestById).toBe('function');
    });

    it('should have getMultipleRegularRequestById method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularRequestById).toBe('function');
    });

    it('should have getMultipleStatusHistoryById method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleStatusHistoryById).toBe('function');
    });

    it('should have getMultipleRegularStatusHistoryById method', () => {
      const store = new DICargosStore();
      expect(typeof store.getMultipleRegularStatusHistoryById).toBe('function');
    });

    it('should have cancelRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.cancelRequest).toBe('function');
    });

    it('should have cancelRegularRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.cancelRegularRequest).toBe('function');
    });

    it('should have approveRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.approveRequest).toBe('function');
    });

    it('should have declineRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.declineRequest).toBe('function');
    });

    it('should have approveRegularRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.approveRegularRequest).toBe('function');
    });

    it('should have declineMultipleRegularRequest method', () => {
      const store = new DICargosStore();
      expect(typeof store.declineMultipleRegularRequest).toBe('function');
    });

    it('should have confirmationReceivingCargo method', () => {
      const store = new DICargosStore();
      expect(typeof store.confirmationReceivingCargo).toBe('function');
    });
  });
});
