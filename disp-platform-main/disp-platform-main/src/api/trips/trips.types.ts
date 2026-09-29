import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import { VehicleModel } from 'api/vehicles/vehicles.types';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { UUID } from 'utils/io-ts';
import { PaginationParams, createPagination } from 'utils/io-ts/pagination';
import {
  CheckinType, GroupTransferClass, ServiceType, TaxiClass, TRIP_STATUSES
} from 'constants/trips.constants';
import { TransportTypes, TripTypes } from 'constants/app.constants';
import { DriverSpecialityTypes } from 'constants/driver.constants';

export const EmployeeShort = t.intersection([
  t.type({}),
  t.partial({
    patronymic: tt.nullable(t.string),
    lastName: tt.nullable(t.string),
    firstName: tt.nullable(t.string),
    humanReadableId: tt.nullable(t.string),
    mobilePhone: tt.nullable(t.string),
    organization: tt.nullable(t.string),
    email: tt.nullable(t.string),
  }),
]);
export type EmployeeShort = t.TypeOf<typeof EmployeeShort>;

export const DriverShort = t.intersection([
  EmployeeShort,
  t.type({
    rating: t.number,
    id: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    contactPhone: tt.nullable(t.string),
    shiftId: tt.nullable(tt.uuid),
  }),
]);
export type DriverShort = t.TypeOf<typeof DriverShort>;

export const WaypointContact = t.type({
  name: t.string,
  phone: t.string,
});
export type WaypointContact = t.TypeOf<typeof WaypointContact>;

export enum TypeOfAction {
  BOARDING = 'BOARDING',
  UNBOARDING = 'UNBOARDING',
  WAIT = 'WAIT',
}

export const WaypointPassenger = t.partial({
  patronymic: t.string,
  lastName: t.string,
  firstName: t.string,
  type: ioTypeFromEnum<TypeOfAction>('TypeOfAction', TypeOfAction),
  phone: t.string,
});
export type WaypointPassenger = t.TypeOf<typeof WaypointPassenger>;

export const Waypoint = t.intersection([
  t.type({
    id: tt.uuid,
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    existInVspGosbTbRegistry: t.boolean,
    region: t.string,
    city: t.string,
    waitingTime: t.string,
    index: t.number,
    contact: WaypointContact,
    fullAddress: t.string,
    passengers: t.array(WaypointPassenger),
  }),
]);
export type Waypoint = t.TypeOf<typeof Waypoint>;

export const VehicleShort = t.intersection([
  t.type({
    id: tt.uuid,
    stateNumber: t.string,
    brand: t.string,
  }),
  t.partial({
    model: tt.nullable(t.string),
    color: tt.nullable(t.string),
    vehicleType: ioTypeFromEnum<TripTypes>('DriverSpeciality', TripTypes),
  }),
]);
export type VehicleShort = t.TypeOf<typeof VehicleShort>;

export const DispatcherShort = t.intersection([
  t.type({
    lastName: t.string,
    firstName: t.string,
  }),
  t.partial({
    phone: tt.nullable(t.string),
    patronymic: tt.nullable(t.string),
  }),
]);
export type DispatcherShort = t.TypeOf<typeof DispatcherShort>;

export const Author = t.partial({
  firstName: tt.nullable(t.string),
  lastName: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  userId: tt.nullable(t.string),
  patronymic: tt.nullable(t.string),
  personnelNumber: tt.nullable(t.string),
  itinerantType: tt.nullable(t.string),
  mvz: tt.nullable(t.string),
  costCenter: tt.nullable(t.string),
  marriageCertificateNumber: tt.nullable(t.string),
  delegatedById: tt.nullable(t.string),
  supervisorId: tt.nullable(t.string),
  positionName: tt.nullable(t.string),
  departmentName: tt.nullable(t.string),
  phone: tt.nullable(t.string),
  mobilePhone: tt.nullable(t.string),
  id: tt.nullable(tt.uuid),
  organizationId: tt.nullable(tt.uuid),
  positionId: tt.nullable(tt.uuid),
  departmentId: tt.nullable(tt.uuid),
  organization: tt.nullable(t.string),
});
export type Author = t.TypeOf<typeof Author>;

export const Address = t.type({
  building: tt.nullable(t.string),
  city: tt.nullable(t.string),
  country: tt.nullable(t.string),
  house: tt.nullable(t.string),
  region: tt.nullable(t.string),
  street: tt.nullable(t.string),
  structure: tt.nullable(t.string),
});
export type Address = t.TypeOf<typeof Address>;

export interface TripsFilters extends PaginationParams {
  statuses?: TRIP_STATUSES[];
  humanReadableId?: string;
  requestHumanReadableId?: string;
  desireDateStart?: string;
  desireDateEnd?: string;
  expectedTime?: number;
  driverIds?: UUID[];
  /** Вид сервиса: Такси / Трансфер */
  serviceType?: ServiceType;
  /** Список классов такси */
  taxiClass?: TaxiClass[];
}

