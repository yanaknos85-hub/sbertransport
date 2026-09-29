import { renderHook } from '@testing-library/react-hooks';
import moment from 'moment';
import { useColumns } from 'modules/Waybill/WaybillMassCreate/steps/Shifts/hooks/useColumns';
import { TMassCreateFirstTitleItem, Shift } from 'api/shifts/shifts.types';
import { UUID } from 'utils/io-ts';

describe('useColumns', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2023-10-20'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  test('should return columns array without error column when no errors', () => {
    const mockFirstTitleResult: TMassCreateFirstTitleItem[] = [
      {
        shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
        ewbId: 'ewb1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p' as UUID,
        humanReadableId: 'EWB-20231020-001',
        fileName: 'shift_report_001.pdf',
        content: 'JVBERi0xLjQKJdPr6eE=',
        creationTime: '2023-10-20T10:30:00',
        errorText: '',
      },
    ];
    const { result } = renderHook(() => useColumns(
      { firstTitleResult: mockFirstTitleResult, errorShiftIds: new Set() }
    ));

    expect(result.current.columns).toBeDefined();
    expect(Array.isArray(result.current.columns)).toBe(true);
    // Без ошибок не должно быть столбца индикатора и карандаша
    expect(result.current.columns.length).toBe(6);
  });

  test('should return columns array with error column when errors exist', () => {
    const mockFirstTitleResult: TMassCreateFirstTitleItem[] = [
      {
        shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
        ewbId: 'ewb1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p' as UUID,
        humanReadableId: 'EWB-20231020-001',
        fileName: 'shift_report_001.pdf',
        content: 'JVBERi0xLjQKJdPr6eE=',
        creationTime: '2023-10-20T10:30:00',
        errorText: 'Ошибка подписи',
      },
    ];
    const { result } = renderHook(() => useColumns({ firstTitleResult: mockFirstTitleResult, errorShiftIds: new Set(['a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8']) }));

    expect(result.current.columns).toBeDefined();
    expect(Array.isArray(result.current.columns)).toBe(true);
    // С ошибками должно быть 8 столбцов (6 обычных + 1 индикатор + 1 карандаш)
    expect(result.current.columns.length).toBe(8);
  });

  test('should have correct column structure', () => {
    const { result } = renderHook(() => useColumns({ firstTitleResult: undefined, errorShiftIds: new Set() }));
    const columns = result.current.columns;

    // Driver column
    expect(columns[0]).toHaveProperty('title', 'Водитель');
    expect(columns[0]).toHaveProperty('dataIndex', 'driverName');
    expect(columns[0]).toHaveProperty('key', 'driverName');
    expect(columns[0]).toHaveProperty('width', 200);

    // Vehicle brand column
    expect(columns[1]).toHaveProperty('title', 'Марка');
    expect(columns[1]).toHaveProperty('dataIndex', 'vehicleBrand');
    expect(columns[1]).toHaveProperty('key', 'vehicleBrand');
    expect(columns[1]).toHaveProperty('width', 120);

    // Vehicle model column
    expect(columns[2]).toHaveProperty('title', 'Модель');
    expect(columns[2]).toHaveProperty('dataIndex', 'vehicleModel');
    expect(columns[2]).toHaveProperty('key', 'vehicleModel');
    expect(columns[2]).toHaveProperty('width', 120);

    // State number column
    expect(columns[3]).toHaveProperty('title', 'Госномер');
    expect(columns[3]).toHaveProperty('dataIndex', 'vehicleStateNumber');
    expect(columns[3]).toHaveProperty('key', 'vehicleStateNumber');
    expect(columns[3]).toHaveProperty('width', 120);

    // Start date column
    expect(columns[4]).toHaveProperty('title', 'Начало смены');
    expect(columns[4]).toHaveProperty('dataIndex', 'startDate');
    expect(columns[4]).toHaveProperty('key', 'startDate');
    expect(columns[4]).toHaveProperty('width', 150);

    // End date column
    expect(columns[5]).toHaveProperty('title', 'Окончание смены');
    expect(columns[5]).toHaveProperty('dataIndex', 'endDate');
    expect(columns[5]).toHaveProperty('key', 'endDate');
    expect(columns[5]).toHaveProperty('width', 150);
  });

  test('should have moment format for date columns', () => {
    const { result } = renderHook(() => useColumns({ firstTitleResult: undefined, errorShiftIds: new Set() }));
    const columns = result.current.columns;

    const startDateColumn = columns[4];
    expect(startDateColumn.render).toBeDefined();

    const endDateColumn = columns[5];
    expect(endDateColumn.render).toBeDefined();

    // Test date formatting
    const testDate = '2023-10-20T00:00:00Z';
    const formattedDate = moment(testDate).format('DD.MM.YYYY');

    const testRecord: Shift = {
      id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
      driverName: 'Test Driver',
      vehicleBrand: 'TestBrand',
      vehicleModel: 'TestModel',
      vehicleStateNumber: 'A123B123',
      startDate: '2023-10-20',
      endDate: '2023-10-21',
    };

    const startDateFormatted = startDateColumn.render?.(testDate, testRecord, 0);
    const endDateFormatted = endDateColumn.render?.(testDate, testRecord, 0);

    expect(startDateFormatted).toBe(formattedDate);
    expect(endDateFormatted).toBe(formattedDate);
  });

  test('columns should be memoized', () => {
    const errorShiftIds = new Set<string>();
    const { result, rerender } = renderHook(() => useColumns({ firstTitleResult: undefined, errorShiftIds }));

    const firstColumns = result.current.columns;
    rerender();
    const secondColumns = result.current.columns;

    expect(firstColumns).toBe(secondColumns);
  });

  test('should handle undefined firstTitleResult', () => {
    const errorShiftIds = new Set<string>();
    const { result } = renderHook(() => useColumns({ firstTitleResult: undefined, errorShiftIds }));
    const columns = result.current.columns;

    const testRecord: Shift = {
      id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
      driverName: 'Test Driver',
      vehicleBrand: 'TestBrand',
      vehicleModel: 'TestModel',
      vehicleStateNumber: 'A123B123',
      startDate: '2023-10-20',
      endDate: '2023-10-21',
    };

    // Test with undefined date
    expect(() => columns[4].render?.(undefined, testRecord, 0)).not.toThrow();
    expect(() => columns[5].render?.(undefined, testRecord, 0)).not.toThrow();
  });

  test('should handle different date formats', () => {
    const errorShiftIds = new Set<string>();
    const { result } = renderHook(() => useColumns({ firstTitleResult: undefined, errorShiftIds }));
    const columns = result.current.columns;

    const testRecord: Shift = {
      id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
      driverName: 'Test Driver',
      vehicleBrand: 'TestBrand',
      vehicleModel: 'TestModel',
      vehicleStateNumber: 'A123B123',
      startDate: '2023-10-20',
      endDate: '2023-10-21',
    };

    const testDates = [
      '2023-10-20T00:00:00Z',
      '2023-12-31T23:59:59Z',
      '2024-01-01T12:00:00Z',
    ];

    testDates.forEach(date => {
      expect(() => columns[4].render?.(date, testRecord, 0)).not.toThrow();
      expect(() => columns[5].render?.(date, testRecord, 0)).not.toThrow();
    });
  });

  test('should have edit column with pencil icon when errors exist', () => {
    const mockFirstTitleResult: TMassCreateFirstTitleItem[] = [
      {
        shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
        ewbId: 'ewb1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p' as UUID,
        humanReadableId: 'EWB-20231020-001',
        fileName: 'shift_report_001.pdf',
        content: 'JVBERi0xLjQKJdPr6eE=',
        creationTime: '2023-10-20T10:30:00',
        errorText: 'Ошибка подписи',
      },
    ];
    const errorShiftIds = new Set(['a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8']);
    const { result } = renderHook(() => useColumns({ firstTitleResult: mockFirstTitleResult, errorShiftIds }));
    const columns = result.current.columns;

    // Проверяем, что последний столбец - это колонка карандаша
    const editColumn = columns[columns.length - 1];
    expect(editColumn.key).toBe('edit');
    expect(editColumn.width).toBe(40);
    expect(editColumn.fixed).toBe('right');

    // Проверяем, что render возвращает Link с иконкой
    const testRecord: Shift = {
      id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
      driverName: 'Test Driver',
      vehicleBrand: 'TestBrand',
      vehicleModel: 'TestModel',
      vehicleStateNumber: 'A123B123',
      startDate: '2023-10-20',
      endDate: '2023-10-21',
    };

    const editContent = editColumn.render?.(testRecord, testRecord, 0);
    expect(editContent).toBeDefined();

    // Проверяем, что для записи без ошибки возвращается null
    const noErrorRecord: Shift = {
      id: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
      driverName: 'Test Driver 2',
      vehicleBrand: 'TestBrand2',
      vehicleModel: 'TestModel2',
      vehicleStateNumber: 'B234C234',
      startDate: '2023-10-21',
      endDate: '2023-10-22',
    };

    const editContentNoError = editColumn.render?.(noErrorRecord, noErrorRecord, 0);
    expect(editContentNoError).toBeNull();
  });
});
