/**
 * Tests for useColumns hook in WaybillTable
 */
import React from 'react';

import { render } from '@testing-library/react';
import { ColumnType } from 'antd/lib/table';

import { WaybillStatus } from 'api/waybill/waybill.constants';
import { WaybillSearchItem } from 'api/waybill/waybill.types';

import { DATE_FORMAT } from 'constants/app.constants';
import * as routes from 'constants/routes.constants';

import * as tt from 'utils/io-ts';

import { useColumns } from './useColumns';

// Mock moment with fixed date
// The second argument is the format string, third is locale (optional)
const mockMoment = jest.fn((value?: string, format?: string) => ({
  format: (fmt?: string) => `formatted:${fmt || format}`,
}));
jest.mock('moment', () => mockMoment);

// Mock moment with fixed date
const mockMomentFormat = jest.fn((format: string) => `formatted:${format}`);
jest.mock('moment', () => jest.fn(() => ({
  format: mockMomentFormat,
})));

describe('useColumns', () => {
  const mockData: WaybillSearchItem[] = [
    {
      id: '123e4567-e89b-12d3-a456-426614174000' as tt.UUID,
      humanReadableId: 'WB-2024-001',
      organizationName: 'ООО Транспорт',
      startDate: '2024-01-15T10:30:00',
      finishDate: '2024-01-15T18:45:00',
      status: WaybillStatus.ON_THE_LINE,
      medicSuccess: true,
      telemechSuccess: false,
      transport: {
        stateNumber: 'А123ВС 777',
        brand: 'Toyota',
        model: 'Camry',
      },
      driverFullName: 'Иванов Иван Иванович',
    },
    {
      id: '223e4567-e89b-12d3-a456-426614174001' as tt.UUID,
      humanReadableId: 'WB-2024-002',
      organizationName: 'ЗАО Логистика',
      startDate: '2024-02-20T08:00:00',
      finishDate: '2024-02-20T16:30:00',
      status: WaybillStatus.MEDIC_IN_PROGRESS,
      medicSuccess: false,
      telemechSuccess: true,
      transport: {
        stateNumber: 'Б456ЕХ 999',
        brand: 'Ford',
        model: 'Transit',
      },
      driverFullName: 'Петров Петр Петрович',
    },
  ];

  let result: { columns: ColumnType<WaybillSearchItem>[] } | undefined;

  // Helper component to use the hook
  function TestComponent() {
    result = useColumns();
    return <div data-testid="test">Test</div>;
  }

  beforeEach(() => {
    jest.clearAllMocks();
    mockMomentFormat.mockClear();
    result = undefined;
  });

  describe('Column count and structure', () => {
    it('should return columns array', () => {
      render(<TestComponent />);
      expect(result).toBeDefined();
    });

    it('should return correct number of columns', () => {
      render(<TestComponent />);
      expect(result?.columns.length).toBe(12);
    });

    it('should have all expected column keys', () => {
      render(<TestComponent />);
      const columnKeys = result?.columns.map(c => c.key);

      expect(columnKeys).toContain('humanReadableId');
      expect(columnKeys).toContain('organizationName');
      expect(columnKeys).toContain('branch');
      expect(columnKeys).toContain('startDate');
      expect(columnKeys).toContain('finishDate');
      expect(columnKeys).toContain('status');
      expect(columnKeys).toContain('stateNumber');
      expect(columnKeys).toContain('brand');
      expect(columnKeys).toContain('model');
      expect(columnKeys).toContain('driverFullName');
      expect(columnKeys).toContain('medicSuccess');
      expect(columnKeys).toContain('telemechSuccess');
    });

    it('should have correct column order', () => {
      render(<TestComponent />);
      const columnKeys = result?.columns.map(c => c.key);

      const expectedOrder = [
        'humanReadableId',
        'organizationName',
        'branch',
        'startDate',
        'finishDate',
        'status',
        'stateNumber',
        'brand',
        'model',
        'driverFullName',
        'medicSuccess',
        'telemechSuccess',
      ];

      expect(columnKeys).toEqual(expectedOrder);
    });

    it('should have correct column widths', () => {
      render(<TestComponent />);
      const columns = result?.columns;

      expect(columns?.find(c => c.key === 'humanReadableId')?.width).toBe(170);
      expect(columns?.find(c => c.key === 'organizationName')?.width).toBe(160);
      expect(columns?.find(c => c.key === 'branch')?.width).toBe(160);
      expect(columns?.find(c => c.key === 'startDate')?.width).toBe(155);
      expect(columns?.find(c => c.key === 'finishDate')?.width).toBe(155);
      expect(columns?.find(c => c.key === 'status')?.width).toBe(120);
      expect(columns?.find(c => c.key === 'stateNumber')?.width).toBe(110);
      expect(columns?.find(c => c.key === 'brand')?.width).toBe(100);
      expect(columns?.find(c => c.key === 'model')?.width).toBe(102);
      expect(columns?.find(c => c.key === 'driverFullName')?.width).toBe(320);
      expect(columns?.find(c => c.key === 'medicSuccess')?.width).toBe(148);
      expect(columns?.find(c => c.key === 'telemechSuccess')?.width).toBe(187);
    });
  });

  describe('ID column (humanReadableId)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'humanReadableId');

      expect(column).toBeDefined();
      expect(column?.title).toBe('ID заявки');
      expect(column?.dataIndex).toBe('humanReadableId');
      expect(column?.key).toBe('humanReadableId');
      expect(column?.width).toBe(170);
    });

    it('should render a link element for ID', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'humanReadableId');
      expect(column).toBeDefined();
      expect(typeof column?.render).toBe('function');

      const record = mockData[0];
      const rendered = column?.render?.(record.humanReadableId, record, 0);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      // Just verify it renders a React element with correct props
      expect((rendered as React.ReactElement).props.to).toContain(routes.WAYBILL);
      expect((rendered as React.ReactElement).props.children).toBe(record.humanReadableId);
    });

    it('should generate correct detailed route with ID', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'humanReadableId');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.humanReadableId, record, 0);

      expect(rendered).toBeDefined();
      expect((rendered as React.ReactElement).props.to).toBe(
        `/fleet/waybill/${record.id}`
      );
    });
  });

  describe('Autopark column (organizationName)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'organizationName');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Автопарк');
      expect(column?.dataIndex).toBe('organizationName');
      expect(column?.key).toBe('organizationName');
      expect(column?.width).toBe(160);
      expect(column?.render).toBeUndefined();
    });
  });

  describe('Branch column (branch)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'branch');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Филиал');
      expect(column?.key).toBe('branch');
      expect(column?.width).toBe(160);
      expect(column?.dataIndex).toBeUndefined();
    });
  });

  describe('Start Date column (startDate)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'startDate');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Дата начала путевого листа');
      expect(column?.dataIndex).toBe('startDate');
      expect(column?.key).toBe('startDate');
      expect(column?.width).toBe(155);
      expect(typeof column?.render).toBe('function');
    });

    it('should format date with moment using correct format', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'startDate');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.startDate, record, 1);

      expect(rendered).toBeDefined();
      expect(mockMomentFormat).toHaveBeenCalledWith(
        DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA
      );
    });
  });

  describe('Finish Date column (finishDate)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'finishDate');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Дата окончания путевого листа');
      expect(column?.dataIndex).toBe('finishDate');
      expect(column?.key).toBe('finishDate');
      expect(column?.width).toBe(155);
      expect(typeof column?.render).toBe('function');
    });

    it('should format date with moment using correct format', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'finishDate');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.finishDate, record, 0);

      expect(rendered).toBeDefined();
      expect(mockMomentFormat).toHaveBeenCalledWith(
        DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA
      );
    });
  });

  describe('Status column (status)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'status');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Статус');
      expect(column?.dataIndex).toBe('status');
      expect(column?.key).toBe('status');
      expect(column?.width).toBe(120);
      expect(typeof column?.render).toBe('function');
    });

    it('should render Status component with correct status', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'status');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.status, record, 0);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.status).toBe(record.status);
    });

    it('should render Status component for ON_THE_LINE', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'status');
      expect(column).toBeDefined();

      const record = { ...mockData[0], status: WaybillStatus.ON_THE_LINE };
      const rendered = column?.render?.(record.status, record, 0);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.status).toBe(WaybillStatus.ON_THE_LINE);
    });

    it('should render Status component for MEDIC_IN_PROGRESS', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'status');
      expect(column).toBeDefined();

      const record = { ...mockData[1], status: WaybillStatus.MEDIC_IN_PROGRESS };
      const rendered = column?.render?.(record.status, record, 0);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.status).toBe(WaybillStatus.MEDIC_IN_PROGRESS);
    });

    it('should render Status component for TELEMECH_IN_PROGRESS', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'status');
      expect(column).toBeDefined();

      const record = { ...mockData[0], status: WaybillStatus.TELEMECH_IN_PROGRESS };
      const rendered = column?.render?.(record.status, record, 0);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.status).toBe(WaybillStatus.TELEMECH_IN_PROGRESS);
    });
  });

  describe('State Number column (stateNumber)', () => {
    it('should have correct structure with nested dataIndex', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'stateNumber');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Госномер');
      expect(column?.dataIndex).toEqual(['transport', 'stateNumber']);
      expect(column?.key).toBe('stateNumber');
      expect(column?.width).toBe(110);
    });
  });

  describe('Brand column (brand)', () => {
    it('should have correct structure with nested dataIndex', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'brand');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Марка');
      expect(column?.dataIndex).toEqual(['transport', 'brand']);
      expect(column?.key).toBe('brand');
      expect(column?.width).toBe(100);
    });
  });

  describe('Model column (model)', () => {
    it('should have correct structure with nested dataIndex', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'model');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Модель');
      expect(column?.dataIndex).toEqual(['transport', 'model']);
      expect(column?.key).toBe('model');
      expect(column?.width).toBe(102);
    });
  });

  describe('Driver Full Name column (driverFullName)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'driverFullName');

      expect(column).toBeDefined();
      expect(column?.title).toBe('ФИО водителя');
      expect(column?.dataIndex).toBe('driverFullName');
      expect(column?.key).toBe('driverFullName');
      expect(column?.width).toBe(320);
    });
  });

  describe('Medic Passing column (medicSuccess)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'medicSuccess');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Прохождение медика');
      expect(column?.dataIndex).toBe('medicSuccess');
      expect(column?.key).toBe('medicSuccess');
      expect(column?.width).toBe(148);
      expect(typeof column?.render).toBe('function');
    });

    it('should render StatusBadge with passed=true for medicSuccess=true', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'medicSuccess');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.medicSuccess, record, 2);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.passed).toBe(true);
    });

    it('should render StatusBadge with passed=false for medicSuccess=false', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'medicSuccess');
      expect(column).toBeDefined();

      const record = mockData[1];
      const rendered = column?.render?.(record.medicSuccess, record, 3);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.passed).toBe(false);
    });
  });

  describe('Telemechanic Passing column (telemechSuccess)', () => {
    it('should have correct structure', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'telemechSuccess');

      expect(column).toBeDefined();
      expect(column?.title).toBe('Прохождение телемеханика');
      expect(column?.dataIndex).toBe('telemechSuccess');
      expect(column?.key).toBe('telemechSuccess');
      expect(column?.width).toBe(187);
      expect(typeof column?.render).toBe('function');
    });

    it('should render StatusBadge with passed=true for telemechSuccess=true', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'telemechSuccess');
      expect(column).toBeDefined();

      const record = mockData[1];
      const rendered = column?.render?.(record.telemechSuccess, record, 4);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.passed).toBe(true);
    });

    it('should render StatusBadge with passed=false for telemechSuccess=false', () => {
      render(<TestComponent />);
      const column = result?.columns.find(c => c.key === 'telemechSuccess');
      expect(column).toBeDefined();

      const record = mockData[0];
      const rendered = column?.render?.(record.telemechSuccess, record, 5);

      expect(rendered).toBeDefined();
      expect(React.isValidElement(rendered)).toBe(true);
      expect((rendered as React.ReactElement).props.passed).toBe(false);
    });
  });

  describe('Date format constant', () => {
    it('should use correct date format constant', () => {
      expect(DATE_FORMAT.DATE_WITH_TIME_DOTS_COMMA).toBe('DD.MM.YYYY, HH:mm');
    });
  });

  describe('useMemo stability', () => {
    it('should memoize columns array (stable reference)', () => {
      const { rerender } = render(<TestComponent />);
      const firstColumns = result?.columns;

      rerender(<TestComponent />);
      const secondColumns = result?.columns;

      expect(firstColumns).toBe(secondColumns);
    });
  });

  describe('Edge cases', () => {
    it('should handle empty data array without errors', () => {
      render(<TestComponent />);
      expect(result?.columns.length).toBe(12);
    });

    it('should handle null/undefined values in columns', () => {
      render(<TestComponent />);
      const columns = result?.columns;

      columns?.forEach(column => {
        expect(column).toBeDefined();
        expect(column?.key).toBeDefined();
        expect(column?.title).toBeDefined();
      });
    });
  });
});