export const TripsStatistic = t.intersection([
  t.type({}),
  t.partial({
    assignCount: tt.nullable(t.number),
    notAssignCount: tt.nullable(t.number),
    totalCount: tt.nullable(t.number),
  }),
]);

export type TripsStatistic = t.TypeOf<typeof TripsStatistic>;

// =================== Пассажиры =====================

export const RequestData = t.intersection([
  t.type({
    distance: t.number,
    cost: t.number,
  }),
  t.partial({
    time: tt.nullable(t.number),
  }),
]);
export type RequestData = t.TypeOf<typeof RequestData>;

export const PassTariff = t.partial({
  transport_type: t.string, // Пока в диспетчерской другого не предусмотрено
  id: tt.uuid,
  humanReadableId: t.string,
  serviceType: t.string,
  organizationId: tt.uuid,
  regionId: tt.uuid,
  region: t.string,
  active: t.boolean,
  contractId: t.string,
  contractorId: tt.uuid,
  taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass),
  rideCostPerKm: t.number,
  distanceIncluded: t.number,
  minRideDistanceCost: t.number,
  rideCostPerMin: t.number,
  timeIncluded: t.number,
  minRideTimeCost: t.number,
  waitCostPerMin: t.number,
  waitCostPerMinIntermediate: t.number,
  freeWaitingTime: t.number,
  carServiceCost: t.number,
  timedTariffParams: t.type({
    coefWorkDayMorning: t.number,
    coefWorkDayNoon: t.number,
    coefWorkDayEvening: t.number,
    coefWorkDayNight: t.number,
    coefDayOff: t.number,
  }),
  coopTariffParams: t.type({
    savingsDeviationPct: t.number,
    distanceDeviationKm: t.number,
    timeDeviationMin: t.number,
    minCancelTimeMin: t.number,
  }),
  suburbTariffParams: t.type({
    costPerKmSuburb: t.number,
    costPerMinSuburb: t.number,
    suburbServiceCostPerKm: t.number,
    suburbServiceCostPerMin: t.number,
    costPerKmInterRegion: t.number,
    costPerMinInterRegion: t.number,
  }),
  contractorDeviationParams: t.type({
    maxDiffComputedDistancePercent: t.number,
    maxDiffFactDistancePercent: t.number,
    maxDiffComputedCostPercent: t.number,
    maxDiffContractorCostPercent: t.number,
    maxDiffComputedWaitingPercent: t.number,
  }),
  coefTraffic: t.number,
  coefChildSeat: t.number,
  coefPetTransport: t.number,
  coefBicycle: t.number,
  coefOrg: t.number,
  workGroup: t.string,
  triggerTime: t.number,
  isNightTariff: t.boolean,
});
export type PassTariff = t.TypeOf<typeof PassTariff>;

/** Доп информация по трансферу */
export const TransferInformation = t.partial({
  childSeat: tt.nullable(t.boolean),
  childSeatDetails: tt.nullable(t.type({
    group1: t.number,
    group2: t.number,
    booster: t.number,
    newborn: t.number,
  })),
  bugsOversized: tt.nullable(t.boolean),
  bugsOversizedComment: tt.nullable(t.string),
  bugs: tt.nullable(t.boolean),
  bugsComment: tt.nullable(t.string),
  animal: tt.nullable(t.boolean),
  animalComment: tt.nullable(t.string),
  phoneHotel: tt.nullable(t.string),
  numberFlight: tt.nullable(t.string),
  dateFlight: tt.nullable(t.number),
  addContactFIO: tt.nullable(t.string),
  addContactPhone: tt.nullable(t.string),
  typeVehicle: tt.nullable(t.string),
});
export type TransferInformation = t.TypeOf<typeof TransferInformation>;

/** Пассажирская заявка */
export const PassRequest = t.intersection([
  t.type({
    id: tt.uuid,
    passengerCount: t.number,
    /** Признак сп */
    coop: t.boolean,
    sharedRideOwner: t.boolean,
    transportType: tt.nullable(ioTypeFromEnum<TransportTypes>('TransportTypes', TransportTypes)),
  }),
  t.partial({
    humanReadableId: tt.nullable(t.string),
    author: tt.nullable(EmployeeShort),
    passenger: tt.nullable(EmployeeShort),
    expected: tt.nullable(RequestData),
    requestOptions: tt.nullable(t.array(t.string)),
    tariff: tt.nullable(PassTariff),
    waypoints: tt.nullable(t.array(Waypoint)),
    commentForDriver: tt.nullable(t.string),
    comment: tt.nullable(t.string),
    finishedTime: tt.nullable(t.string),
    status: tt.nullable(t.string),
    timeZone: tt.nullable(t.string),
    /** Признак межгорода */
    suburb: tt.nullable(t.boolean),
    creationTime: tt.nullable(t.string),

    // =========== Трансфер ===============
    update_time: tt.nullable(t.string),
    update_user: tt.nullable(tt.uuid),
    min_order_time: tt.nullable(t.number),
    groupTransferClass: tt.nullable(ioTypeFromEnum<GroupTransferClass>('GroupTransferClass', GroupTransferClass)),
    vip: tt.nullable(t.boolean),
    information: tt.nullable(TransferInformation),
    // =========== /Трансфер ===============
  }),
]);
export type PassRequest = t.TypeOf<typeof PassRequest>;

