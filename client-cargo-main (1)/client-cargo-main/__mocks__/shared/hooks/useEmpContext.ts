// Mock for useAppStoreContext in tests
const mockExchangeStore = {
  exchangeRequest: {
    totalElements: 0,
    content: [],
  },
  pageSetting: {
    page: 0,
    size: 10,
  },
  setPageSetting: jest.fn(),
  setSortSetting: jest.fn(),
  resetSettings: jest.fn(),
};

const mockUseAppStoreContext = () => ({
  exchangeStore: mockExchangeStore,
});

const mockUseModalState = () => [false, { show: jest.fn(), hide: jest.fn() }];

const mockUseGetFilters = () => ({ data: null });

export {
  mockExchangeStore,
  mockUseAppStoreContext,
  mockUseModalState,
  mockUseGetFilters,
};

export default {
  useAppStoreContext: mockUseAppStoreContext,
  useModalState: mockUseModalState,
};
