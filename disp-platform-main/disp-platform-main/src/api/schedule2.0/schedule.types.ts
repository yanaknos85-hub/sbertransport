import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, PaginationParams } from 'utils/io-ts/pagination';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { DriverSpecialityTypes } from 'constants/driver.constants';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { TripTypes } from 'constants/app.constants';

// ===================== Смены ==========================

/** Водитель в смене */
export const ShiftDriver = t.intersection([
  t.type({
    /** Уникальный идентификатор водителя */
    id: tt.uuid,

    /** Человекопонятный идентификатор водителя */
    humanReadableId: t.string,

    /** Специализация водителя */
    driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
  }),
  t.partial({
    /** Имя водителя */
    firstName: t.string,

    /** Фамилия водителя */
    lastName: t.string,
  }),
]);
export type ShiftDriver = t.TypeOf<typeof ShiftDriver>;

/** Смена */
export const Shift = t.intersection([
  t.type({
    /** Уникальный идентификатор смены */
    id: tt.uuid,

    /** Дата начала смены */
    startDate: t.string,

    /** Дата окончания смены */
    endDate: t.string,

    /** Признак активности смены */
    active: t.boolean,

    /** Водитель */
    driver: ShiftDriver,
  }),
  t.partial({
    /** Идентифиатор ряда */
    rowId: tt.uuid,

    /** Идентификатор ЭПЛ */
    ewbId: tt.uuid,
  }),
]);
export type Shift = t.TypeOf<typeof Shift>;

/** Информация о бренде и модели авто */
export const VehicleModel = t.partial({
  /** Бренд авто */
  brand: t.string,

  /** Модель авто */
  name: t.string,
});
export type VehicleModel = t.TypeOf<typeof VehicleModel>;

/** Авто в сменах */
export const ScheduleVehicle = t.type({
  /** Уникальный идентификатор водителя */
  id: tt.uuid,

  /** Госномер */
  stateNumber: t.string,

  /** Тип авто */
  vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),

  /** Модель авто */
  model: VehicleModel,
});
export type ScheduleVehicle = t.TypeOf<typeof ScheduleVehicle>;

/** Авто и его смены */
export const VehicleWithShifts = t.type({
  /** Авто в сменах */
  vehicle: ScheduleVehicle,

  /** Список смен данного авто */
  shifts: t.array(Shift),
});
export type VehicleWithShifts = t.TypeOf<typeof VehicleWithShifts>;

/** График работы авто */
export const Schedule = createPagination(VehicleWithShifts);
export type Schedule = t.TypeOf<typeof Schedule>;

/** Фильтры графика работы авто */
export interface ScheduleFilters extends PaginationParams {
  /** Дата начала */
  startDate: string;

  /** Дата окончания */
  endDate: string;

  /** Поиск по гос. номеру или марке */
  search?: string;
}

/** Детальна информация о водителе */
export const DriverDetailed = t.intersection([
  ShiftDriver,
  t.type({
    /** Статус водителя */
    online: t.boolean,
  }),
  t.partial({
    /** Отчество */
    patronymic: t.string,
  }),
]);
export type DriverDetailed = t.TypeOf<typeof DriverDetailed>;

// ===================== Статусы онлайн ==========================

/** Статус водителя на текущем авто */
export const DriverStatus = t.type({
  /** Идентификатор автомобиля */
  vehicleId: tt.uuid,

  /** Статус водителя */
  online: t.boolean,

  /** Идентифакатор водителя, который сейчас находится на данном авто */
  driverId: tt.uuid,
});
export type DriverStatus = t.TypeOf<typeof DriverStatus>;

/** Данные для получения списка статусов водителей */
export interface DriverStatusFilters {
  /** Идентификаторы авто, по которым нужно получить статусы водителей */
  vehicleIds: tt.UUID[];

  /** Используется только для ключа, чтобы обновлять список при смене даты */
  startDate?: string;

  /** Используется только для ключа, чтобы обновлять список при смене даты */
  endDate?: string;
}

