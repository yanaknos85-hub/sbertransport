
import React from 'react';
import '@testing-library/jest-dom';
import 'whatwg-fetch';
import { render, screen, waitFor } from '@testing-library/react';
import OrderExecutionContent from '../../OrderExecutionContent';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';

// Полифил fetch для jsdom
if (typeof global.fetch === 'undefined') {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({}),
      text: () => Promise.resolve(''),
      ok: true,
      status: 200,
      headers: new Headers(),
      clone: jest.fn(),
      blob: jest.fn(),
      arrayBuffer: jest.fn(),
      formData: jest.fn(),
    })
  ) as any;
}

// Моки для зависимостей
jest.mock('components/Toolbar', () => ({
  ToolbarProvider: ({ children }: { children: React.ReactNode }) => <div data-testid="toolbar-provider">{children}</div>,
}));

jest.mock('../../components/OrderTable/Cargo', () => () => <div data-testid="order-table">Order Table</div>);
jest.mock('../../components/Scheduler/SchedulerTable', () => () => <div data-testid="scheduler-table">Scheduler Table</div>);
jest.mock('../../components/FilterSection', () => () => <div data-testid="filter-section">Filter Section</div>);

jest.mock('shared/hooks/useAppStoreContext');
jest.mock('context/Organization.context');

// ВАЖНО: Мокаем observer из mobx-react, чтобы он не кэшировал рендеринг
jest.mock('mobx-react', () => ({
  observer: (component: any) => component,
}));