/** Запланированный водитель */
export const Planned = t.type({
  driver: DriverShort,
  vehicle: VehicleShort,
});
export type Planned = t.TypeOf<typeof Planned>;

/** Пассажирская поездка */
export const PassTrip = t.intersection([
  t.type({
    id: tt.uuid,
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
    requests: t.array(PassRequest),
    waypoints: t.array(Waypoint),
    passengerCount: t.number,
  }),
  t.partial({
    planned: tt.nullable(Planned),
    taxiClass: tt.nullable(ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass)),
    humanReadableId: tt.nullable(t.string),
    externalHumanReadableId: tt.nullable(t.string),
    // да, startTime параметр не обязателен,
    // т к например, для грузов, в случае удаления записи он чистится, но запись продолжает отображаться в списке
    expectedStartTime: tt.nullable(t.string),
    factStartTime: tt.nullable(t.string),
    expectedEndTime: tt.nullable(t.string),
    factEndTime: tt.nullable(t.string),
    driver: tt.nullable(DriverShort),
    vehicle: tt.nullable(VehicleShort),
    dispatcher: tt.nullable(DispatcherShort),
    /** Фактический километраж, км */
    factDistance: tt.nullable(t.number),
    /** Время ожидания, секунды */
    driverWaitingTime: tt.nullable(t.number),
    /** Фактическая стоимость */
    factCost: t.number,
    isNew: tt.nullable(t.boolean),
    creationTime: tt.nullable(t.string),
    /** Дата и время просмотра заявки диспетчером */
    dispatcherTakeToWork: tt.nullable(t.string),
    /** Дата и время изменения статуса диспетчером */
    statusChangedAt: tt.nullable(t.string),
    expectedCost: tt.nullable(t.number),
    expectedDistance: tt.nullable(t.number),
    expectedTime: tt.nullable(t.number),
    contractorId: tt.nullable(t.string),
    comment: t.string,
    information: TransferInformation,
  }),
]);
export type PassTrip = t.TypeOf<typeof PassTrip>;

/** Пагинированный список пассажирских поездок */
export const PassTrips = createPagination(PassTrip);
export type PassTrips = t.TypeOf<typeof PassTrips>;

export type EditTripData = (
  | {
    field: 'status';
    value: TRIP_STATUSES;
  }
  | {
    field: 'driverId';
    value: UUID;
  }
  | {
    field: 'planningShiftId';
    value: UUID;
  }
  | {
    field: 'factDistance';
    value: number | null;
  }
  | {
    field: 'driverWaitingTime';
    value: number | null;
  }
  | {
    field: 'factCost';
    value: number;
  })[];

// ===================== Водители ==========================

export const ShiftsDriver = t.intersection([
  t.strict({
    id: tt.uuid,
    humanReadableId: t.string,
    firstName: t.string,
    lastName: t.string,
    online: t.boolean,
  }),
  t.partial({
    patronymic: tt.nullable(t.string),
    driverSpeciality: tt.nullable(ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes)),
  }),
]);

export type ShiftsDriver = t.TypeOf<typeof ShiftsDriver>;

export const ShiftsVehicle = t.intersection([
  t.type({
    stateNumber: t.string,
    id: tt.uuid,
  }),
  t.partial({
    active: tt.nullable(t.boolean),
    vehicleType: tt.nullable(ioTypeFromEnum<TripTypes>('TripTypes', TripTypes)),
  }),
]);

export type ShiftsVehicle = t.TypeOf<typeof ShiftsVehicle>;

export const Shift = t.intersection([
  t.type({
    id: tt.uuid,
    vehicle: ShiftsVehicle,
    startDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export type Shift = t.TypeOf<typeof Shift>;

/** Сокращенная геолокация водителя lдля вебсокетов */
export const DriversLocationWebsocket = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    currentShift: tt.nullable(t.type({ endDate: t.string, id: tt.uuid })),
    latitude: tt.nullable(t.number),
    longitude: tt.nullable(t.number),
    azimuth: tt.nullable(t.number),
    online: tt.nullable(t.boolean),
    activeTripId: tt.nullable(tt.uuid),
  }),
]);
export type DriversLocationWebsocket = t.TypeOf<typeof DriversLocationWebsocket>;

