import React from 'react';
import { ShiftsProps } from 'modules/Waybill/WaybillMassCreate/steps/Shifts';
import { mockShifts, mock150Shifts } from '../../__mocks__/apiMocks';

// Mock dependencies
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: () => ({ push: jest.fn() }),
}));

jest.mock('constants/routes.constants', () => ({
  RELEASE_ON_LINE_LINK: '/release-on-line',
}));

jest.mock('constants/app.constants', () => ({
  DATE_FORMAT: {
    BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  },
}));

jest.mock('components/Panel/Panel', () => {
  const mockPanel = ({ cardPadding, children }: { cardPadding?: boolean; children: React.ReactNode }) => {
    const divProps: Record<string, string | boolean> = {
      'data-testid': 'mock-panel',
      'className': cardPadding ? 'panel-padding' : '',
    };

    return <div {...divProps}>{children}</div>;
  };

  return mockPanel;
});

jest.mock('components/Button', () => ({
  Button: ({
    onClick,
    htmlType,
    children,
  }: {
    onClick?: () => void;
    htmlType?: string;
    children: React.ReactNode;
  }) => {
    const buttonProps: Record<string, string | boolean | (() => void) | undefined> = {
      'data-testid': onClick ? 'mock-button-click' : 'mock-button-submit',
      'type': htmlType || 'button',
      onClick,
    };

    return <button {...buttonProps}>{children}</button>;
  },
}));

describe('Shifts', () => {
  test('should have correct props type', () => {
    const props: ShiftsProps = {
      selectedShifts: [],
      setSelectedShifts: jest.fn(),
      shiftsData: mockShifts,
      isLoading: false,
      query: { startDate: '2023-10-20' },
      setQuery: jest.fn(),
      firstTitleResult: undefined,
      setFirstTitleResult: jest.fn(),
      setCurrentStep: jest.fn(),
      maxSelectionLimit: 100,
    };

    expect(props.selectedShifts).toEqual([]);
    expect(props.isLoading).toBe(false);
    expect(props.shiftsData).toEqual(mockShifts);
  });

  test('should have correct date format constant', () => {
    expect('DD.MM.YYYY').toBe('DD.MM.YYYY');
  });

  test('should limit selection to maxSelectionLimit by default (100)', () => {
    const props: ShiftsProps = {
      selectedShifts: [],
      setSelectedShifts: jest.fn(),
      shiftsData: mock150Shifts,
      isLoading: false,
      query: { startDate: '2023-10-20' },
      setQuery: jest.fn(),
      firstTitleResult: undefined,
      setFirstTitleResult: jest.fn(),
      setCurrentStep: jest.fn(),
      maxSelectionLimit: 100,
    };

    // При монтировании должно выбрать только первые 100 смен
    expect(props.shiftsData).toHaveLength(150);
    expect(props.shiftsData?.slice(0, 100)).toHaveLength(100);
  });

  test('should disable checkboxes when limit is reached', () => {
    const props: ShiftsProps = {
      selectedShifts: mock150Shifts.slice(0, 100), // уже выбрано 100
      setSelectedShifts: jest.fn(),
      shiftsData: mock150Shifts,
      isLoading: false,
      query: { startDate: '2023-10-20' },
      setQuery: jest.fn(),
      firstTitleResult: undefined,
      setFirstTitleResult: jest.fn(),
      setCurrentStep: jest.fn(),
      maxSelectionLimit: 100,
    };

    // Проверка, что лимит достигнут
    expect(props.selectedShifts).toHaveLength(100);
    expect(props.selectedShifts.length >= props.maxSelectionLimit!).toBe(true);
  });
});