jest.mock('ioc/ioc.container', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

const createMockStore = (overrides = {}) => ({
  activeCategory: 'all',
  getCargoOrderListDeferredPost: jest.fn(),
  getCargoSchedulerList: jest.fn(),
  setCargoFilterQueryProps: jest.fn(),
  setOrganizationId: jest.fn(),
  setExecutorGroupId: jest.fn(),
  setIsOrganization: jest.fn(),
  ...overrides,
});

const createMockContext = (overrides = {}) => ({
  organizationId: 'org-1',
  executorGroupId: ['group-1'],
  isOrganization: true,
  ...overrides,
});

describe('OrderExecutionContent', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Рендеринг', () => {
    it('должен отображать SchedulerTable для категории template', () => {
      const mockStore = createMockStore({ activeCategory: 'template' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      render(<OrderExecutionContent />);

      expect(screen.getByTestId('scheduler-table')).toBeInTheDocument();
      expect(screen.queryByTestId('order-table')).not.toBeInTheDocument();
    });

    it('должен отображать OrderTable для не-template категории', () => {
      const mockStore = createMockStore({ activeCategory: 'courier' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      render(<OrderExecutionContent />);

      expect(screen.getByTestId('order-table')).toBeInTheDocument();
      expect(screen.queryByTestId('scheduler-table')).not.toBeInTheDocument();
    });

    it('должен отображать FilterSection внутри ToolbarProvider', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      render(<OrderExecutionContent />);

      expect(screen.getByTestId('toolbar-provider')).toBeInTheDocument();
      expect(screen.getByTestId('filter-section')).toBeInTheDocument();
    });
  });

  describe('Инициализация стора', () => {
    it('должен вызывать setOrganizationId, setExecutorGroupId и setIsOrganization при монтировании', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      const contextValue = createMockContext({
        organizationId: 'org-1',
        executorGroupId: ['group-1'],
        isOrganization: true
      });

      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(contextValue);

      render(<OrderExecutionContent />);

      expect(mockStore.setOrganizationId).toHaveBeenCalledWith('org-1');
      expect(mockStore.setExecutorGroupId).toHaveBeenCalledWith(['group-1']);
      expect(mockStore.setIsOrganization).toHaveBeenCalledWith(true);
    });

    it('должен обновлять данные в сторе при изменении контекста организации', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });

      const initialContext = createMockContext({
        organizationId: 'org-1',
        executorGroupId: ['group-1'],
        isOrganization: true
      });

      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(initialContext);

      const { rerender } = render(<OrderExecutionContent />);

      expect(mockStore.setOrganizationId).toHaveBeenCalledWith('org-1');

      const updatedContext = createMockContext({
        organizationId: 'org-2',
        executorGroupId: ['group-2'],
        isOrganization: false
      });

      (useOrganizationContext as jest.Mock).mockReturnValue(updatedContext);

      rerender(<OrderExecutionContent />);

      expect(mockStore.setOrganizationId).toHaveBeenLastCalledWith('org-2');
      expect(mockStore.setExecutorGroupId).toHaveBeenLastCalledWith(['group-2']);
      expect(mockStore.setIsOrganization).toHaveBeenLastCalledWith(false);
    });
  });

  describe('Loading data', () => {
    it('вызывает getCargoSchedulerList при монтировании с категорией template', () => {
      const mockStore = createMockStore({ activeCategory: 'template' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      render(<OrderExecutionContent />);

      expect(mockStore.getCargoSchedulerList).toHaveBeenCalled();
      expect(mockStore.getCargoOrderListDeferredPost).not.toHaveBeenCalled();
    });

    it('должен вызывать getCargoOrderListDeferredPost с фильтром для конкретной категории при изменении категории', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const updatedStore = createMockStore({ activeCategory: 'courier' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(updatedStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        transportType: 'COURIER',
      });
      expect(updatedStore.getCargoOrderListDeferredPost).toHaveBeenCalled();
    });

    it('должен вызывать getCargoOrderListDeferredPost без фильтра для категории all при изменении на all', () => {
      const mockStore = createMockStore({ activeCategory: 'courier' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const updatedStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(updatedStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        transportType: undefined,
      });
      expect(updatedStore.getCargoOrderListDeferredPost).toHaveBeenCalled();
    });

    it('должен преобразовывать категорию в верхний регистр для фильтра', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const updatedStore = createMockStore({ activeCategory: 'cargo' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(updatedStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        transportType: 'CARGO',
      });
    });
  });

  describe('Обновление при изменении параметров', () => {
    it('должен загружать данные при изменении категории', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const updatedStore = createMockStore({ activeCategory: 'courier' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(updatedStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        transportType: 'COURIER',
      });
      expect(updatedStore.getCargoOrderListDeferredPost).toHaveBeenCalled();
    });

    it('должен загружать данные при изменении организации', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const initialCallCount = mockStore.getCargoOrderListDeferredPost.mock.calls.length;

      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ organizationId: 'org-2' })
      );

      rerender(<OrderExecutionContent />);

      expect(mockStore.getCargoOrderListDeferredPost.mock.calls.length).toBeGreaterThan(initialCallCount);
    });

    it('должен загружать данные при изменении executorGroupId', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const initialCallCount = mockStore.getCargoOrderListDeferredPost.mock.calls.length;

      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ executorGroupId: ['group-2'] })
      );

      rerender(<OrderExecutionContent />);

      expect(mockStore.getCargoOrderListDeferredPost.mock.calls.length).toBeGreaterThan(initialCallCount);
    });

    it('должен загружать данные при изменении isOrganization', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const initialCallCount = mockStore.getCargoOrderListDeferredPost.mock.calls.length;

      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ isOrganization: false })
      );

      rerender(<OrderExecutionContent />);

      expect(mockStore.getCargoOrderListDeferredPost.mock.calls.length).toBeGreaterThan(initialCallCount);
    });

    it('НЕ должен загружать данные при повторном рендере с теми же параметрами', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      const contextValue = createMockContext();

      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(contextValue);

      const { rerender } = render(<OrderExecutionContent />);

      const initialOrderCalls = mockStore.getCargoOrderListDeferredPost.mock.calls.length;
      const initialSchedulerCalls = mockStore.getCargoSchedulerList.mock.calls.length;

      rerender(<OrderExecutionContent />);

      expect(mockStore.getCargoOrderListDeferredPost.mock.calls.length).toBe(initialOrderCalls);
      expect(mockStore.getCargoSchedulerList.mock.calls.length).toBe(initialSchedulerCalls);
    });
  });

  describe('Оптимизация и предотвращение лишних запросов', () => {
    it('не должен дублировать запросы при одинаковых параметрах', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const initialOrderCalls = mockStore.getCargoOrderListDeferredPost.mock.calls.length;
      const initialSchedulerCalls = mockStore.getCargoSchedulerList.mock.calls.length;

      rerender(<OrderExecutionContent />);

      expect(mockStore.getCargoOrderListDeferredPost.mock.calls.length).toBe(initialOrderCalls);
      expect(mockStore.getCargoSchedulerList.mock.calls.length).toBe(initialSchedulerCalls);
    });

    it('должен переключаться между SchedulerTable и OrderTable при смене категории', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      expect(screen.getByTestId('order-table')).toBeInTheDocument();
      expect(screen.queryByTestId('scheduler-table')).not.toBeInTheDocument();

      const updatedStore = createMockStore({ activeCategory: 'template' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(screen.getByTestId('scheduler-table')).toBeInTheDocument();
      expect(screen.queryByTestId('order-table')).not.toBeInTheDocument();
    });

    it('должен корректно обрабатывать смену организации и категории одновременно', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const initialSchedulerCalls = mockStore.getCargoSchedulerList.mock.calls.length;

      const updatedStore = createMockStore({ activeCategory: 'template' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ organizationId: 'org-2' })
      );

      rerender(<OrderExecutionContent />);

      expect(updatedStore.getCargoSchedulerList.mock.calls.length).toBe(initialSchedulerCalls + 1);
      expect(updatedStore.setCargoFilterQueryProps).not.toHaveBeenCalled();
      expect(updatedStore.getCargoOrderListDeferredPost).not.toHaveBeenCalled();
    });
  });

  describe('Обработка ошибок и крайних случаев', () => {
    it('должен работать без executorGroupId', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ executorGroupId: undefined })
      );

      render(<OrderExecutionContent />);

      expect(mockStore.setExecutorGroupId).toHaveBeenCalledWith(undefined);
    });

    it('должен работать с пустой организацией', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(
        createMockContext({ organizationId: undefined })
      );

      render(<OrderExecutionContent />);

      expect(mockStore.setOrganizationId).toHaveBeenCalledWith(undefined);
    });

    it('должен корректно обрабатывать неизвестные категории', () => {
      const mockStore = createMockStore({ activeCategory: 'all' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockStore });
      (useOrganizationContext as jest.Mock).mockReturnValue(createMockContext());

      const { rerender } = render(<OrderExecutionContent />);

      const updatedStore = createMockStore({ activeCategory: 'unknown' });
      (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: updatedStore });

      rerender(<OrderExecutionContent />);

      expect(updatedStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
        transportType: 'UNKNOWN',
      });
      expect(updatedStore.getCargoOrderListDeferredPost).toHaveBeenCalled();
    });
  });
});

