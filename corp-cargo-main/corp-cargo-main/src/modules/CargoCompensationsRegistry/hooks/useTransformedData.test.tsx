global.fetch = jest.fn();

// Перехватываем все импорты, которые начинаются с src/ или @/
jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/stores/index', () => ({}), { virtual: true });
jest.mock('src/shared/hooks/useAppStoreContext', () => ({}), { virtual: true });
jest.mock('src/api/index', () => ({}), { virtual: true });
jest.mock('src/api/register-search', () => ({}), { virtual: true });
jest.mock('src/stores/PersonalSearch/PersonalSearch.interface', () => ({}), { virtual: true });
jest.mock('src/modules/Planner/types', () => ({}), { virtual: true });
jest.mock('src/stores/Planner/DIPlanner.store', () => ({}), { virtual: true });
jest.mock('src/ioc/ioc.stores', () => ({}), { virtual: true });

// Правильный мок для io-ts
jest.mock('io-ts', () => ({
  type: jest.fn(() => ({})),
  number: jest.fn(),
  string: jest.fn(),
  boolean: jest.fn(),
  array: jest.fn(() => ({})),
  union: jest.fn(() => ({})),
  literal: jest.fn(() => ({})),
  readonly: jest.fn(() => ({})),
  partial: jest.fn(() => ({})),
  record: jest.fn(() => ({})),
  unknown: jest.fn(),
  undefined: jest.fn(),
  null: jest.fn(),
  never: jest.fn(),
  intersection: jest.fn(() => ({})),
  keyof: jest.fn(() => ({})),
  exact: jest.fn(() => ({})),
}));

jest.mock('@sber-sbertransport/mf-core', () => ({}), { virtual: true });

// Мокаем зависимости хука
jest.mock('utils/formatTime', () => ({
  formatBaseDate: jest.fn(),
}));
jest.mock('utils/convertToRubles', () => ({
  convertToRubles: jest.fn(),
}));
jest.mock('constants/constants.app', () => ({
  emptySign: '—',
}));

import { renderHook } from '@testing-library/react-hooks';
import { useTransformedData } from './useTransformedData';
import * as formatTime from 'utils/formatTime';
import * as convertToRubles from 'utils/convertToRubles';
import { CompensationType } from '../types';
import { VisibleFields } from '../constants';