// ===================== Занятость ==========================

/** Поездка в занятости */
export const BusynessTrip = t.intersection([
  t.type({
    /** Идентификатор поездки */
    id: tt.uuid,

    /** Человекопонятный идентификатор поездки */
    humanReadableId: tt.uuid,

    /** Статус поездки */
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),

    /** Ожидаемое время начала поездки */
    expectedStartTime: t.string,

    /** Ожидаемое время окончания поездки */
    expectedEndTime: t.string,

    /** Признак бронирования */
    ordered: t.boolean,
  }),
  t.partial({
    /** Признак плановой поездки */
    planning: t.boolean,

    /** Время, когда водитель взял поездку и поехал к клиенту */
    driverProcessingTime: t.string,

    /** Фактическое время начала поездки */
    factStartTime: t.string,

    /** Фактическое время окончания поездки */
    factEndTime: t.string,
  }),
]);
export type BusynessTrip = t.TypeOf<typeof BusynessTrip>;

/** Занятость автомобиля */
export const Busyness = t.type({
  /** Идентификатор автомобиля */
  vehicleId: tt.uuid,

  /** Список поездок в занятости */
  trips: t.array(BusynessTrip),
});
export type Busyness = t.TypeOf<typeof Busyness>;

export interface BusynessFilters {
  /** Дата начала */
  startTime: string;

  /** Дата окончания */
  endTime: string;

  /** Список идентификаторов авто */
  vehicleIds: tt.UUID[];

  /** Идентификатор контрагента */
  contractorId: tt.UUID;
}

// ========================= Старое апи, которое не менялось

export const ShiftsVehicle = t.type({
  stateNumber: t.string,
  id: tt.uuid,
  active: t.boolean,
  vehicleType: ioTypeFromEnum<TripTypes>('DriverSpeciality', TripTypes),
  model: VehicleModel,
});
export type ShiftsVehicle = t.TypeOf<typeof ShiftsVehicle>;

/** Детальная информация о смене */
export const ShiftDetailed = t.intersection([
  Shift,
  t.type({
    driver: DriverDetailed,
    vehicle: ShiftsVehicle,
  }),
]);
export type ShiftDetailed = t.TypeOf<typeof ShiftDetailed>;

export const ShiftsCreate = t.strict({
  index: t.number,
  driverId: tt.uuid,
  vehicleId: tt.uuid,
  startDate: t.string,
  endDate: t.string,
});
export type ShiftsCreate = t.TypeOf<typeof ShiftsCreate>;

export const ShiftsCreateRequest = t.array(t.intersection([
  t.strict({
    index: t.number,
    driverId: tt.uuid,
    vehicleId: tt.uuid,
    startDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    routeId: t.string,
  }),
]));
export type ShiftsCreateRequest = t.TypeOf<typeof ShiftsCreateRequest>;

export const ShiftsCreateResponse = t.array(t.union([
  t.intersection([
    ShiftsCreate,
    t.strict({ id: tt.uuid }),
  ]),
  t.strict({
    message: t.string,
    problems: t.array(t.type({
      conflictEntitiesIds: t.array(tt.uuid),
    })),
  }),
]));
export type ShiftsCreateResponse = t.TypeOf<typeof ShiftsCreateResponse>;

export const ShiftsUpdate = t.intersection([
  ShiftsCreate,
  t.strict({ id: tt.uuid }),
]);
export type ShiftsUpdate = t.TypeOf<typeof ShiftsUpdate>;

export const Workload = t.type({
  vehicles: t.array(
    t.type({
      id: tt.uuid,
      workload: t.number,
    })
  ),
  general: t.number,
});
export type Workload = t.TypeOf<typeof Workload>;

export const WorkloadRequest = t.partial({
  startTime: t.string,
  endTime: t.string,
  vehicleIds: t.array(tt.uuid),
  estimatedHours: t.number,
});
export type WorkloadRequest = t.TypeOf<typeof WorkloadRequest>;
