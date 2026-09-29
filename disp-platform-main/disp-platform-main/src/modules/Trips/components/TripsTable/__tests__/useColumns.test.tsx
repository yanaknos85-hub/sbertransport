/* eslint-disable @typescript-eslint/no-explicit-any */
jest.mock('i18n', () => {
  const t = (key: string) => key.split('.').pop() || key;
  t.Requests = {
    Columns: {
      HumanReadableId: '№ поездки',
      Requests: 'Заявки',
      Status: 'Статус',
      ExpectedStartTime: 'Время старта',
      ExpectedTime: 'Время (мин)',
      Vehicle: 'ТС',
      Driver: 'Водитель',
      Passenger: 'Пассажир',
      AddressFrom: 'Откуда',
      AddressTo: 'Куда',
      IntermediateAddresses: 'Промежут.',
      CommentForDriver: 'Комментарий',
      PassengerCount: 'Кол-во',
      ExpectedDistance: 'Расст. (план)',
      FactDistance: 'Расст. (факт)',
      ExpectedCost: 'Стоимость',
      DriverWaitingTime: 'Ожидание',
      TaxiClass: 'Класс',
      Dispatcher: 'Диспетчер',
      assignToMe: 'Взять себе',
      auto: 'Авто',
    },
  };
  return { useTranslation: () => ({ t }) };
});

jest.mock('modules/Trips/constants', () => ({
  Columns: {
    Icon: 'icon',
    Number: 'number',
    HumanReadableId: 'humanReadableId',
    Requests: 'requests',
    Status: 'status',
    StartTime: 'startTime',
    ExpectedTime: 'expectedTime',
    Vehicle: 'vehicle',
    Driver: 'driver',
    Passenger: 'passenger',
    AddressFrom: 'addressFrom',
    AddressTo: 'addressTo',
    IntermediateAddresses: 'intermediateAddresses',
    CommentForDriver: 'commentForDriver',
    PassengerCount: 'passengerCount',
    ExpectedDistance: 'expectedDistance',
    FactDistance: 'factDistance',
    ExpectedCost: 'expectedCost',
    DriverWaitingTime: 'driverWaitingTime',
    TaxiClass: 'taxiClass',
    Dispatcher: 'dispatcher',
  },
}));

jest.mock('ioc', () => ({
  useAppStore: () => ({
    logger: { toMessage: jest.fn() },
  }),
}));

jest.mock('api', () => ({
  useAPIQueryCache: () => ({ invalidateQueries: jest.fn() }),
}));

jest.mock('api/profile/profile.api', () => ({
  useProfile: () => ({ data: { contractorId: 'contractor-123' } }),
}));

const mockCreateShift = jest.fn(() => Promise.resolve([{ id: 'shift-1', driverId: 'driver-1' }]));
const mockEditTrip = jest.fn(() => Promise.resolve());
const mockAssignTrip = jest.fn(() => Promise.resolve());
const mockSetDriver = jest.fn(() => Promise.resolve());

jest.mock('api/schedule2.0/schedule.api', () => ({
  useCreateShifts: () => [mockCreateShift, { isLoading: false }],
}));

jest.mock('api/trips/trips.api', () => ({
  useAssignTripToDispatcher: () => [mockAssignTrip, { isLoading: false }],
  useEditTrip: () => [mockEditTrip, { isLoading: false }],
  useSetDriverToRequest: () => [mockSetDriver, { isLoading: false }],
}));

const mockOpenSetDriver = jest.fn(() => {
  // Никаких операций с trip.passenger
});
const mockOpenEdit = jest.fn();

jest.mock('modules/Trips/context/TripsModal', () => ({
  useTripsModal: () => ({
    openSetDriver: mockOpenSetDriver,
    openEdit: mockOpenEdit,
  }),
}));

jest.mock('modules/Trips/context/TripsQuery', () => ({
  useTripsQuery: () => ({ query: { field: null, direction: null } }),
}));

jest.mock('utils/getFullName', () => ({
  getFullName: (person: any) => person?.firstName || 'Unknown',
}));

jest.mock('utils/getAddress', () => ({
  getAddress: (waypoint: any) => waypoint?.address || 'Unknown Address',
}));

jest.mock('utils/convertToRubles', () => ({
  convertToRubles: (value: number) => value * 100,
}));

jest.mock('utils/formatRubles', () => ({
  formatRubles: (value: number) => `${value} ₽`,
}));

jest.mock('components/SelectDrivers/SelectDrivers', () => ({
  SelectDrivers: ({ onChange }: { onChange: (id: string) => void }) => (
    <div data-testid="select-drivers" onClick={() => onChange('driver-456')} />
  ),
}));

jest.mock('constants/trips.constants', () => ({
  TRIP_STATUSES: {
    NEW: 'NEW', CONFIRMED: 'CONFIRMED', CANCELLED: 'CANCELLED',
  },
  TripStatuses: {
    NEW: { title: 'Новый', isEditable: true },
    CONFIRMED: { title: 'Подтвержден', isEditable: true },
    CANCELLED: { title: 'Отменен', isEditable: false },
  },
  TAXI_REQUEST_STATUSES: { TAXI_CANCELLED: 'TAXI_CANCELLED' },
  TaxiClassDescriptions: { ECONOM: 'Эконом', COMFORT: 'Комфорт' },
  GroupTransferClassTitles: { GROUP_TRANSFER: 'Групповой трансфер' },
}));

