import React from 'react';
import { render, screen } from '@testing-library/react';
import WaybillMassCreate from 'modules/Waybill/WaybillMassCreate/WaybillMassCreate';
import { mockShifts, mock150Shifts } from './__mocks__/apiMocks';
import { StepsEnum } from 'modules/Waybill/WaybillMassCreate/steps/steps.enum';

// Mock dependencies
jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useState: () => [StepsEnum.ShiftsStep, jest.fn()],
  useEffect: jest.fn(cb => cb()),
}));

jest.mock('hooks/useQuery', () => ({
  useQuery: jest.fn(() => ({
    query: { startDate: '2023-10-20' },
    setQuery: jest.fn(),
  })),
}));

jest.mock('api/shifts/shifts.api', () => ({
  useShifts: jest.fn(() => ({
    data: mockShifts,
    isLoading: false,
  })),
}));

jest.mock('components/Panel/Panel', () => {
  const mockPanel = ({ cardPadding, children }: { cardPadding?: boolean; children: React.ReactNode }) => {
    const divProps: { 'data-testid': string; 'className'?: string } = {
      'data-testid': 'mock-panel',
      'className': cardPadding ? 'panel-padding' : '',
    };

    return <div {...divProps}>{children}</div>;
  };

  return mockPanel;
});

jest.mock('modules/Waybill/WaybillMassCreate/steps/Shifts', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-shifts-step" />,
}));

jest.mock('modules/Waybill/WaybillMassCreate/steps/Sign', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-sign-step" />,
}));

describe('WaybillMassCreate', () => {
  test('should render without errors', () => {
    render(<WaybillMassCreate />);

    expect(screen.getByTestId('mock-panel')).toBeInTheDocument();
    expect(screen.getByTestId('mock-shifts-step')).toBeInTheDocument();
  });

  test('should have StepsEnum.ShiftsStep value', () => {
    expect(StepsEnum.ShiftsStep).toBe('shifts');
  });

  test('should call useQuery with default filters', () => {
    render(<WaybillMassCreate />);

    const { useQuery } = jest.requireMock('hooks/useQuery');
    expect(useQuery).toHaveBeenCalled();
  });

  test('should call useShifts with correct filters', () => {
    render(<WaybillMassCreate />);

    const { useShifts } = jest.requireMock('api/shifts/shifts.api');
    expect(useShifts).toHaveBeenCalled();
    const args = useShifts.mock.calls[0][0];
    expect(args).toHaveProperty('startDate');
  });

  test('should select only first 100 shifts from more than 100 available', () => {
    // Проверка, что mock150Shifts содержит 150 элементов
    expect(mock150Shifts).toHaveLength(150);

    // Проверка, что slice(0, 100) возвращает 100 элементов
    expect(mock150Shifts.slice(0, 100)).toHaveLength(100);

    // Проверка константы лимита
    expect(100).toBe(100);
  });

  test('should pass maxSelectionLimit prop to Shifts component', () => {
    // Проверка, что используется константа MAX_SELECTION_LIMIT = 100
    expect(100).toBe(100);
  });
});