/** Геолокация водителя */
export const DriversLocation = t.intersection([
  DriverShort,
  t.partial({
    activeTripId: tt.nullable(tt.uuid),
    serving: tt.nullable(t.boolean),
    shiftId: tt.nullable(tt.uuid),
    currentShift: tt.nullable(Shift),
    latitude: tt.nullable(t.number),
    longitude: tt.nullable(t.number),
    azimuth: tt.nullable(t.number),
    distanceInKilometer: tt.nullable(t.number),
    online: tt.nullable(t.boolean),
  }),
]);
export type DriversLocation = t.TypeOf<typeof DriversLocation>;

/** Пагинированный список геолокаций водителя */
export const DriversLocations = createPagination(DriversLocation);
export type DriversLocations = t.TypeOf<typeof DriversLocations>;

export interface SetDriversData {
  tripId: UUID;
  driverId: UUID;
  planningShiftId?: UUID;
}

export interface UseDriversLocationsParams {
  contractorId: UUID;
  query: PaginationParams & {
    latitude: number;
    longitude: number;
    tripStartDate?: string;
    tripEndDate?: string;
    fullSearch?: boolean;
    enableShiftFilter?: boolean;
    forPlanning?: boolean;
    name?: string;
  };
}

export type SearchDriverParams = Omit<UseDriversLocationsParams['query'], keyof PaginationParams>;

/** Чекины */
export const Checkin = t.type({
  longitude: t.number,
  latitude: t.number,
  time: t.string,
  status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
  type: ioTypeFromEnum<CheckinType>('CheckinType', CheckinType),
});
export type Checkin = t.TypeOf<typeof Checkin>;

/** Длительность */
export const TripDuration = t.type({
  days: t.number,
  hours: t.number,
  minutes: t.number,
  seconds: t.number,
});
export type TripDuration = t.TypeOf<typeof TripDuration>;

/** Текущее местоположение водителя */
export const DriverCurrentLocation = t.type({
  longitude: t.number,
  latitude: t.number,
});
export type DriverCurrentLocation = t.TypeOf<typeof DriverCurrentLocation>;

/** Чекины (отметки водителя на точках) */
export const CheckinInfo = t.intersection([
  t.type({
    chekins: t.array(Checkin),
    complete: t.boolean,
  }),
  t.partial({
    driverLocation: tt.nullable(DriverCurrentLocation),
    tripDuration: tt.nullable(TripDuration),
  }),
]);
export type CheckinInfo = t.TypeOf<typeof CheckinInfo>;

/** Поездка в занятости водителей */
const DriverBusynessTrip = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
    expectedStartTime: t.string,
    expectedEndTime: t.string,
    planning: t.boolean,
    vehicle: t.type({
      id: tt.uuid,
      stateNumber: t.string,
      vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),
    }),
  }),
  t.partial({
    factStartTime: t.string,
    factEndTime: t.string,
    driverProcessingTime: t.string,
  }),
]);
export type DriverBusynessTrip = t.TypeOf<typeof DriverBusynessTrip>;

export const DriverBusynessShort = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
});

export type DriverBusynessShort = t.TypeOf<typeof DriverBusynessShort>;

/** Бронь на автомобиль */
const BusynessOrderResponse = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
    expectedStartTime: t.string,
    expectedEndTime: t.string,
    planning: t.boolean,
    vehicle: t.type({
      id: tt.uuid,
      stateNumber: t.string,
      vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),
      model: VehicleModel,
    }),
  }),
  t.partial({
    factStartTime: t.string,
    factEndTime: t.string,
  }),
]);

export type BusynessOrderResponse = t.TypeOf<typeof BusynessOrderResponse>;

/** Занятость водителей */
export const DriverBusyness = t.type({
  busyness: t.array(
    t.type({
      driver: DriverBusynessShort,
      trips: t.array(DriverBusynessTrip),
    })
  ),
  orders: t.array(BusynessOrderResponse),
});

export type DriverBusyness = t.TypeOf<typeof DriverBusyness>;

/** Тело для запроса занятости */
export const DriverBusynessData = t.intersection([
  t.type({
    startTime: t.string,
    endTime: t.string,
  }),
  t.partial({
    driverIds: t.array(tt.uuid),
    includeOrderedVehicles: t.boolean,
    contractorId: tt.uuid,
  }),
]);
export type DriverBusynessData = t.TypeOf<typeof DriverBusynessData>;

export interface ChangeVehicleData {
  tripId: UUID;
  vehicleId: UUID;
}
