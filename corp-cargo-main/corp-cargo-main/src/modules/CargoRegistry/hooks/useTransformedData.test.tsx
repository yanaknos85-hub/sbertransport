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
jest.mock('io-ts', () => ({}), { virtual: true });
jest.mock('@sber-sbertransport/mf-core', () => ({}), { virtual: true });

// Мокаем зависимости хука
jest.mock('api/cargo-registry-search', () => ({
  useCargoTripStatuses: jest.fn(),
  useCargoTransportTypes: jest.fn(),
}));
jest.mock('utils/reportsUtils', () => ({ getTripStatus: jest.fn() }));
jest.mock('utils/formatTime', () => ({
  formatBaseDate: jest.fn(),
  formatBaseTime: jest.fn(),
  formatTimeDate: jest.fn(),
}));
jest.mock('utils/convertToRubles', () => ({ convertToRubles: jest.fn() }));
jest.mock('utils/formatPhoneNumber', () => ({ formatPhoneNumber: jest.fn() }));
jest.mock('utils/formatVolume', () => ({ formatVolume: jest.fn() }));
jest.mock('constants/constants.app', () => ({ emptySign: '—' }));

import { renderHook } from '@testing-library/react-hooks';
import { useTransformedData } from './useTransformedData';
import { useCargoTripStatuses, useCargoTransportTypes } from 'api/cargo-registry-search';
import * as reportsUtils from 'utils/reportsUtils';
import * as formatTime from 'utils/formatTime';
import * as convertToRubles from 'utils/convertToRubles';
import * as formatPhoneNumber from 'utils/formatPhoneNumber';
import * as formatVolume from 'utils/formatVolume';
import { TripInfo } from 'stores/CargoRegistry/CargoRegistry.interface';
import { VisibleFields } from '../constants';