describe('OrderExecutionContent', () => {
  const mockCargoStoreInstance = {
    ...createMockStore(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('передает emptyExecutorGroup=true в getCargoOrderListDeferredPost при выборе «Без групп»', async () => {
    (useOrganizationContext as jest.Mock).mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });
    (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockCargoStoreInstance });

    render(<OrderExecutionContent />);

    await waitFor(() => {
      expect(mockCargoStoreInstance.getCargoOrderListDeferredPost).toHaveBeenCalledWith(
        true
      );
    });
  });

  it('повторно вызывает getCargoOrderListDeferredPost при смене флага emptyExecutorGroup', async () => {
    const useOrg = useOrganizationContext as jest.Mock;
    useOrg.mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: [],
      isOrganization: false,
      emptyExecutorGroup: true,
    });
    (useAppStoreContext as jest.Mock).mockReturnValue({ cargoStore: mockCargoStoreInstance });

    const { rerender } = render(<OrderExecutionContent />);
    await waitFor(() => {
      expect(mockCargoStoreInstance.getCargoOrderListDeferredPost).toHaveBeenCalledWith(true);
    });

    mockCargoStoreInstance.getCargoOrderListDeferredPost.mockClear();

    useOrg.mockReturnValue({
      organizationId: 'org-123',
      executorGroupId: ['exec-1'],
      isOrganization: false,
      emptyExecutorGroup: false,
    });

    rerender(<OrderExecutionContent />);

    await waitFor(() => {
      expect(mockCargoStoreInstance.getCargoOrderListDeferredPost).toHaveBeenCalledWith(false);
    });
  });
});
