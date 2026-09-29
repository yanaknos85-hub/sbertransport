/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';

// Мокаем контекст информации о поездке (useTripInfo)
const mockUseTripInfo = jest.fn();
jest.mock('modules/Trip/context/TripInfo.context', () => ({
  useTripInfo: (...args: any[]) => mockUseTripInfo(...args),
}));

// Мокаем хук useSegments (default export)
const mockUseSegments = jest.fn();
jest.mock('../hooks/useSegments', () => ({
  __esModule: true,
  default: (...args: any[]) => mockUseSegments(...args),
}));

// Мокаем ErrorBoundary, чтобы не тащить реальную реализацию
jest.mock('components/ErrorBoundary', () => ({
  __esModule: true,
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

// Мокаем дочерние компоненты как простые заглушки
jest.mock('../components/Route/Route', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-route">Route stub</div>,
}));

jest.mock('../components/Map/Map', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-map">Map stub</div>,
}));

jest.mock('../components/RouteNullAlert/RouteNullAlert', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-route-null-alert">RouteNullAlert stub</div>,
}));

// Импортируем компонент после моков
import { RouteCard } from '../RouteCard';

const BANNER_TEXT =
  'Расчет фактического маршрута произведен с учетом "спуффинга" геокоординат с высокой погрешностью';

const mockTrip = (status: string) => ({ id: 'trip-1', status });

describe('RouteCard', () => {
  beforeEach(() => {
    mockUseTripInfo.mockReset();
    mockUseSegments.mockReset();
  });

  afterEach(() => {
    cleanup();
  });

  describe('SpoofingWarningBanner', () => {
    it('should render the banner for a finished trip with a built route', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('ORDER_FINISHED') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: false,
      });

      render(<RouteCard />);

      expect(screen.getByText(BANNER_TEXT)).toBeInTheDocument();
    });

    it('should NOT render the banner for a finished trip without a route', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('ORDER_FINISHED') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: null,
        isFetching: false,
      });

      render(<RouteCard />);

      expect(screen.queryByText(BANNER_TEXT)).not.toBeInTheDocument();
      // Вместо баннера отображается RouteNullAlert (взаимоисключение)
      expect(screen.getByTestId('mock-route-null-alert')).toBeInTheDocument();
    });

    it('should NOT render the banner while fetching route data even for a finished trip', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('ORDER_FINISHED') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: true,
      });

      render(<RouteCard />);

      // Условие показа требует !isFetching, поэтому во время загрузки баннер скрыт.
      expect(screen.queryByText(BANNER_TEXT)).not.toBeInTheDocument();
    });

    it('should NOT render the banner for an unfinished trip even with fact segments', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('IN_PROGRESS') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: false,
      });

      render(<RouteCard />);

      expect(screen.queryByText(BANNER_TEXT)).not.toBeInTheDocument();
    });

    it('should NOT render the banner for a trip without status', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: false,
      });

      render(<RouteCard />);

      expect(screen.queryByText(BANNER_TEXT)).not.toBeInTheDocument();
    });

    it('should keep the banner visible when the card is collapsed (header position)', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('ORDER_FINISHED') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: false,
      });

      render(<RouteCard />);

      // Баннер в блоке заголовка, где находится RouteNullAlert, поэтому не зависит от isOpened
      expect(screen.getByText(BANNER_TEXT)).toBeInTheDocument();

      // Сворачиваем карточку
      fireEvent.click(screen.getByText('Скрыть'));

      expect(screen.getByText(BANNER_TEXT)).toBeInTheDocument();
    });

    it('should render the banner together with mocked Route and Map children', () => {
      mockUseTripInfo.mockReturnValue({ trip: mockTrip('ORDER_FINISHED') });
      mockUseSegments.mockReturnValue({
        planSegments: [],
        factSegments: [{ points: [{ longitude: 1, latitude: 1 }] }],
        isFetching: false,
      });

      render(<RouteCard />);

      expect(screen.getByTestId('mock-route')).toBeInTheDocument();
      expect(screen.getByTestId('mock-map')).toBeInTheDocument();
      expect(screen.getByText(BANNER_TEXT)).toBeInTheDocument();
      // Баннер не должен конфликтовать с RouteNullAlert
      expect(screen.queryByTestId('mock-route-null-alert')).not.toBeInTheDocument();
    });
  });
});