describe('useTransformedData', () => {
  beforeEach(() => {
    (formatTime.formatBaseDate as jest.Mock).mockImplementation(date => date || '—');
    (convertToRubles.convertToRubles as jest.Mock).mockImplementation(cost => cost ? cost / 100 : null);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('возвращает пустой массив если data пустой', () => {
    const { result } = renderHook(() => useTransformedData([]));
    expect(result.current).toEqual([]);
  });

  it('возвращает пустой массив если data undefined', () => {
    const { result } = renderHook(() => useTransformedData(undefined as any));
    expect(result.current).toEqual([]);
  });

  it('трансформирует полные данные', () => {
    const mockData: CompensationType[] = [
      {
        id: '123',
        humanReadableId: 'COMP-001',
        routeNumber: 'RTE-001',
        courier: 'Иван Иванов',
        department: 'Отдел доставки',
        mvz: 'МВЗ-001',
        cost: 50000,
        status: 'Одобрено',
        deadline: '2023-12-25',
        approvedBy: 'Петр Петров',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-21',
      },
    ];

    const { result } = renderHook(() => useTransformedData(mockData));
    const row = result.current[0];

    expect(row.id).toBe('123');
    expect(row[VisibleFields.humanReadableId]).toBe('COMP-001');
    expect(row[VisibleFields.routeNumber]).toBe('RTE-001');
    expect(row[VisibleFields.courier]).toBe('Иван Иванов');
    expect(row[VisibleFields.department]).toBe('Отдел доставки');
    expect(row[VisibleFields.mvz]).toBe('МВЗ-001');
    expect(row[VisibleFields.cost]).toBe(500);
    expect(row[VisibleFields.status]).toBe('Одобрено');
    expect(row[VisibleFields.deadline]).toBe('2023-12-25');
    expect(row[VisibleFields.approvedBy]).toBe('Петр Петров');
    expect(row[VisibleFields.approvalDate]).toBe('2023-12-20');
    expect(row[VisibleFields.formationDate]).toBe('2023-12-21');
  });

  it('использует emptySign для отсутствующих полей', () => {
    const mockData: CompensationType[] = [
      {
        id: null,
        humanReadableId: null,
        routeNumber: null,
        courier: null,
        department: null,
        mvz: null,
        cost: null,
        status: null,
        deadline: null,
        approvedBy: null,
        approvalDate: null,
        formationDate: null,
      } as any,
    ];

    (convertToRubles.convertToRubles as jest.Mock).mockReturnValue(null);
    (formatTime.formatBaseDate as jest.Mock).mockImplementation(date => date || '—');

    const { result } = renderHook(() => useTransformedData(mockData));
    const row = result.current[0];

    expect(row.id).toBe('—');
    expect(row[VisibleFields.humanReadableId]).toBe('—');
    expect(row[VisibleFields.routeNumber]).toBe('—');
    expect(row[VisibleFields.courier]).toBe('—');
    expect(row[VisibleFields.department]).toBe('—');
    expect(row[VisibleFields.mvz]).toBe('—');
    expect(row[VisibleFields.cost]).toBe('—');
    expect(row[VisibleFields.status]).toBe('—');
    expect(row[VisibleFields.deadline]).toBe('—');
    expect(row[VisibleFields.approvedBy]).toBe('—');
    expect(row[VisibleFields.approvalDate]).toBe('—');
    expect(row[VisibleFields.formationDate]).toBe('—');
  });

  it('корректно обрабатывает нулевые значения cost', () => {
    const mockData: CompensationType[] = [
      {
        id: '1',
        humanReadableId: 'TEST-001',
        routeNumber: 'RTE-001',
        courier: 'Тестовый Курьер',
        department: 'Тестовый отдел',
        mvz: 'МВЗ-001',
        cost: 0,
        status: 'В обработке',
        deadline: '2023-12-25',
        approvedBy: 'Тестовый',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-21',
      },
    ];

    (convertToRubles.convertToRubles as jest.Mock).mockReturnValue(null);

    const { result } = renderHook(() => useTransformedData(mockData));
    const row = result.current[0];

    expect(row[VisibleFields.cost]).toBe('—');
  });

  it('корректно обрабатывает несколько записей', () => {
    const mockData: CompensationType[] = [
      {
        id: '1',
        humanReadableId: 'COMP-001',
        routeNumber: 'RTE-001',
        courier: 'Курьер 1',
        department: 'Отдел 1',
        mvz: 'МВЗ-001',
        cost: 10000,
        status: 'Одобрено',
        deadline: '2023-12-25',
        approvedBy: 'Утверждающий 1',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-21',
      },
      {
        id: '2',
        humanReadableId: 'COMP-002',
        routeNumber: 'RTE-002',
        courier: 'Курьер 2',
        department: 'Отдел 2',
        mvz: 'МВЗ-002',
        cost: 20000,
        status: 'На рассмотрении',
        deadline: '2023-12-26',
        approvedBy: 'Утверждающий 2',
        approvalDate: '2023-12-21',
        formationDate: '2023-12-23',
      },
    ];

    const { result } = renderHook(() => useTransformedData(mockData));

    expect(result.current).toHaveLength(2);
    expect(result.current[0].id).toBe('1');
    expect(result.current[1].id).toBe('2');
    expect(result.current[0][VisibleFields.courier]).toBe('Курьер 1');
    expect(result.current[1][VisibleFields.courier]).toBe('Курьер 2');
  });

  it('корректно вызывает convertToRubles для cost', () => {
    const mockData: CompensationType[] = [
      {
        id: '1',
        humanReadableId: 'COMP-001',
        routeNumber: 'RTE-001',
        courier: 'Курьер',
        department: 'Отдел',
        mvz: 'МВЗ-001',
        cost: 10000,
        status: 'Одобрено',
        deadline: '2023-12-25',
        approvedBy: 'Утверждающий',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-21',
      },
    ];

    renderHook(() => useTransformedData(mockData));

    expect(convertToRubles.convertToRubles).toHaveBeenCalledTimes(1);
    expect(convertToRubles.convertToRubles).toHaveBeenCalledWith(10000);
  });

  it('использует useMemo для оптимизации', () => {
    const mockData: CompensationType[] = [
      {
        id: '1',
        humanReadableId: 'COMP-001',
        routeNumber: 'RTE-001',
        courier: 'Курьер',
        department: 'Отдел',
        mvz: 'МВЗ-001',
        cost: 10000,
        status: 'Одобрено',
        deadline: '2023-12-25',
        approvedBy: 'Утверждающий',
        approvalDate: '2023-12-20',
        formationDate: '2023-12-21',
      },
    ];

    const { result, rerender } = renderHook(
      ({ data }) => useTransformedData(data),
      { initialProps: { data: mockData } }
    );

    const firstResult = result.current;

    rerender({ data: mockData });

    const secondResult = result.current;

    expect(firstResult).toBe(secondResult);

    const newData = [{ ...mockData[0], id: '2' }];
    rerender({ data: newData });

    const thirdResult = result.current;

    expect(firstResult).not.toBe(thirdResult);
  });
});