jest.mock('constants/app.constants', () => ({
  DirectionMapSortToOrder: { asc: 'ascend', desc: 'descend' },
  EMPTY_CELL_CONTENT: '-',
  TripTypes: {
    Passenger: 'passenger',
    Cargo: 'cargo',
  },
}));

jest.mock('constants/routes.constants', () => ({
  TRIP: '/trip/:id',
}));

jest.mock('../../StatusWithConfirm/StatusConfirm', () => ({
  StatusWithConfirm: ({ value, onChange }: { value: string; onChange: (v: string) => void }) => (
    <div data-testid="status-with-confirm" onClick={() => onChange('CONFIRMED')}>
      {value}
    </div>
  ),
}));

import React from 'react';
import { renderHook } from '@testing-library/react-hooks';
import { useColumns } from '../useColumns';

// Тесты
describe('useColumns', () => {
  const mockTrip: any = {
    id: 'trip-1',
    humanReadableId: 'HID-001',
    status: 'NEW',
    expectedStartTime: '2023-10-01T10:00:00Z',
    vehicle: { id: 'vehicle-1', vehicleType: 'car' },
    driver: null,
    dispatcher: null,
    expectedTime: 1800,
    expectedDistance: 12.5,
    expectedCost: 500,
    driverWaitingTime: 300,
    taxiClass: 'ECONOM',
    passenger: { firstName: 'Иван', patronymic: 'Иванович' },
    requests: [
      {
        id: 'req-1',
        humanReadableId: 'REQ-001',
        status: 'NEW',
        passenger: { firstName: 'Иван', patronymic: 'Иванович' },
        contactPhone: '+79991234567',
        commentForDriver: 'Буду ждать у подъезда',
        groupTransferClass: null,
      },
    ],
    waypoints: [
      {
        index: 0, fullAddress: 'ул. Ленина, д. 1', contact: { name: 'Иван', phone: '+79991234567' },
      },
      { index: 1, fullAddress: 'ул. Пушкина, д. 2' },
      { index: 2, fullAddress: 'ул. Гоголя, д. 3' },
    ],
    passengerCount: 1,
    factDistance: 12.7,
  };

  it('should return columns array', () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    expect(result.current.passColumns).toBeInstanceOf(Array);
    expect(result.current.passColumns.length).toBeGreaterThan(0);
  });

  it('should include essential columns', () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const keys = result.current.passColumns.map(col => col.key);
    expect(keys).toContain('humanReadableId');
    expect(keys).toContain('status');
    expect(keys).toContain('startTime');
  });

  it('should render HumanReadableId correctly', () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const col = result.current.passColumns.find(c => c.key === 'humanReadableId');
    const rendered = col?.render?.(mockTrip.humanReadableId, mockTrip, 0);
    expect(rendered).toBeDefined();
    expect(rendered).toMatchSnapshot();
  });

  it('should render Status with StatusWithConfirm', () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const col = result.current.passColumns.find(c => c.key === 'status');
    const rendered = col?.render?.(mockTrip.status, mockTrip, 0);
    expect(rendered).toBeDefined();
  });

  it('should render Start Time with formatted dates', () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const col = result.current.passColumns.find(c => c.key === 'startTime');
    const rendered = col?.render?.(mockTrip.expectedStartTime, mockTrip, 0);
    expect(rendered).toBeDefined();
    expect((rendered as any)?.type).toBe(React.Fragment);
  });

  it('should call editTrip when changeStatus is called', async () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const col = result.current.passColumns.find(c => c.key === 'status');
    const rendered = col?.render?.(mockTrip.status, mockTrip, 0);
    const onChange = (rendered as any)?.props.onChange;

    if (!onChange) {
      fail('onChange not found in StatusWithConfirm');
      return;
    }

    await onChange('CONFIRMED');

    expect(mockEditTrip).toHaveBeenCalledWith({
      tripId: 'trip-1',
      data: [{ field: 'status', value: 'CONFIRMED' }],
    });
  });

  it('should call createShift and handleSetDriver when setDriverToOrder is called', async () => {
    const { result } = renderHook(() => useColumns(columns => columns));
    const col = result.current.passColumns.find(c => c.key === 'driver');
    const rendered = col?.render?.(null, mockTrip, 0);
    const onChange = (rendered as any)?.props.onChange;

    if (!onChange) {
      fail('onChange not found in SelectDrivers');
      return;
    }

    await onChange('driver-456');

    expect(mockCreateShift).toHaveBeenCalled();
    expect(mockCreateShift).toHaveBeenCalledWith([
      expect.objectContaining({
        driverId: 'driver-456',
        vehicleId: 'vehicle-1',
      }),
    ]);
  });
});
