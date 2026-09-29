import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { DetailedView } from '../DetailedView';

// ==========================
// МОКИ ЗАВИСИМОСТЕЙ
// ==========================

beforeAll(() => {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: jest.fn().mockImplementation((query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    })),
  });
});
jest.mock('api/profile', () => ({
  useProfile: jest.fn(),
}));

jest.mock('api/cargo-registry-route-search', () => ({
  useGetSearchRouteRegistry: jest.fn(),
  useSearchCargoRegistry: jest.fn(),
}));

jest.mock('../../hooks/useTransformedData', () => ({
  useTransformedData: jest.fn(),
}));

jest.mock('../../constants', () => ({
  VisibleFields: {
    humanReadableId: 'humanReadableId',
    status: 'status',
  },
}));

jest.mock('shared/styles/reportsDetailedView.module.scss', () => ({
  tripDetailedView: 'tripDetailedView',
  tableWrapper: 'tableWrapper',
  label: 'label',
  coopTripItem: 'coopTripItem',
}));

jest.mock('i18n', () => {
  const t: any = (key: string) => key;
  t.Forms = {
    registryCargoRoutesSettings: {
      humanReadableId: 'ID заявки',
      status: 'Статус',
      unknownField: 'Неизвестное поле',
    },
  };
  return { useTranslation: () => ({ t }) };
});

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  SpinWrapped: () => <div data-testid="spin-wrapped">Loading...</div>,
}));

// Приведение типов для моков
const mUseProfile = require('api/profile').useProfile as jest.Mock;
const mUseGetRoute = require('api/cargo-registry-route-search').useGetSearchRouteRegistry as jest.Mock;
const mUseSearchCargo = require('api/cargo-registry-route-search').useSearchCargoRegistry as jest.Mock;
const mUseTransform = require('../../hooks/useTransformedData').useTransformedData as jest.Mock;

describe('DetailedView Component', () => {
  const mockId = 'route-123';

  beforeEach(() => {
    jest.clearAllMocks();

    mUseProfile.mockReturnValue({
      data: { organizationId: 99 },
    });

    mUseGetRoute.mockReturnValue({
      data: {
        humanReadableId: 'HR-001',
        routeName: 'Moscow - SPB',
      },
    });

    mUseSearchCargo.mockReturnValue({
      data: {
        content: [
          { status: 'ACTIVE', cargoType: 'FTL' },
        ],
      },
    });

    mUseTransform.mockImplementation((items) => items);
  });

  describe('Rendering Logic', () => {
    it('should render nothing when mainContent is undefined/null', () => {
      mUseGetRoute.mockReturnValue({ data: null });

      const { container } = render(<DetailedView id={mockId} />);

      expect(container.firstChild).toBeNull();
    });

    it('should render Descriptions with correct labels and values when data is loaded', async () => {
      render(<DetailedView id={mockId} />);

      await waitFor(() => {
        expect(screen.getByText('ID заявки')).toBeInTheDocument();
      });

      expect(screen.getByText('HR-001')).toBeInTheDocument();
      expect(screen.getByText('Статус')).toBeInTheDocument();
      expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    });

    it('should merge auxContent and mainContent correctly via useTransformedData', () => {
      render(<DetailedView id={mockId} />);

      expect(mUseTransform).toHaveBeenCalledWith([
        expect.objectContaining({
          humanReadableId: 'HR-001',
          routeName: 'Moscow - SPB',
          status: 'ACTIVE',
          cargoType: 'FTL',
        }),
      ]);
    });
  });

  describe('API Calls', () => {
    it('should call useGetSearchRouteRegistry with provided id', () => {
      render(<DetailedView id={mockId} />);

      expect(mUseGetRoute).toHaveBeenCalledWith(mockId);
    });

    it('should call useSearchCargoRegistry with humanReadableId and organizationId', () => {
      render(<DetailedView id={mockId} />);

      expect(mUseSearchCargo).toHaveBeenCalledWith(
        {
          humanReadableId: 'HR-001',
          organizationId: 99,
        },
        { enabled: true }
      );
    });

    it('should handle case when auxContent is missing in search response', () => {
      mUseSearchCargo.mockReturnValue({
        data: { content: [] },
      });
      mUseTransform.mockImplementation((items) => items);

      render(<DetailedView id={mockId} />);

      expect(mUseTransform).toHaveBeenCalledWith([
        expect.objectContaining({
          humanReadableId: 'HR-001',
          routeName: 'Moscow - SPB',
        }),
      ]);
    });
  });

  describe('Records Generation (useMemo)', () => {
    it('should map transformed data keys to labels from i18n', async () => {
      mUseTransform.mockReturnValue([{
        humanReadableId: 'HR-001',
        status: 'COMPLETED',
      }]);

      render(<DetailedView id={mockId} />);

      await waitFor(() => {
        expect(screen.getByText('ID заявки')).toBeInTheDocument();
        expect(screen.getByText('Статус')).toBeInTheDocument();
      });
    });

    it('should render value even if translation label is missing', async () => {
      mUseTransform.mockReturnValue([{
        someUnknownKey: 'some-value',
      }]);

      render(<DetailedView id={mockId} />);

      await waitFor(() => {
        expect(screen.getByText('some-value')).toBeInTheDocument();
      });
    });
  });
});
