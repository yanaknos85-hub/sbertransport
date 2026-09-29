/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route } from 'react-router-dom';
import MonitorCargoOrderDetailed from '.';

// Мокируем все зависимости
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useRouteMatch: jest.fn(),
}));

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  SpinWrapped: () => <div data-testid="spinner">Загрузка...</div>,
}));

jest.mock('./ShowOrder', () => ({
  __esModule: true,
  default: ({ data, className }) => (
    <div data-testid="show-order" className={className}>
      Order Data: {JSON.stringify(data)}
    </div>
  ),
}));

jest.mock('../styles.module.scss', () => ({}));

describe('MonitorCargoOrderDetailed', () => {
  let mockCargoStore: any;
  let mockUseRouteMatch: jest.Mock;
  let mockUseAppStoreContext: jest.Mock;

  const renderComponent = (id = 'test-id', source = 'test-source') => {
    mockUseRouteMatch.mockReturnValue({
      params: { id, source },
    });

    return render(
      <MemoryRouter initialEntries={[`/order/${id}/${source}`]}>
        <Route path="/order/:id/:source">
          <MonitorCargoOrderDetailed />
        </Route>
      </MemoryRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();

    mockCargoStore = {
      getCargoOrderActive: jest.fn(),
      clearOrderActive: jest.fn(),
      cargoOrderActive: null,
    };

    mockUseRouteMatch = jest.requireMock('react-router-dom').useRouteMatch;
    mockUseAppStoreContext = jest.requireMock('shared/hooks/useAppStoreContext')
      .useAppStoreContext;

    mockUseAppStoreContext.mockReturnValue({
      cargoStore: mockCargoStore,
    });
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  it('должен вызывать getCargoOrderActive с правильными параметрами при монтировании', () => {
    renderComponent('order-123', 'api');

    expect(mockCargoStore.getCargoOrderActive).toHaveBeenCalledTimes(1);
    expect(mockCargoStore.getCargoOrderActive).toHaveBeenCalledWith(
      'order-123',
      'api'
    );
  });

  it('должен вызывать clearOrderActive при размонтировании', () => {
    const { unmount } = renderComponent();

    unmount();

    expect(mockCargoStore.clearOrderActive).toHaveBeenCalledTimes(1);
  });

  it('должен отображать спиннер пока данные загружаются', () => {
    renderComponent();

    expect(screen.getByTestId('spinner')).toBeInTheDocument();
    expect(screen.queryByTestId('show-order')).not.toBeInTheDocument();
  });

  it('должен отображать ShowOrder когда данные загружены', async () => {
    const mockOrderData = { id: 'order-123', name: 'Test Order' };
    mockCargoStore.cargoOrderActive = mockOrderData;

    renderComponent();

    // Проверяем что спиннер скрылся
    await waitFor(() => {
      expect(screen.queryByTestId('spinner')).not.toBeInTheDocument();
    });

    // Проверяем что ShowOrder отобразился
    expect(screen.getByTestId('show-order')).toBeInTheDocument();

    // Проверяем что данные переданы правильно - используем более гибкий поиск
    const showOrderElement = screen.getByTestId('show-order');
    expect(showOrderElement.textContent).toContain('Order Data:');
    expect(showOrderElement.textContent).toContain('"id":"order-123"');
    expect(showOrderElement.textContent).toContain('"name":"Test Order"');
  });

  it('должен повторно запрашивать данные при изменении id или source', () => {
    const { rerender } = renderComponent('order-123', 'api');

    expect(mockCargoStore.getCargoOrderActive).toHaveBeenCalledWith(
      'order-123',
      'api'
    );

    mockUseRouteMatch.mockReturnValue({
      params: { id: 'order-456', source: 'database' },
    });

    rerender(
      <MemoryRouter initialEntries={['/order/order-456/database']}>
        <Route path="/order/:id/:source">
          <MonitorCargoOrderDetailed />
        </Route>
      </MemoryRouter>
    );

    expect(mockCargoStore.getCargoOrderActive).toHaveBeenCalledWith(
      'order-456',
      'database'
    );
    expect(mockCargoStore.getCargoOrderActive).toHaveBeenCalledTimes(2);
  });
});