describe('useTransformedData', () => {
  const mockStatuses = [{ id: '1', name: 'Pending' }];
  const mockTransportTypes = [{ name: 'truck', rusName: 'Грузовик' }];

  beforeEach(() => {
    (useCargoTripStatuses as jest.Mock).mockReturnValue({ data: mockStatuses });
    (useCargoTransportTypes as jest.Mock).mockReturnValue({ data: mockTransportTypes });

    (reportsUtils.getTripStatus as jest.Mock).mockImplementation((_, status, fallback) => status || fallback);
    (formatTime.formatTimeDate as jest.Mock).mockImplementation((date, fallback) => date || fallback);
    (formatTime.formatBaseDate as jest.Mock).mockImplementation((date, fallback) => date || fallback);
    (formatTime.formatBaseTime as jest.Mock).mockImplementation((time, fallback) => time || fallback);
    (convertToRubles.convertToRubles as jest.Mock).mockImplementation((val) => val / 100);
    (formatPhoneNumber.formatPhoneNumber as jest.Mock).mockImplementation((phone) => phone);
    (formatVolume.formatVolume as jest.Mock).mockImplementation((vol) => vol);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('возвращает пустой массив если data пустой', () => {
    const { result } = renderHook(() => useTransformedData([]));
    expect(result.current).toEqual([]);
  });

  it('трансформирует полные данные', () => {
    const mockTrip = {
      id: '123',
      humanReadableId: 'TRIP-001',
      status: 'PENDING',
      transportType: 'truck',
      desiredDate: '2023-01-01T10:00:00Z',
      contractor: { name: 'Contractor A', id: 'c1' },
      author: { fio: 'John Doe', mobilePhone: '1234567890', personnelNumber: 'P123' },
      expected: { distance: 100.5, waypointsCount: 3, cost: 50000 },
      weight: 1500,
      volume: 10,
      sender: { fio: 'Sender Name', mobilePhone: '111222333' },
      recipient: { fio: 'Recipient Name', mobilePhone: '444555666' },
      senderAddress: 'Sender St',
      recipientAddress: 'Recipient St',
      senderOrganization: 'Sender Org',
      recipientOrganization: 'Recipient Org',
      creationTime: '2023-01-02T08:30:00Z',
      source: 'web',
      cargoTripId: 'TRIP-001',
      templateNumber: 'TMPL-1',
      evaluation: { rating: 5 },
      costCenter: 'CC-1',
      plannedDeliveryDate: '2023-01-05',
      economy: 10000,
      shipmentTime: '2023-01-03',
      transferTime: '2023-01-04',
      deadlineDate: '2023-01-06',
      actualCost: 55000,
      actualDistance: 110.2,
    } as any as TripInfo;

    const { result } = renderHook(() => useTransformedData([mockTrip]));
    const row = result.current[0] as any;

    expect(row.id).toBe('123');
    expect(row[VisibleFields.humanReadableId]).toBe('TRIP-001');
    expect(row[VisibleFields.status]).toBe('PENDING');
    expect(row[VisibleFields.transportType]).toBe('Грузовик');
    expect(row[VisibleFields.expectedCost]).toBe('500,00');
    expect(row[VisibleFields.expectedDistance]).toBe('100,5');
    expect(row[VisibleFields.economy]).toBe('100');
    expect(row[VisibleFields.actualCost]).toBe('550,00');
    expect(row[VisibleFields.actualDistance]).toBe('110,2');
  });

  it('использует emptySign для отсутствующих полей', () => {
    // Переопределяем моки для этого конкретного теста
    (formatVolume.formatVolume as jest.Mock).mockReturnValue('');
    (convertToRubles.convertToRubles as jest.Mock).mockReturnValue(null);
    
    const mockTrip = {
      id: null,
      humanReadableId: null,
      status: null,
      transportType: null,
      desiredDate: null,
      contractor: null,
      author: null,
      expected: null,
      weight: null,
      volume: null,
      sender: null,
      recipient: null,
      senderAddress: null,
      recipientAddress: null,
      senderOrganization: null,
      recipientOrganization: null,
      creationTime: null,
      source: null,
      cargoTripId: null,
      templateNumber: null,
      evaluation: null,
      costCenter: null,
      plannedDeliveryDate: null,
      economy: null,
      shipmentTime: null,
      transferTime: null,
      deadlineDate: null,
      actualCost: null,
      actualDistance: null,
    } as any as TripInfo;
  
    const { result } = renderHook(() => useTransformedData([mockTrip]));
    const row = result.current[0] as any;
  
    expect(row.id).toBe('—');
    expect(row[VisibleFields.humanReadableId]).toBeNull();
    expect(row[VisibleFields.status]).toBe('-');
    expect(row[VisibleFields.transportType]).toBe('-');
    expect(row[VisibleFields.expectedCost]).toBe('—');
    expect(row[VisibleFields.expectedDistance]).toBe('—');
    expect(row[VisibleFields.economy]).toBe('—');
    expect(row[VisibleFields.actualCost]).toBe('—');
    expect(row[VisibleFields.actualDistance]).toBe('—');
    // Добавьте остальные проверки по необходимости
  });

  it('корректно обрабатывает нулевые значения', () => {
    const mockTrip = {
      id: '1',
      humanReadableId: 'T1',
      status: 'PENDING',
      transportType: 'truck',
      desiredDate: '2023-01-01',
      expected: { distance: 0, cost: 0, waypointsCount: 0 },
      weight: 0,
      volume: 0,
      economy: 0,
      actualCost: 0,
      actualDistance: 0,
      contractor: { name: 'C' },
      author: { fio: 'A' },
      sender: { fio: 'S' },
      recipient: { fio: 'R' },
      senderAddress: 'addr',
      recipientAddress: 'addr',
      creationTime: '2023-01-01',
      source: 'web',
    } as any as TripInfo;
  
    const { result } = renderHook(() => useTransformedData([mockTrip]));
    const row = result.current[0] as any;
  
    expect(row[VisibleFields.expectedDistance]).toBe('0,0');
    expect(row[VisibleFields.weight]).toBe('0,000');
    expect(row[VisibleFields.volume]).toBe('0');
    expect(row[VisibleFields.waypointsCount]).toBe('0');
    expect(row[VisibleFields.economy]).toBe('0');
    // Для expectedCost и actualCost при значении 0 возвращается emptySign, а не '0,00'
    expect(row[VisibleFields.expectedCost]).toBe('—');
    expect(row[VisibleFields.actualCost]).toBe('—');
    expect(row[VisibleFields.actualDistance]).toBe('0,0');
  });

  it('показывает оценку только для статуса CARGO_DELIVERY_CONFIRMATION_FINISHED', () => {
    const baseTrip = {
      id: '1',
      humanReadableId: 'T1',
      transportType: 'truck',
      desiredDate: '2023-01-01',
      contractor: { name: 'C' },
      author: { fio: 'A', mobilePhone: '123', personnelNumber: 'P1' },
      expected: { distance: 10, cost: 1000, waypointsCount: 2 },
      weight: 100,
      volume: 5,
      sender: { fio: 'S', mobilePhone: '456' },
      recipient: { fio: 'R', mobilePhone: '789' },
      senderAddress: 'addr1',
      recipientAddress: 'addr2',
      senderOrganization: 'OrgS',
      recipientOrganization: 'OrgR',
      creationTime: '2023-01-01T10:00:00',
      source: 'web',
      cargoTripId: 'TRIP1',
      templateNumber: 'TMPL1',
      costCenter: 'CC1',
      plannedDeliveryDate: '2023-01-02',
      economy: 500,
      shipmentTime: '2023-01-03',
      transferTime: '2023-01-04',
      deadlineDate: '2023-01-05',
      actualCost: 2000,
      actualDistance: 15,
    };
  
    const finishedTrip = {
      ...baseTrip,
      status: 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
      evaluation: { rating: 4 },
    } as any as TripInfo;
  
    const { result: finishedResult } = renderHook(() => useTransformedData([finishedTrip]));
    const finishedRow = finishedResult.current[0] as any;
    expect(finishedRow[VisibleFields.evaluation]).toBe('4');
  
    const otherTrip = {
      ...baseTrip,
      status: 'OTHER',
      evaluation: { rating: 5 },
    } as any as TripInfo;
    const { result: otherResult } = renderHook(() => useTransformedData([otherTrip]));
    const otherRow = otherResult.current[0] as any;
    expect(otherRow[VisibleFields.evaluation]).toBe('—');
  });
});