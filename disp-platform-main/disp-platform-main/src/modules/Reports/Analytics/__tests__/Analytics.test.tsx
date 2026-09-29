import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Analytics from '../Analytics';

// Моки для зависимостей
jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Analytics: {
        title: 'Аналитика',
      },
    },
  }),
}));

jest.mock('api/contractors/contractors.api', () => ({
  useSelfAutopark: jest.fn(),
}));

jest.mock('../components/Filters/Filters', () => () => (
  <div data-testid="filters">Filters Component</div>
));

// Моки для хуков
jest.mock('hooks/useRole', () => ({
  __esModule: true,
  default: () => [],
}));

jest.mock('hooks/useRoleMap', () => ({
  __esModule: true,
  default: () => ({}),
}));

// Мок для хранилища
jest.mock('ioc', () => ({
  useAppStore: () => ({
    authStore: {
      token: 'test-token',
    },
  }),
}));

// Моки для утилит
jest.mock('utils/utils', () => ({
  jwtDecode: jest.fn(() => ({ roles: ['admin'] })),
}));

// Моки для чартов
jest.mock('../charts/VehicleAnalytics/VehicleAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="vehicle-analytics">Vehicle Analytics</div>,
}));

jest.mock('../charts/OneVehicleAnalytics/OneVehicleAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="one-vehicle-analytics">One Vehicle Analytics</div>,
}));

jest.mock('../charts/RepairAnalytics/RepairAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="repair-analytics">Repair Analytics</div>,
}));

jest.mock('../charts/MileageCostAnalytics/MileageCostAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="mileage-cost-analytics">Mileage Cost Analytics</div>,
}));

jest.mock('../charts/FuelConsumptionAnalytics/FuelConsumptionAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="fuel-consumption-analytics">Fuel Consumption Analytics</div>,
}));

jest.mock('../charts/UpdateAnalytics/UpdateAnalytics', () => ({
  __esModule: true,
  default: () => <div data-testid="update-analytics">Update Analytics</div>,
}));

// Моки для компонентов UI
jest.mock('components/ErrorBoundary', () => ({ children }: { children: React.ReactNode }) => <div>{children}</div>
);

jest.mock('components/SpinWrapped/SpinWrapped', () => () => (
  <div data-testid="spinner">Loading...</div>
));

// Мок для всего модуля AnalyticsQuery чтобы избежать ошибок
jest.mock('../context/AnalyticsQuery', () => ({
  AnalyticsQueryProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="analytics-query-provider">{children}</div>
  ),
  useAnalyticsQuery: () => ({
    query: {
      stateNumbers: ['ABC123', 'DEF456'],
    },
    setQuery: jest.fn(),
  }),
}));

// Типизация для моков
const mockUseSelfAutopark = jest.requireMock('api/contractors/contractors.api').useSelfAutopark as jest.Mock;

describe('Analytics Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseSelfAutopark.mockReturnValue({
      data: {
        isInternal: true,
      },
    });
  });

  test('рендерит заголовок и предупреждение', () => {
    render(
      <MemoryRouter>
        <Analytics />
      </MemoryRouter>
    );

    expect(screen.getByText('Аналитика')).toBeInTheDocument();
    expect(screen.getByText('Раздел находится в разработке')).toBeInTheDocument();
  });

  test('рендерит компонент Filters', () => {
    render(
      <MemoryRouter>
        <Analytics />
      </MemoryRouter>
    );

    expect(screen.getByTestId('filters')).toBeInTheDocument();
  });

  test('рендерит основные компоненты чартов', async () => {
    render(
      <MemoryRouter>
        <Analytics />
      </MemoryRouter>
    );

    // Проверяем наличие основных чартов
    await waitFor(() => {
      expect(screen.getByTestId('vehicle-analytics')).toBeInTheDocument();
      expect(screen.getByTestId('repair-analytics')).toBeInTheDocument();
      expect(screen.getByTestId('mileage-cost-analytics')).toBeInTheDocument();
      expect(screen.getByTestId('fuel-consumption-analytics')).toBeInTheDocument();
      expect(screen.getByTestId('update-analytics')).toBeInTheDocument();
    });
  });

  test('не перенаправляет при наличии доступа', () => {
    render(
      <MemoryRouter initialEntries={['/analytics']}>
        <Analytics />
      </MemoryRouter>
    );

    // Просто проверяем, что компонент отрендерился
    expect(screen.getByText('Аналитика')).toBeInTheDocument();
  });
});
